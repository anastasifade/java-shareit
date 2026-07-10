package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemClientTests {

    private static final String SERVER_URL = "http://localhost:8080";

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private ItemClient itemClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        itemClient = new ItemClient(SERVER_URL, restTemplateBuilder);
    }

    @Test
    void findByOwner_ShouldSendGetRequestWithEmptyPathAndUserIdHeader() {
        long userId = 1L;
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("owner-items");

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = itemClient.findByOwner(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("owner-items");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(""), eq(HttpMethod.GET), entityCaptor.capture(), eq(Object.class));
        assertThat(entityCaptor.getValue().getHeaders().get("X-Sharer-User-Id"))
                .containsExactly(String.valueOf(userId));
    }

    @Test
    void findByOwner_ShouldHandleHttpStatusCodeException() {
        long userId = 1L;
        byte[] errorBody = "Server error".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = itemClient.findByOwner(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void search_ShouldSendGetRequestWithTextParameterAndUserIdHeader() {
        long userId = 2L;
        String searchText = "hammer";
        Map<String, Object> expectedParams = Map.of("text", searchText);
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("search-results");

        when(restTemplate.exchange(
                eq("/search"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(expectedParams)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = itemClient.search(userId, searchText);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("search-results");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/search"), eq(HttpMethod.GET), entityCaptor.capture(),
                eq(Object.class), eq(expectedParams));
        assertThat(entityCaptor.getValue().getHeaders().get("X-Sharer-User-Id"))
                .containsExactly(String.valueOf(userId));
    }

    @Test
    void search_ShouldHandleHttpStatusCodeException() {
        long userId = 2L;
        String searchText = "invalid";
        Map<String, Object> params = Map.of("text", searchText);
        byte[] errorBody = "Bad request".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq("/search"), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(Object.class), eq(params)))
                .thenThrow(exception);

        ResponseEntity<Object> response = itemClient.search(userId, searchText);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void findById_ShouldSendGetRequestWithItemIdPathAndUserIdHeader() {
        long userId = 3L;
        long itemId = 42L;
        String expectedPath = "/" + itemId;
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("single-item");

        when(restTemplate.exchange(
                eq(expectedPath),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = itemClient.findById(userId, itemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("single-item");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(expectedPath), eq(HttpMethod.GET), entityCaptor.capture(), eq(Object.class));
        assertThat(entityCaptor.getValue().getHeaders().get("X-Sharer-User-Id"))
                .containsExactly(String.valueOf(userId));
    }

    @Test
    void findById_ShouldHandleHttpStatusCodeException() {
        long userId = 3L;
        long itemId = 99L;
        String path = "/" + itemId;
        byte[] errorBody = "Not found".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq(path), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = itemClient.findById(userId, itemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void create_ShouldSendPostRequestWithDtoBodyAndUserIdHeader() {
        long userId = 4L;
        NewItemDto newItemDto = new NewItemDto("a", "b", true, 1L);
        ResponseEntity<Object> serverResponse = ResponseEntity.status(HttpStatus.CREATED).body("created-item");

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = itemClient.create(userId, newItemDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("created-item");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(""), eq(HttpMethod.POST), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isSameAs(newItemDto);
        assertThat(entity.getHeaders().get("X-Sharer-User-Id"))
                .containsExactly(String.valueOf(userId));
    }

    @Test
    void create_ShouldHandleHttpStatusCodeException() {
        long userId = 4L;
        NewItemDto dto = new NewItemDto("a", "b", true, null);
        byte[] errorBody = "Conflict".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.CONFLICT);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = itemClient.create(userId, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void createComment_ShouldSendPostRequestToItemCommentPathWithDtoAndUserId() {
        long userId = 5L;
        long itemId = 10L;
        CommentDto commentDto = new CommentDto("comment_text");
        String expectedPath = "/" + itemId + "/comment";
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("comment-created");

        when(restTemplate.exchange(
                eq(expectedPath),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = itemClient.createComment(userId, itemId, commentDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("comment-created");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(expectedPath), eq(HttpMethod.POST), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isSameAs(commentDto);
        assertThat(entity.getHeaders().get("X-Sharer-User-Id"))
                .containsExactly(String.valueOf(userId));
    }

    @Test
    void createComment_ShouldHandleHttpStatusCodeException() {
        long userId = 5L;
        long itemId = 10L;
        CommentDto dto = new CommentDto("comment text");
        String path = "/" + itemId + "/comment";
        byte[] errorBody = "Bad request".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq(path), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = itemClient.createComment(userId, itemId, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void update_ShouldSendPatchRequestWithDtoBodyAndUserIdHeader() {
        long userId = 6L;
        long itemId = 77L;
        UpdateItemDto updateDto = new UpdateItemDto("new_name", null, null);
        String expectedPath = "/" + itemId;
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("updated-item");

        when(restTemplate.exchange(
                eq(expectedPath),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = itemClient.update(userId, itemId, updateDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("updated-item");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(expectedPath), eq(HttpMethod.PATCH), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isSameAs(updateDto);
        assertThat(entity.getHeaders().get("X-Sharer-User-Id"))
                .containsExactly(String.valueOf(userId));
    }

    @Test
    void update_ShouldHandleHttpStatusCodeException() {
        long userId = 6L;
        long itemId = 77L;
        UpdateItemDto dto = new UpdateItemDto(null, "new_desc", true);
        String path = "/" + itemId;
        byte[] errorBody = "Forbidden".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.FORBIDDEN);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq(path), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = itemClient.update(userId, itemId, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void deleteItem_ShouldSendDeleteRequestToItemPathAndUserIdHeader() {
        long userId = 7L;
        long itemId = 33L;
        String expectedPath = "/" + itemId;
        ResponseEntity<Object> serverResponse = ResponseEntity.noContent().build();

        when(restTemplate.exchange(
                eq(expectedPath),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        itemClient.deleteItem(userId, itemId);

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(expectedPath), eq(HttpMethod.DELETE), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isNull();
        assertThat(entity.getHeaders().get("X-Sharer-User-Id"))
                .containsExactly(String.valueOf(userId));
    }

    @Test
    void deleteItem_ShouldHandleHttpStatusCodeExceptionWithoutThrowing() {
        long userId = 7L;
        long itemId = 33L;
        String path = "/" + itemId;
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn("not found".getBytes());

        when(restTemplate.exchange(eq(path), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        itemClient.deleteItem(userId, itemId);

        verify(restTemplate).exchange(eq(path), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class));
    }

}
