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
import ru.practicum.shareit.request.RequestClient;
import ru.practicum.shareit.request.RequestController;
import ru.practicum.shareit.request.ItemRequestDto;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RequestController.class, ErrorHandler.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestControllerTests {

    private static final String REQUESTS_URL = "/requests";
    private static final String HEADER = "X-Sharer-User-Id";

    @MockBean
    private final RequestClient client;
    private final ObjectMapper mapper;
    private final MockMvc mvc;


    @Test
    void testCreate_invalidDescription() throws Exception {
        ItemRequestDto nullDescription = new ItemRequestDto(null);
        mvc.perform(post(REQUESTS_URL)
                        .content(mapper.writeValueAsString(nullDescription))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest());

        ItemRequestDto blankDescription = new ItemRequestDto("    ");
        mvc.perform(post(REQUESTS_URL)
                        .content(mapper.writeValueAsString(blankDescription))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }
}
