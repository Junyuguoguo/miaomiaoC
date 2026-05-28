package sen.yuhuang.backend.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    // 移除 Redis 依赖（因为不再做 Token 校验）
    public TokenAuthenticationFilter() {}

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. 仅保留“放行无需认证接口”的逻辑
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 其他接口：完全放行，不做任何 Token 校验（交给前端控制）
        // 注意：这里直接放行，不再判断 Token 是否存在/有效
        filterChain.doFilter(request, response);
    }
}