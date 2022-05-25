package cauBlackHole.photoragemember.domain;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class User {

    private Long id;

    @NotEmpty
    private String email; //로그인  이메일
    @NotEmpty
    private String name; //사용자 이름
    @NotEmpty
    private String password; //비밀번호
}
