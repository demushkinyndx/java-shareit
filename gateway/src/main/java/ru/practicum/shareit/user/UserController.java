package ru.practicum.shareit.user;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.validation.OnUpdate;

@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserClient userClient;

	@PostMapping
	public ResponseEntity<Object> create(@RequestBody @Validated(OnCreate.class) UserDto userDto) {
		return userClient.create(userDto);
	}

	@PatchMapping("/{userId}")
	public ResponseEntity<Object> update(@PathVariable @NotNull(message = "Идентификатор пользователя обязателен") Long userId,
										 @RequestBody @Validated(OnUpdate.class) UserDto userDto) {
		return userClient.update(userId, userDto);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<Object> getById(@PathVariable @NotNull(message = "Идентификатор пользователя обязателен") Long userId) {
		return userClient.getById(userId);
	}

	@GetMapping
	public ResponseEntity<Object> getAll() {
		return userClient.getAll();
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Object> delete(@PathVariable @NotNull(message = "Идентификатор пользователя обязателен") Long userId) {
		return userClient.delete(userId);
	}
}
