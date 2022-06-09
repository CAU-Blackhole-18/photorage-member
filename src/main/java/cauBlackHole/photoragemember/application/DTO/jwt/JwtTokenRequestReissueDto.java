package cauBlackHole.photoragemember.application.DTO.jwt;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class JwtTokenRequestReissueDto {
    @NotNull
    private String accessToken;
    @NotNull
    private String refreshToken;
}
