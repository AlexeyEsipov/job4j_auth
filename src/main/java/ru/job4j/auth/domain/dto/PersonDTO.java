package ru.job4j.auth.domain.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class PersonDTO {
    private String login;
    private String password;
}