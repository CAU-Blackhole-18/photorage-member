package cauBlackHole.photoragemember.application.DTO.jwt;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class JwtTokenRequestDto {
    private String accessToken;
    private String refreshToken;
}
