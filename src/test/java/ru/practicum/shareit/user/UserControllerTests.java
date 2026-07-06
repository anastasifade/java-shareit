package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.ErrorHandler;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, ErrorHandler.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserControllerTests {

    private static final String USERS_URL = "/users";
    private static final String NAME = "name";
    private static final String EMAIL = "email@email.com";

    @MockBean
    private final UserService userService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;


    // GET

    @Test
    void testFindAll() throws Exception {
        ResponseUserDto dto = ResponseUserDto.builder()
                .id(1L)
                .name(NAME)
                .email(EMAIL)
                .build();

        when(userService.findAll())
                .thenReturn(List.of(dto));

        mvc.perform(get(USERS_URL)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(NAME)))
                .andExpect(jsonPath("$[0].email", is(EMAIL)));

        verify(userService).findAll();
    }

    @Test
    void testFindById() throws Exception {
        ResponseUserDto dto = ResponseUserDto.builder()
                .id(1L)
                .name(NAME)
                .email(EMAIL)
                .build();

        when(userService.findById(anyLong()))
                .thenReturn(dto);

        mvc.perform(get(USERS_URL + "/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(dto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(NAME)))
                .andExpect(jsonPath("$.email", is(EMAIL)));

        verify(userService).findById(dto.getId());
    }


    // POST

    @Test
    void testCreateValidUser() throws Exception {
        NewUserDto newUserDto = new NewUserDto(NAME, EMAIL);

        User user = UserMapper.toUser(newUserDto);
        user.setId(1L);
        ResponseUserDto responseUserDto = UserMapper.toDto(user);

        when(userService.create(any()))
                .thenReturn(responseUserDto);

        mvc.perform(post(USERS_URL)
                        .content(mapper.writeValueAsString(newUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(NAME)))
                .andExpect(jsonPath("$.email", is(EMAIL)));

        verify(userService).create(newUserDto);
    }

    @Test
    void testCreateUser_InvalidName() throws Exception {
        NewUserDto newUserDto = new NewUserDto(" ", EMAIL);

        mvc.perform(post(USERS_URL)
                        .content(mapper.writeValueAsString(newUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void testCreateUser_InvalidEmail() throws Exception {
        NewUserDto invalidEmailFormat = new NewUserDto(NAME, "not_an_email");
        mvc.perform(post(USERS_URL)
                        .content(mapper.writeValueAsString(invalidEmailFormat))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("[Validation error: invalid email format.]")));

        NewUserDto nullEmail = new NewUserDto(NAME, null);
        mvc.perform(post(USERS_URL)
                        .content(mapper.writeValueAsString(nullEmail))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("[Validation error: email cannot be blank.]")));

        verifyNoInteractions(userService);
    }


    // PATCH

    @Test
    void testPatchUser_validDto() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName(NAME);
        user.setEmail(EMAIL);

        String newEmail = "new@email.com";
        UpdateUserDto dto = new UpdateUserDto(null, newEmail);

        when(userService.update(1, dto))
                .thenReturn(UserMapper.toDto(UserMapper.toUser(dto, user)));

        mvc.perform(patch(USERS_URL + "/1")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(NAME)))
                .andExpect(jsonPath("$.email", is(newEmail)));

        verify(userService).update(user.getId(), dto);
    }

    @Test
    void testPatchUser_invalidName() throws Exception {
        UpdateUserDto dto = new UpdateUserDto(" ", EMAIL);

        mvc.perform(patch(USERS_URL + "/1")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("[Validation error: username cannot be blank.]")));

        verifyNoInteractions(userService);
    }

    @Test
    void testPatchUser_invalidEmail() throws Exception {
        UpdateUserDto dto = new UpdateUserDto(NAME, "not an email");

        mvc.perform(patch(USERS_URL + "/1")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("[Validation error: invalid email format.]")));

        verifyNoInteractions(userService);
    }


    // DELETE

    @Test
    void testDeleteUser() throws Exception {
        mvc.perform(delete(USERS_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

}
