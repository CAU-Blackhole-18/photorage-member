package cauBlackHole.photoragemember.infrastructure;

import cauBlackHole.photoragemember.domain.member.Member;

public interface MailSender {

     void sendPassword(Member member);
     String getTempPassword();
}
