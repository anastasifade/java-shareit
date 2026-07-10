package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.error.ErrorHandler;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
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

    @MockBean
    private final BookingClient client;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    @Test
    void testFindAllBookings_asBooker_noStateParamSpecified() throws Exception {
        when(client.findAll(1L, Role.BOOKER, BookingSearch.ALL))
                .thenReturn(null);

        mvc.perform(get(BOOKING_URL)
                        .param("role", "BOOKER")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk());

        verify(client).findAll(1L, Role.BOOKER, BookingSearch.ALL);
    }

    @Test
    void testFindBookings_asBooker_withStateParam() throws Exception {
        when(client.findAll(anyLong(), any(), any()))
                .thenReturn(null);

        // ALL
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("role", "BOOKER")
                        .param("state", "ALL")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());

        verify(client).findAll(1L, Role.BOOKER, BookingSearch.ALL);

        // PAST
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("role", "BOOKER")
                        .param("state", "PAST")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
        verify(client).findAll(1L, Role.BOOKER, BookingSearch.PAST);

        // CURRENT
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("role", "BOOKER")
                        .param("state", "CURRENT")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
        verify(client).findAll(1L, Role.BOOKER, BookingSearch.CURRENT);

        // FUTURE
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("role", "BOOKER")
                        .param("state", "FUTURE")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
        verify(client).findAll(1L, Role.BOOKER, BookingSearch.FUTURE);

        // WAITING
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("role", "BOOKER")
                        .param("state", "WAITING")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
        verify(client).findAll(1L, Role.BOOKER, BookingSearch.WAITING);

        // REJECTED
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("role", "BOOKER")
                        .param("state", "REJECTED")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
        verify(client).findAll(1L, Role.BOOKER, BookingSearch.REJECTED);
    }

    @Test
    void testFindAllBookings_asBooker_invalidStateParam() throws Exception {
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "NOT_A_STATE")
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void testFindAllBookings_asOwner_noStateParamSpecified() throws Exception {
        when(client.findAll(1L, Role.OWNER, BookingSearch.ALL))
                .thenReturn(null);

        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("role", "OWNER")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());

        verify(client).findAll(1L, Role.OWNER, BookingSearch.ALL);
    }

    @Test
    void testFindBookings_asOwner_withStateParam() throws Exception {
        when(client.findAll(anyLong(), any(), any()))
                .thenReturn(null);

        // ALL
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "ALL")
                        .param("role", "OWNER")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
        verify(client).findAll(1L, Role.OWNER, BookingSearch.ALL);

        // PAST
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("role", "OWNER")
                        .param("state", "PAST")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
        verify(client).findAll(1L, Role.OWNER, BookingSearch.PAST);

        // CURRENT
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("role", "OWNER")
                        .param("state", "CURRENT")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
        verify(client).findAll(1L, Role.OWNER, BookingSearch.CURRENT);

        // FUTURE
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("role", "OWNER")
                        .param("state", "FUTURE")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
        verify(client).findAll(1L, Role.OWNER, BookingSearch.FUTURE);

        // WAITING
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("role", "OWNER")
                        .param("state", "WAITING")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
        verify(client).findAll(1L, Role.OWNER, BookingSearch.WAITING);

        // REJECTED
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("role", "OWNER")
                        .param("state", "REJECTED")
                        .header(HEADER, 1L))
                .andExpect(status().isOk());
        verify(client).findAll(1L, Role.OWNER, BookingSearch.REJECTED);
    }

    @Test
    void testFindAllBookings_asOwner_invalidStateParam() throws Exception {
        mvc.perform(get(BOOKING_URL + "/owner")
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "NOT_A_STATE")
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
    }

    @Test
    void testFindBookingById() throws Exception {
        when(client.findById(anyLong(), anyLong()))
                .thenReturn(null);

        mvc.perform(get(BOOKING_URL + "/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk());

        verify(client).findById(1L, 1L);
    }

    @Test
    void testCreateBooking() throws Exception {
        NewBookingDto dto = new NewBookingDto(1L, FUTURE_START_DATE, FUTURE_END_DATE);

        when(client.create(anyLong(), any()))
                .thenReturn(null);

        mvc.perform(post(BOOKING_URL)
                        .content(mapper.writeValueAsString(dto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isCreated());

        verify(client).create(1L, dto);
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
                .andExpect(jsonPath("$.error", is("[Item id cannot be null.]")));

        verifyNoInteractions(client);
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
                .andExpect(jsonPath("$.error", is("[Booking start date cannot be in the past.]")));

        NewBookingDto nullStartDate = new NewBookingDto(1L, null, FUTURE_END_DATE);
        mvc.perform(post(BOOKING_URL)
                        .content(mapper.writeValueAsString(nullStartDate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("[Booking start date must be specified.]")));

        verifyNoInteractions(client);
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
                .andExpect(jsonPath("$.error", is("[Booking end date cannot be in the past.]")));

        NewBookingDto nullEndDate = new NewBookingDto(1L, FUTURE_START_DATE, null);
        mvc.perform(post(BOOKING_URL)
                        .content(mapper.writeValueAsString(nullEndDate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("[Booking end date must be specified.]")));

        verifyNoInteractions(client);
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
                .andExpect(jsonPath("$.error",
                        is("[Booking end date must be after booking start date.]")));

        verifyNoInteractions(client);
    }

    void testUpdateStatus() throws Exception {

        doNothing()
                .when(client.updateStatus(anyLong(), anyLong(), anyBoolean()));

        mvc.perform(patch(BOOKING_URL + "/1")
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER, 1L))
                .andExpect(status().isOk());

        verify(client).updateStatus(1L, 1L, true);
    }
}
