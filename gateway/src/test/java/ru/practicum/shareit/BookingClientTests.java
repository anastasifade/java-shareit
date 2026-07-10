package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingClientTests {

    @MockBean
    private final RestTemplate rest;


}
