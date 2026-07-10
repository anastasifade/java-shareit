package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.base.exceptions.*;
import ru.practicum.shareit.error.ErrorHandler;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.service.UserService;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(controllers = {UserController.class, ErrorHandler.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ErrorHandlerTests {

    private static final String URL = "/users";

    @MockBean
    private final UserService service;
    private final MockMvc mvc;

    @Test
    void test_CommentRequestException() throws Exception {
        when(service.findAll())
                .thenThrow(CommentRequestException.class);

        mvc.perform(get(URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_DuplicateDataException() throws Exception {
        when(service.findAll())
                .thenThrow(DuplicateDataException.class);

        mvc.perform(get(URL))
                .andExpect(status().isConflict());
    }

    @Test
    void test_ItemUnavailableException() throws Exception {
        when(service.findAll())
                .thenThrow(ItemUnavailableException.class);

        mvc.perform(get(URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_NotFoundException() throws Exception {
        when(service.findAll())
                .thenThrow(NotFoundException.class);

        mvc.perform(get(URL))
                .andExpect(status().isNotFound());
    }

    @Test
    void test_UserValidationException() throws Exception {
        when(service.findAll())
                .thenThrow(UserValidationException.class);

        mvc.perform(get(URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void test_unexpectedException() throws Exception {
        when(service.findAll())
                .thenThrow(RuntimeException.class);

        mvc.perform(get(URL))
                .andExpect(status().isInternalServerError());
    }
}
