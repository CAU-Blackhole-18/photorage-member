package cauBlackHole.photoragemember.application.DTO.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberRequestFindPasswordDto {

    @Email(message = "잘못된 이메일 형식입니다.")
    @NotNull(message = "이메일이 입력되지 않았습니다.")
    private String email;

    @NotBlank(message = "사용자 이름이 입력되지 않았습니다.")
    private String name;

    @NotNull(message = "닉네임이 입력되지 않았습니다.")
    private String nickname;
}
