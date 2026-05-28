package sen.yuhuang.backend.common.utils;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;

@Component
public class TokenUtil {
    /**
     * 使用SHA-256哈希生成token
     */
    public static String generateHashedToken(String username, String password) {
        try {
            String data = username + ":" + password + ":" + System.currentTimeMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
    }
}
