package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;

	@Override
	public UserDto create(UserDto userDto) {
		checkEmailUsage(userDto.getEmail());
		User user = userRepository.save(UserMapper.toUser(userDto));
		log.info("Создан пользователь id={}", user.getId());
		return UserMapper.toUserDto(user);
	}

	@Override
	public UserDto update(Long userId, UserDto userDto) {
		User user = findUser(userId);

		if (userDto.getName() != null && !userDto.getName().isBlank()) {
			user.setName(userDto.getName());
		}
		String email = userDto.getEmail();
		if (email != null && !email.isBlank() && !email.equalsIgnoreCase(user.getEmail())) {
			checkEmailUsage(email);
			user.setEmail(email);
		}

		log.info("Обновлен пользователь id={}", userId);
		return UserMapper.toUserDto(userRepository.update(user));
	}

	@Override
	public UserDto getById(Long userId) {
		return UserMapper.toUserDto(findUser(userId));
	}

	@Override
	public List<UserDto> getAll() {
		return userRepository.findAll().stream()
				.map(UserMapper::toUserDto)
				.toList();
	}

	@Override
	public void delete(Long userId) {
		userRepository.deleteById(userId);
		log.info("Удален пользователь id={}", userId);
	}

	private User findUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
	}

	private void checkEmailUsage(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("Email " + email + " уже используется");
		}
	}
}
