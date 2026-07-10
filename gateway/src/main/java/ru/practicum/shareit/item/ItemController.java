package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public final class ItemController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private final ItemClient client;

    @GetMapping
    public ResponseEntity<Object> findByOwner(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("GET /items request received by ItemController. [X-Sharer-User-Id = {}]", userId);
        return client.findByOwner(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                    @PathVariable long itemId) {
        log.info("GET /items/{} request received by ItemController. [X-Sharer-User-Id = {}]", itemId, userId);
        return client.findById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(X_SHARER_USER_ID) long userId,
                                              @RequestParam String text) {
        log.info("GET /items/search?text={} request received by ItemController. [X-Sharer-User-Id = {}]", text, userId);
        return client.search(userId, text);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                  @Valid @RequestBody NewItemDto dto) {
        log.info("POST /items request received by ItemController. [X-Sharer-User-Id = {}]", userId);
        return client.create(userId, dto);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createComment(@RequestHeader(X_SHARER_USER_ID) long userId,
                                            @PathVariable("itemId") long itemId,
                                            @Valid @RequestBody CommentDto dto) {
        log.info("POST /items/{}/comment request received by ItemController. [X-Sharer-User-Id = {}]", itemId, userId);
        return client.createComment(userId, itemId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(X_SHARER_USER_ID) long userId,
                                  @PathVariable long itemId,
                                  @Valid @RequestBody UpdateItemDto dto) {
        log.info("PATCH /items/{} request received by ItemController. [X-Sharer-User-Id = {}]", itemId, userId);
        return client.update(userId, itemId, dto);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(X_SHARER_USER_ID) long userId,
                       @PathVariable long itemId) {
        log.info("DELETE /items/{} request received by ItemController. [X-Sharer-User-Id = {}]", itemId, userId);
        client.deleteItem(userId, itemId);
    }

}
