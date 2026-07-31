package ru.practicum.shareit.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.base.exceptions.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ResponseRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.RequestService;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceTests {

    private static final ItemRequestDto TEST_REQUEST = new ItemRequestDto("abc");

    private final RequestService requestService;
    private final UserRepository userRepository;

    private final EntityManager em;

    private User requestor;
    private User viewer;

    @BeforeEach
    void setUp() {
        requestor = new User();
        requestor.setName("R_Name");
        requestor.setEmail("r@email.com");
        userRepository.save(requestor);

        viewer = new User();
        viewer.setName("V_Name");
        viewer.setEmail("v@email.com");
        userRepository.save(viewer);
    }

    @Test
    void testFindAll_shouldReturnEmptyList_whenNoRequests() {
        assertThat(requestService.findAll(requestor.getId()), empty());
    }

    @Test
    void testFindAll_shouldReturnAllRequest_ByAllUsers() {
        requestService.create(requestor.getId(), TEST_REQUEST);
        requestService.create(viewer.getId(), TEST_REQUEST);

        assertThat(requestService.findAll(requestor.getId()), hasSize(2));
    }

    @Test
    void testFindAll_shouldThrow_ifUserNotFound() {
        assertThrows(NotFoundException.class, () -> requestService.findAll(9999));
    }

    @Test
    void testFindAllByUser_shouldReturnEmptyList_whenNoRequests() {
        assertThat(requestService.findAllByUser(requestor.getId()), empty());
    }

    @Test
    void testFindAllByUser_shouldReturnRequests_MadeByUser() {
        requestService.create(requestor.getId(), TEST_REQUEST);
        requestService.create(viewer.getId(), TEST_REQUEST);

        assertThat(requestService.findAllByUser(requestor.getId()), hasSize(1));
    }

    @Test
    void testFindAllByUser_shouldThrow_ifUserNotFound() {
        assertThrows(NotFoundException.class, () -> requestService.findAllByUser(9999));
    }

    @Test
    void testFindById_shouldReturnRequestById() {
        Long requestId = requestService.create(requestor.getId(), TEST_REQUEST).getId();

        ResponseRequestDto request = requestService.findById(requestor.getId(), requestId);

        assertThat(request.getId(), is(requestId));
        assertTrue(request.getCreated().isBefore(LocalDateTime.now()));
        assertThat(request.getDescription(), is(TEST_REQUEST.getDescription()));
        assertThat(request.getItems(), notNullValue());
    }

    @Test
    void testFindById_shouldThrow_ifUserNotFound() {
        Long requestId = requestService.create(requestor.getId(), TEST_REQUEST).getId();
        assertThrows(NotFoundException.class, () -> requestService.findById(9999, requestId));
    }

    @Test
    void testFindById_shouldThrow_ifRequestNotFound() {
        assertThrows(NotFoundException.class, () -> requestService.findById(requestor.getId(), 9999));
    }

    @Test
    void testCreate_shouldCreateRequest() {
        Long requestId = requestService.create(requestor.getId(), TEST_REQUEST).getId();

        TypedQuery<ItemRequest> query =
                em.createQuery("select ir from ItemRequest ir where id = :id", ItemRequest.class);
        ItemRequest itemRequest = query.setParameter("id", requestId).getSingleResult();

        assertThat(itemRequest, notNullValue());
        assertThat(itemRequest.getId(), is(requestId));
        assertThat(itemRequest.getDescription(), is(TEST_REQUEST.getDescription()));
        assertThat(itemRequest.getCreated(), notNullValue());
        assertThat(itemRequest.getRequestor().getId(), is(requestor.getId()));
    }

    @Test
    void testCreate_descriptionShouldGetTrimmed() {
        ItemRequestDto dto = new ItemRequestDto(TEST_REQUEST.getDescription() + "      ");

        Long requestId = requestService.create(requestor.getId(), dto).getId();

        TypedQuery<ItemRequest> query =
                em.createQuery("select ir from ItemRequest ir where id = :id", ItemRequest.class);
        ItemRequest itemRequest = query.setParameter("id", requestId).getSingleResult();

        assertThat(itemRequest, notNullValue());
        assertThat(itemRequest.getDescription(), is(TEST_REQUEST.getDescription()));
    }

    @Test
    void testCreate_shouldThrow_ifUserNotFound() {
        assertThrows(NotFoundException.class, () -> requestService.create(9999, TEST_REQUEST));
    }
}
