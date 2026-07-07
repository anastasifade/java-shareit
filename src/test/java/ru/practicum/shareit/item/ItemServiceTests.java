package ru.practicum.shareit.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.base.exceptions.CommentRequestException;
import ru.practicum.shareit.base.exceptions.NotFoundException;
import ru.practicum.shareit.base.exceptions.UserValidationException;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.comment.CommentDto;
import ru.practicum.shareit.item.dto.item.NewItemDto;
import ru.practicum.shareit.item.dto.item.OwnerItemDto;
import ru.practicum.shareit.item.dto.item.ResponseItemDto;
import ru.practicum.shareit.item.dto.item.UpdateItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceTests {

    private static final NewItemDto TEST_ITEM =
            new NewItemDto("name", "desc", true, null);
    private static final CommentDto TEST_COMMENT = new CommentDto("comment_text");

    private final ItemService itemService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    private final EntityManager em;

    private User itemViewer;
    private User itemOwner;

    @BeforeEach
    void setUp() {
        itemViewer = new User();
        itemViewer.setName("name");
        itemViewer.setEmail("email@email.com");
        userRepository.save(itemViewer);

        itemOwner = new User();
        itemOwner.setName("item_owner_name");
        itemOwner.setEmail("items@gmail.com");
        userRepository.save(itemOwner);
    }

    @Test
    void findByOwner_returnsEmptyList_whenUserHasNoItems() {
        Collection<? extends ResponseItemDto> result = itemService.findByOwner(itemViewer.getId());
        assertTrue(result.isEmpty());

        itemService.create(itemOwner.getId(), TEST_ITEM);

        result = itemService.findByOwner(itemViewer.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    void findByOwner_returnsAllItems_ownedByUser() {
        itemService.create(itemOwner.getId(), TEST_ITEM);

        Collection<? extends ResponseItemDto> result = itemService.findByOwner(itemOwner.getId());
        assertThat(result, hasSize(1));

        itemService.create(itemViewer.getId(), TEST_ITEM);

        result = itemService.findByOwner(itemOwner.getId());
        assertThat(result, hasSize(1));
        assertTrue(result.stream().findFirst().get() instanceof OwnerItemDto);
    }

    @Test
    void findByOwner_shouldThrowIfUserNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.findByOwner(9999));
    }

    @Test
    void search_shouldReturnEmptyList_forEmptyTextParam() {
        itemService.create(itemOwner.getId(), TEST_ITEM);
        Collection<? extends ResponseItemDto> result = itemService.search(itemOwner.getId(), " ");

        assertTrue(result.isEmpty());
    }

    @Test
    void search_shouldReturnItems_whereName_containsText() {
        itemService.create(itemOwner.getId(), TEST_ITEM);
        itemService.create(itemOwner.getId(), new NewItemDto("a", "b", true, null));

        Collection<? extends ResponseItemDto> result = itemService.search(itemOwner.getId(), TEST_ITEM.getName());

        assertThat(result, hasSize(1));
        assertTrue(result.stream().findFirst().get().getName().contains(TEST_ITEM.getName()));
    }

    @Test
    void search_shouldReturnItems_whereDescription_containsText() {
        itemService.create(itemOwner.getId(), TEST_ITEM);
        itemService.create(itemOwner.getId(), new NewItemDto("a", "b", true, null));

        Collection<? extends ResponseItemDto> result =
                itemService.search(itemOwner.getId(), TEST_ITEM.getDescription());

        assertThat(result, hasSize(1));
        assertTrue(result.stream().findFirst().get().getDescription().contains(TEST_ITEM.getDescription()));
    }

    @Test
    void search_shouldReturnOwnerItemDto_ifUserIsOwner() {
        itemService.create(itemOwner.getId(), TEST_ITEM);

        Collection<? extends ResponseItemDto> result = itemService.search(itemOwner.getId(), TEST_ITEM.getName());
        assertTrue(result.stream().findFirst().get() instanceof OwnerItemDto);

        result = itemService.search(itemViewer.getId(), TEST_ITEM.getName());
        assertFalse(result.stream().findFirst().get() instanceof OwnerItemDto);
    }

    @Test
    void search_shouldThrow_ifUserNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.search(9999, "abc"));
    }

    @Test
    void findById_shouldFindItemById() {
        Long id = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        ResponseItemDto result = itemService.findById(itemOwner.getId(), id);

        assertThat(result.getId(), is(id));
        assertThat(result.getName(), is(TEST_ITEM.getName()));
        assertThat(result.getDescription(), is(TEST_ITEM.getDescription()));
        assertThat(result, hasProperty("comments"));
        assertThat(result, hasProperty("nextBooking"));
        assertThat(result, hasProperty("lastBooking"));
    }

    @Test
    void findById_shouldThrow_whenItemDoesNotExist() {
        assertThrows(NotFoundException.class, () -> itemService.findById(itemOwner.getId(), 9999));
    }

    @Test
    void findById_shouldThrow_whenUserDoesNotExist() {
        Long id = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        assertThrows(NotFoundException.class, () -> itemService.findById(9999, id));
    }

    @Test
    void createItem_shouldCreateItem() {
        Long id = itemService.create(itemOwner.getId(), TEST_ITEM).getId();

        TypedQuery<Item> query = em.createQuery("select i from Item i where id = :id", Item.class);
        Item item = query.setParameter("id", id).getSingleResult();

        assertThat(item, notNullValue());
        assertThat(item.getId(), is(id));
        assertThat(item.getOwner().getId(), is(itemOwner.getId()));
        assertThat(item.getName(), is(TEST_ITEM.getName()));
        assertThat(item.getDescription(), is(TEST_ITEM.getDescription()));
    }

    @Test
    void createItem_shouldTrimNameAndDescription() {
        Long id = itemService.create(itemOwner.getId(),
                new NewItemDto(TEST_ITEM.getName() + "  ",
                        "  " + TEST_ITEM.getDescription(),
                        true, null))
                .getId();

        TypedQuery<Item> query = em.createQuery("select i from Item i where id = :id", Item.class);
        Item item = query.setParameter("id", id).getSingleResult();

        assertThat(item.getName(), is(TEST_ITEM.getName()));
        assertThat(item.getDescription(), is(TEST_ITEM.getDescription()));
    }

    @Test
    void createItem_shouldThrow_ifUserDoesNotExist() {
        assertThrows(NotFoundException.class, () -> itemService.create(9999, TEST_ITEM));
    }

    @Test
    void createComment_shouldCreateComment() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        createPastBookingForItem(itemViewer, itemId, BookingStatus.APPROVED);

        Long commentId = itemService.createComment(itemViewer.getId(), itemId, TEST_COMMENT).getId();

        TypedQuery<Comment> query = em.createQuery("select c from Comment c where :id = id", Comment.class);
        Comment comment = query.setParameter("id", commentId).getSingleResult();

        assertThat(comment.getId(), is(commentId));
        assertThat(comment.getText(), is(TEST_COMMENT.getText()));
        assertThat(comment.getAuthor(), is(itemViewer));
        assertThat(comment.getItem().getId(), is(itemId));
        assertThat(comment.getCreated(), notNullValue());
    }

    @Test
    void createComment_commentTextShouldBeTrimmed() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        createPastBookingForItem(itemViewer, itemId, BookingStatus.APPROVED);

        String text = TEST_COMMENT.getText() + "   ";
        Long commentId = itemService.createComment(itemViewer.getId(), itemId, new CommentDto(text)).getId();

        TypedQuery<Comment> query = em.createQuery("select c from Comment c where :id = id", Comment.class);
        Comment comment = query.setParameter("id", commentId).getSingleResult();

        assertThat(comment.getId(), is(commentId));
        assertThat(comment.getText(), is(TEST_COMMENT.getText()));
    }

    @Test
    void createComment_shouldThrow_ifAuthorDidNotBookItem() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        assertThrows(CommentRequestException.class,
                () -> itemService.createComment(itemViewer.getId(), itemId, TEST_COMMENT));
    }

    @Test
    void createComment_shouldThrow_ifBookingStartDate_isInTheFuture() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        createFutureBookingForItem(itemViewer, itemId);
        assertThrows(CommentRequestException.class,
                () -> itemService.createComment(itemViewer.getId(), itemId, TEST_COMMENT));
    }

    @Test
    void createComment_shouldThrow_ifBookingNotApproved() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();

        createPastBookingForItem(itemViewer, itemId, BookingStatus.WAITING);
        createPastBookingForItem(itemViewer, itemId, BookingStatus.CANCELED);
        createPastBookingForItem(itemViewer, itemId, BookingStatus.REJECTED);

        assertThrows(CommentRequestException.class,
                () -> itemService.createComment(itemViewer.getId(), itemId, TEST_COMMENT));
    }

    @Test
    void createComment_shouldThrow_ifUserNotFound() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        createPastBookingForItem(itemViewer, itemId, BookingStatus.APPROVED);

        assertThrows(NotFoundException.class, () -> itemService.createComment(9999, itemId, TEST_COMMENT));
    }

    @Test
    void createComment_shouldThrow_ifItemNotFound() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        createPastBookingForItem(itemViewer, itemId, BookingStatus.APPROVED);

        assertThrows(NotFoundException.class,
                () -> itemService.createComment(itemViewer.getId(), 9999, TEST_COMMENT));
    }

    @Test
    void update_shouldUpdateItem() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();

        UpdateItemDto dto = new UpdateItemDto("newName", "new_description_123", false);
        itemService.update(itemOwner.getId(), itemId, dto);

        TypedQuery<Item> query = em.createQuery("select i from Item i where id = :id", Item.class);
        Item item = query.setParameter("id", itemId).getSingleResult();

        assertThat(item.getId(), is(itemId));
        assertThat(item.getName(), is(dto.getName()));
        assertThat(item.getDescription(), is(dto.getDescription()));
        assertThat(item.isAvailable(), is(dto.getAvailable()));
    }

    @Test
    void update_nameAndDescription_shouldGetTrimmed() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();

        UpdateItemDto dto = new UpdateItemDto("newName   ", "  new_description_123  ", false);
        itemService.update(itemOwner.getId(), itemId, dto);

        TypedQuery<Item> query = em.createQuery("select i from Item i where id = :id", Item.class);
        Item item = query.setParameter("id", itemId).getSingleResult();

        assertThat(item.getName(), is(dto.getName().trim()));
        assertThat(item.getDescription(), is(dto.getDescription().trim()));
    }

    @Test
    void update_shouldOnlyUpdate_nonNullFields() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();

        UpdateItemDto dto = new UpdateItemDto(null, "new_description_123", null);
        itemService.update(itemOwner.getId(), itemId, dto);

        TypedQuery<Item> query = em.createQuery("select i from Item i where id = :id", Item.class);
        Item item = query.setParameter("id", itemId).getSingleResult();

        assertThat(item.getName(), is(TEST_ITEM.getName()));
        assertThat(item.getDescription(), is(dto.getDescription()));
        assertThat(item.isAvailable(), is(TEST_ITEM.getAvailable()));
    }

    @Test
    void update_shouldThrow_ifUserIsNotOwner() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        UpdateItemDto dto = new UpdateItemDto(null, "new_description_123", null);
        assertThrows(UserValidationException.class, () -> itemService.update(itemViewer.getId(), itemId, dto));
    }

    @Test
    void update_shouldThrow_ifUserNotFound() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        UpdateItemDto dto = new UpdateItemDto(null, "new_description_123", null);
        assertThrows(NotFoundException.class, () -> itemService.update(9999, itemId, dto));
    }

    @Test
    void update_shouldThrow_ifItemNotFound() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        UpdateItemDto dto = new UpdateItemDto(null, "new_description_123", null);
        assertThrows(NotFoundException.class, () -> itemService.update(itemOwner.getId(), 9999, dto));
    }

    @Test
    void delete_shouldDeleteItem() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        itemService.delete(itemOwner.getId(), itemId);

        assertThat(itemService.findByOwner(itemOwner.getId()), empty());
    }

    @Test
    void delete_shouldThrow_ifUserNotOwner() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        assertThrows(UserValidationException.class, () -> itemService.delete(itemViewer.getId(), itemId));
    }

    @Test
    void delete_shouldThrow_ifUserNotFound() {
        Long itemId = itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        assertThrows(NotFoundException.class, () -> itemService.delete(9999, itemId));
    }

    @Test
    void delete_shouldThrow_ifItemNotFound() {
        itemService.create(itemOwner.getId(), TEST_ITEM).getId();
        assertThrows(NotFoundException.class, () -> itemService.delete(itemOwner.getId(), 9999));
    }

    private Booking createPastBookingForItem(User booker, Long itemId, BookingStatus status) {
        TypedQuery<Item> query = em.createQuery("select i from Item i where id = :id", Item.class);
        Item item = query.setParameter("id", itemId).getSingleResult();

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.of(2021, 1, 1, 12, 12));
        booking.setEnd(LocalDateTime.of(2021, 2, 2, 12, 12));
        booking.setStatus(status);

        return bookingRepository.save(booking);
    }

    private Booking createFutureBookingForItem(User booker, Long itemId) {
        TypedQuery<Item> query = em.createQuery("select i from Item i where id = :id", Item.class);
        Item item = query.setParameter("id", itemId).getSingleResult();

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.of(3021, 1, 1, 12, 12));
        booking.setEnd(LocalDateTime.of(3021, 2, 2, 12, 12));
        booking.setStatus(BookingStatus.APPROVED);

        return bookingRepository.save(booking);
    }


}
