package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserServiceInterface {
	private final UserRepository userRepository;

	@Override
	@Transactional
	public UserDto create(UserDto userDto) {
		checkEmailUsage(userDto.getEmail());
		User user = userRepository.save(UserMapper.toUser(userDto));
		log.info("Создан пользователь id={}", user.getId());
		return UserMapper.toUserDto(user);
	}

	@Override
	@Transactional
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
		return UserMapper.toUserDto(userRepository.save(user));
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
	@Transactional
	public void delete(Long userId) {
		userRepository.deleteById(userId);
		log.info("Удален пользователь id={}", userId);
	}

	private User findUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
	}

	private void checkEmailUsage(String email) {
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new ConflictException("Почта " + email + " уже используется");
		}
	}
}
