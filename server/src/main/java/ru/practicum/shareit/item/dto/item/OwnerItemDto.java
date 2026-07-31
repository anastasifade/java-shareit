package ru.practicum.shareit.item.dto.item;

import lombok.Getter;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.item.dto.comment.ResponseCommentDto;

import java.util.Collection;

@Getter
public final class OwnerItemDto extends ResponseItemDto {
    ResponseBookingDto lastBooking;
    ResponseBookingDto nextBooking;

    public OwnerItemDto(Long id, String name, String description, boolean available,
                        ResponseBookingDto lastBooking, ResponseBookingDto nextBooking,
                        Collection<ResponseCommentDto> comments) {
        super(id, name, description, available, comments);
        this.lastBooking = lastBooking;
        this.nextBooking = nextBooking;
    }

}
