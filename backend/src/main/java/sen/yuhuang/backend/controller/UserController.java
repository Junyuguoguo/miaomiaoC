package sen.yuhuang.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.service.UserService;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String role = request.get("role");

        if (username == null || username.trim().isEmpty()) {
            return Result.badRequest("用户名不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.badRequest("密码不能为空");
        }

        return userService.login(username,password,role);
    }

    @PostMapping("/logout")
    public Result logout(@RequestBody(required = false) Map<String, String> request) {
        String username = request == null ? null : request.get("username");
        return userService.logout(username);
    }

    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String role = request.get("role");

        if (username == null || username.trim().isEmpty()) return Result.badRequest("用户名不能为空");
        if (password == null || password.trim().isEmpty()) return Result.badRequest("密码不能为空");

        return userService.register(username,password,role);
    }

    @PostMapping("/sendEmail")
    public Result sendEmail(@RequestBody Map<String, String> request) {
        // 从请求中获取邮箱地址
        String email = request.get("email");

        // 参数校验（前端已校验格式，这里只做非空检查）
        if (email == null || email.trim().isEmpty()) {
            return Result.error("邮箱地址不能为空");
        }

        return userService.sendEmail(email);
    }

    @PutMapping("/nextResetPassWord")
    public Result nextResetPassWord(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String email = request.get("email");
        if (code == null || code.trim().isEmpty()) return Result.badRequest("验证码为空！");
        if (email == null || email.trim().isEmpty()) return Result.badRequest("邮箱为空!");

        return userService.nextResetPassWord(code,email);
    }

    @PutMapping("/resetPassword")
    public Result resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("newPassword");
        if (email.equals("") || email == null) return Result.badRequest("邮箱不可以为空!");
        if (password.equals("") || password == null) return Result.badRequest("新密码为空");

        return userService.resetPassword(email,password);
    }

    @PostMapping("/verifyToken")
    public Result verifyToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String username = request.get("username");
        String flag = request.get("flag");
    
        if (flag == null || flag.trim().isEmpty()) return Result.badRequest("flag 不可以为空");
        if (token == null || token.trim().isEmpty()) return Result.badRequest("Token 不可以为空");
        if (username == null || username.trim().isEmpty()) return Result.badRequest("username 不可以空");
    
        return userService.verifyToken(token,username,flag);
    }

    @PostMapping("/getUserInfo")
    public Result getUserInfo(@RequestBody Map<String, Object> request) {
        String username = request.get("username")+"";
        if (username.equals("") || username == null) return Result.badRequest("获取用户信息失败!");
        return userService.getUserInfo(username);
    }

    @PutMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody Map<String, String> request) {
        String userId =  request.get("userId");
        String avatar = request.get("avatar");
        String email = request.get("email");
        String major = request.get("major");
        String phone = request.get("phone");
        String realName = request.get("real_name");
        String school = request.get("school");
        String score = request.get("score");
        System.out.println("ssssss,"+userId+"s"+avatar+"S"+email+"S"+major+"s"+phone+"S"+realName+"S"+school+"S"+score);

        if (email == null || major == null || phone == null
                || realName == null || school == null || score == null || userId == null)
            return Result.badRequest("信息不可以存在空的!!!");
        return userService.updateUserInfo(avatar,email,major,phone,realName,school,score,userId);
    }

    @PostMapping("/getStatsData")
    public Result getStatsData(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        if (userId == null || userId.equals("")) return Result.error("加载用户数据用户ID不可为空!");
        return userService.getStatsData(userId);
    }

    @PostMapping("/uploadAvatar")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file,
                               @RequestHeader(value = "X-Username", required = false) String username) {
        if (file.isEmpty()) {
            return Result.badRequest("请选择要上传的文件");
        }

        String contentType = file.getContentType();
        Set<String> allowedTypes = Set.of("image/jpeg", "image/png", "image/webp");
        if (contentType == null || !allowedTypes.contains(contentType)) {
            return Result.badRequest("仅支持 JPG、PNG、WebP 格式");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.badRequest("文件大小不能超过 5MB");
        }

        // Determine file extension
        String ext = "jpg";
        if ("image/png".equals(contentType)) ext = "png";
        else if ("image/webp".equals(contentType)) ext = "webp";

        // Generate unique filename
        String filename = "user_" + (username != null ? username : "unknown") + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;

        // Save file — use absolute path so transferTo doesn't resolve into Tomcat temp dir
        String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(uploadDir + filename));
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }

        String avatarUrl = "/uploads/avatars/" + filename;
        return Result.ok(avatarUrl);
    }

}
