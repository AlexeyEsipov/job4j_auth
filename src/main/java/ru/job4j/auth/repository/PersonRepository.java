package ru.job4j.auth.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.job4j.auth.domain.Person;

import java.util.List;
@Repository
public interface PersonRepository extends CrudRepository<Person, Integer> {
    List<Person> findAll();
}