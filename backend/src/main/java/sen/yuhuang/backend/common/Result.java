package sen.yuhuang.backend.common;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应类 - 所有接口都返回这个格式
 */
@Data
public class Result {

    private Integer code;      // 状态码：200成功，其他失败
    private String message;    // 提示信息
    private Object data;       // 返回数据

    // 私有构造方法
    private Result() {}

    // ==================== 成功响应 ====================

    /**
     * 成功（无返回数据）
     */
    public static Result ok() {
        Result result = new Result();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 成功（有返回数据）
     */
    public static Result ok(Object data) {
        Result result = new Result();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功（自定义提示信息）
     */
    public static Result ok(String message, Object data) {
        Result result = new Result();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // ==================== 失败响应 ====================

    /**
     * 失败（默认错误信息）
     */
    public static Result error() {
        Result result = new Result();
        result.setCode(500);
        result.setMessage("操作失败");
        return result;
    }

    /**
     * 失败（自定义错误信息）
     */
    public static Result error(String message) {
        Result result = new Result();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败（自定义状态码和错误信息）
     */
    public static Result error(Integer code, String message) {
        Result result = new Result();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    // ==================== 特定状态码响应 ====================

    /**
     * 参数错误（400）
     */
    public static Result badRequest(String message) {
        Result result = new Result();
        result.setCode(400);
        result.setMessage(message);
        return result;
    }

    /**
     * 未授权（401）
     */
    public static Result unauthorized(String message) {
        Result result = new Result();
        result.setCode(401);
        result.setMessage(message);
        return result;
    }

    /**
     * 禁止访问（403）
     */
    public static Result forbidden(String message) {
        Result result = new Result();
        result.setCode(403);
        result.setMessage(message);
        return result;
    }

    /**
     * 资源不存在（404）
     */
    public static Result notFound(String message) {
        Result result = new Result();
        result.setCode(404);
        result.setMessage(message);
        return result;
    }

    // ==================== 链式调用方法 ====================

    public Result setCode(Integer code) {
        this.code = code;
        return this;
    }

    public Result setMessage(String message) {
        this.message = message;
        return this;
    }

    public Result setData(Object data) {
        this.data = data;
        return this;
    }

    /**
     * 添加自定义属性（用于扩展）
     */
    public Result put(String key, Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        if (this.data instanceof Map) {
            ((Map<String, Object>) this.data).put(key, value);
        }
        return this;
    }
}