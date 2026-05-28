package sen.yuhuang.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.common.utils.CCodeExecutor;
import sen.yuhuang.backend.dto.PracticeQuestionDTO;
import sen.yuhuang.backend.dto.QuestionDto;
import sen.yuhuang.backend.dto.teacher.QuestionManagerDTO;
import sen.yuhuang.backend.entity.*;
import sen.yuhuang.backend.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class QuestionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    QuestionBankRepository  questionBankRepository;

    @Autowired
    TestSampleRepository testSampleRepository;

    @Autowired
    TestSampleResultRepository testSampleResultRepository;

    @Autowired
    UserQuestionRecordRepository userQuestionRecordRepository;

    @Autowired
    UserLearningStatsRepository userLearningStatsRepository;


    @Autowired
    SampleQuestionRepository sampleQuestionRepository;

    @Autowired
    UserWrongQuestionRepository userWrongQuestionRepository;


    public Result getExamQuestion(String examId) {

        try {
            List<Question> questions = questionRepository.findQuestionBySimulationExamId(Long.valueOf(examId));
            System.out.println("正在查找examId的问题=："+examId);
            System.out.println("查到的问题有："+questions);

            for (Question question : questions) {
                // 1.查找每个问题的样例
                List<SampleQuestion> sampleQuestion = sampleQuestionRepository.findSampleQuestionByQuestionId(question.getId());
                question.setSampleQuestions(sampleQuestion);
            }
            return Result.ok(questions);
        }catch (Exception e){
            e.printStackTrace();
            return Result.badRequest(e.getMessage());
        }
    }

    /**
     * 提交题目代码并执行测试用例
     * @param userId 用户ID（字符串转Long）
     * @param examId 考试ID（字符串转Long）
     * @param questionId 题目ID（字符串转Long）
     * @param code 用户提交的C语言代码
     * @return 包含得分、测试结果的统一返回体
     */
    @Transactional(rollbackFor = Exception.class) // 事务注解：任意异常回滚所有数据库操作
    public Result submitQuestion(String userId, String examId, String questionId, String code) {
        try {
            // 1. 入参校验（避免空指针/类型错误） ==========
            if (userId == null || userId.trim().isEmpty() ||
                    questionId == null || questionId.trim().isEmpty() ||
                    code == null || code.trim().isEmpty())
                return Result.error("参数不能为空");

            // 字符串转Long（兼容前端传参格式）
            Long userIdLong = Long.parseLong(userId.trim());
            Long questionIdLong = Long.parseLong(questionId.trim());
            Long examIdLong = null;
            ArrayList<String> testRecord = new ArrayList<>();

            if(examId != null) Long.parseLong(examId.trim());

            System.out.println("前端传过来的数据：userId=" + userIdLong + ", examId=" + examIdLong + ", questionId=" + questionIdLong);
            System.out.println("前端代码:" + code);

            // 2. 获取该题的所有有效测试用例（test_sample） ==========
            List<TestSample> testSampleList = testSampleRepository.findAllByQuestionId(questionIdLong);
            if (testSampleList.isEmpty()) {
                return Result.error("该题目暂无有效测试用例，无法判分");
            }

            // 3. 编译运行代码，循环执行每个测试用例 ==========
            CCodeExecutor executor = new CCodeExecutor();
            int passCount = 0; // 通过的用例数
            int totalCount = testSampleList.size(); // 总用例数
            int sumUsedTime = 0;
            System.out.println("查询的id是：userIdLong=" + userIdLong);
            System.out.println("查询的id是：questionIdLong=" + questionIdLong);

            //  3.1如果该题已经存在测试记录了，删除旧的，存储新的
            List<TestSampleResult> testSampleResults = testSampleResultRepository.
                    findTestSampleResultByUserIdAndQuestionId(userIdLong,questionIdLong);
            if (testSampleResults.size() > 0) {
                System.out.println("旧测试用例结果为："+testSampleResults);
                // 删除所有测试用例
                testSampleResultRepository.deleteTestSampleResultByUserIdAndQuestionId(userIdLong,questionIdLong);
            }

            // 3.2记录新的用例
            for (TestSample testSample : testSampleList) {
                // 执行前记录开始时间
                long startTime = System.currentTimeMillis();

                // 执行代码：传入用户代码 + 测试用例输入（无输入则传null）
                CCodeExecutor.CodeRunResult runResult = executor.execute(code, testSample.getInput());
                System.out.println("用例[" + testSample.getSort() + "]执行结果：output=" + runResult.getOutput() + ", message=" + runResult.getMessage());

                if (examId == null)
                    testRecord.add("测试用例["
                            + testSample.getSort()
                            + "]\n输入:"
                            +testSample.getInput()
                            +"\n期望输出:"
                            +testSample.getOutput()
                            +"\n实际输出:"
                            + runResult.getOutput()
                            + "\nmessage="
                            + runResult.getMessage());

                // 计算耗时（毫秒）
                int usedTime = (int) (System.currentTimeMillis() - startTime);
                sumUsedTime+=usedTime;

                // 判断是否通过：编译运行成功 + 输出与预期一致（去除首尾空格避免格式问题）
                boolean isPassed = false;
                if (runResult.isSuccess()) { // 先判断编译/运行是否成功
                    // 先拿到原始输出
                    String actual = runResult.getOutput() == null ? "" : runResult.getOutput();
                    String expect = testSample.getOutput() == null ? "" : testSample.getOutput();

                    // 按任意换行分割
                    String[] aLines = actual.split("\\R");
                    String[] eLines = expect.split("\\R");

                    // 过滤空行并trim每一行
                    List<String> aList = new ArrayList<>();
                    for (String s : aLines) {
                        String t = s.trim();
                        if (!t.isEmpty()) aList.add(t);
                    }
                    List<String> eList = new ArrayList<>();
                    for (String s : eLines) {
                        String t = s.trim();
                        if (!t.isEmpty()) eList.add(t);
                    }

                    // 最终比较
                    isPassed = aList.equals(eList);
                    System.out.println("用例比较结果="+isPassed);
                }
                if (isPassed) {
                    passCount++;
                }

                // ========== 保存单条测试用例结果到 test_sample_result ==========
                TestSampleResult resultEntity = new TestSampleResult();

                if(runResult.isSuccess()){
                    resultEntity.setActualOutput(runResult.getOutput());
                }else{
                    resultEntity.setErrorMsg(runResult.getMessage() == null ? "" : runResult.getMessage()); // 报错信息
                }
                resultEntity.setUserId(userIdLong);
                resultEntity.setExamId(examIdLong);
                resultEntity.setQuestionId(questionIdLong);
                resultEntity.setTestSampleId(testSample.getId());
                resultEntity.setIsPassed(isPassed ? 1 : 0); // 1=通过，0=未通过
                resultEntity.setUsedTime(usedTime); // 用时（毫秒）
                resultEntity.setIsDeleted(0); // 未删除
                resultEntity.setCreateTime(LocalDateTime.now());
                resultEntity.setUpdateTime(LocalDateTime.now());
                testSampleResultRepository.save(resultEntity); // 插入数据库
            }
            // 3.2更新题目参与人数
            Question question = questionRepository.findQuestionById(questionIdLong);
            if(question == null) return Result.error("获取题目失败！");
            questionRepository.
                    updateParticipantCountByQuestionId(questionIdLong,question.getParticipantCount()+1);


            // 3.3计算本题得分（按用例通过率 × 题目满分）
            BigDecimal fullScore = question.getFullScore();

            // 计算最终得分 = 通过率 * 总分    通过率 = 通过数/总数，保留1位小数（和题目分值精度一致）
            BigDecimal passRate = new BigDecimal(passCount)
                    .divide(new BigDecimal(totalCount), 1, BigDecimal.ROUND_HALF_UP);
            BigDecimal userScore = fullScore.multiply(passRate);

            // flag表示是否本次做题记录是新增的
            Boolean flag = false;


            // 3.4 保存/更新用户做题记录（user_question_record） ==========
            UserQuestionRecord record = userQuestionRecordRepository.findQuestionRecordByUserIdAndQuestionId(userIdLong,questionIdLong);

            // 如果本题是初次做 新增记录
            if ((record == null)) {
                flag = true;
                record = new UserQuestionRecord();
                record.setUserId(userIdLong);
                record.setQuestionId(questionIdLong);
                record.setScore(userScore);
                record.setCreatedAt(LocalDateTime.now());
                record.setUpdatedAt(LocalDateTime.now());
                record.setCode(code);
                userQuestionRecordRepository.save(record);
            } else{
                // 如果不是初次则更新分数 时间 代码
                userQuestionRecordRepository.updateQuestionRecordById(record.getId(),userScore,LocalDateTime.now(),code);
            }

             // 3.5更新学习统计
            UserLearningStats stats = userLearningStatsRepository.findUserLearningStatsByUserId(userIdLong);
            UserWrongQuestion wrongQuestion = userWrongQuestionRepository.
                    findUserWrongQuestionsByUserIdAndQuestionId(userIdLong, questionIdLong);

            // 如果是第一次触发学习统计
            if (stats == null) {
                // 新增
                UserLearningStats userLearningStats = new UserLearningStats();
                userLearningStats.setUserId(userIdLong);
                userLearningStats.setExamCount(0);
                userLearningStats.setQuestionCount(1);
                //如果第一次做这个题，还满分了
                if (fullScore.compareTo(userScore) == 0){
                    userLearningStats.setSuccessCount(1);
                    userLearningStats.setQuestionPassRate(BigDecimal.valueOf(1));
                } else{
                    userLearningStats.setSuccessCount(0);
                    userLearningStats.setQuestionPassRate(BigDecimal.valueOf(0));
                }
                userLearningStats.setNoteCount(0);
                userLearningStats.setUpdatedAt(LocalDateTime.now());
                userLearningStatsRepository.save(userLearningStats);
            }else {
                // 如果不是第一次触发统计
                if (flag)
                    stats.setQuestionCount(stats.getQuestionCount()+1);

                // 如果第一次做这个题是满分
                if ((flag) && (fullScore.compareTo(userScore) == 0)) {
                    stats.setSuccessCount(stats.getSuccessCount()+1);
                // 如果非第一次做这个题 且是满分 且之前是错的
                }else if((!flag) && (fullScore.compareTo(userScore) == 0) && (wrongQuestion != null)){
                    stats.setSuccessCount(stats.getSuccessCount()+1);
                }

                // 重新计算通过率
                BigDecimal userRate = BigDecimal.valueOf(stats.getSuccessCount())
                        .divide(BigDecimal.valueOf(stats.getQuestionCount()), 4, RoundingMode.HALF_UP);
                stats.setQuestionPassRate(userRate);
                System.out.println("getQuestionCount"+stats.getQuestionCount());
                System.out.println("getSuccessCount"+stats.getSuccessCount());
                System.out.println("getQuestionPassRate"+stats.getQuestionPassRate());

                userLearningStatsRepository.updateCountAndRateById(stats.getId(),
                        stats.getQuestionCount(),stats.getSuccessCount(),stats.getQuestionPassRate());
            }

            // 3.6检查是否插入错题本 如果是考试则不需要插入，如果是题库且没有满分则插入否则不插入
            if((examId == null) && (fullScore.compareTo(userScore) != 0)){
                System.out.println("examId"+examId);
                System.out.println("fullScore"+fullScore);
                System.out.println("userScore"+userScore);
                System.out.println("fullScore == userScore"+(fullScore.compareTo(userScore)));
                UserWrongQuestion newWrongQuestion = new UserWrongQuestion();
                newWrongQuestion.setUserId(userIdLong);
                newWrongQuestion.setQuestionId(questionIdLong);
                newWrongQuestion.setQuestionBankId(question.getBankId());
                newWrongQuestion.setCode(code);
                newWrongQuestion.setLastWrongTime(LocalDateTime.now());

                if (wrongQuestion == null) {
                    // 第一次错，插入错题数据
                    userWrongQuestionRepository.save(newWrongQuestion);
                }else {
                    // 第二次错误，直接更新错题数据
                    userWrongQuestionRepository.updateCodeAndTimeById(wrongQuestion.getId(),code,LocalDateTime.now());
                }
            }else if((examId == null) && (fullScore.compareTo(userScore) == 0) && (wrongQuestion != null)){
                // 如果是题库的题且满分了且错题记录存在 则删除错题记录
                userWrongQuestionRepository.deleteById(wrongQuestion.getId());
            }

            // ========== 6. 封装返回结果 ==========
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("testSampleRecord",testRecord);
            resultMap.put("score", userScore); // 本题得分
            resultMap.put("passCount", passCount); // 通过用例数
            resultMap.put("totalCount", totalCount); // 总用例数
            resultMap.put("usedTime",sumUsedTime);
            resultMap.put("passRate", passRate.multiply(new BigDecimal(100)) + "%"); // 通过率
            resultMap.put("message", passCount == totalCount ? "所有测试用例通过" : "部分测试用例未通过");
            return Result.ok(resultMap);
        } catch (NumberFormatException e) {
            // 处理ID转Long失败的异常
            System.err.println("参数类型错误：" + e.getMessage());
            return Result.error("用户ID/考试ID/题目ID必须为数字");
        } catch (Exception e) {
            // 捕获所有其他异常，保证事务回滚
            System.err.println("提交题目失败：" + e.getMessage());
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    /**
     * 运行测试代码（仅执行测试用例，不修改任何数据库数据）
     * @param questionId 题目ID（字符串转Long，需从前端传入）
     * @param code 用户编写的C语言代码
     * @return 包含测试结果的统一返回体
     */
    public Result runTestCode(String questionId, String code,int type) {
        try {
            // ========== 1. 入参校验（避免空指针/类型错误） ==========
            if (questionId == null || questionId.trim().isEmpty() ||
                    code == null || code.trim().isEmpty()) {
                return Result.error("题目ID和代码不能为空");
            }
            // 字符串转Long（兼容前端传参格式）
            Long questionIdLong = Long.parseLong(questionId.trim());

            System.out.println("测试代码请求：questionId=" + questionIdLong);
            System.out.println("用户测试代码:" + code);
            Map<String, Object> resultMap = new HashMap<>();
            ArrayList<String> record = new ArrayList<>();

            // ========== 2. 获取该题的所有有效测试用例（仅读取，不修改） ==========
            List<TestSample> testSampleList = testSampleRepository.findAllByQuestionId(questionIdLong);
            if (testSampleList.isEmpty()) {
                return Result.ok().setMessage("该题目暂无有效测试用例，无法测试");
            }

            // ========== 3. 编译运行代码，循环执行每个测试用例 ==========
            CCodeExecutor executor = new CCodeExecutor();
            int passCount = 0; // 通过的用例数
            int totalCount = testSampleList.size(); // 总用例数
            int sumUsedTime = 0;

            // 【核心区别】移除所有数据库删除/新增操作，仅执行测试逻辑
            for (TestSample testSample : testSampleList) {
                // 执行前记录开始时间
                long startTime = System.currentTimeMillis();

                // 执行代码：传入用户代码 + 测试用例输入（无输入则传null）
                CCodeExecutor.CodeRunResult runResult = executor.execute(code, testSample.getInput());
                System.out.println("测试用例[" + testSample.getSort() + "]\n输入:"+testSample.getInput()+"\n代码输出:" + runResult.getOutput() + "\nmessage=" + runResult.getMessage());
                if (type == 0){
                    // 说明不是考试环境，需要将用例具体情况封装进去
                    record.add("测试用例["
                            + testSample.getSort()
                            + "]\n输入:"
                            +testSample.getInput()
                            +"\n期望输出:"
                            +testSample.getOutput()
                            +"\n代码输出:"
                            + runResult.getOutput()
                            + "\nmessage="
                            + runResult.getMessage());
                }

                // 计算耗时（毫秒）
                int usedTime = (int) (System.currentTimeMillis() - startTime);
                sumUsedTime += usedTime;

                // 判断是否通过：编译运行成功 + 输出与预期一致（去除首尾空格避免格式问题）
                boolean isPassed = false;
                if (runResult.isSuccess()) { // 先判断编译/运行是否成功
                    // 先拿到原始输出
                    String actual = runResult.getOutput() == null ? "" : runResult.getOutput();
                    String expect = testSample.getOutput() == null ? "" : testSample.getOutput();

                    // 按任意换行分割
                    String[] aLines = actual.split("\\R");
                    String[] eLines = expect.split("\\R");

                    // 过滤空行并trim每一行
                    List<String> aList = new ArrayList<>();
                    for (String s : aLines) {
                        String t = s.trim();
                        if (!t.isEmpty()) aList.add(t);
                    }
                    List<String> eList = new ArrayList<>();
                    for (String s : eLines) {
                        String t = s.trim();
                        if (!t.isEmpty()) eList.add(t);
                    }

                    // 最终比较
                    isPassed = aList.equals(eList);
                    System.out.println("用例比较结果="+isPassed);
                }
                if (isPassed) {
                    passCount++;
                }
                // 【核心区别】移除保存测试用例结果到数据库的逻辑
            }

            // ========== 4. 计算测试结果（仅内存计算，不保存到数据库） ==========
            Question question = questionRepository.findQuestionById(questionIdLong);
            if (question == null) {
                return Result.error("获取题目信息失败！");
            }

            BigDecimal fullScore = question.getFullScore();
            // 通过率 = 通过数/总数，保留1位小数
            BigDecimal passRate = new BigDecimal(passCount)
                    .divide(new BigDecimal(totalCount), 1, BigDecimal.ROUND_HALF_UP);
            BigDecimal userScore = fullScore.multiply(passRate); // 测试得分（仅展示，不入库）

            // ========== 5. 封装返回结果（和提交接口格式保持一致） ==========
            if (type == 0){
                resultMap.put("testSampleRecord", record);
            }
            resultMap.put("score", userScore); // 测试得分（仅临时计算）
            resultMap.put("passCount", passCount); // 通过用例数
            resultMap.put("totalCount", totalCount); // 总用例数
            resultMap.put("usedTime", sumUsedTime); // 总耗时
            resultMap.put("passRate", passRate.multiply(new BigDecimal(100)) + "%"); // 通过率
            resultMap.put("message", passCount == totalCount ? "所有测试用例通过" : "部分测试用例未通过");

            return Result.ok(resultMap);
        } catch (NumberFormatException e) {
            // 处理ID转Long失败的异常
            System.err.println("参数类型错误：" + e.getMessage());
            return Result.error("题目ID必须为数字");
        } catch (Exception e) {
            // 捕获所有其他异常
            System.err.println("运行测试代码失败：" + e.getMessage());
            return Result.error("测试失败：" + e.getMessage());
        }
    }

    public Result getQuestionById(String userId) {
        try {
            Long questionId = Long.parseLong(userId);
            Question question = questionRepository.findQuestionById(questionId);
            PracticeQuestionDTO dto = new PracticeQuestionDTO();
            dto.setFullScore(question.getFullScore());
            dto.setQuestionDesc(question.getQuestionDesc());
            dto.setHint(question.getHint());
            dto.setInputFormat(question.getInputFormat());
            dto.setOutputFormat(question.getOutputFormat());

            // 查询问题样例
            List<SampleQuestion> sample = sampleQuestionRepository.findSampleQuestionByQuestionId(questionId);
            dto.setSampleQuestions(sample);
            return Result.ok(dto);
        }catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());

        }

    }

    public Result loadQuestionList() {
        try {
            // 1.查询所有问题
            List<Question> questionList = questionRepository.findAll();
            List<QuestionManagerDTO> questionManagerDTOS = new ArrayList<>();

            // 2.查询所有样例
            List<SampleQuestion> sampleQuestionList = sampleQuestionRepository.findAll();

            // 3.查询所有测试样例
            List<TestSample> testSampleList = testSampleRepository.findAll();

            // 4.查询所有题库
            List<QuestionBank> bankList = questionBankRepository.findAll();

            for (Question question : questionList) {
                QuestionManagerDTO dto = new QuestionManagerDTO();
                ArrayList<SampleQuestion> sampleQuestionDTO = new ArrayList<>();
                ArrayList<TestSample> testSamplesDTO = new ArrayList<>();

                dto.setQuestionName(question.getQuestionName());
                dto.setQuestionDesc(question.getQuestionDesc());

                // 封装题库标题
                for (QuestionBank questionBank : bankList) {
                    if (questionBank.getId().equals(question.getBankId())){
                        dto.setBankTitle(questionBank.getTitle());
                        break;
                    }
                }

                dto.setType(question.getSourceType().toString());
                dto.setFullScore(question.getFullScore().toString());
                dto.setLevel(question.getLevel().toString());
                dto.setHint(question.getHint());
                dto.setInputFormat(question.getInputFormat());
                dto.setOutputFormat(question.getOutputFormat());
                dto.setCreateTime(question.getCreateTime());
                dto.setId(question.getId().toString());
                if(question.getBankId() != null)
                    dto.setBankId(question.getBankId().toString());

                if (question.getSimulationExamId() != null)
                    dto.setExamId(String.valueOf(question.getSimulationExamId()));

                // 使用迭代器安全删除样例
                Iterator<SampleQuestion> sampleIterator = sampleQuestionList.iterator();
                while (sampleIterator.hasNext()) {
                    SampleQuestion sampleQuestion = sampleIterator.next();
                    if (sampleQuestion.getQuestionId().equals(question.getId())) {
                        sampleQuestionDTO.add(sampleQuestion);
                        sampleIterator.remove(); // 安全删除
                    }
                }

                dto.setSampleQuestionList(sampleQuestionDTO);

                // 使用迭代器安全删除测试用例
                Iterator<TestSample> testIterator = testSampleList.iterator();
                while (testIterator.hasNext()) {
                    TestSample testSample = testIterator.next();
                    if (testSample.getQuestionId().equals(question.getId())) {
                        testSamplesDTO.add(testSample);
                        testIterator.remove(); // 安全删除
                    }
                }

                dto.setTestSampleList(testSamplesDTO);
                dto.setAnswerCode(question.getQuestionCode());
                questionManagerDTOS.add(dto);
            }

            return Result.ok(questionManagerDTOS);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新题目（优化版：前端只传递变化的数据，没变化的字段传null）
     */
    @Transactional
    public Result updateQuestion(Question question, Map<String, Object> request) {
        try {
            // 1. 检查题目是否存在
            Question existingQuestion = questionRepository.findById(question.getId())
                    .orElseThrow(() -> new IllegalArgumentException("题目不存在"));

            // 2. 更新基本信息（只更新传入的非null字段）
            updateBasicInfoIfPresent(existingQuestion, question);

            // 3. 处理测试用例（只有传入了才更新）
            if (request.containsKey("testCasesJson") && request.get("testCasesJson") != null) {
                String testCasesJson = request.get("testCasesJson").toString();
                if (!testCasesJson.trim().isEmpty()) {
                    updateTestCases(question.getId(), testCasesJson);
                }
            }

            // 4. 处理样例（只有传入了才更新）
            if (request.containsKey("samples") && request.get("samples") != null) {
                String samplesJson = request.get("samples").toString();
                if (!samplesJson.trim().isEmpty()) {
                    updateSamples(question.getId(), samplesJson);
                }
            }

            // 5. 保存题目
            questionRepository.save(existingQuestion);

            return Result.ok("更新成功");

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    /**
     * 更新基本信息（只更新传入的非null字段）
     */
    private void updateBasicInfoIfPresent(Question existing, Question newQuestion) {
        if (newQuestion.getQuestionName() != null) {
            existing.setQuestionName(newQuestion.getQuestionName());
        }
        if (newQuestion.getQuestionDesc() != null) {
            existing.setQuestionDesc(newQuestion.getQuestionDesc());
        }
        if (newQuestion.getFullScore() != null) {
            existing.setFullScore(newQuestion.getFullScore());
        }
        if (newQuestion.getHint() != null) {
            existing.setHint(newQuestion.getHint());
        }
        if (newQuestion.getInputFormat() != null) {
            existing.setInputFormat(newQuestion.getInputFormat());
        }
        if (newQuestion.getOutputFormat() != null) {
            existing.setOutputFormat(newQuestion.getOutputFormat());
        }
        if (newQuestion.getQuestionCode() != null) {
            existing.setQuestionCode(newQuestion.getQuestionCode());
        }
        if (newQuestion.getLevel() != null) {
            existing.setLevel(newQuestion.getLevel());
        }
        if (newQuestion.getBankId() != null) {
            existing.setBankId(newQuestion.getBankId());
        }
        if (newQuestion.getSimulationExamId() != null) {
            existing.setSimulationExamId(newQuestion.getSimulationExamId());
        }
    }

    /**
     * 更新测试用例
     */
    private void updateTestCases(Long questionId, String testCasesJson) {
        try {
            // 1. 解析新的测试用例
            JsonNode rootNode = objectMapper.readTree(testCasesJson);
            if (!rootNode.isArray()) {
                throw new IllegalArgumentException("测试用例格式错误，必须是数组");
            }

            // 2. 获取现有的测试用例（用于删除关联的测试结果）
            List<TestSample> oldTestSamples = testSampleRepository.findAllByQuestionId(questionId);

            // 3. 删除关联的测试结果（避免外键约束）
            if (!oldTestSamples.isEmpty()) {
                List<Long> testSampleIds = oldTestSamples.stream()
                        .map(TestSample::getId)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                testSampleResultRepository.deleteByTestSampleIds(testSampleIds);
            }

            // 4. 删除原有的测试用例
            testSampleRepository.deleteByQuestionId(questionId);

            // 5. 构建新的测试用例列表
            List<TestSample> newTestSamples = new ArrayList<>();
            int sort = 1;
            for (JsonNode node : rootNode) {
                String input = node.has("input") && !node.get("input").isNull() ?
                        node.get("input").asText() : "";
                String output = node.has("output") && !node.get("output").isNull() ?
                        node.get("output").asText() : "";

                if (output == null || output.trim().isEmpty()) {
                    throw new IllegalArgumentException("第" + sort + "个测试用例缺少输出内容");
                }

                TestSample testSample = new TestSample();
                testSample.setQuestionId(questionId);
                testSample.setInput(input);
                testSample.setOutput(output);
                testSample.setSort(sort);
                testSample.setIsDeleted(0);
                testSample.setCreateTime(LocalDateTime.now());
                testSample.setUpdateTime(LocalDateTime.now());

                newTestSamples.add(testSample);
                sort++;
            }

            // 6. 插入新的测试用例
            if (!newTestSamples.isEmpty()) {
                testSampleRepository.saveAll(newTestSamples);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("测试用例更新失败：" + e.getMessage());
        }
    }

    /**
     * 更新样例
     */
    private void updateSamples(Long questionId, String samplesJson) {
        try {
            // 1. 解析新的样例
            JsonNode rootNode = objectMapper.readTree(samplesJson);
            if (!rootNode.isArray()) {
                throw new IllegalArgumentException("样例格式错误，必须是数组");
            }

            // 2. 删除原有的样例
            sampleQuestionRepository.deleteByQuestionId(questionId);

            // 3. 构建新的样例列表
            List<SampleQuestion> newSamples = new ArrayList<>();
            for (JsonNode node : rootNode) {
                String input = node.has("input") && !node.get("input").isNull() ?
                        node.get("input").asText() : "";
                String output = node.has("output") && !node.get("output").isNull() ?
                        node.get("output").asText() : "";
                String sampleDesc = node.has("sampleDesc") && !node.get("sampleDesc").isNull() ?
                        node.get("sampleDesc").asText() : "";

                SampleQuestion sample = new SampleQuestion();
                sample.setQuestionId(questionId);
                sample.setInput(input);
                sample.setOutput(output);
                sample.setSampleDesc(sampleDesc);

                newSamples.add(sample);
            }

            // 4. 插入新的样例
            if (!newSamples.isEmpty()) {
                sampleQuestionRepository.saveAll(newSamples);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("样例更新失败：" + e.getMessage());
        }
    }

    public Result addQuestion(Question question, String samples, String testSamples) {
        try{
            System.out.println("保存的问题是=question"+question);
            // 1.保存问题
            Question save = questionRepository.save(question);

            // 2.添加样例
            JsonNode rootNode = objectMapper.readTree(samples);
            if (!rootNode.isArray()) {
                throw new IllegalArgumentException("样例格式错误，必须是数组");
            }

            List<SampleQuestion> newSamples = new ArrayList<>();
            for (JsonNode node : rootNode) {
                String input = node.has("input") && !node.get("input").isNull() ?
                        node.get("input").asText() : "";
                String output = node.has("output") && !node.get("output").isNull() ?
                        node.get("output").asText() : "";
                String sampleDesc = node.has("sampleDesc") && !node.get("sampleDesc").isNull() ?
                        node.get("sampleDesc").asText() : "";

                SampleQuestion sample = new SampleQuestion();
                sample.setQuestionId(save.getId());
                sample.setInput(input);
                sample.setOutput(output);
                sample.setSampleDesc(sampleDesc);

                newSamples.add(sample);
            }

            // 3. 插入新的样例
            if (!newSamples.isEmpty()) {
                sampleQuestionRepository.saveAll(newSamples);
            }

            // 4.添加测试用例
            JsonNode rootNode2 = objectMapper.readTree(testSamples);
            if (!rootNode2.isArray()) {
                throw new IllegalArgumentException("测试用例格式错误，必须是数组");
            }

            // 5. 构建新的测试用例列表
            List<TestSample> newTestSamples = new ArrayList<>();
            int sort = 1;
            for (JsonNode node : rootNode2) {
                String input = node.has("input") && !node.get("input").isNull() ?
                        node.get("input").asText() : "";
                String output = node.has("output") && !node.get("output").isNull() ?
                        node.get("output").asText() : "";

                if (output == null || output.trim().isEmpty()) {
                    throw new IllegalArgumentException("第" + sort + "个测试用例缺少输出内容");
                }

                TestSample testSample = new TestSample();
                testSample.setQuestionId(save.getId());
                testSample.setInput(input);
                testSample.setOutput(output);
                testSample.setSort(sort);
                testSample.setIsDeleted(0);
                testSample.setCreateTime(LocalDateTime.now());
                testSample.setUpdateTime(LocalDateTime.now());

                newTestSamples.add(testSample);
                sort++;
            }

            // 6. 插入新的测试用例
            if (!newTestSamples.isEmpty()) {
                testSampleRepository.saveAll(newTestSamples);
            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    @Transactional
    public Result deleteQ(String questionId) {
        try {
            Long questionIdLong = Long.parseLong(questionId);

            // 1.删除样例
            sampleQuestionRepository.deleteByQuestionId(questionIdLong);

            // 3.删除测试用例结果
            testSampleResultRepository.deleteTestSampleResultsByQuestionId(questionIdLong);

            // 2.删除测试用例
            testSampleRepository.deleteByQuestionId(questionIdLong);

            // 5.删除错误记录

            userWrongQuestionRepository.deleteUserWrongQuestionsByQuestionId(questionIdLong);

            // 6.删除记录
            userQuestionRecordRepository.deleteUserQuestionRecordsByQuestionId(questionIdLong);

            // 7.删除问题
            questionRepository.deleteById(questionIdLong);
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}
