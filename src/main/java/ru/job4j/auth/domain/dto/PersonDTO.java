package ru.job4j.auth.domain.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class PersonDTO {
    @NotBlank(message = "Login cannot be empty")
    @Length(min = 6, max = 20)
    private String login;
    @NotBlank(message = "Password cannot be empty")
    @Length(min = 8)
    private String password;
}