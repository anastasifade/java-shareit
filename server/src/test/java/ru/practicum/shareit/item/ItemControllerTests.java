package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.ErrorHandler;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.comment.CommentDto;
import ru.practicum.shareit.item.dto.comment.ResponseCommentDto;
import ru.practicum.shareit.item.dto.item.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

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

@WebMvcTest(controllers = {ItemController.class, ErrorHandler.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTests {

    private static final String ITEMS_URL = "/items";
    private static final String HEADER = "X-Sharer-User-Id";
    private static final String NAME = "item";
    private static final String DESCRIPTION = "description";

    private static final ResponseBookingDto BOOKING_DTO = ResponseBookingDto.builder()
            .id(1L)
            .item(ItemNameDto.builder().id(1L).name(NAME).build())
            .start(LocalDateTime.of(2026, 1, 1, 10, 0))
            .end(LocalDateTime.of(2026, 1, 3, 10, 0))
            .status(BookingStatus.APPROVED)
            .build();

    private static final ResponseItemDto RESPONSE_ITEM_DTO = ResponseItemDto.builder()
            .id(1L)
            .name(NAME)
            .description(DESCRIPTION)
            .comments(List.of())
            .available(true)
            .build();

    private static final OwnerItemDto OWNER_RESPONSE_ITEM_DTO =
            new OwnerItemDto(1L, NAME, DESCRIPTION, true,
                    BOOKING_DTO, // lastBooking
                    BOOKING_DTO, // nextBooking
                    List.of());

    @MockBean
    private final ItemService itemService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;


    // GET

    @Test
    void testFindAllItems_byOwner() throws Exception {
        doReturn(List.of(OWNER_RESPONSE_ITEM_DTO))
                .when(itemService).findByOwner(anyLong());

        mvc.perform(get(ITEMS_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1L), Long.class))
                .andExpect(jsonPath("$[0].nextBooking").exists())
                .andExpect(jsonPath("$[0].lastBooking").exists());

        verify(itemService).findByOwner(1L);
    }

    @Test
    void testFindById() throws Exception {
        when(itemService.findById(anyLong(), anyLong()))
                .thenReturn(RESPONSE_ITEM_DTO);

        mvc.perform(get(ITEMS_URL + "/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class));

        verify(itemService).findById(1L, 1L);
    }

    @Test
    void testSearch() throws Exception {
        doReturn(List.of(RESPONSE_ITEM_DTO))
                .when(itemService).search(1L, NAME);

        doReturn(List.of())
                .when(itemService).search(1L, "");

        // test search with 1 result
        mvc.perform(get(ITEMS_URL + "/search")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("text", NAME)
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(NAME)));

        // test search with 0 results
        mvc.perform(get(ITEMS_URL + "/search")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("text", "")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(itemService, times(2)).search(anyLong(), anyString());
    }


    // POST

    @Test
    void testCreateItem() throws Exception {
        NewItemDto dto = new NewItemDto(NAME, DESCRIPTION, true, null);

        Item item = ItemMapper.toItem(dto, null);
        item.setId(1L);
        OwnerItemDto result = ItemMapper.toOwnerDto(item, null, null, List.of());

        when(itemService.create(1L, dto))
                .thenReturn(result);

        mvc.perform(post(ITEMS_URL)
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is(NAME)))
                .andExpect(jsonPath("$.description", is(DESCRIPTION)))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.comments").isEmpty())
                .andExpect(jsonPath("$.nextBooking").value(nullValue()))
                .andExpect(jsonPath("$.lastBooking").value(nullValue()));

        verify(itemService).create(anyLong(), any());
    }

    // POST comment

    @Test
    void testPostComment() throws Exception {
        CommentDto newComment = new CommentDto("comment_text");
        ResponseCommentDto result = ResponseCommentDto.builder()
                .id(1L)
                .text(newComment.getText())
                .created(LocalDateTime.of(2026, 1, 1, 1, 1))
                .authorName("John Doe")
                .build();

        when(itemService.createComment(anyLong(), anyLong(), any()))
                .thenReturn(result);

        mvc.perform(post(ITEMS_URL + "/1/comment")
                        .content(mapper.writeValueAsString(newComment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.text", is(result.getText())))
                .andExpect(jsonPath("$.created",
                        is(result.getCreated().format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.authorName", is(result.getAuthorName())));

        verify(itemService).createComment(anyLong(), anyLong(), any());
    }

    // PATCH

    @Test
    void testUpdateItem() throws Exception {
        UpdateItemDto dto = new UpdateItemDto(null, null, false);
        ResponseItemDto result = ResponseItemDto.builder()
                .id(1L)
                .name(dto.getName())
                .description(DESCRIPTION)
                .available(dto.getAvailable())
                .comments(List.of())
                .build();

        when(itemService.update(anyLong(), anyLong(), any()))
                .thenReturn(result);

        mvc.perform(patch(ITEMS_URL + "/1")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk());

        verify(itemService).update(anyLong(), anyLong(), any());
    }

    // DELETE

    @Test
    void testDeleteItem() throws Exception {
        mvc.perform(delete(ITEMS_URL + "/1")
                        .header(HEADER, 1L))
                .andExpect(status().isNoContent());

        verify(itemService).delete(1L, 1L);
    }

}
