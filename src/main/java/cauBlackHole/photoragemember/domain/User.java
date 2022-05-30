package cauBlackHole.photoragemember.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {

    private Long id;
    private String email; //로그인  이메일
    private String name; //사용자 이름
    private String password; //비밀번호
}
