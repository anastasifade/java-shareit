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
    void testFindAllBookings_asBooker_invalidStateParam() throws Exception {
        mvc.perform(get(BOOKING_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("state", "NOT_A_STATE")
                        .header(HEADER, 1L))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(client);
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
}
