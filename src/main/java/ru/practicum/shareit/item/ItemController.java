package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.NewItemDto;
import ru.practicum.shareit.item.dto.ResponseItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public final class ItemController {

    private final ItemService itemService;

    @GetMapping
    public Collection<ResponseItemDto> findByOwner(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("GET /items request received by ItemController. [X-Sharer-User-Id = {}]", userId);
        return itemService.findByOwner(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseItemDto findById(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PathVariable long itemId) {
        log.info("GET /items/{} request received by ItemController. [X-Sharer-User-Id = {}]", itemId, userId);
        return itemService.findById(userId, itemId);
    }

    @GetMapping("/search")
    public Collection<ResponseItemDto> search(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam String text) {
        log.info("GET /items/search?text={} request received by ItemController. [X-Sharer-User-Id = {}]", text, userId);
        return itemService.search(userId, text);
    }

    @PostMapping
    public ResponseItemDto create(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Valid @RequestBody NewItemDto dto) {
        log.info("POST /items request received by ItemController. [X-Sharer-User-Id = {}]", userId);
        return itemService.create(userId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseItemDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable long itemId,
                                  @Valid @RequestBody UpdateItemDto dto) {
        log.info("PATCH /items/{} request received by ItemController. [X-Sharer-User-Id = {}]", itemId, userId);
        return itemService.update(userId, itemId, dto);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-Sharer-User-Id") long userId,
                       @PathVariable long itemId) {
        log.info("DELETE /items/{} request received by ItemController. [X-Sharer-User-Id = {}]", itemId, userId);
        itemService.delete(userId, itemId);
    }

}
