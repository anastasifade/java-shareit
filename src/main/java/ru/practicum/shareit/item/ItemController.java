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
        return itemService.findByOwner(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseItemDto findById(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PathVariable long itemId) {
        return itemService.findById(userId, itemId);
    }

    @GetMapping("/search")
    public Collection<ResponseItemDto> search(@RequestHeader("X-Sharer-User-Id") long userId,
                                              @RequestParam String text) {
        return itemService.search(userId, text);
    }

    @PostMapping
    public ResponseItemDto create(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Valid @RequestBody NewItemDto dto) {
        return itemService.create(userId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseItemDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable long itemId,
                                  @Valid @RequestBody UpdateItemDto dto) {
        return itemService.update(userId, itemId, dto);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-Sharer-User-Id") long userId,
                       @PathVariable long itemId) {
        itemService.delete(userId, itemId);
    }

}
