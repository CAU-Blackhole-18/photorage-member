package cauBlackHole.photoragemember.application.DTO.member;

import cauBlackHole.photoragemember.domain.Member;
import cauBlackHole.photoragemember.domain.MemberDomainModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberResponseDto {
    private Long id;
    private String email;
    private String name;

    private MemberResponseDto(Long id, String email, String name){
        this.id = id; this.email = email; this.name = name;
    }

    public static MemberResponseDto of(Member member){
        return new MemberResponseDto(
                member.getId(),
                member.getEmail(),
                member.getName());
    }

    public static MemberResponseDto of(MemberDomainModel memberDomainModel){
        return new MemberResponseDto(
                memberDomainModel.getId(),
                memberDomainModel.getEmail(),
                memberDomainModel.getName());
    }
}
