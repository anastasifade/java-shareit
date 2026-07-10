package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.ErrorHandler;
import ru.practicum.shareit.item.dto.item.ItemNameDto;
import ru.practicum.shareit.request.controller.RequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ResponseRequestDto;
import ru.practicum.shareit.request.service.RequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RequestController.class, ErrorHandler.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestControllerTests {

    private static final String REQUESTS_URL = "/requests";
    private static final String HEADER = "X-Sharer-User-Id";
    private static final String DESCRIPTION = "description";
    private static final LocalDateTime CREATED =
            LocalDateTime.of(2026, 7, 7, 12, 45);
    private static final ResponseRequestDto RESPONSE_DTO = ResponseRequestDto
            .builder()
            .id(1L)
            .description(DESCRIPTION)
            .created(CREATED)
            .items(List.of(ItemNameDto
                    .builder()
                    .id(1L)
                    .name("name")
                    .build()))
            .build();

    @MockBean
    private final RequestService requestService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @Test
    void testFindAll() throws Exception {
        when(requestService.findAll(anyLong()))
                .thenReturn(List.of(RESPONSE_DTO));

        mvc.perform(get(REQUESTS_URL + "/all")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id", is(1L), Long.class))
                .andExpect(jsonPath("$[0].created", is(CREATED.format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$[0].description", is(DESCRIPTION)));

        verify(requestService).findAll(1L);
    }

    @Test
    void testFindAllByUser() throws Exception {
        when(requestService.findAllByUser(anyLong()))
                .thenReturn(List.of(RESPONSE_DTO));

        mvc.perform(get(REQUESTS_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(requestService).findAllByUser(1L);
    }

    @Test
    void testFindById() throws Exception {
        when(requestService.findById(anyLong(), anyLong()))
                .thenReturn(RESPONSE_DTO);

        mvc.perform(get(REQUESTS_URL + "/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.items").isArray());

        verify(requestService).findById(1L, 1L);
    }

    @Test
    void testCreate() throws Exception {
        when(requestService.create(anyLong(), any()))
                .thenReturn(RESPONSE_DTO);

        ItemRequestDto dto = new ItemRequestDto(DESCRIPTION);

        mvc.perform(post(REQUESTS_URL)
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isCreated());

        verify(requestService).create(anyLong(), any());
    }
}
