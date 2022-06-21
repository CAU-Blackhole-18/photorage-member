package cauBlackHole.photoragemember.domain.member;

import cauBlackHole.photoragemember.application.DTO.member.MemberRequestFindPasswordDto;
import cauBlackHole.photoragemember.application.DTO.member.MemberRequestUpdateDto;
import cauBlackHole.photoragemember.config.exception.BadRequestException;
import cauBlackHole.photoragemember.config.exception.ErrorCode;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
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

    public Member updateNameAndNickname( MemberRequestUpdateDto memberRequestUpdateDto ){
        if (memberRequestUpdateDto.getName() != null)
        {
            if(!("".equals(memberRequestUpdateDto.getName().trim())))
            {
                this.name = memberRequestUpdateDto.getName();
            }
        }
        if (memberRequestUpdateDto.getNickname() != null)
        {
            if(!("".equals(memberRequestUpdateDto.getNickname().trim()))) {
                this.nickname = memberRequestUpdateDto.getNickname();
            }
        }
        return this;
    }

    public Member matchPassword(PasswordEncoder passwordEncoder, String password ){
        if(!passwordEncoder.matches(password, this.password))
        {
            throw new BadRequestException(ErrorCode.INVALID_PASSWORD, "비밀번호가 틀렸습니다.");
        }
        return this;
    }

    public void validateFindPassword(MemberRequestFindPasswordDto findPasswordDto){
        if(!this.getEmail().equals(findPasswordDto.getEmail()))
        {
            throw new BadRequestException(ErrorCode.INVALID_EMAIL, "이메일이 틀렸습니다.");
        }
        if(!this.getName().equals(findPasswordDto.getName()))
        {
            throw new BadRequestException(ErrorCode.INVALID_NAME, "이름이 틀렸습니다.");
        }
        if(!this.getNickname().equals(findPasswordDto.getNickname()))
        {
            throw new BadRequestException(ErrorCode.INVALID_NICKNAME, "닉네임이 틀렸습니다.");
        }
    }

    public static Member createMember(String email, String password, String name, String nickname, Authority authority){
        Member member = new Member();
        member.email = email;
        member.password = password;
        member.name = name;
        member.nickname = nickname;
        member.authority = authority;
        return member;
    }
}
