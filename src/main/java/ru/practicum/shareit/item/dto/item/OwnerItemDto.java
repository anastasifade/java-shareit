package ru.practicum.shareit.item.dto.item;

import lombok.Getter;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.item.dto.comment.ResponseCommentDto;

import java.util.Collection;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OwnerItemDto that = (OwnerItemDto) o;
        return Objects.equals(lastBooking, that.lastBooking) && Objects.equals(nextBooking, that.nextBooking);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lastBooking, nextBooking);
    }
}
