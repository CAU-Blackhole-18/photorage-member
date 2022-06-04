package cauBlackHole.photoragemember.application.service;

import cauBlackHole.photoragemember.adapter.persistence.MemberPortImpl;
import cauBlackHole.photoragemember.adapter.persistence.MemberRepository;
import cauBlackHole.photoragemember.application.port.out.MemberPort;
import cauBlackHole.photoragemember.config.exception.ErrorCode;
import cauBlackHole.photoragemember.config.exception.NotFoundException;
import cauBlackHole.photoragemember.domain.Member;
import cauBlackHole.photoragemember.domain.MemberDomainModel;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.MemberImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberPort memberPort;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return this.memberPort.findByEmail(username)
                .map(this::createUserDetails)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_USER, "데이터베이스에 유저를 찾을 수 없습니다."));
    }

    // DB 에 User 값이 존재한다면 UserDetails 객체로 만들어서 리턴(authenticate.getName() -> Id)
    private UserDetails createUserDetails(MemberDomainModel memberDomainModel) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(memberDomainModel.getAuthority().toString());

        return new User(
                String.valueOf(memberDomainModel.getId()),
                memberDomainModel.getPassword(),
                Collections.singleton(grantedAuthority)
        );
    }
}
