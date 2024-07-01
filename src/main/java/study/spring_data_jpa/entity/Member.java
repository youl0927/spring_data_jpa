package study.spring_data_jpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

@Entity
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue
    private Long id;
    private String username;

    protected  Member(){

    }

    public Member(String username) {
        this.username = username;
    }
}
