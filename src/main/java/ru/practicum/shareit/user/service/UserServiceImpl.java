package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.base.exceptions.DuplicateDataException;
import ru.practicum.shareit.base.exceptions.NotFoundException;
import ru.practicum.shareit.base.exceptions.UserValidationException;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.dto.NewUserDto;
import ru.practicum.shareit.user.dto.ResponseUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Collection<ResponseUserDto> findAll() {
        log.debug("Request for list of all users received by UserService.");
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public ResponseUserDto findById(long id) {
        log.debug("Request for user [id={}] received by UserService.", id);
        return UserMapper.toDto(getUser(id));
    }

    @Transactional
    @Override
    public ResponseUserDto create(NewUserDto dto) {
        validateEmail(dto.getEmail());
        log.debug("Request to create new user received by UserService.");
        log.trace("Creating user: [name={}, email={}].", dto.getName(), dto.getEmail());
        User user = userRepository.save(UserMapper.toUser(dto));
        return UserMapper.toDto(user);
    }

    @Transactional
    @Override
    public ResponseUserDto update(long id, UpdateUserDto dto) {
        log.debug("Request to update user received by UserService.");
        User existingUser = getUser(id);
        if (dto.getEmail() != null &&
                !dto.getEmail().isBlank() &&
                !dto.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            validateEmail(dto.getEmail());
        }
        User user = userRepository.save(UserMapper.toUser(dto, existingUser));
        return UserMapper.toDto(user);
    }

    @Transactional
    @Override
    public void delete(long id) {
        validateUser(id);
        log.debug("Request to delete user [id={}] received by UserService.", id);
        userRepository.deleteById(id);
    }

    @Override
    public void validateUser(long id) {
        if (!userRepository.existsById(id)) {
            throw new UserValidationException(String.format("Request validation failed: user with id=[%s] not found.",
                    id));
        }
    }

    @Override
    public User getUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=[%s] not found.", id)));
    }

    private void validateEmail(String email) {
         if (userRepository.existsByEmailIgnoreCase(email)) {
             log.debug("Failed to validate email [{}]. Email already exists.", email);
             throw new DuplicateDataException(String.format("Email [%s] already exists.", email));
         }
    }

}
