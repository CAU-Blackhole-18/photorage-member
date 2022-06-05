package cauBlackHole.photoragemember.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDomainModel {
    private String id;
    private String email;
    private String name;

    private String password;

    private Authority authority;

    public MemberDomainModel(
            String id,
            String email,
            String name,
            String password,
            Authority authority
    ){
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.authority = authority;
    }

    public static MemberDomainModel of(
            String email,
            String name,
            String password
    ){
        return new MemberDomainModel(
                null,
                email,
                name,
                password,
                Authority.ROLE_USER
        );
    }
}
