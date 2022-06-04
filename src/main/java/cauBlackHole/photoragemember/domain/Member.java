package cauBlackHole.photoragemember.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    private String name;

    @Enumerated(EnumType.STRING)
    private Authority authority;

    public void pauseMember(){
        this.authority = Authority.ROLE_PAUSE;
    }

    public void banMember(){
        this.authority = Authority.ROLE_BAN;
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
    @Builder
    public Member(String email, String password, String name, Authority authority){
        this.email = email; this.password = password; this.name = name; this.authority = authority;
    }
}
