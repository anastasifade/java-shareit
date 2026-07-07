package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.base.auth.Role;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.dto.ResponseBookingDto;
import ru.practicum.shareit.booking.model.BookingSearch;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.ErrorHandler;
import ru.practicum.shareit.item.dto.item.ItemNameDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {BookingController.class, ErrorHandler.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerTests {

    private static final String BOOKING_URL = "/bookings";
    private static final String HEADER = "X-Sharer-User-Id";
    private static final LocalDateTime FUTURE_START_DATE =
            LocalDateTime.of(2500, 1, 1, 0, 0);
    private static final LocalDateTime FUTURE_END_DATE = FUTURE_START_DATE.plusDays(7);

    private static final ResponseBookingDto RESPONSE_DTO = ResponseBookingDto.builder()
            .id(1L)
            .item(ItemNameDto.builder().id(1L).name("name").build())
            .booker(ResponseUserDto.builder().id(1L).name("username").email("a@b.c").build())
            .start(FUTURE_START_DATE)
            .end(FUTURE_END_DATE)
            .status(BookingStatus.APPROVED)
            .build();

    @MockBean
    private final BookingService bookingService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;


    // GET

    @Test
    void testFindAllBookings_asBooker_noStateParamSpecified() throws Exception {
        when(bookingService.findAll(1L, Role.BOOKER, BookingSearch.ALL))
                .thenReturn(List.of(RESPONSE_DTO));

        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(bookingService).findAll(1L, Role.BOOKER, BookingSearch.ALL);
    }

    @Test
    void testFindBookings_asBooker_withStateParam() throws Exception {
        when(bookingService.findAll(anyLong(), any(), any()))
                .thenReturn(List.of(RESPONSE_DTO));

        // ALL
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(bookingService).findAll(1L, Role.BOOKER, BookingSearch.ALL);

        // PAST
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "PAST")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(bookingService).findAll(1L, Role.BOOKER, BookingSearch.PAST);

        // CURRENT
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "CURRENT")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(bookingService).findAll(1L, Role.BOOKER, BookingSearch.CURRENT);

        // FUTURE
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "FUTURE")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(bookingService).findAll(1L, Role.BOOKER, BookingSearch.FUTURE);

        // WAITING
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "WAITING")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(bookingService).findAll(1L, Role.BOOKER, BookingSearch.WAITING);

        // REJECTED
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "REJECTED")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(bookingService).findAll(1L, Role.BOOKER, BookingSearch.REJECTED);
    }

    @Test
    void testFindAllBookings_asBooker_invalidStateParam() throws Exception {
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "NOT_A_STATE")
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void testFindAllBookings_asOwner_noStateParamSpecified() throws Exception {
        when(bookingService.findAll(1L, Role.OWNER, BookingSearch.ALL))
                .thenReturn(List.of(RESPONSE_DTO));

        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(bookingService).findAll(1L, Role.OWNER, BookingSearch.ALL);
    }

    @Test
    void testFindBookings_asOwner_withStateParam() throws Exception {
        when(bookingService.findAll(anyLong(), any(), any()))
                .thenReturn(List.of(RESPONSE_DTO));

        // ALL
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(bookingService).findAll(1L, Role.OWNER, BookingSearch.ALL);

        // PAST
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "PAST")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(bookingService).findAll(1L, Role.OWNER, BookingSearch.PAST);

        // CURRENT
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "CURRENT")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(bookingService).findAll(1L, Role.OWNER, BookingSearch.CURRENT);

        // FUTURE
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "FUTURE")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(bookingService).findAll(1L, Role.OWNER, BookingSearch.FUTURE);

        // WAITING
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "WAITING")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(bookingService).findAll(1L, Role.OWNER, BookingSearch.WAITING);

        // REJECTED
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "REJECTED")
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(bookingService).findAll(1L, Role.OWNER, BookingSearch.REJECTED);
    }

    @Test
    void testFindAllBookings_asOwner_invalidStateParam() throws Exception {
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "NOT_A_STATE")
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void testFindBookingById() throws Exception {
        when(bookingService.findById(anyLong(), anyLong()))
                .thenReturn(RESPONSE_DTO);

        mvc.perform(get(BOOKING_URL + "/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.start", is(FUTURE_START_DATE.format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.end", is(FUTURE_END_DATE.format(DateTimeFormatter.ISO_DATE_TIME))))
                .andExpect(jsonPath("$.item", hasKey("id")))
                .andExpect(jsonPath("$.item", hasKey("name")))
                .andExpect(jsonPath("$.booker", hasKey("id")))
                .andExpect(jsonPath("$.booker", hasKey("name")))
                .andExpect(jsonPath("$.booker", hasKey("email")))
                .andExpect(jsonPath("$.status").exists());

        verify(bookingService).findById(1L, 1L);
    }


    // POST

    @Test
    void testCreateBooking() throws Exception {
        NewBookingDto dto = new NewBookingDto(1L, FUTURE_START_DATE, FUTURE_END_DATE);

        when(bookingService.create(anyLong(), any()))
                .thenReturn(RESPONSE_DTO);

        mvc.perform(post(BOOKING_URL)
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isCreated());

        verify(bookingService).create(1L, dto);
    }

    @Test
    void testCreateBooking_invalidItemId() throws Exception {
        NewBookingDto dto = new NewBookingDto(null, FUTURE_START_DATE, FUTURE_END_DATE);

        mvc.perform(post(BOOKING_URL)
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("[Item id cannot be null.]")));

        verifyNoInteractions(bookingService);
    }

    @Test
    void testCreateBooking_invalidStartDate() throws Exception {
        NewBookingDto startDateInPast = new NewBookingDto(1L,
                LocalDateTime.of(1990, 1, 1, 12, 12),
                FUTURE_END_DATE);
        mvc.perform(post(BOOKING_URL)
                        .content(mapper.writeValueAsString(startDateInPast))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("[Booking start date cannot be in the past.]")));

        NewBookingDto nullStartDate = new NewBookingDto(1L, null, FUTURE_END_DATE);
        mvc.perform(post(BOOKING_URL)
                        .content(mapper.writeValueAsString(nullStartDate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("[Booking start date must be specified.]")));

        verifyNoInteractions(bookingService);
    }

    @Test
    void testCreateBooking_invalidEndDate() throws Exception {
        NewBookingDto endDateInPast = new NewBookingDto(1L,
                FUTURE_START_DATE,
                LocalDateTime.of(1990, 1, 1, 12, 12));
        mvc.perform(post(BOOKING_URL)
                        .content(mapper.writeValueAsString(endDateInPast))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("[Booking end date cannot be in the past.]")));

        NewBookingDto nullEndDate = new NewBookingDto(1L, FUTURE_START_DATE, null);
        mvc.perform(post(BOOKING_URL)
                        .content(mapper.writeValueAsString(nullEndDate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("[Booking end date must be specified.]")));

        verifyNoInteractions(bookingService);
    }

    @Test
    void testCreateBooking_startBeforeEnd() throws Exception {
        NewBookingDto dto = new NewBookingDto(1L, FUTURE_END_DATE, FUTURE_START_DATE);
        mvc.perform(post(BOOKING_URL)
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        is("[Booking end date must be after booking start date.]")));

        verifyNoInteractions(bookingService);
    }


    // PATCH

    void testUpdateStatus() throws Exception {
        when(bookingService.updateStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(RESPONSE_DTO);

        mvc.perform(patch(BOOKING_URL + "/1")
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk());

        verify(bookingService).updateStatus(1L, 1L, true);
    }
}
