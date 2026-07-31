package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.base.exceptions.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dal.RequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ResponseRequestDto;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private static final Sort SORT_BY_CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    private final RequestRepository requestRepository;

    private final ItemService itemService;
    private final UserService userService;

    public Collection<ResponseRequestDto> findAll(long userId) {
        log.debug("Request by user [id = {}] for all item requests received by RequestService.", userId);
        userService.validateUser(userId);

        List<ItemRequest> requests = requestRepository.findAll(SORT_BY_CREATED_DESC);
        return requestsToDto(requests);
    }

    public Collection<ResponseRequestDto> findAllByUser(long userId) {
        log.debug("Request for all item requests submitted by user [id = {}] received by RequestService.", userId);
        userService.validateUser(userId);

        List<ItemRequest> requests = requestRepository.findByRequestorId(userId, SORT_BY_CREATED_DESC);
        return requestsToDto(requests);
    }

    public ResponseRequestDto findById(long userId, long requestId) {
        log.debug("Request for item request [id = {}] by user [id = {}] received by RequestService.",
                requestId, userId);
        userService.validateUser(userId);

        ItemRequest request = requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException(String.format("Item request with id=%d not found.", requestId)));
        List<Item> items = getItemsForRequests(List.of(request)).getOrDefault(requestId, List.of());

        return RequestMapper.toDto(request, items);
    }

    @Transactional
    public ResponseRequestDto create(long userId, ItemRequestDto dto) {
        log.debug("Request to create a new item request submitted by user [id = {}], received by RequestService.",
                userId);
        log.trace("Creating item request: {}. Requestor id: {}.", dto, userId);
        User requestor = userService.getUser(userId); // validates user and returns if exists

        ItemRequest request = requestRepository.save(RequestMapper.toRequest(dto, requestor));
        return RequestMapper.toDto(request, List.of());
    }

    private Collection<ResponseRequestDto> requestsToDto(List<ItemRequest> requests) {
        Map<Long, List<Item>> itemsForRequest = getItemsForRequests(requests);

        return requests.stream()
                .map(request -> RequestMapper.toDto(request,
                        itemsForRequest.getOrDefault(request.getId(), List.of())))
                .toList();
    }

    private Map<Long, List<Item>> getItemsForRequests(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return new HashMap<>();
        }

        Map<Long, List<Item>> itemsForRequest = new HashMap<>();
        itemService.findByRequests(requests).forEach(item -> {
            if (!itemsForRequest.containsKey(item.getRequest().getId())) {
                itemsForRequest.put(item.getRequest().getId(), new ArrayList<>());
            }
            itemsForRequest.get(item.getRequest().getId()).add(item);
        });

        return itemsForRequest;
    }
}
