package study.spring_data_jpa.controller;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.spring_data_jpa.dto.MemberDto;
import study.spring_data_jpa.entity.Member;
import study.spring_data_jpa.repository.MemberRepository;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/member/{id}")
    public String findMember(@PathVariable("id") Long id){
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    @GetMapping("/members2/{id}")
    public String findMember(@PathVariable("id") Member member){
        return member.getUsername();
    }

    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size = 5) Pageable pageable){
        return memberRepository.findAll(pageable)
                .map(MemberDto::new);
    }

    @PostConstruct      // 스프링이 올라올때 한번 무조건 실행되는 어노테이션
    public void init(){
//        memberRepository.save(new Member("userA"));

        for (int i = 0; i < 100; i++){
            memberRepository.save(new Member("user" + i, i));
        }
    }
}
