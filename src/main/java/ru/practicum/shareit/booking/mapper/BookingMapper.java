package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class BookingMapper {

    public ResponseBookingDto toDto(Booking booking) {
        return ResponseBookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(ItemMapper.toBookingItemDto(booking.getItem()))
                .booker(UserMapper.toDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }

    public Booking toBooking(NewBookingDto dto, User booker, Item item) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

}
