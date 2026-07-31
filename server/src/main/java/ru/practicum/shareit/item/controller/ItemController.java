package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.comment.CommentDto;
import ru.practicum.shareit.item.dto.comment.ResponseCommentDto;
import ru.practicum.shareit.item.dto.item.NewItemDto;
import ru.practicum.shareit.item.dto.item.ResponseItemDto;
import ru.practicum.shareit.item.dto.item.UpdateItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public final class ItemController {

    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    private final ItemService itemService;

    @GetMapping
    public Collection<? extends ResponseItemDto> findByOwner(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("GET /items request received by ItemController. [X-Sharer-User-Id = {}]", userId);
        return itemService.findByOwner(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseItemDto findById(@RequestHeader(X_SHARER_USER_ID) long userId,
                                    @PathVariable long itemId) {
        log.info("GET /items/{} request received by ItemController. [X-Sharer-User-Id = {}]", itemId, userId);
        return itemService.findById(userId, itemId);
    }

    @GetMapping("/search")
    public Collection<ResponseItemDto> search(@RequestHeader(X_SHARER_USER_ID) long userId,
                                              @RequestParam String text) {
        log.info("GET /items/search?text={} request received by ItemController. [X-Sharer-User-Id = {}]", text, userId);
        return itemService.search(userId, text);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseItemDto create(@RequestHeader(X_SHARER_USER_ID) long userId,
                                  @RequestBody NewItemDto dto) {
        log.info("POST /items request received by ItemController. [X-Sharer-User-Id = {}]", userId);
        return itemService.create(userId, dto);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseCommentDto createComment(@RequestHeader(X_SHARER_USER_ID) long userId,
                                            @PathVariable("itemId") long itemId,
                                            @RequestBody CommentDto dto) {
        log.info("POST /items/{}/comment request received by ItemController. [X-Sharer-User-Id = {}]", itemId, userId);
        return itemService.createComment(userId, itemId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseItemDto update(@RequestHeader(X_SHARER_USER_ID) long userId,
                                  @PathVariable long itemId,
                                  @RequestBody UpdateItemDto dto) {
        log.info("PATCH /items/{} request received by ItemController. [X-Sharer-User-Id = {}]", itemId, userId);
        return itemService.update(userId, itemId, dto);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader(X_SHARER_USER_ID) long userId,
                       @PathVariable long itemId) {
        log.info("DELETE /items/{} request received by ItemController. [X-Sharer-User-Id = {}]", itemId, userId);
        itemService.delete(userId, itemId);
    }

}
