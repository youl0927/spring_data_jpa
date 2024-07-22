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
---
### 20240718
- JPA Hint
```
@QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
Member findReadOnlyByUsername(String username);
```
   -쿼리 힌트 사용 확인
```
```java
@Test
public void queryHint() throws Exception {
//given
memberRepository.save(new Member("member1", 10));
em.flush();
em.clear();
//when
Member member = memberRepository.findReadOnlyByUsername("member1");
member.setUsername("member2");
em.flush(); //Update Query 실행X
}
```
- 쿼리 힌트에 Page 추가 예제
```
@QueryHints(value = { @QueryHint(name = "org.hibernate.readOnly", value = "true")}, forCounting = true)
Page<Member> findByUsername(String name, Pageable pageable);
```
---
- 사용자 정의 인터페이스 구현 클래스
   - 기본적으로 제공하는 JPA 가 아닌, 사용자가 커스텀해서 사용할수 있는 인터페이스
   - 먼저 기본적인 interface를 작성해 준다.
```
public interface MemberRepositoryCustom {
List<Member> findMemberCustom();
}
```
   - 그다음에 이 인터페이스를 상속해줄 구현클래스를 작성한다
```
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
   private final EntityManager em;
   @Override
   public List<Member> findMemberCustom() {
      return em.createQuery("select m from Member m")
                  .getResultList();
   }
}

OR

@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {
   private final EntityManager em;
   @Override
   public List<Member> findMemberCustom() {
      return em.createQuery("select m from Member m")
               .getResultList();
   }
}
```
   - 그 다음에 사용자 정의 인터페이스에 상속해주면 된다
```
   public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
   }
```
   - 정리하면 MemberRepository(interface) -> MemberRepositoryCustom(interface) <- MemberRepositoryImpl(class), MemberRepositoryCustomImp(Class) : 두개다 가
   - 꼭 JPA에서 제공하는 인터페이스에서 Impl를 붙여줘야 됨, 규칙임!!!!!!!!!!
   - 그 다음에 상위 인터페이스에서 호출해주면 됨
```
List<Member> result = memberRepository.findMemberCustom();
```

- Auditing : 실무에서 각 테이블, 엔티티에는 기본적으로 등록일 수정일 등록자, 수정자 이런 컬럼이 들어가는 경우가 많다.
   - 먼저 순수 JPA 사용할 경우
```
@MappedSuperclass
@Getter
public class JpaBaseEntity {

   @Column(updatable = false)
   private LocalDateTime createdDate;
   private LocalDateTime updatedDate;

   @PrePersist
   public void prePersist() {
   LocalDateTime now = LocalDateTime.now();
   createdDate = now;
   updatedDate = now;
   }

   @PreUpdate
   public void preUpdate() {
   updatedDate = LocalDateTime.now();
   }
}
```
   - 중요한 어노테이션이 @PrePersist, @PostPersist, @PreUpdate, @PostUpdate
   - 그 다음에는 엔티티에서 상속을 해준다
```
public class Member extends JpaBaseEntity {}
```

   - 테스트 코드
```
@Test
public void jpaEventBaseEntity() throws Exception {
   //given
   Member member = new Member("member1");
   memberRepository.save(member); //@PrePersist
   Thread.sleep(100);
   member.setUsername("member2");
   em.flush(); //@PreUpdate
   em.clear();
   //when
   Member findMember = memberRepository.findById(member.getId()).get();
   //then
   System.out.println("findMember.createdDate = " + findMember.getCreatedDate());
   System.out.println("findMember.updatedDate = " + findMember.getUpdatedDate());
}
```

   - 순수 JPA가 아닌 Spring date jpa를 활용
   - 먼저 스프링 부트 클래스, 엔티에 어노테이션을 설정 해줘야됨
      - @EnableJpaAuditing -> 클래스, @EntityListeners(AuditingEntityListener.class) -> 엔티티
```
@EnableJpaAuditing         //이 어노테이션을 곡 써줘야됨
@SpringBootApplication
public class DataJpaApplication {

   public static void main(String[] args) {
      SpringApplication.run(DataJpaApplication.class, args);
   }

   @Bean            //이건 임시 UUID이고 실제로는 유저 ID가 들어갈듯
   public AuditorAware<String> auditorProvider() {
      return () -> Optional.of(UUID.randomUUID().toString());
   }
}
```
   - 먼저 등록일, 수정일 적용
```
package study.datajpa.entity;
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseEntity {

   @CreatedDate
   @Column(updatable = false)
   private LocalDateTime createdDate;

   @LastModifiedDate
   private LocalDateTime lastModifiedDate;
}
```
   - 등록자, 수정자 적용
```
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class BaseEntity {
   @CreatedDate
   @Column(updatable = false)
   private LocalDateTime createdDate;

   @LastModifiedDate
   private LocalDateTime lastModifiedDate;

   @CreatedBy
   @Column(updatable = false)
   private String createdBy;

   @LastModifiedBy
   private String lastModifiedBy;
}
```

   - 참고로 실무에서는 엔티티 수정 시간만 있고, 수정자는 없을 수도 있으니, 분리하는 것이 좋을 수도 있음
```
public class BaseTimeEntity {
   @CreatedDate
   @Column(updatable = false)
   private LocalDateTime createdDate;

   @LastModifiedDate
   private LocalDateTime lastModifiedDate;
}

public class BaseEntity extends BaseTimeEntity {
   @CreatedBy
   @Column(updatable = false)
   private String createdBy;

   @LastModifiedBy
   private String lastModifiedBy;
}
```

- Web 확장 도메인 클래스 컨버터
     - 네트워크 상에서 Id 값 같은 키값으로 통신 하는것이 보안 적으로 위험할수 있음
```
사용전
@RestController
@RequiredArgsConstructor
public class MemberController {
   private final MemberRepository memberRepository;

   @GetMapping("/members/{id}")
   public String findMember(@PathVariable("id") Long id) {
   Member member = memberRepository.findById(id).get();
   return member.getUsername();
   }
}

사용후
@RestController
@RequiredArgsConstructor
public class MemberController {

   private final MemberRepository memberRepository;

   @GetMapping("/members/{id}")
   public String findMember(@PathVariable("id") Member member) {
   return member.getUsername();
   }
}
```

- web 확장 페이징
```
@GetMapping("/members")
public Page<Member> list(Pageable pageable) {
   Page<Member> page = memberRepository.findAll(pageable);
   return page;
}

api 요청 예)
/members?page=0&size=3&sort=id,desc&sort=username,desc
```

- 파라미터가 아닌 글로벌 성정도 가능함 - yml 파일
```
spring.data.web.pageable.default-page-size=20 /# 기본 페이지 사이즈/
spring.data.web.pageable.max-page-size=2000 /# 최대 페이지 사이즈/
```

- 엔티티를 직접적으로 넘기는 것은 매우 권장하지 않기 때문에 DTO를 활용해야함, 근데 page는 내부적으로 map을 제공하기 때문에쉬움
```
기존 코드
@GetMapping("/members")
public Page<MemberDto> list(Pageable pageable) {
   Page<Member> page = memberRepository.findAll(pageable);
   Page<MemberDto> pageDto = page.map(MemberDto::new);
return pageDto;
}

map으로 최적화 된 코드
@GetMapping("/members")
public Page<MemberDto> list(Pageable pageable) {
   return memberRepository.findAll(pageable).map(MemberDto::new);
}
```

### 20240722
- Query By Example
```
@SpringBootTest
@Transactional
public class QueryByExampleTest {
   @Autowired MemberRepository memberRepository;
   @Autowired EntityManager em;
   @Test
   public void basic() throws Exception {
      //given
      Team teamA = new Team("teamA");
      em.persist(teamA);
      em.persist(new Member("m1", 0, teamA));
      em.persist(new Member("m2", 0, teamA));
      em.flush();

      //when
      //Probe 생성
      Member member = new Member("m1");
      Team team = new Team("teamA"); //내부조인으로 teamA 가능
      member.setTeam(team);
      //ExampleMatcher 생성, age 프로퍼티는 무시
      ExampleMatcher matcher = ExampleMatcher.matching()
      .withIgnorePaths("age");
      Example<Member> example = Example.of(member, matcher);
      List<Member> result = memberRepository.findAll(example);

      //then
      assertThat(result.size()).isEqualTo(1);
   }
}
```
- 실무에서 잘 안씀

---

- Projection
   - 실무에서 그나마 조금 쓴다
   - 모든 엔티티가 아닌 변수 하나만 가져오고 싶을때 많이 사용한다고 함
```
public interface UsernameOnly {
   String getUsername();
}
```

```
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, JpaSpecificationExecutor {
   List<UsernameOnly> findProjectionsByUsername(String username);
}
```

```
@Test
public void projections() throws Exception {
   //given
   Team teamA = new Team("teamA");
   em.persist(teamA);
   Member m1 = new Member("m1", 0, teamA);
   Member m2 = new Member("m2", 0, teamA);
   em.persist(m1);
   em.persist(m2);
   em.flush();
   em.clear();

   //when
   List<UsernameOnly> result =
   memberRepository.findProjectionsByUsername("m1");

   //then
   Assertions.assertThat(result.size()).isEqualTo(1);
}
```

- MemberRepository에서 Projection 기반의 인터페이스를 상속한 뒤, 인터페이스 추가 후 사용하면됨
   - 네이밍 할대 find...ByUsername 이런식으로 규칙을 지켜 주면 된다.

```
public interface UsernameOnly {
   @Value("#{target.username + ' ' + target.age + ' ' + target.team.name}")
   String getUsername();
}
```
- 이런식으로 Open Projection 문법도 지원한다.

---
- 클래스 기반 Projection
   - 인터페이스 기반을 Class 기반으로 바뀜, 장잠은 Dto를 활용할 수 있음
```
public class UsernameOnlyDto {
private final String username;
   public UsernameOnlyDto(String username) {
      this.username = username;
   }
   
   public String getUsername() {
      return username;
   }
}
```

- 동적 Projections
```
<T> List<T> findProjectionsByUsername(String username, Class<T> type);

List<UsernameOnly> result = memberRepository.findProjectionsByUsername("m1", UsernameOnly.class);
```

- 중첩 구조 처리
```
public interface NestedClosedProjection {
   String getUsername();
   TeamInfo getTeam();
   interface TeamInfo {
   String getName();
   }
}
```
---
- 네이티브 쿼리
   - 자주 사용되지는 않지만, 동적쿼리가 아닌, 어쩔수 없을때 사용해야됨
   - 페이징도 가능함
```
public interface MemberRepository extends JpaRepository<Member, Long> {
   @Query(value = "select * from member where username = ?", nativeQuery =true)
   Member findByNativeQuery(String username);
}
```
- Projections을 활용해서 Dto에 담을 수도 있음
```
@Query(value = "SELECT m.member_id as id, m.username, t.name as teamName FROM member m left join team t ON m.team_id = t.team_id",
      countQuery = "SELECT count(*) from member",
      nativeQuery = true)
Page<MemberProjection> findByNativeProjection(Pageable pageable);
```
