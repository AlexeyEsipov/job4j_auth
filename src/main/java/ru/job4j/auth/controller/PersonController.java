package ru.job4j.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.job4j.auth.domain.Person;
import ru.job4j.auth.domain.dto.PersonDTO;
import ru.job4j.auth.service.PersonService;

@RestController
@AllArgsConstructor
@RequestMapping("/person")
public class PersonController {
    private static final String LOGIN_INVALID = "Login is incorrect";
    private static final String PASSWORD_INVALID = "Password is incorrect";
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PersonController.class.getSimpleName());
    private final PersonService persons;
    private final BCryptPasswordEncoder encoder;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;

    @GetMapping("/all")
    public ResponseEntity<List<Person>> findAll() {
        List<Person> personList = persons.findAll();
        return new ResponseEntity<>(personList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        return new ResponseEntity<>(persons.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                      "Person is not found. Please, check parameters."
                )), HttpStatus.OK);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Person> create(@Valid @RequestBody PersonDTO personDTO) {
        String login = personDTO.getLogin();
        String password = personDTO.getPassword();
        if (isInvalidLogin(login)) {
            throw new IllegalArgumentException(LOGIN_INVALID);
        }
        if (isInvalidPassword(password)) {
            throw new IllegalArgumentException(PASSWORD_INVALID);
        }
        if (persons.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException("User with this login already exists!");
        }
        Person person = convertToPerson(personDTO);
        person.setPassword(encoder.encode(password));
        return new ResponseEntity<>(persons.create(person), HttpStatus.CREATED);
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@Valid @RequestBody PersonDTO personDTO) {
        if (isInvalidLogin(personDTO.getLogin())) {
            throw new IllegalArgumentException(LOGIN_INVALID);
        }
        if (isInvalidPassword(personDTO.getPassword())) {
            throw new IllegalArgumentException(PASSWORD_INVALID);
        }
        this.persons.create(convertToPerson(personDTO));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        Person person = new Person();
        person.setId(id);
        this.persons.delete(person);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/")
    public ResponseEntity<Person> patch(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                              "You need to provide a valid id.");
        }
        Person rsl = persons.findById(Integer.parseInt(id))
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "A person with this id has not been found."));
        if (body.containsKey("login")) {
            String login = body.get("login");
            if (login.isBlank()) {
                throw new NullPointerException(LOGIN_INVALID);
            }
            if (isInvalidLogin(login)) {
                throw new IllegalArgumentException(LOGIN_INVALID);
            }
            rsl.setLogin(login);
        }
        if (body.containsKey("password")) {
            String password = body.get("password");
            if (password.isBlank()) {
                throw new NullPointerException(PASSWORD_INVALID);
            }
            if (isInvalidPassword(password)) {
                throw new IllegalArgumentException(PASSWORD_INVALID);
            }
            rsl.setPassword(password);
        }
        persons.create(rsl);
        return ResponseEntity.ok(rsl);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public void exceptionHandler(Exception e, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        Map<String, String> source = new HashMap<>();
        source.put("message", "Some of fields empty");
        source.put("details", e.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(source));
        LOGGER.error(e.getLocalizedMessage());
    }

    private boolean isInvalidLogin(String login) {
        return  !Character.isLetter(login.charAt(0))
                || login.contains(" ");
    }

    private boolean isInvalidPassword(String password) {
        password = password.toLowerCase();
        return password.contains("12345")
                || password.contains("user")
                || password.contains("password")
                || password.contains("qwerty")
                || password.contains("admin");
    }

    private Person convertToPerson(PersonDTO personDTO) {
        return modelMapper.map(personDTO, Person.class);
    }
}