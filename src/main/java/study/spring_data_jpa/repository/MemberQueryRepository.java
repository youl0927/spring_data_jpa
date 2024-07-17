package study.spring_data_jpa.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.spring_data_jpa.entity.Member;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {
    private final EntityManager em;

    List<Member> findAllMembers(){
        return em.createQuery("selkect m from Member m")
                .getResultList();
    }
}
