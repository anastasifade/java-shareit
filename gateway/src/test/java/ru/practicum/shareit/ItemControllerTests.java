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
import ru.practicum.shareit.item.*;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
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


    @MockBean
    private final ItemClient client;
    private final ObjectMapper mapper;
    private final MockMvc mvc;


    @Test
    void testFindAllItems_byOwner() throws Exception {
        doReturn(null)
                .when(client).findByOwner(anyLong());

        mvc.perform(get(ITEMS_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk());

        verify(client).findByOwner(1L);
    }

    @Test
    void testFindById() throws Exception {
        when(client.findById(anyLong(), anyLong()))
                .thenReturn(null);

        mvc.perform(get(ITEMS_URL + "/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk());

        verify(client).findById(1L, 1L);
    }

    @Test
    void testSearch() throws Exception {
        doReturn(null)
                .when(client).search(anyLong(), anyString());

        mvc.perform(get(ITEMS_URL + "/search")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("text", NAME)
                        .header(HEADER, 1L))
                .andExpect(status().isOk());

        verify(client).search(anyLong(), anyString());
    }

    @Test
    void testCreateItem() throws Exception {
        NewItemDto dto = new NewItemDto(NAME, DESCRIPTION, true, null);


        when(client.create(1L, dto))
                .thenReturn(null);

        mvc.perform(post(ITEMS_URL)
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isCreated());

        verify(client).create(anyLong(), any());
    }

    @Test
    void testCreateItem_invalidName() throws Exception {
        NewItemDto blankName = new NewItemDto("  ", DESCRIPTION, true, null);
        mvc.perform(post(ITEMS_URL)
                        .content(mapper.writeValueAsString(blankName))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("[Validation error: item name cannot be blank.]")));

        NewItemDto nullName = new NewItemDto(null, DESCRIPTION, true, null);
        mvc.perform(post(ITEMS_URL)
                        .content(mapper.writeValueAsString(nullName))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("[Validation error: item name cannot be blank.]")));

        verifyNoInteractions(client);
    }

    @Test
    void testCreateItem_invalidDescription() throws Exception {
        NewItemDto blankDescription = new NewItemDto(NAME, "   ", true, null);
        mvc.perform(post(ITEMS_URL)
                        .content(mapper.writeValueAsString(blankDescription))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        is("[Validation error: item description cannot be blank.]")));

        NewItemDto nullDescription = new NewItemDto(NAME, null, true, null);
        mvc.perform(post(ITEMS_URL)
                        .content(mapper.writeValueAsString(nullDescription))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        is("[Validation error: item description cannot be blank.]")));

        verifyNoInteractions(client);
    }

    @Test
    void testCreateItem_invalidAvailability() throws Exception {
        NewItemDto dto = new NewItemDto(NAME, DESCRIPTION, null, null);

        mvc.perform(post(ITEMS_URL)
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        is("[Validation error: item availability status must be specified.]")));

        verifyNoInteractions(client);
    }

    @Test
    void testPostComment() throws Exception {
        CommentDto newComment = new CommentDto("comment_text");

        when(client.createComment(anyLong(), anyLong(), any()))
                .thenReturn(null);

        mvc.perform(post(ITEMS_URL + "/1/comment")
                        .content(mapper.writeValueAsString(newComment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isCreated());

        verify(client).createComment(anyLong(), anyLong(), any());
    }

    @Test
    void testPostComment_invalidText() throws Exception {
        CommentDto blankText = new CommentDto(" ");
        mvc.perform(post(ITEMS_URL + "/1/comment")
                        .content(mapper.writeValueAsString(blankText))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("[Comment text cannot be blank.]")));

        CommentDto nullText = new CommentDto(null);
        mvc.perform(post(ITEMS_URL + "/1/comment")
                        .content(mapper.writeValueAsString(nullText))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("[Comment text cannot be blank.]")));

        verifyNoInteractions(client);
    }

    @Test
    void testUpdateItem() throws Exception {
        UpdateItemDto dto = new UpdateItemDto(null, null, false);

        when(client.update(anyLong(), anyLong(), any()))
                .thenReturn(null);

        mvc.perform(patch(ITEMS_URL + "/1")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk());

        verify(client).update(anyLong(), anyLong(), any());
    }

    @Test
    void testUpdateItem_blankName() throws Exception {
        UpdateItemDto dto = new UpdateItemDto("  ", DESCRIPTION, false);

        mvc.perform(patch(ITEMS_URL + "/1")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("[Validation error: item name cannot be blank.]")));

        verifyNoInteractions(client);
    }

    @Test
    void testUpdateItem_blankDescription() throws Exception {
        UpdateItemDto dto = new UpdateItemDto(NAME, "  ", false);

        mvc.perform(patch(ITEMS_URL + "/1")
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error",
                        is("[Validation error: item description cannot be blank.]")));

        verifyNoInteractions(client);
    }

    @Test
    void testDeleteItem() throws Exception {
        doNothing()
                .when(client).deleteItem(anyLong(), anyLong());

        mvc.perform(delete(ITEMS_URL + "/1")
                        .header(HEADER, 1L))
                .andExpect(status().isNoContent());

        verify(client).deleteItem(1L, 1L);
    }

}
