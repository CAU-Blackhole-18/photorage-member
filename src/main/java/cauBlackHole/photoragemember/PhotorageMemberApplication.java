package cauBlackHole.photoragemember;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PhotorageMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotorageMemberApplication.class, args);
	}

}
