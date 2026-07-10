package ru.practicum.shareit.booking;

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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingClientTests {

    private static final String SERVER_URL = "http://localhost:8080";
    private static final LocalDateTime FUTURE_START_DATE =
            LocalDateTime.of(2500, 1, 1, 0, 0);
    private static final LocalDateTime FUTURE_END_DATE = FUTURE_START_DATE.plusDays(7);

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private BookingClient bookingClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        bookingClient = new BookingClient(SERVER_URL, restTemplateBuilder);
    }

    @Test
    void findAll_ShouldSendGetRequestWithParametersAndUserIdHeader() {
        long userId = 1L;
        Role role = Role.BOOKER;
        BookingSearch state = BookingSearch.ALL;
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("result");

        when(restTemplate.exchange(
                eq("?state={state}&role={role}"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("state", "ALL", "role", "BOOKER"))
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = bookingClient.findAll(userId, role, state);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("result");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(), eq(HttpMethod.GET), entityCaptor.capture(), eq(Object.class), anyMap());
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getHeaders().get("X-Sharer-User-Id")).containsExactly(String.valueOf(userId));
    }

    @Test
    void findAll_ShouldHandleHttpStatusCodeException() {
        long userId = 1L;
        byte[] errorBody = "Not found".getBytes(StandardCharsets.UTF_8);
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(exception.getResponseBodyAsByteArray()).thenReturn(errorBody);

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), anyMap()
        )).thenThrow(exception);

        ResponseEntity<Object> response = bookingClient.findAll(userId, Role.BOOKER, BookingSearch.ALL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(errorBody);
    }

    @Test
    void findById_ShouldSendGetRequestWithCorrectPathAndUserId() {
        long userId = 2L;
        long bookingId = 10L;
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("booking");

        when(restTemplate.exchange(
                eq("/" + bookingId),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = bookingClient.findById(userId, bookingId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("booking");
    }

    @Test
    void findById_ShouldHandleHttpStatusCodeException() {
        long userId = 2L;
        long bookingId = 99L;
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(exception.getResponseBodyAsByteArray()).thenReturn("error".getBytes());

        when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)
        )).thenThrow(exception);

        ResponseEntity<Object> response = bookingClient.findById(userId, bookingId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(new String((byte[]) response.getBody())).isEqualTo("error");
    }

    @Test
    void create_ShouldSendPostRequestWithBodyAndUserId() {
        long userId = 3L;
        NewBookingDto dto = new NewBookingDto(1L, FUTURE_START_DATE, FUTURE_END_DATE);
        ResponseEntity<Object> serverResponse = ResponseEntity.status(HttpStatus.CREATED).body("created");

        when(restTemplate.exchange(
                eq(""),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Object.class)
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = bookingClient.create(userId, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("created");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), entityCaptor.capture(), eq(Object.class));
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isSameAs(dto);
        assertThat(entity.getHeaders().get("X-Sharer-User-Id")).containsExactly(String.valueOf(userId));
    }

    @Test
    void create_ShouldHandleHttpStatusCodeException() {
        long userId = 3L;
        NewBookingDto dto = new NewBookingDto(1L, FUTURE_START_DATE, FUTURE_END_DATE);
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsByteArray()).thenReturn("bad".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = bookingClient.create(userId, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("bad".getBytes());
    }


    @Test
    void updateStatus_ShouldSendPatchRequestWithParametersAndUserId() {
        long userId = 4L;
        long bookingId = 20L;
        boolean approved = true;
        ResponseEntity<Object> serverResponse = ResponseEntity.ok("updated");

        when(restTemplate.exchange(
                eq("/" + bookingId + "?approved={approved}"),
                eq(HttpMethod.PATCH),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("approved", approved))
        )).thenReturn(serverResponse);

        ResponseEntity<Object> response = bookingClient.updateStatus(userId, bookingId, approved);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("updated");

        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.PATCH), entityCaptor.capture(), eq(Object.class), anyMap());
        HttpEntity<?> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isNull();
        assertThat(entity.getHeaders().get("X-Sharer-User-Id")).containsExactly(String.valueOf(userId));
    }

    @Test
    void updateStatus_ShouldHandleHttpStatusCodeException() {
        long userId = 4L;
        long bookingId = 20L;
        boolean approved = false;
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.CONFLICT);
        when(exception.getResponseBodyAsByteArray()).thenReturn("conflict".getBytes());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenThrow(exception);

        ResponseEntity<Object> response = bookingClient.updateStatus(userId, bookingId, approved);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("conflict".getBytes());
    }
}