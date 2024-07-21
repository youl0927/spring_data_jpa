package study.spring_data_jpa.repository;

import lombok.Getter;

public class UsernameOnlyDto {

    private final String username;

    public UsernameOnlyDto(String username) {
        this.username = username;
    }

    public String getUsername(){
        return username;
    }
}
