package sen.yuhuang.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 */
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        
        // 设置 key 的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        // 设置 value 的序列化器
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // 设置 hash key 的序列化器
        template.setHashKeySerializer(new StringRedisSerializer());
        // 设置 hash value 的序列化器
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}
