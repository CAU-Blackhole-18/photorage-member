package cauBlackHole.photoragemember.infrastructure;

import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.config.exception.ConflictException;
import cauBlackHole.photoragemember.config.exception.ErrorCode;
import cauBlackHole.photoragemember.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverMailSender {

    private final JavaMailSender javaMailSender;
    private final MemberPort memberPort;

    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String sender;

    public void sendPassword(Member member){

        log.info("임시 비밀번호 발급 시작");

        String tempPassword = getTempPassword();
        member.updatePassword(passwordEncoder.encode(tempPassword));

        Member updateMember = this.memberPort.update(member).orElseThrow(() -> {
                    throw new ConflictException(ErrorCode.UPDATE_FAIL, "임시 비밀번호 업데이트를 실패했습니다.");
                }
        );

        //메세지 생성하고 보낼 메일 설정 저장
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(updateMember.getEmail());
        message.setFrom(sender);

        message.setSubject(updateMember.getName()+" : New Temporary Password is here!");
        message.setText("Hello" + updateMember.getName() + "! We send your temporary password here. \nBut this is not secured so please change password once you sign into our site. \nPassword : " + tempPassword);
        javaMailSender.send(message);
        log.info("임시 비밀번호 발급 완료");
    }
    //임시 비밀번호 발급
    public String getTempPassword(){
        char[] charSet = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '!', '@', '#', '$', '%', '^', '&', '*', '(', ')'};

        char[] num = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        char[] alphabet = new char[] { 'a', 'b', 'c', 'd', 'e', 'f',
                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        char[] special = new char[] {'!', '@', '#', '$', '%', '^', '&', '*', '(', ')'};


        StringBuilder str = new StringBuilder();
        int idx = 0;

        //숫자
        idx = (int) (num.length * Math.random());
        str.append(num[idx]);
        //문자
        idx = (int) (alphabet.length * Math.random());
        str.append(alphabet[idx]);
        //특수문자
        idx = (int) (special.length * Math.random());
        str.append(special[idx]);

        for (int i = 3; i < 14; i++) {
            idx = (int) (charSet.length * Math.random());
            str.append(charSet[idx]);
        }
        return str.toString();
    }
}
