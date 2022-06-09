package cauBlackHole.photoragemember.application.DTO.member;

import cauBlackHole.photoragemember.domain.Authority;
import cauBlackHole.photoragemember.domain.MemberDomainModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequestUpdateDto {
    String name;
    String nickname;
}
