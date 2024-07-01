package study.spring_data_jpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.boot.model.source.internal.hbm.XmlElementMetadata;
import org.springframework.stereotype.Repository;
import study.spring_data_jpa.entity.Member;

@Repository
public class MemberJpaRepository {
    @PersistenceContext
    private EntityManager em;

    public Member save(Member member){
        em.persist(member);
        return member;
    }

    public Member find(Long id){
        return em.find(Member.class, id);
    }
}
