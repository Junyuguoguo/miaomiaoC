package sen.yuhuang.backend.common.utils;

import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CCodeExecutor {

    // 临时文件存储目录（需确保有读写权限）
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/c_code/";
    // 运行超时时间（秒），防止死循环
    private static final int TIMEOUT_SECONDS = 5;

    // 初始化临时目录
    public CCodeExecutor() {
        File dir = new File(TEMP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * 编译并运行C代码
     * @param code 前端传递的C语言代码（String）
     * @param input 程序运行时的输入（如scanf需要的参数，无则传空字符串）
     * @return 运行结果封装
     */
// 修改 execute 方法中的调用
    public CodeRunResult execute(String code, String input) {
        CodeRunResult result = new CodeRunResult();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String cFilePath = TEMP_DIR + uuid + ".c";
        String exeFilePath = TEMP_DIR + uuid;

        try {
            // 步骤1：将字符串代码写入.c文件
            writeCodeToFile(code, cFilePath);

            // 步骤2：编译C代码（传入code用于分析头文件）
            CompileResult compileResult = compileCCode(cFilePath, exeFilePath, code);
            if (!compileResult.isSuccess()) {
                result.setSuccess(false);
                result.setMessage("编译失败：" + compileResult.getErrorMsg());
                return result;
            }

            // 步骤3：运行编译后的程序
            RunResult runResult = runExeFile(exeFilePath, input);
            if (!runResult.isSuccess()) {
                result.setSuccess(false);
                result.setMessage("运行失败：" + runResult.getErrorMsg());
                return result;
            }

            // 步骤4：返回成功结果
            result.setSuccess(true);
            result.setOutput(runResult.getOutput());
            result.setMessage("执行成功");

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("系统异常：" + e.getMessage());
        } finally {
            // 步骤5：清理临时文件
            cleanTempFiles(cFilePath, exeFilePath);
        }

        return result;
    }
    /**
     * 头文件与链接库的映射关系
     */
    private static final Map<String, String> HEADER_TO_LIB = new HashMap<>();

    static {
        // 初始化映射关系
        HEADER_TO_LIB.put("math.h", "-lm");
        HEADER_TO_LIB.put("pthread.h", "-lpthread");
        HEADER_TO_LIB.put("dlfcn.h", "-ldl");
        HEADER_TO_LIB.put("rt.h", "-lrt");
        HEADER_TO_LIB.put("mqueue.h", "-lrt");
        HEADER_TO_LIB.put("crypt.h", "-lcrypt");
        HEADER_TO_LIB.put("ncurses.h", "-lncurses");
        // 可以继续添加更多映射...
    }

    /**
     * 从C代码中提取所有包含的头文件
     * @param code C语言源代码
     * @return 头文件列表
     */
    private Set<String> extractHeaders(String code) {
        Set<String> headers = new HashSet<>();

        // 正则表达式匹配 #include <xxx.h> 或 #include "xxx.h"
        Pattern pattern = Pattern.compile("#include\\s*[<\"]([^>\"]+\\.h)[>\"]");
        Matcher matcher = pattern.matcher(code);

        while (matcher.find()) {
            String header = matcher.group(1);
            headers.add(header);
        }

        return headers;
    }

    /**
     * 根据头文件列表生成链接库参数
     * @param headers 头文件列表
     * @return 链接库参数列表
     */
    private List<String> getLinkFlags(Set<String> headers) {
        List<String> linkFlags = new ArrayList<>();

        for (String header : headers) {
            String lib = HEADER_TO_LIB.get(header);
            if (lib != null && !linkFlags.contains(lib)) {
                linkFlags.add(lib);
            }
        }

        return linkFlags;
    }


    // 1. 写入代码到临时文件
    private void writeCodeToFile(String code, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(code);
        }
    }

    // 2.编译 修改 compileCCode 方法
    private CompileResult compileCCode(String cFilePath, String exeFilePath, String code) throws IOException {
        CompileResult compileResult = new CompileResult();

        // 分析代码中的头文件
        Set<String> headers = extractHeaders(code);
        List<String> linkFlags = getLinkFlags(headers);

        // 构建编译命令
        List<String> cmdList = new ArrayList<>();
        cmdList.add("gcc");
        cmdList.add("-std=c99");
        cmdList.add(cFilePath);
        cmdList.add("-o");
        cmdList.add(exeFilePath);

        // 添加所有需要的链接库
        cmdList.addAll(linkFlags);

        // 如果有自定义的库搜索路径，可以添加
        // cmdList.add("-L/usr/local/lib");

        String[] cmd = cmdList.toArray(new String[0]);

        // 可选：打印编译命令（调试用）
        System.out.println("编译命令: " + String.join(" ", cmd));

        // 执行编译命令
        Process process = Runtime.getRuntime().exec(cmd);
        String errorMsg = readInputStream(process.getErrorStream());

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                compileResult.setSuccess(false);
                compileResult.setErrorMsg(errorMsg);
            } else {
                compileResult.setSuccess(true);
            }
        } catch (InterruptedException e) {
            compileResult.setSuccess(false);
            compileResult.setErrorMsg("编译被中断：" + e.getMessage());
            Thread.currentThread().interrupt();
        }

        return compileResult;
    }
    // 3. 运行可执行文件
    private RunResult runExeFile(String exeFilePath, String input) throws IOException {
        RunResult runResult = new RunResult();
        Process process = Runtime.getRuntime().exec(exeFilePath);

        // 处理程序输入（如scanf需要的参数）
        if (input != null && !input.isEmpty()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(input);
                writer.flush();
            }
        }

        // 异步读取输出（防止缓冲区阻塞）
        StringBuilder outputBuilder = new StringBuilder();
        Thread outputThread = new Thread(() -> outputBuilder.append(readInputStream(process.getInputStream())));
        outputThread.start();

        // 异步读取错误输出
        StringBuilder errorBuilder = new StringBuilder();
        Thread errorThread = new Thread(() -> errorBuilder.append(readInputStream(process.getErrorStream())));
        errorThread.start();

        try {
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy(); // 超时杀死进程
                runResult.setSuccess(false);
                runResult.setErrorMsg("程序运行超时（超过" + TIMEOUT_SECONDS + "秒）");
                return runResult;
            }

            // 等待输出读取完成
            outputThread.join();
            errorThread.join();

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                runResult.setSuccess(false);
                runResult.setErrorMsg(errorBuilder.toString());
            } else {
                runResult.setSuccess(true);
                runResult.setOutput(outputBuilder.toString());
            }

        } catch (InterruptedException e) {
            runResult.setSuccess(false);
            runResult.setErrorMsg("运行被中断：" + e.getMessage());
            Thread.currentThread().interrupt();
        }

        return runResult;
    }

    // 工具方法：读取输入流（InputStream转String）
    private String readInputStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            sb.append("读取流失败：").append(e.getMessage());
        }
        return sb.toString().trim();
    }

    // 清理临时文件
    private void cleanTempFiles(String... filePaths) {
        for (String path : filePaths) {
            File file = new File(path);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    // 日志记录：文件删除失败（生产环境建议用日志框架）
                    System.out.println("临时文件删除失败：" + path);
                }
            }
        }
    }

    // 内部类：编译结果封装
    private static class CompileResult {
        private boolean success;
        private String errorMsg;

        // getter/setter
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMsg() { return errorMsg; }
        public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    }

    // 内部类：运行结果封装
    private static class RunResult {
        private boolean success;
        private String output;
        private String errorMsg;

        // getter/setter
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public String getErrorMsg() { return errorMsg; }
        public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    }

    // 对外返回的结果类（可单独抽成公共类）
    public static class CodeRunResult {
        private boolean success;    // 是否执行成功
        private String output;      // 程序输出结果
        private String message;     // 提示信息（失败原因/成功提示）

        // getter/setter
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}