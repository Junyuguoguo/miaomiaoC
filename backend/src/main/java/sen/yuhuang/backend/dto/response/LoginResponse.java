package sen.yuhuang.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String realName;
    private String role;
    private Long expiresIn;
}