package ru.job4j.auth.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.auth.domain.Person;
import ru.job4j.auth.repository.PersonRepository;

import java.util.List;
import java.util.Optional;
@Service
@AllArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;

    public Person create(Person person) {
        person.setChecked(true);
        return personRepository.save(person);
    }
    public void delete(Person person) {
        personRepository.delete(person);
    }
    public Optional<Person> findById(int id) {
        return personRepository.findById(id);
    }

    public List<Person> findAll() {
        return personRepository.findAll();
    }

    public Optional<Person> findByLogin(String login) {
        return personRepository.findByLogin(login);
    }
}
