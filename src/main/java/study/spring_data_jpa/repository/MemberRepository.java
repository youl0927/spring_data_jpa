package study.spring_data_jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.spring_data_jpa.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {         //shift+option+T 테스트 생성
}
