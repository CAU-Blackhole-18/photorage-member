package cauBlackHole.photoragemember.application.DTO.member;

import cauBlackHole.photoragemember.domain.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberResponseDto {
    private String id;
    private String email;
    private String name;

    private String nickname;

    public MemberResponseDto(String id, String email, String name, String nickname){
        this.id = id; this.email = email; this.name = name; this.nickname = nickname;
    }

    public static MemberResponseDto of(Member member){
        return new MemberResponseDto(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getNickname());
    }
}
