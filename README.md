### 20240701
* 스프링 jpa 프로젝트 생성
* DB 커넥션 연결 확인 -> h2
* 엔티티 생성 후 테스트코드 작성
* 스프링 jpa 레파짓토리 생성 및 테스트
* p6spy 라이브러리 추가 -> 쿼리 확인용 (implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.7')
---
### 20240702
*  Member, Team 엔티티 객체 생성 및 양방향 연간관계 설정
  * Member -> Team N:1 연관관계 매핑
* 엔티티 테스트 코드 작성 (순수 jpa)
* 순수 JPA기반 리포짓토리 작성 (Member, Team) entity
* 순수 JPA 리포짓토리 -> spring Date JPA 리포짓토리로 변경
