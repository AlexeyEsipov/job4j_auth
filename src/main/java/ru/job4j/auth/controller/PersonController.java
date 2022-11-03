package ru.job4j.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.job4j.auth.domain.Person;
import ru.job4j.auth.service.PersonService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/person")
public class PersonController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonController.class.getSimpleName());
    private final PersonService persons;
    private final BCryptPasswordEncoder encoder;
    private final ObjectMapper objectMapper;

    @GetMapping("/all")
    public ResponseEntity<List<Person>> findAll() {
        List<Person> personList = persons.findAll();
        return new ResponseEntity<>(personList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Person> findById(@PathVariable int id) {
        return new ResponseEntity<>(persons.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                                      HttpStatus.NOT_FOUND,
                                      "Person is not found. Please, check parameters."
                )), HttpStatus.OK);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Person> create(@RequestBody Person person) {
        String login = person.getLogin();
        String password = person.getPassword();
        if (isInvalidLogin(login)) {
            throw new IllegalArgumentException("Login is incorrect");
        }
        if (isInvalidPassword(password)) {
            throw new IllegalArgumentException("Password is incorrect");
        }
        if (persons.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException("User with this login already exists!");
        }
        person.setPassword(encoder.encode(password));
        return new ResponseEntity<>(persons.create(person), HttpStatus.CREATED);
    }

    @PutMapping("/")
    public ResponseEntity<Void> update(@RequestBody Person person) {
        if (isInvalidLogin(person.getLogin())) {
            throw new IllegalArgumentException("Login is incorrect");
        }
        if (isInvalidPassword(person.getPassword())) {
            throw new IllegalArgumentException("Password is incorrect");
        }
        this.persons.create(person);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        Person person = new Person();
        person.setId(id);
        this.persons.delete(person);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public void exceptionHandler(Exception e, HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        Map<String, String> source = new HashMap<>();
        source.put("message", "Some of fields empty");
        source.put("details", e.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(source));
        LOGGER.error(e.getLocalizedMessage());
    }

    private boolean isInvalidLogin(String login) {
        return login.length() < 6
                || login.length() > 20
                || !Character.isLetter(login.charAt(0))
                || login.contains(" ");
    }

    private boolean isInvalidPassword(String password) {
        password = password.toLowerCase();
        return password.length() < 8
                || password.contains("12345")
                || password.contains("user")
                || password.contains("password")
                || password.contains("qwerty")
                || password.contains("admin");
    }
}