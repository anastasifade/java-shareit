package ru.practicum.shareit.item;

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
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> findByOwner(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> search(long userId, String text) {
        Map<String, Object> params = Map.of(
                "text", text
        );
        return get("/search", userId, params);
    }

    public ResponseEntity<Object> findById(long userId, long itemId) {
        return get(String.format("/%d", itemId), userId);
    }

    public ResponseEntity<Object> create(long userId, NewItemDto dto) {
        return post("", userId, dto);
    }

    public ResponseEntity<Object> createComment(long userId, long itemId, CommentDto dto) {
        return post(String.format("/%d/comment", itemId), userId, dto);
    }

    public ResponseEntity<Object> update(long userId, long itemId, UpdateItemDto dto) {
        return patch(String.format("/%d", itemId), userId, dto);
    }

    public ResponseEntity<Object> deleteItem(long userId, long itemId) {
        return delete(String.format("/%d", itemId), userId);
    }
}
