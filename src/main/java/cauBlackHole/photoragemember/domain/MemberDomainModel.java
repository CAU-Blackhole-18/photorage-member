package cauBlackHole.photoragemember.domain;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class MemberDomainModel {
    private Long id;
    private String email;
    private String name;

    private String password;

    private Authority authority;

    public MemberDomainModel(
            Long id,
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
                0L,
                email,
                name,
                password,
                Authority.ROLE_USER
        );
    }
}
