package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.ErrorHandler;
import ru.practicum.shareit.error.InvalidQueryParameterException;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, ErrorHandler.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ErrorHandlerTests {

    private static final String URL = "/users";

    @MockBean
    private final UserClient service;
    private final MockMvc mvc;

    @Test
    void test_invalidQuery_exception() throws Exception {
        when(service.findAll())
                .thenThrow(InvalidQueryParameterException.class);

        mvc.perform(get(URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_internalServerError() throws Exception {
        when(service.findAll())
                .thenThrow(RuntimeException.class);

        mvc.perform(get(URL))
                .andExpect(status().isInternalServerError());
    }

}
