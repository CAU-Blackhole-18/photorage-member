package cauBlackHole.photoragemember.config.jwt;

import cauBlackHole.photoragemember.application.port.outPort.MemberPort;
import cauBlackHole.photoragemember.config.exception.ErrorCode;
import cauBlackHole.photoragemember.config.exception.NotFoundException;
import cauBlackHole.photoragemember.config.exception.UnauthorizedException;
import cauBlackHole.photoragemember.config.util.SecurityUtil;
import cauBlackHole.photoragemember.domain.Authority;
import cauBlackHole.photoragemember.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final MemberPort memberPort;
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        //필요한 권한이 없이 접근하려 할때 403
        Member member = this.memberPort.findByEmail(SecurityUtil.getCurrentMemberEmail())
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_USER, "현재 접속한 유저를 찾을 수 없습니다."));

        if(member.getAuthority() == Authority.ROLE_USER){
            throw new UnauthorizedException(ErrorCode.NOT_ADMIN, "관리자가 아닙니다.");
        }
        else if(member.getAuthority() == Authority.ROLE_PAUSE){
            throw new UnauthorizedException(ErrorCode.PAUSE_USER, "정지된 유저 입니다.");
        }
        else if(member.getAuthority() == Authority.ROLE_DELETE){
            throw new UnauthorizedException(ErrorCode.DELETE_USER, "탈퇴한 유저입니다.");
        }
    }
}
