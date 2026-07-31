package ru.practicum.shareit.booking.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.base.exceptions.ItemUnavailableException;
import ru.practicum.shareit.base.exceptions.NotFoundException;
import ru.practicum.shareit.base.exceptions.UserValidationException;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.base.auth.Role;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final Sort SORT_BY_DATE = Sort.by(Sort.Direction.DESC, "start");

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public Collection<ResponseBookingDto> findAll(long userId, Role userRole, BookingSearch filter) {
        userService.validateUser(userId);

        log.debug("Request for all bookings [search filter = {}] by user[role = {}, id = {}] received by BookingService.",
                filter, userRole, userId);

        BooleanExpression exp = getBooleanExpression(filter, userRole, userId);
        return StreamSupport.stream(bookingRepository.findAll(exp, SORT_BY_DATE).spliterator(), false)
                .map(BookingMapper::toDto)
                .toList();
    }

    @Override
    public ResponseBookingDto findById(long userId, long bookingId) {
        userService.validateUser(userId);

        log.debug("Request for booking [id={}] by user [id={}] received by BookingService.", bookingId, userId);

        Booking booking = getBooking(bookingId);
        if (booking.getBooker().getId() != userId
                && booking.getItem().getOwner().getId() != userId) {
            log.warn("User [id={}] has no authorized access to booking [id={}].", userId, bookingId);
            throw new UserValidationException(String.format("Access to booking [id=%d] denied.", bookingId));
        }

        return BookingMapper.toDto(booking);
    }

    @Transactional
    @Override
    public ResponseBookingDto create(long userId, NewBookingDto dto) {
        User booker = userService.getUser(userId);
        Item item = itemService.getItem(dto.getItemId());

        log.debug("Request to book item [id={}] by user [id={}] received by BookingService.",
                item.getId(), userId);
        log.trace("Booking request details: {}.", dto);

        if (!item.isAvailable()) {
            log.warn("Item [id={}] is unavailable for booking. Request denied.", item.getId());
            throw new ItemUnavailableException("Item is unavailable for booking.");
        }

        Booking booking = bookingRepository.save(BookingMapper.toBooking(dto, booker, item));
        return BookingMapper.toDto(booking);
    }

    @Transactional
    public ResponseBookingDto updateStatus(long userId, long bookingId, boolean approved) {
        userService.validateUser(userId);
        Booking booking = getBooking(bookingId);

        BookingStatus status = getNewStatus(approved, booking.getStatus());

        log.debug("Request to update booking [id={}] status to {} by user [id={}] received by BookingService.",
                bookingId, status, userId);

        if (booking.getItem().getOwner().getId() != userId) {
            log.warn("Booking status update denied: user [id={}] does not have access to modify booking.", userId);
            throw new UserValidationException(String.format("Access to booking [id=%d] denied.", bookingId));
        }

        booking.setStatus(status);
        bookingRepository.save(booking);
        return BookingMapper.toDto(booking);
    }

    private Booking getBooking(long id) {
        return bookingRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Booking [id=%d] not found.", id)));
    }

    private BooleanExpression getBooleanExpression(BookingSearch filter, Role role, long userId) {
        BooleanExpression exp = role.equals(Role.OWNER) ?
                QBooking.booking.item.owner.id.eq(userId) :
                QBooking.booking.booker.id.eq(userId);

        switch (filter) {
            case CURRENT -> {
                return exp.and(QBooking.booking.start.before(LocalDateTime.now())
                        .and(QBooking.booking.end.after(LocalDateTime.now())));
            }
            case PAST -> {
                return exp.and(QBooking.booking.end.before(LocalDateTime.now()));
            }
            case FUTURE -> {
                return exp.and(QBooking.booking.start.after(LocalDateTime.now()));
            }
            case WAITING -> {
                return exp.and(QBooking.booking.status.eq(BookingStatus.WAITING));
            }
            case REJECTED -> {
                return exp.and(QBooking.booking.status.eq(BookingStatus.REJECTED));
            }
            default -> {
                return exp;
            }
        }
    }

    private BookingStatus getNewStatus(boolean approved, BookingStatus oldStatus) {
        if (approved) {
            return BookingStatus.APPROVED;
        }

        switch (oldStatus) {
            case APPROVED, CANCELED -> {
                return BookingStatus.CANCELED;
            }
            default -> {
                return BookingStatus.REJECTED;
            }
        }
    }
}
