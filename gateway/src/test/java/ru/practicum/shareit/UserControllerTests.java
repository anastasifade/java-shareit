package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.ErrorHandler;
import ru.practicum.shareit.user.NewUserDto;
import ru.practicum.shareit.user.UpdateUserDto;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
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
    private final UserClient client;
    private final ObjectMapper mapper;
    private final MockMvc mvc;


    @Test
    void testCreateUser_InvalidName() throws Exception {
        NewUserDto userDto = new NewUserDto(" ", EMAIL);

        mvc.perform(post(USERS_URL)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
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
                .andExpect(jsonPath("$.error", is("[Validation error: invalid email format.]")));

        NewUserDto nullEmail = new NewUserDto(NAME, null);
        mvc.perform(post(USERS_URL)
                        .content(mapper.writeValueAsString(nullEmail))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("[Validation error: email cannot be blank.]")));

        verifyNoInteractions(client);
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
                .andExpect(jsonPath("$.error", is("[Validation error: username cannot be blank.]")));

        verifyNoInteractions(client);
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
                .andExpect(jsonPath("$.error", is("[Validation error: invalid email format.]")));

        verifyNoInteractions(client);
    }

}
