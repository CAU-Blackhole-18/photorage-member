package cauBlackHole.photoragemember.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDomainModel {
    private String id;
    private String email;
    private String name;

    private String nickname;
    private String password;

    private Authority authority;

    public MemberDomainModel(
            String id,
            String email,
            String name,
            String nickname,
            String password,
            Authority authority
    ){
        this.id = id;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.authority = authority;
    }

    public static MemberDomainModel of(
            String email,
            String name,
            String nickname,
            String password
    ){
        return new MemberDomainModel(
                null,
                email,
                name,
                nickname,
                password,
                Authority.ROLE_USER
        );
    }
}
