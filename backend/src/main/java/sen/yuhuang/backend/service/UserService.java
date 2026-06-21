package sen.yuhuang.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import sen.yuhuang.backend.common.Result;
import sen.yuhuang.backend.common.utils.RedisUtil;
import sen.yuhuang.backend.common.utils.TokenUtil;
import sen.yuhuang.backend.entity.Role;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.entity.UserLearningStats;
import sen.yuhuang.backend.repository.RoleRepository;
import sen.yuhuang.backend.repository.UserLearningStatsRepository;
import sen.yuhuang.backend.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    UserLearningStatsRepository  userLearningStatsRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    RedisUtil redisUtil;

    public Result login(String username, String password,String role) {
        // 1.从数据库验证
        try {
            User user = userRepository.findUserByUsername(username);

            if (user != null && user.getPassword().equals(password)) {
                // 查询角色信息
                Long roleId = user.getRoleId();
                Optional<Role> Role = roleRepository.findById(roleId);

                // 严格匹配角色：VIP_STUDENT 只能用 student 角色登录
                String userRoleCode = Role.get().getRoleCode();
                boolean roleMatch = userRoleCode.equalsIgnoreCase(role)
                        || (userRoleCode.equals("VIP_STUDENT") && role.equalsIgnoreCase("student"));

                if (Role.isPresent() && roleMatch) {
                    // 使用userName+Password 生成 token
                    String token = TokenUtil.generateHashedToken(user.getUsername(), user.getPassword());

                    // 将Token 存储Redis中 12个小时过期
                    redisUtil.setToken(user.getUsername(), token, 43200);

                    // 构建返回数据
                    Map<String, Object> data = new HashMap<>();
                    data.put("token", token);
                    data.put("user", user);
                    data.put("role", Role.get());

                    return Result.ok("登录成功!", data);
                }else {
                    return Result.unauthorized("角色无权限");
                }
            } else {
                return Result.unauthorized("用户名或密码错误");
            }
        } catch (Exception e) {
            // 数据库查询异常
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    public Result register(String username, String password, String role, String college) {
        // 1.当前userName没有被使用
        User user = userRepository.findUserByUsername(username);
        if (user != null) return Result.badRequest("用户名被占用");

        // 2.注册
        User newUser = User.builder()
                .username(username)
                .password(password)
                .avatar("/avatars/17.jpg")
                .college(college)
                .roleId(1L).build();

        User save = userRepository.save(newUser);
        return Result.ok(save);
    }

    public Result logout(String username) {
        if (username != null && !username.trim().isEmpty()) {
            redisUtil.deleteToken(username.trim());
        }
        return Result.ok().setMessage("退出登录成功");
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送验证码邮件（使用163邮箱）
     */
    private void sendVerificationEmail(String toEmail, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);  // 发件人邮箱（163邮箱）
        message.setTo(toEmail);      // 收件人邮箱
        message.setSubject("验证码邮件"); // 邮件主题

        // 邮件内容
        String content = String.format(
                "【您的验证码】\n\n" +
                        "验证码：%s\n\n" +
                        "此验证码5分钟内有效，请勿泄露给他人。\n\n" +
                        "如果不是您本人操作，请忽略此邮件。",
                verificationCode
        );

        message.setText(content);

        // 发送邮件
        mailSender.send(message);
    }

    public Result sendEmail(String email) {
        try {
            // 1.查询是否有改Email
            User user = userRepository.findUserByEmail(email);

            if(user.getUsername() != null && user.getUsername().length() >= 3){
                // 生成6位随机验证码
                String verificationCode = generateVerificationCode();

                // 发送邮件（使用163邮箱配置）
                sendVerificationEmail(email, verificationCode);

                // 将验证码存储到Redis，设置5分钟过期
                redisUtil.setToken(email, verificationCode, 300);

                return Result.ok("验证码发送成功!");

            }else{
                return Result.ok("该邮箱没有绑定任何账户!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("验证码发送失败：" + e.getMessage());
        }
    }

    public Result nextResetPassWord(String code,String email) {
        String oldCode = redisUtil.getToken(email);

        if(oldCode.equals(code)){
            redisUtil.deleteToken(email);
            return Result.ok().setMessage("验证成功!");
        }else{
            return Result.badRequest("验证码错误！");
        }
    }

    public Result resetPassword(String email, String password) {
        // 1.根据邮箱获取账户
        User user = userRepository.findUserByEmail(email);

        // 2.修改用户密码
        user.setPassword(password);
        User save = userRepository.save(user);

        return Result.ok(save);
    }

    public Result verifyToken(String token,String username,String flag) {
        // 1.从redis找到token
        String oldToken = redisUtil.getToken(username);

        if (oldToken.equals(token)) {
            if (flag.equals("1")){
                System.out.println("flag="+flag);
                // 额外权限校验
                User user = userRepository.findUserByUsername(username);
                System.out.println("被校验的用户="+user);
                if(user.getRoleId() <= 2){
                    return Result.error("不具备权限！");
                }
            }
            return Result.ok().setMessage("验证token成功!");
        }else {
            return Result.badRequest("Token失效!");
        }
    }

    public Result getUserInfo(String username) {

        // 1.查询用户信息
        User user = userRepository.findUserByUsername(username);
        if(user != null){
            return Result.ok(userRepository.findUserByUsername(username));
        }else {
            return Result.badRequest("获取用户信息失败");
        }
    }

    public Result updateUserInfo(String avatar, String email, String major, String phone, String realName,
                                 String school, String score, String college, String userId) {
        System.out.println("userId="+userId);
        try {
            // 1.查询用户是否存在
            User user = userRepository.findUserByUserId(Long.valueOf(userId+""));
            System.out.println("用户信息"+user);
            if (user == null) return Result.badRequest("用户不存在!");

            // 2.修改
            userRepository.updateUserByUserId(
                    Long.valueOf(userId),      // 第1位：userId
                    avatar,      // 第2位：avatar
                    email,       // 第3位：email
                    major,       // 第4位：major
                    phone,       // 第5位：phone
                    realName,    // 第6位：realName
                    school,      // 第7位：school
                    score,       // 第8位：score
                    college      // 第9位：college
            );
            return Result.ok().setMessage("更新信息成功！");
        }catch (Exception e){
            e.printStackTrace();
            return Result.badRequest("更新失败！");
        }
    }

    public Result getStatsData(String userId) {
        System.out.println("获取用户数据userId="+userId);
        try {
            Long userIdLong =  Long.valueOf(userId);
            // 1.确定有这个用户
            User user = userRepository.findUserByUserId(userIdLong);
            if (user == null) return Result.error("加载用户数据中，用户不存在！");

            // 2.查询统计数据
            UserLearningStats stats = userLearningStatsRepository.findUserLearningStatsByUserId(userIdLong);
            HashMap<String, Object> result = new HashMap<>();
            System.out.println("查询统计数据为stats="+stats);
            if (stats == null){
                result.put("examCount", 0);
                result.put("passRate", 0);
                result.put("noteCount", 0);
                result.put("questionCount", 0);
                return Result.ok(result);
            }else {
                result.put("examCount", stats.getExamCount());
                result.put("passRate", stats.getQuestionPassRate());
                result.put("noteCount", stats.getNoteCount());
                result.put("questionCount", stats.getQuestionCount());
                return Result.ok(result);
            }
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
}
