package ru.practicum.shareit.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserIntegrationTest extends AbstractIntegrationTest {

	@Test
	@DisplayName("создаем пользователя и сохраняем в БД")
	void create() {
		UserDto created = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");

		UserDto fromDb = userService.getById(created.getId());

		assertEquals(created.getId(), fromDb.getId());
		assertEquals("Маркус Зоргенфрей", fromDb.getName());
		assertTrue(fromDb.getEmail().contains("marcus"));
	}

	@Test
	@DisplayName("обновляем имя пользователя в БД")
	void update() {
		UserDto created = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");

		UserDto updated = userService.update(created.getId(), UserDto.builder()
				.name("Мардук Забаба Шам Иддин")
				.build());

		assertEquals("Мардук Забаба Шам Иддин", updated.getName());
		assertEquals("Мардук Забаба Шам Иддин", userService.getById(created.getId()).getName());
	}

	@Test
	@DisplayName("возвращаем пользователя по id из БД")
	void getById() {
		UserDto created = createUser("Ломас", "lomas@transhumanism.inc");

		UserDto fromDb = userService.getById(created.getId());

		assertEquals(created.getId(), fromDb.getId());
		assertEquals("Ломас", fromDb.getName());
	}

	@Test
	@DisplayName("возвращаем список пользователей из БД")
	void getAll() {
		UserDto galina = createUser("Галина Юзефович", "fish@roma-3.ru");
		UserDto denkov = createUser("Деньков", "denkoff@sinistra.uk");
		List<UserDto> expected = List.of(galina, denkov);
		List<UserDto> allUsersActual = userService.getAll();
		assertTrue(allUsersActual.size() >= 2);
		List<UserDto> actual = allUsersActual.stream()
				.filter(user -> user.getId().equals(galina.getId()) || user.getId().equals(denkov.getId()))
				.toList();

		assertEquals(expected, actual);
	}

	@Test
	@DisplayName("удаляем пользователя из БД")
	void delete() {
		UserDto created = createUser("Деньков", "denkoff@sinistra.uk");

		userService.delete(created.getId());

		assertThrows(NotFoundException.class, () -> userService.getById(created.getId()));
	}
}
