package ru.practicum.shareit.request;

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

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestClientTests {

    private static final String SERVER_URL = "http://localhost:8080";

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private RequestClient requestClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        requestClient = new RequestClient(SERVER_URL, restTemplateBuilder);
    }

    @Test
    void findAllByUser_ShouldSendGetRequestWithEmptyPathAndUserIdHeader() {
        long userId = 1L;
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("user-requests");

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = requestClient.findAllByUser(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("user-requests");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(""), eq(HttpMethod.GET), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getHeaders().get("X-Sharer-User-Id"))
                .containsExactly(String.valueOf(userId));
    }

    @Test
    void findAllByUser_ShouldHandleHttpStatusCodeException() {
        long userId = 1L;
        byte[] errorBody = "Server error".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = requestClient.findAllByUser(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void findAll_ShouldSendGetRequestToAllPathWithUserIdHeader() {
        long userId = 2L;
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("all-requests");

        when(restTemplate.exchange(
                eq("/all"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = requestClient.findAll(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("all-requests");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("/all"), eq(HttpMethod.GET), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getHeaders().get("X-Sharer-User-Id"))
                .containsExactly(String.valueOf(userId));
    }

    @Test
    void findAll_ShouldHandleHttpStatusCodeException() {
        long userId = 2L;
        byte[] errorBody = "Not found".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq("/all"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = requestClient.findAll(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void findById_ShouldSendGetRequestWithRequestIdPathAndUserIdHeader() {
        long userId = 3L;
        long requestId = 10L;
        String path = "/" + requestId;
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("single-request");

        when(restTemplate.exchange(
                eq(path),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = requestClient.findById(userId, requestId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("single-request");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.GET), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getHeaders().get("X-Sharer-User-Id"))
                .containsExactly(String.valueOf(userId));
    }

    @Test
    void findById_ShouldHandleHttpStatusCodeException() {
        long userId = 3L;
        long requestId = 99L;
        String path = "/" + requestId;
        byte[] errorBody = "Not found".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq(path), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = requestClient.findById(userId, requestId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void create_ShouldSendPostRequestWithDtoBodyAndUserIdHeader() {
        long userId = 4L;
        ItemRequestDto dto = new ItemRequestDto("request for item: [item]");
        ResponseEntity<Object> serverResponse = ResponseEntity.status(HttpStatus.CREATED).body("created-request");

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = requestClient.create(userId, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("created-request");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(""), eq(HttpMethod.POST), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isSameAs(dto);
        assertThat(entity.getHeaders().get("X-Sharer-User-Id"))
                .containsExactly(String.valueOf(userId));
    }

    @Test
    void create_ShouldHandleHttpStatusCodeException() {
        long userId = 4L;
        ItemRequestDto dto = new ItemRequestDto("abcdefg...");
        byte[] errorBody = "Bad request".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = requestClient.create(userId, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }
}