package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.item.BookingItemDto;
import ru.practicum.shareit.item.dto.item.OwnerItemDto;
import ru.practicum.shareit.item.dto.item.ResponseItemDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemDtoJsonTests {

    private static final String NAME = "name";
    private static final String DESCRIPTION = "desc";

    private final JacksonTester<ResponseItemDto> json;
    private final JacksonTester<OwnerItemDto> jsonOwner;

    @Test
    void testResponseItemDto() throws IOException {
        ResponseItemDto dto = ResponseItemDto.builder()
                .id(1L)
                .name(NAME)
                .description(DESCRIPTION)
                .available(true)
                .comments(List.of())
                .build();

        JsonContent<ResponseItemDto> content = json.write(dto);

        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(content).extractingJsonPathStringValue("$.name").isEqualTo(NAME);
        assertThat(content).extractingJsonPathStringValue("$.description").isEqualTo(DESCRIPTION);
        assertThat(content).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(content).hasJsonPath("$.comments");

        assertThat(content).doesNotHaveJsonPath("$.nextBooking");
        assertThat(content).doesNotHaveJsonPath("$.lastBooking");
    }

    @Test
    void testOwnerItemDto() throws IOException {
        BookingItemDto itemDto = BookingItemDto.builder()
                .id(1L)
                .name(NAME)
                .build();

        ResponseUserDto userDto = ResponseUserDto.builder()
                .id(1L)
                .name(NAME)
                .email("email@email.email")
                .build();

        LocalDateTime start = LocalDateTime.of(2020, 01, 01, 20, 20);
        LocalDateTime end = start.plusDays(2);

        ResponseBookingDto bookingDto = ResponseBookingDto.builder()
                .id(1L)
                .item(itemDto)
                .booker(userDto)
                .start(start)
                .end(end)
                .status(BookingStatus.APPROVED)
                .build();

        OwnerItemDto dto = new OwnerItemDto(1L, NAME, DESCRIPTION, true, bookingDto, bookingDto, List.of());

        JsonContent<OwnerItemDto> content = jsonOwner.write(dto);

        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(content).extractingJsonPathStringValue("$.name").isEqualTo(NAME);
        assertThat(content).extractingJsonPathStringValue("$.description").isEqualTo(DESCRIPTION);
        assertThat(content).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(content).hasJsonPath("$.comments");

        assertThat(content).extractingJsonPathValue("$.nextBooking").hasFieldOrProperty("id");
        assertThat(content).extractingJsonPathValue("$.nextBooking").hasFieldOrProperty("item");
        assertThat(content).extractingJsonPathValue("$.nextBooking").hasFieldOrProperty("booker");
        assertThat(content).extractingJsonPathValue("$.nextBooking").hasFieldOrProperty("status");
        assertThat(content).extractingJsonPathValue("$.nextBooking")
                .hasFieldOrPropertyWithValue("start", start.format(DateTimeFormatter.ISO_DATE_TIME));
        assertThat(content).extractingJsonPathValue("$.nextBooking")
                .hasFieldOrPropertyWithValue("end", end.format(DateTimeFormatter.ISO_DATE_TIME));

        assertThat(content).extractingJsonPathValue("$.lastBooking").hasFieldOrProperty("start");
        assertThat(content).extractingJsonPathValue("$.lastBooking").hasFieldOrProperty("end");
    }
}
