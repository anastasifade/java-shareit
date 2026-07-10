package ru.practicum.shareit.user;

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
class UserClientTests {

    private static final String SERVER_URL = "http://localhost:8080";

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private UserClient userClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        userClient = new UserClient(SERVER_URL, restTemplateBuilder);
    }

    @Test
    void findAll_ShouldSendGetRequestWithEmptyPathAndNoUserIdHeader() {
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("all-users");

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = userClient.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("all-users");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(""), eq(HttpMethod.GET), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getHeaders().get("X-Sharer-User-Id")).isNull();
    }

    @Test
    void findAll_ShouldHandleHttpStatusCodeException() {
        byte[] errorBody = "Service unavailable".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.SERVICE_UNAVAILABLE);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = userClient.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void findById_ShouldSendGetRequestWithIdPathAndNoUserIdHeader() {
        long userId = 1L;
        String path = "/" + userId;
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("user");

        when(restTemplate.exchange(
                eq(path),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = userClient.findById(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("user");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.GET), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getHeaders().get("X-Sharer-User-Id")).isNull();
    }

    @Test
    void findById_ShouldHandleHttpStatusCodeException() {
        long userId = 99L;
        String path = "/" + userId;
        byte[] errorBody = "Not found".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq(path), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = userClient.findById(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void create_ShouldSendPostRequestWithDtoBodyAndNoUserIdHeader() {
        NewUserDto newUser = new NewUserDto("User", "user@address.ru");
        ResponseEntity<Object> serverResponse = ResponseEntity.status(HttpStatus.CREATED).body("created-user");

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = userClient.create(newUser);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("created-user");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(""), eq(HttpMethod.POST), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isSameAs(newUser);
        assertThat(entity.getHeaders().get("X-Sharer-User-Id")).isNull();
    }

    @Test
    void create_ShouldHandleHttpStatusCodeException() {
        NewUserDto dto = new NewUserDto("random_name", "random@email.com");
        byte[] errorBody = "Conflict".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.CONFLICT);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq(""), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = userClient.create(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void update_ShouldSendPatchRequestWithDtoBodyAndNoUserIdHeader() {
        long userId = 1L;
        String path = "/" + userId;
        UpdateUserDto updateDto = new UpdateUserDto(null, "updated@email.address");
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("updated-user");

        when(restTemplate.exchange(
                eq(path),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = userClient.update(userId, updateDto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("updated-user");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.PATCH), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isSameAs(updateDto);
        assertThat(entity.getHeaders().get("X-Sharer-User-Id")).isNull();
    }

    @Test
    void update_ShouldHandleHttpStatusCodeException() {
        long userId = 1L;
        String path = "/" + userId;
        UpdateUserDto dto = new UpdateUserDto("new name", null);
        byte[] errorBody = "Bad request".getBytes();
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(eq(path), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = userClient.update(userId, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void deleteUser_ShouldSendDeleteRequestWithIdPathAndNoUserIdHeader() {
        long userId = 5L;
        String path = "/" + userId;
        ResponseEntity<Object> serverResponse = ResponseEntity.noContent().build();

        when(restTemplate.exchange(
                eq(path),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        userClient.deleteUser(userId);

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(path), eq(HttpMethod.DELETE), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isNull();
        assertThat(entity.getHeaders().get("X-Sharer-User-Id")).isNull();
    }

    @Test
    void deleteUser_ShouldHandleHttpStatusCodeExceptionWithoutThrowing() {
        long userId = 5L;
        String path = "/" + userId;
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn("not found".getBytes());

        when(restTemplate.exchange(eq(path), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        userClient.deleteUser(userId);

        verify(restTemplate).exchange(eq(path), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Object.class));
    }
}