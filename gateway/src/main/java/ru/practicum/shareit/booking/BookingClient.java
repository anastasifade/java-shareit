package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> findAll(long userId, Role userRole, BookingSearch state) {
        Map<String, Object> parameters = Map.of(
                "state", state.name(),
                "role", userRole.name()
        );
        return get("?state={state}&role={role}", userId, parameters);
    }

    public ResponseEntity<Object> findById(long userId, long bookingId) {
        return get(String.format("/%d", bookingId), userId);
    }

    public ResponseEntity<Object> create(long userId, NewBookingDto dto) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> updateStatus(long userId, long bookingId, boolean approved) {
        Map<String, Object> parameters = Map.of(
                "approved", approved
        );
        return patch(String.format("/%d?approved={approved}", bookingId), userId, parameters, null);
    }
}
