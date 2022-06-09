package cauBlackHole.photoragemember.domain;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(value = {AuditingEntityListener.class})
public class Member {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "member_id", nullable = false, unique = true)
    private String id;

    @Column(unique = true)
    private String email;

    private String password;

    private String name;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Authority authority;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void pauseMember(){
        this.authority = Authority.ROLE_PAUSE;
    }

    public void deleteMember(){
        this.authority = Authority.ROLE_DELETE;
    }
    public void update(
            String name,
            String nickname
    ) {
        this.name = name;
        this.nickname = nickname;
    }

    public String updatePassword(String newPassword) {
        this.password = newPassword;
        return this.password;
    }
    @Builder
    public Member(String email, String password, String name, String nickname, Authority authority){
        this.email = email; this.password = password; this.name = name;
        this.nickname = nickname; this.authority = authority;
    }
}
