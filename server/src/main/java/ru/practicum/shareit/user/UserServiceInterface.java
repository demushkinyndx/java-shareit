package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserServiceInterface {
	UserDto create(UserDto userDto);

	UserDto update(Long userId, UserDto userDto);

	UserDto getById(Long userId);

	List<UserDto> getAll();

	void delete(Long userId);

	void checkUserExists(Long userId);

	User findUser(Long userId);
}
