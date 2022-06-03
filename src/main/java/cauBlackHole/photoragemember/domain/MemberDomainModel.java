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
    private String name;
    private String email;

    private String password;

    private Authority authority;

    public MemberDomainModel(
            Long id,
            String name,
            String email,
            String password,
            Authority authority
    ){
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.authority = authority;
    }

    public static MemberDomainModel of(
            Long id,
            String email,
            String name,
            String password,
            Authority authority
    ){
        return new MemberDomainModel(
                id,
                email,
                name,
                password,
                authority
        );
    }

    public void update(
            String email,
            String name
    ) {
        this.email = email;
        this.name = name;
    }

    public String updatePassword(String newPassword) {
        this.password = newPassword;
        return this.password;
    }
}
