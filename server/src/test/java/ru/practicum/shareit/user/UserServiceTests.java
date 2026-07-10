package ru.practicum.shareit.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.base.exceptions.DuplicateDataException;
import ru.practicum.shareit.base.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;


import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTests {

    private static final String NAME = "name";
    private static final String EMAIL = "valid@email.com";
    private static final UserDto TEST_USER = new UserDto(NAME, EMAIL);

    private final EntityManager em;
    private final UserService service;

    @Test
    void findAll_shouldReturnEmptyListWhenDbEmpty() {
        Collection<ResponseUserDto> result = service.findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_shouldReturnAll() {
        service.create(TEST_USER);

        Collection<ResponseUserDto> result = service.findAll();
        assertThat(result, hasSize(1));
        assertThat(result.stream().findFirst().get(), hasProperty("id"));
        assertThat(result.stream().findFirst().get(), hasProperty("name"));
        assertThat(result.stream().findFirst().get(), hasProperty("email"));
    }

    @Test
    void findById_shouldReturnUser_withId() {
        service.create(TEST_USER);
        long id = service.findAll().stream().findFirst().get().getId();

        ResponseUserDto dto = service.findById(id);
        assertThat(dto.getId(), equalTo(id));
        assertThat(dto.getName(), equalTo(TEST_USER.getName()));
        assertThat(dto.getEmail(), equalTo(TEST_USER.getEmail()));
    }

    @Test
    void findById_shouldThrowNotFound_whenIdNotFound() {
        assertThrows(NotFoundException.class, () -> service.findById(9999));
    }

    @Test
    void create_shouldCreateUser() {
        service.create(TEST_USER);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", TEST_USER.getEmail())
                .getSingleResult();

        assertThat(user.getId(), is(notNullValue()));
        assertThat(user.getName(), is(TEST_USER.getName()));
        assertThat(user.getEmail(), is(TEST_USER.getEmail()));
    }

    @Test
    void create_shouldTrimNameAndEmail() {
        UserDto dto = new UserDto(NAME + "  ", " " + EMAIL);
        Long id = service.create(dto).getId();

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        User user = query.setParameter("id", id)
                .getSingleResult();

        assertThat(user.getId(), is(notNullValue()));
        assertThat(user.getName(), is(NAME));
        assertThat(user.getEmail(), is(EMAIL));
    }

    @Test
    void create_shouldThrow_ifEmailNotUnique() {
        service.create(TEST_USER);
        UserDto duplicateEmailDto = new UserDto("username123", TEST_USER.getEmail());
        assertThrows(DuplicateDataException.class, () -> service.create(duplicateEmailDto));
    }

    @Test
    void update_shouldUpdateUser() {
        Long id = service.create(TEST_USER).getId();

        UserDto dto = new UserDto(TEST_USER.getName() + "123", "123" + EMAIL);
        service.update(id, dto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        User user = query.setParameter("id", id)
                .getSingleResult();

        assertThat(user.getId(), is(id));
        assertThat(user.getName(), is(dto.getName()));
        assertThat(user.getEmail(), is(dto.getEmail()));
    }

    @Test
    void update_shouldNotThrow_withNullValues() {
        Long id = service.create(TEST_USER).getId();
        UserDto dto = new UserDto(null, null);
        assertDoesNotThrow(() -> service.update(id, dto));
    }

    @Test
    void update_shouldNotThrow_withUnchangedEmail() {
        Long id = service.create(TEST_USER).getId();
        UserDto dto = new UserDto(null, TEST_USER.getEmail());
        assertDoesNotThrow(() -> service.update(id, dto));
    }

    @Test
    void update_shouldThrow_ifEmailNotUnique() {
        service.create(TEST_USER);

        UserDto newUser = new UserDto("differentName", "differentEmail@something.com");
        Long id = service.create(newUser).getId();

        UserDto updateDto = new UserDto(null, TEST_USER.getEmail());
        assertThrows(DuplicateDataException.class, () -> service.update(id, updateDto));
    }

    @Test
    void update_shouldThrow_ifIdNotFound() {
        assertThrows(NotFoundException.class, () -> service.update(9999, new UserDto("", "")));
    }

    @Test
    void delete_shouldDeleteUser() {
        Long id = service.create(TEST_USER).getId();
        service.delete(id);

        Collection<ResponseUserDto> users = service.findAll();
        assertTrue(users.isEmpty());
    }

    @Test
    void delete_shouldThrow_ifIdNotFound() {
        assertThrows(NotFoundException.class, () -> service.delete(9999));
    }
}
