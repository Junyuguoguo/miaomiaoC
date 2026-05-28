package sen.yuhuang.backend.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;  // 使用 StringRedisTemplate

    // 存储token
    public void setToken(String username, String token,long timeout) {
        String key = "token:" + username;
        stringRedisTemplate.opsForValue().set(key, token, timeout, TimeUnit.SECONDS);
    }

    // 获取token
    public String getToken(String username) {
        String key = "token:" + username;
        return stringRedisTemplate.opsForValue().get(key);
    }

    // 删除token
    public void deleteToken(String username) {
        String key = "token:" + username;
        stringRedisTemplate.delete(key);
    }

    // 验证token
    public boolean validateToken(String username, String token) {
        String storedToken = getToken(username);
        return token != null && token.equals(storedToken);
    }
}