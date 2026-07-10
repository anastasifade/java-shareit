package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.base.exceptions.CommentRequestException;
import ru.practicum.shareit.base.exceptions.NotFoundException;
import ru.practicum.shareit.base.exceptions.UserValidationException;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.QBooking;
import ru.practicum.shareit.item.dal.CommentRepository;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.dto.comment.CommentDto;
import ru.practicum.shareit.item.dto.comment.ResponseCommentDto;
import ru.practicum.shareit.item.dto.item.OwnerItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.dto.item.NewItemDto;
import ru.practicum.shareit.item.dto.item.ResponseItemDto;
import ru.practicum.shareit.item.dto.item.UpdateItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private final UserService userService;

    @Override
    public Collection<? extends ResponseItemDto> findByOwner(long userId) {
        userService.validateUser(userId);
        log.debug("Request for items [owner id={}] received by ItemService.", userId);

        Collection<Item> items = itemRepository.findByOwnerId(userId);
        return manyToOwnerDto(items);
    }

    @Override
    public Collection<ResponseItemDto> search(long userId, String text) {
        userService.validateUser(userId);
        log.debug("Search request for items [search text = '{}'] received by ItemService.", text);

        if (text.isBlank()) {
            return List.of();
        }

        Collection<Item> items = itemRepository.search(text);
        Map<Item, List<Comment>> commentsByItem = getCommentsForItems(items);

        return  items.stream()
                .map(item -> item.getOwner().getId() == userId ?
                        oneToOwnerDto(item, commentsByItem.getOrDefault(item, List.of())) :
                        ItemMapper.toDto(item, commentsByItem.getOrDefault(item, List.of())))
                .toList();
    }

    @Override
    public ResponseItemDto findById(long userId, long itemId) {
        userService.validateUser(userId);
        log.debug("Request for item [id={}] received by ItemService.", itemId);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item with id=[%s] not found.", itemId)));

        /* в связи с расхождениями требований в ТЗ
         * (просмотр последнего и следующего бронирования доступен только для владельца)
         *
         * и требований в Postman-тестах
         * (в ответе на запрос GET/items/{id} ожидается наличие полей последнего и следующего броинрования
         * вне зависимости от статуса пользователя, ввыполнившего запрос)
         *
         * - метод возвращает объект OwnerItemDto, при этом поля lastBooking и nextBooking оставются пустыми,
         *   если пользователь не является владельцем вещи
        */

        return item.getOwner().getId() == userId ?
                oneToOwnerDto(item, getCommentsForItem(item)) :
                ItemMapper.toOwnerDto(item, null, null, getCommentsForItem(item));
    }

    @Override
    public Item getItem(long id) {
        return itemRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Item with id=[%s] not found.", id)));
    }

    @Transactional
    @Override
    public ResponseItemDto create(long userId, NewItemDto dto) {
        User owner = userService.getUser(userId);
        log.debug("Create request for an item, owner [id={}] received by ItemService.", userId);
        log.trace("Creating item: {}.", dto);
        Item item = ItemMapper.toItem(dto, owner);
        return ItemMapper.toDto(itemRepository.save(item), List.of());
    }

    @Transactional
    @Override
    public ResponseCommentDto createComment(long userId, long itemId, CommentDto dto) {
        User user = userService.getUser(userId);
        Item item = getItem(itemId);

        log.debug("Create request for a comment, author [id={}], item [id={}] received by ItemService.", userId, itemId);
        log.trace("Creating comment: {}.", dto);

        boolean validCommentRequest = bookingRepository
                .exists(QBooking.booking.item.id.eq(itemId)
                .and(QBooking.booking.booker.id.eq(userId))
                .and(QBooking.booking.start.before(LocalDateTime.now())));

        if (!validCommentRequest) {
            log.warn("User [id={}] not authorized to leave a comment on item [id={}].", userId, itemId);
            throw new CommentRequestException("Only users who have previously borrowed the item may leave a comment.");
        }

        Comment comment = commentRepository.save(CommentMapper.toComment(dto, user, item));
        return CommentMapper.toDto(comment);
    }

    @Transactional
    @Override
    public ResponseItemDto update(long userId, long itemId, UpdateItemDto dto) {
        userService.validateUser(userId);
        log.debug("Request to update item [id={}] by user [id={}] received by ItemService.", itemId, userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item with id=[%s] not found.", itemId)));

        if (item.getOwner().getId() != userId) {
            log.debug("Update failed: user [id={}] is not the owner of the item [id={}].", userId, itemId);
            throw new UserValidationException("Only item owner can edit item information.");
        }

        log.trace("Updating item: {}.", dto);
        item = ItemMapper.toItem(dto, item);
        return oneToOwnerDto(itemRepository.save(item), getCommentsForItem(item));
    }

    @Transactional
    @Override
    public void delete(long userId, long itemId) {
        userService.validateUser(userId);
        log.debug("Delete request for item [id={}] by user [id={}] received by ItemService.", itemId, userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item with id=[%s] not found.", itemId)));
        if (item.getOwner().getId() != userId) {
            log.debug("Item not deleted: user [id={}] not the item's owner.", userId);
            throw new UserValidationException("Only item's owner can remove the item.");
        }
        itemRepository.deleteById(itemId);
    }

    private Collection<OwnerItemDto> manyToOwnerDto(Collection<Item> items) {
        Map<Item, Booking> nextBookings = getNextBookings(items);
        Map<Item, Booking> lastBookings = getLastBookings(items);
        Map<Item, List<Comment>> commentsByItem = getCommentsForItems(items);

        return items.stream()
                .map(item -> ItemMapper.toOwnerDto(item,
                        nextBookings.getOrDefault(item, null),
                        lastBookings.getOrDefault(item, null),
                        commentsByItem.getOrDefault(item, List.of())))
                .toList();
    }

    private OwnerItemDto oneToOwnerDto(Item item, List<Comment> comments) {
        return ItemMapper.toOwnerDto(item,
                bookingRepository.findNextBooking(item).orElse(null),
                bookingRepository.findLastBooking(item).orElse(null),
                comments);
    }

    private Map<Item, Booking> getNextBookings(Collection<Item> items) {
        return bookingRepository.findFutureBookingsForItems(items)
                .stream()
                .collect(Collectors.toMap(Booking::getItem, Function.identity(),
                        (b1, b2) -> b1.getStart().isBefore(b2.getStart()) ? b1 : b2));
    }

    private Map<Item, Booking> getLastBookings(Collection<Item> items) {
        return bookingRepository.findPastBookingsForItems(items)
                .stream()
                .collect(Collectors.toMap(Booking::getItem, Function.identity(),
                        (b1, b2) -> b1.getEnd().isAfter(b2.getEnd()) ? b1 : b2));
    }

    private Map<Item, List<Comment>> getCommentsForItems(Collection<Item> items) {
        return commentRepository.findAllByItemIn(items)
                .stream()
                .collect(Collectors.groupingBy(Comment::getItem));
    }

    private List<Comment> getCommentsForItem(Item item) {
        return commentRepository.findAllByItem(item);
    }
}
