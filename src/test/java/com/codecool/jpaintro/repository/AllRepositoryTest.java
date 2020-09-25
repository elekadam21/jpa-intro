package com.codecool.jpaintro.repository;

import com.codecool.jpaintro.entity.Address;
import com.codecool.jpaintro.entity.Location;
import com.codecool.jpaintro.entity.School;
import com.codecool.jpaintro.entity.Student;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class AllRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void saveOneSimple() {
        Student john = Student.builder()
                .email("john@codecool.com")
                .name("John")
                .build();

        studentRepository.save(john);

        List<Student> studentList = studentRepository.findAll();
        assertThat(studentList).hasSize(1);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void saveUniqueFieldTwice() {
        Student student1 = Student.builder()
                .email("john@codecool.com")
                .name("John")
                .build();

        studentRepository.save(student1);

        Student student2 = Student.builder()
                .email("john@codecool.com")
                .name("Peter")
                .build();

        studentRepository.saveAndFlush(student2);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void emailShouldBeNotNull() {
        Student student = Student.builder()
                .name("John")
                .build();

        studentRepository.save(student);
    }

    @Test
    public void transientIsNotSaved() {
        Student student = Student.builder()
                .birthDate(LocalDate.of(1990, 1, 1))
                .email("john@codecool.com")
                .name("John")
                .build();
        student.calculateAge();
        assertThat(student.getAge()).isGreaterThanOrEqualTo(30);

        studentRepository.save(student);
        entityManager.clear();

        List<Student> students = studentRepository.findAll();
        assertThat(students).allMatch(student1 -> student1.getAge() == 0L);
    }

    @Test
    public void addressIsPersistedWithStudent() {
        Address address = Address.builder()
                .country("Hungary")
                .city("Budapest")
                .address("Nagymezo street 44")
                .zipCode(1065)
                .build();

        Student student = Student.builder()
                .email("temp@codecool.com")
                .address(address)
                .build();

        studentRepository.save(student);

        List<Address> addresses = addressRepository.findAll();
        assertThat(addresses)
                .hasSize(1)
                .allMatch(address1 -> address1.getId() > 0L);
    }

    @Test
    public void studentsArePersistedAndDeletedWithNewSchool() {
        Set<Student> students = IntStream.range(1, 10)
                .boxed()
                .map(integer -> Student.builder().email("student" + integer + "@codecool.com").build())
                .collect(Collectors.toSet());

        School school = School.builder()
                .students(students)
                .location(Location.BUDAPEST)
                .build();

        schoolRepository.save(school);

        assertThat(studentRepository.findAll())
                .hasSize(9)
                .anyMatch(student -> student.getEmail().equals("student9@codecool.com"));

        schoolRepository.deleteAll();

        assertThat(studentRepository.findAll())
                .hasSize(0);
    }
}