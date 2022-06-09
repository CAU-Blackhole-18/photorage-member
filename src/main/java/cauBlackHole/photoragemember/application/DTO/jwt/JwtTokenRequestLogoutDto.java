package cauBlackHole.photoragemember.application.DTO.jwt;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class JwtTokenRequestLogoutDto {
    @NotNull
    private String accessToken;
}
