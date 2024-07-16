### 20240701
- 스프링 jpa 프로젝트 생성
- DB 커넥션 연결 확인 -> h2
- 엔티티 생성 후 테스트코드 작성
- 스프링 jpa 레파짓토리 생성 및 테스트
- p6spy 라이브러리 추가 -> 쿼리 확인용 (implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.7')
---
### 20240702
-  Member, Team 엔티티 객체 생성 및 양방향 연간관계 설정
   - Member -> Team N:1 연관관계 매핑
- 엔티티 테스트 코드 작성 (순수 jpa)
- 순수 JPA기반 리포짓토리 작성 (Member, Team) entity
- 순수 JPA 리포짓토리 -> spring Date JPA 리포짓토리로 변경
---
### 20240703
- 스프링 데이터 JPA에서 지원하는 쿼리 메소드 적용 (findBy ...등등)
- JPA NamedQuery 적용 -> 실무에서는 활용력 X
- 리포지토리에 쿼리 메소드 직접 정의
   - Query 어노테이션을 활용하여 정의
```
@Query("select m from Member m where m.username= :username and m.age = :age")
 List<Member> findUser(@Param("username") String username, @Param("age") int age);
```
- 단순히 값 하나를 조회하는 방법
```
 @Query("select m.username from Member m")
 List<String> findUsernameList();
```
- Dto로 직접 조회 방법 (사실 별로 안쓸거 같음..)
```
 @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
List<MemberDto> findMemberDto();
```
- 파라미터 바인팅
```
@Query("select m from Member m where m.username = :name")
Member findMembers(@Param("name") String username);
```
- 컬랙션 바이팅 ( In 절로 여러 파라미터를 넣을때)
```
 @Query("select m from Member m where m.username in :names")
 List<Member> findByNames(@Param("names") List<String> names);
```
- 최근에는 조회를 할 경우 Optional을 제공한다
```
Optional<Member> findByUsername(String name); //단건 Optional
```
- 순수 JPA에서 페이징 하는 방법
   - 검색 조건 : 나이가 10살
   - 정렬 조건 : 이름으로 내림차순
   - 페이징 조건 : 첫 번째 페이지(offset), 페이지당 보여줄 데이터는 3건 (limit)
```
public List<Member> findByPage(int age, int offset, int limit) {
     return em.createQuery("select m from Member m where m.age = :age order bym.username desc")
             .setParameter("age", age)
             .setFirstResult(offset)
             .setMaxResults(limit)
             .getResultList();
}

public long totalCount(int age) {
    return em.createQuery("select count(m) from Member m where m.age = :age",Long.class)
            .setParameter("age", age)
}

@Test
 public void paging() throws Exception {
      //given
     memberJpaRepository.save(new Member("member1", 10));
     memberJpaRepository.save(new Member("member2", 10));
     memberJpaRepository.save(new Member("member3", 10));
     memberJpaRepository.save(new Member("member4", 10));
     memberJpaRepository.save(new Member("member5", 10));
     int age = 10;
     int offset = 0;
     int limit = 3;

      //when
     List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
     long totalCount = memberJpaRepository.totalCount(age);
      //페이지 계산 공식 적용...
      // totalPage = totalCount / size ... // 마지막 페이지 ...
      // 최초 페이지 ..

      //then
     assertThat(members.size()).isEqualTo(3);
     assertThat(totalCount).isEqualTo(5);
 }
```
- 스프링 Data JPA에서 페이징 하는 방법
   - 검색 조건 : 나이가 10살
   - 정렬 조건 : 이름으로 내림차순
   - 페이징 조건 : 첫 번째 페이지, 페이지당 보여줄 데이터는 3건
```
public interface MemberRepository extends Repository<Member, Long> {
     Page<Member> findByAge(int age, Pageable pageable);
}

@Test
public void page() throws Exception {
      //given
     memberRepository.save(new Member("member1", 10));
     memberRepository.save(new Member("member2", 10));
     memberRepository.save(new Member("member3", 10));
     memberRepository.save(new Member("member4", 10));
     memberRepository.save(new Member("member5", 10));

      //when
    PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC,"username"));
    Page<Member> page = memberRepository.findByAge(10, pageRequest);

      //then
   List<Member> content = page.getContent(); //조회된 데이터
   assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
   assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
   assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
   assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
   assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
   assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
}
```
- Page객체에 count 쿼리가 포함이 되어있지만, 여러 테이블을 조인하게 되어있다면 totalCount 쿼리를 분리해서 사용하는게 좋을수도 있음
```
 @Query(value = "select m from Member m", countQuery = "select count(m.username) from Member m")
Page<Member> findMemberAllCountBy(Pageable pageable);
```
- JPA에서 엔티티를 그대로 return 시키는 것은 매우매우 위험함!! 때문에 DTO로 변환해서 return 해줘야됨
```
Page<Member> page = memberRepository.findByAge(10, pageRequest);
 Page<MemberDto> dtoPage = page.map(m -> new MemberDto());

OR

Page<MemberDto> toMap = page.map(m-> new MemberDto(m.getId(), m.getUsername(), null));
```
---
### 20240716
- 벌크성 쿼리 추가(한번여 여러 테이블 업데이트 하기)
```
    public int bulkAgePlus(int age) {

        return em.createQuery("update Member m set m.age = m.age + 1 where m.age >= :age")
                .setParameter("age", age)
                .executeUpdate();
    }
```

- 스프링 데이터 JPA에서 벌크성 수정 쿼리 추가
  - @Modifying 어노테이션을 꼭 추가 해줘야 됨
```
@Modifying
 @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
int bulkAgePlus(@Param("age") int age);
```

- 참고로 영속성 컨텍스트로 인하여 업데이트를 하고 다시 특정 데이터를 불러오면, update가 안되는 현상이 있음. 그때 강제로 엔티티 메니저를 활용해서 flush랑 clear를 해줘야됨
```
    @Test
    public void bulkUpdate(){
        //give
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));
        memberRepository.save(new Member("member6", 12));

        //when
        int resultCount = memberRepository.bulkAgePlus(20);
        //영속성 컨텍스트로 인하여 플레쉬 및 클리어를 해줘야됨. 이미 가져온 데이터는 1차 캐시에서 듥고 있기 때문에
        // 출력되는 데이터랑 디비의 저장되있는 데이터랑 다를수 있음
          em.flush();
          em.clear();

        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);
        System.out.println("member5= " + member5);

        //then
        assertThat(resultCount).isEqualTo(3);

    }
```

- 만약 저런식으로 강제로 엔티티 메니저를 활용하기 귀찮다면 @Modifying 어노테이션이 클리어오토메티컬리 값을 true로 변경해주면 됨
```
    @Modifying(clearAutomatically = true)          //변경 한다고 알려주는 어노테이션,  일걸 빼면 에러가 남 , clearAutomatically를 해주면 따로 플레시랑 클리어를 안해줘도 됨
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age); 
```

- @EntityGraph 어노테이션이란  연관된 엔티티들을 한번에 조회하는 방법을 뜻함
```
 @Test
 public void findMemberLazy() throws Exception {
     //given
     //member1 -> teamA
     //member2 -> teamB
     Team teamA = new Team("teamA");
     Team teamB = new Team("teamB");
     teamRepository.save(teamA);
     teamRepository.save(teamB);
     memberRepository.save(new Member("member1", 10, teamA));
     memberRepository.save(new Member("member2", 20, teamB));
     em.flush();
     em.clear();   
   //when
     List<Member> members = memberRepository.findAll();
   //then
     for (Member member : members) {
         member.getTeam().getName();
} }
```
 - 위에 코드를 분석해보면 마지막 for문을 돌때 쿼리가 한번 더 실행되는 것을 확인 할 수 있음, 그 이유가 연관관계를 Lazy로 했기 때문임

- 만약 이것을 한번에 조회하려면 Fetch join을 활용하면 됨
```
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();
```

- 하지만 jpa에서 표준스팩으로 도 있는 eintityGraph를 활용하면 좀더 간편하게 사용 가능
  - 첫번째는 일단 findAll은 기본 jpa에서 제공해주는 인터페이스인데, 이걸 오버라이드 해서 엔티티 그래프 어노테이션 추가해주면 됨
  - JPQL을 활용한 entityGraph인데, 첫번째와 성능은 똑같음
  - 세번째는 메서드 네임트쿼리를 활용할 수 있음
```
    @Override
    @EntityGraph(attributePaths = {"team"})         //이걸 쓰면 그냥 fetch join 되는거라고 생각하면 됨
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")            //이렇게 해도 똑같음
    List<Member> findMemberEntityGraph();

    //@EntityGraph(attributePaths = ("team"))
    @EntityGraph("Member.all")          //이렇게 하면 엔티에 네임드쿼리를 활용해서 할 수 있음
    List<Member> findEntityGraphByUsername(@Param("username") String username);
```

- NamedEntityGraph를 활용하는 방법이 있음, 엔티티에 님임드 어트리뷰트 노트  어노테이션을 먼저 추가 한 후, 인터페이스에서 활용하면 됨
```
@NamedEntityGraph(name = "Member.all", attributeNodes = @NamedAttributeNode("team"))
public class Member {

 @EntityGraph("Member.all")          //이렇게 하면 엔티에 네임드쿼리를 활용해서 할 수 있음
 List<Member> findEntityGraphByUsername(@Param("username") String username);
```  
