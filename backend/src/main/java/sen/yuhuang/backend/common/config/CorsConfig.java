package sen.yuhuang.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许前端开发服务器地址
                // 关键修复：用 allowedOriginPatterns 替代 allowedOrigins（SpringBoot 2.4+ 适配）
                .allowedOriginPatterns(
                        "http://localhost:5173",
                        "http://106.53.50.72:5173",
                        "http://aa.junyuguoguo.xyz",
                        "https://aa.junyuguoguo.xyz"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
