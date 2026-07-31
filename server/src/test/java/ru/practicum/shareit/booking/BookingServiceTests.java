package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.base.auth.Role;
import ru.practicum.shareit.base.exceptions.ItemUnavailableException;
import ru.practicum.shareit.base.exceptions.NotFoundException;
import ru.practicum.shareit.base.exceptions.UserValidationException;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingSearch;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
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
public class BookingServiceTests {

    private static final LocalDateTime FUTURE = LocalDateTime.of(3021, 1, 1, 12, 12);
    private static final LocalDateTime PAST = LocalDateTime.of(2021, 1, 1, 12, 12);

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private final EntityManager em;

    private Item item;
    private User booker;
    private User itemOwner;

    @BeforeEach
    void setUp() {
        booker = new User();
        booker.setName("name");
        booker.setEmail("a@b.c");
        userRepository.save(booker);

        itemOwner = new User();
        itemOwner.setName("owner");
        itemOwner.setEmail("email@yandex.ru");
        userRepository.save(itemOwner);

        item = new Item();
        item.setName("name");
        item.setDescription("desc");
        item.setAvailable(true);
        item.setOwner(itemOwner);
        itemRepository.save(item);
    }

    @Test
    void findAll_returnsEmptyList_whenNoBookingFound() {
        assertThat(bookingService.findAll(itemOwner.getId(), Role.OWNER, BookingSearch.ALL), empty());
    }

    @Test
    void findAll_filterAll_returnsAllBookings_forRole() {
        // creates 12 bookings with itemOwner as item owner, booker as item booker
        populateBookings();

        // should return all 12 bookings
        assertThat(bookingService.findAll(itemOwner.getId(), Role.OWNER, BookingSearch.ALL), hasSize(12));

        // should return no bookings - itemOwner has not booked any items
        assertThat(bookingService.findAll(itemOwner.getId(), Role.BOOKER, BookingSearch.ALL), empty());

        // should return 12 bookings -> booker has booked 12 items
        assertThat(bookingService.findAll(booker.getId(), Role.BOOKER, BookingSearch.ALL), hasSize(12));

        // should return no bookings - booker does not own any items that were booked
        assertThat(bookingService.findAll(booker.getId(), Role.OWNER, BookingSearch.ALL), empty());
    }

    @Test
    void findAll_filterPast_returnsOnlyPastBookings() {
        // creates 12 bookings, out of which 4 bookings have start and end dates in the past
        populateBookings();

        Collection<ResponseBookingDto> result =
                bookingService.findAll(itemOwner.getId(), Role.OWNER, BookingSearch.PAST);

        assertThat(result, hasSize(4));
        assertTrue(result.stream().allMatch(booking ->
                booking.getStart().isBefore(LocalDateTime.now()) &&
                booking.getEnd().isBefore(LocalDateTime.now())));
    }

    @Test
    void findAll_filterCurrent_returnsOnlyCurrentBookings() {
        // creates 12 bookings, out of which 4 bookings have start date in the past and end date in the future
        populateBookings();

        Collection<ResponseBookingDto> result =
                bookingService.findAll(itemOwner.getId(), Role.OWNER, BookingSearch.CURRENT);

        assertThat(result, hasSize(4));
        assertTrue(result.stream().allMatch(booking ->
                booking.getStart().isBefore(LocalDateTime.now()) &&
                        booking.getEnd().isAfter(LocalDateTime.now())));
    }

    @Test
    void findAll_filterFuture_returnsOnlyFutureBookings() {
        // creates 12 bookings, out of which 4 bookings have start and end dates in the future
        populateBookings();

        Collection<ResponseBookingDto> result =
                bookingService.findAll(itemOwner.getId(), Role.OWNER, BookingSearch.FUTURE);

        assertThat(result, hasSize(4));
        assertTrue(result.stream().allMatch(booking ->
                booking.getStart().isAfter(LocalDateTime.now()) &&
                        booking.getEnd().isAfter(LocalDateTime.now())));
    }

    @Test
    void findAll_filterWaiting_shouldReturnOnlyBookingsWithStatusWaiting() {
        // creates 12 bookings, out of which 3 have status WAITING
        populateBookings();

        Collection<ResponseBookingDto> result =
                bookingService.findAll(itemOwner.getId(), Role.OWNER, BookingSearch.WAITING);

        assertThat(result, hasSize(3));
        assertTrue(result.stream().allMatch(booking ->
                booking.getStatus().equals(BookingStatus.WAITING)));
    }

    @Test
    void findAll_filterRejected_shouldReturnOnlyBookingsWithStatusRejected() {
        // creates 12 bookings, out of which 3 have status REJECTED
        populateBookings();

        Collection<ResponseBookingDto> result =
                bookingService.findAll(itemOwner.getId(), Role.OWNER, BookingSearch.REJECTED);

        assertThat(result, hasSize(3));
        assertTrue(result.stream().allMatch(booking ->
                booking.getStatus().equals(BookingStatus.REJECTED)));
    }

    @Test
    void findAll_shouldThrow_ifUserNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.findAll(9999, Role.OWNER, BookingSearch.ALL));
    }

    @Test
    void findById_shouldReturnBookingById() {
        Long id = createTestBooking_Future(BookingStatus.APPROVED).getId();
        ResponseBookingDto result = bookingService.findById(booker.getId(), id);

        assertThat(result.getId(), is(id));
        assertThat(result.getBooker().getId(), is(booker.getId()));
        assertThat(result.getItem(), notNullValue());
        assertThat(result.getStatus(), is(BookingStatus.APPROVED));
        assertThat(result.getStart(), notNullValue());
        assertThat(result.getEnd(), notNullValue());
    }

    @Test
    void findById_shouldThrow_ifUserIsNotBookerOrOwner() {
        Long bookingId = createTestBooking_Future(BookingStatus.APPROVED).getId();

        // creating user that is neither booker not item owner
        User user = new User();
        user.setName("name");
        user.setEmail("something@email.com");
        Long id = userRepository.save(user).getId();

        assertThrows(UserValidationException.class, () -> bookingService.findById(id, bookingId));
    }

    @Test
    void findById_shouldThrow_ifUserNotFound() {
        Long bookingId = createTestBooking_Future(BookingStatus.APPROVED).getId();
        assertThrows(NotFoundException.class, () -> bookingService.findById(9999, bookingId));
    }

    @Test
    void findById_shouldThrowIfBookingNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.findById(booker.getId(), 9999));
    }

    @Test
    void create_shouldCreateBooking() {
        NewBookingDto dto = new NewBookingDto(item.getId(), FUTURE, FUTURE.plusDays(7));
        Long id = bookingService.create(booker.getId(), dto).getId();

        TypedQuery<Booking> query = em.createQuery("select b from Booking b where id = :id", Booking.class);
        Booking booking = query.setParameter("id", id).getSingleResult();

        assertThat(booking.getId(), is(id));
        assertThat(booking.getStatus(), is(BookingStatus.WAITING));
        assertThat(booking.getBooker().getId(), is(booker.getId()));
        assertThat(booking.getStart(), is(dto.getStart()));
        assertThat(booking.getEnd(), is(dto.getEnd()));
        assertThat(booking.getItem().getId(), is(item.getId()));
    }

    @Test
    void create_shouldThrow_ifItemNotAvailable() {
        item.setAvailable(false);
        itemRepository.save(item);

        NewBookingDto dto = new NewBookingDto(item.getId(), FUTURE, FUTURE.plusDays(7));
        assertThrows(ItemUnavailableException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void create_shouldThrow_ifUserNotFound() {
        NewBookingDto dto = new NewBookingDto(item.getId(), FUTURE, FUTURE.plusDays(7));
        assertThrows(NotFoundException.class, () -> bookingService.create(9999, dto));
    }

    @Test
    void create_shouldThrow_ifItemNotFound() {
        NewBookingDto dto = new NewBookingDto(item.getId(), FUTURE, FUTURE.plusDays(7));
        assertThrows(NotFoundException.class, () -> bookingService.create(9999, dto));
    }

    @Test
    void updateStatus_shouldUpdateStatusToApproved_IfApprovedIsTrue() {
        Booking booking = createTestBooking_Current(BookingStatus.WAITING);
        bookingService.updateStatus(itemOwner.getId(), booking.getId(), true);

        TypedQuery<Booking> query = em.createQuery("select b from Booking b where id = :id", Booking.class);
        booking = query.setParameter("id", booking.getId()).getSingleResult();

        assertThat(booking.getStatus(), is(BookingStatus.APPROVED));
    }

    @Test
    void updateStatus_shouldUpdateStatusToRejected_ifStatusIsWaiting_andApprovedIsFalse() {
        Booking booking = createTestBooking_Current(BookingStatus.WAITING);
        bookingService.updateStatus(itemOwner.getId(), booking.getId(), false);

        TypedQuery<Booking> query = em.createQuery("select b from Booking b where id = :id", Booking.class);
        booking = query.setParameter("id", booking.getId()).getSingleResult();

        assertThat(booking.getStatus(), is(BookingStatus.REJECTED));
    }

    @Test
    void updateStatus_shouldUpdateStatusToCanceled_isStatusIsAppproved_andApprovedIsFalse() {
        Booking booking = createTestBooking_Current(BookingStatus.APPROVED);
        bookingService.updateStatus(itemOwner.getId(), booking.getId(), false);

        TypedQuery<Booking> query = em.createQuery("select b from Booking b where id = :id", Booking.class);
        booking = query.setParameter("id", booking.getId()).getSingleResult();

        assertThat(booking.getStatus(), is(BookingStatus.CANCELED));
    }

    @Test
    void updateStatus_shouldThrow_ifUserNotItemOwner() {
        Long bookingId = createTestBooking_Current(BookingStatus.APPROVED).getId();
        assertThrows(UserValidationException.class,
                () -> bookingService.updateStatus(booker.getId(), bookingId, true));
    }

    @Test
    void updateStatus_shouldThrow_ifUserNotFound() {
        Long bookingId = createTestBooking_Current(BookingStatus.APPROVED).getId();
        assertThrows(NotFoundException.class,
                () -> bookingService.updateStatus(9999, bookingId, true));
    }

    @Test
    void updateStatus_shouldThrow_ifBookingNotFound() {
        assertThrows(NotFoundException.class,
                () -> bookingService.updateStatus(itemOwner.getId(), 9999, true));
    }

    // creates 12 bookings - 4 past, 4 current, 4 present - with each booking status
    private void populateBookings() {
        createTestBooking_Current(BookingStatus.APPROVED);
        createTestBooking_Current(BookingStatus.WAITING);
        createTestBooking_Current(BookingStatus.REJECTED);
        createTestBooking_Current(BookingStatus.CANCELED);

        createTestBooking_Past(BookingStatus.APPROVED);
        createTestBooking_Past(BookingStatus.WAITING);
        createTestBooking_Past(BookingStatus.REJECTED);
        createTestBooking_Past(BookingStatus.CANCELED);

        createTestBooking_Future(BookingStatus.APPROVED);
        createTestBooking_Future(BookingStatus.WAITING);
        createTestBooking_Future(BookingStatus.REJECTED);
        createTestBooking_Future(BookingStatus.CANCELED);
    }

    private Booking createTestBooking_Past(BookingStatus status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(PAST);
        booking.setEnd(booking.getStart().plusDays(7));
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private Booking createTestBooking_Current(BookingStatus status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(booking.getStart().plusDays(7));
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    private Booking createTestBooking_Future(BookingStatus status) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(FUTURE);
        booking.setEnd(booking.getStart().plusDays(7));
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }
}
