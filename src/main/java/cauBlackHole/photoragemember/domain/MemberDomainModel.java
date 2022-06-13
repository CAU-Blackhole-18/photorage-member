package cauBlackHole.photoragemember.domain;

import cauBlackHole.photoragemember.application.DTO.member.MemberRequestUpdateDto;
import cauBlackHole.photoragemember.config.exception.ErrorCode;
import cauBlackHole.photoragemember.config.exception.UnauthorizedException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    public MemberDomainModel update( MemberRequestUpdateDto memberRequestUpdateDto ){
        if (memberRequestUpdateDto.getName() != null || "".equals(memberRequestUpdateDto.getName().trim())) {
            this.name = memberRequestUpdateDto.getName();
        }
        if (memberRequestUpdateDto.getNickname() != null|| "".equals(memberRequestUpdateDto.getNickname().trim())) {
            this.nickname = memberRequestUpdateDto.getNickname();
        }
        return this;
    }

    public MemberDomainModel matchPassword(PasswordEncoder passwordEncoder, String updatePassword ){
        if(!passwordEncoder.matches(updatePassword, this.password))
        {
            throw new UnauthorizedException(ErrorCode.WRONG_PASSWORD, "기존 비밀번호를 틀렸습니다.");
        }
        return this;
    }
}
