package sen.yuhuang.backend.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sen.yuhuang.backend.common.utils.RedisUtil;
import sen.yuhuang.backend.entity.User;
import sen.yuhuang.backend.repository.UserRepository;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final RedisUtil redisUtil;
    private final UserRepository userRepository;

    public TokenAuthenticationFilter(RedisUtil redisUtil, UserRepository userRepository) {
        this.redisUtil = redisUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // skip public auth endpoints, uploaded static files, and websocket
        if (requestURI.startsWith("/api/auth/") || requestURI.startsWith("/uploads/") || requestURI.startsWith("/ws-chat")) {
            filterChain.doFilter(request, response);
            return;
        }

        // extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // get username from X-Username header, validate via Redis
            String username = request.getHeader("X-Username");
            if (username != null && !username.isEmpty()) {
                boolean valid = redisUtil.validateToken(username, token);
                if (valid) {
                    User user = userRepository.findUserByUsername(username);
                    if (user != null) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
