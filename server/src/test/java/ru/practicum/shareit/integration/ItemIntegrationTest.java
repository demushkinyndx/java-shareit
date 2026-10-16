package ru.practicum.shareit.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemIntegrationTest extends AbstractIntegrationTest {

	@Test
	@DisplayName("создаем вещь и сохраняем в БД")
	void create() {
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");

		ItemDto created = createItem(owner.getId(), "Тепловизор");

		ItemDto fromDb = itemService.getById(owner.getId(), created.getId());
		assertEquals("Тепловизор", fromDb.getName());
		assertEquals(true, fromDb.getAvailable());
	}

	@Test
	@DisplayName("создаем вещь в ответ на запрос")
	void createWithRequestId() {
		UserDto requestor = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		ItemRequestDto request = createRequest(requestor.getId(), "Нужен USB тепловизор");

		ItemDto created = itemService.create(owner.getId(), ItemDto.builder()
				.name("Тепловизор")
				.description("USB")
				.available(true)
				.requestId(request.getId())
				.build());

		assertEquals(request.getId(), created.getRequestId());
		ItemRequestDto fromDb = itemRequestService.getById(requestor.getId(), request.getId());
		assertEquals(1, fromDb.getItems().size());
		assertEquals("Тепловизор", fromDb.getItems().getFirst().getName());
	}

	@Test
	@DisplayName("обновляем доступность вещи в БД")
	void update() {
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		ItemDto created = createItem(owner.getId(), "Тепловизор");

		ItemDto updated = itemService.update(owner.getId(), created.getId(), ItemDto.builder()
				.available(false)
				.build());

		assertFalse(updated.getAvailable());
		assertFalse(itemService.getById(owner.getId(), created.getId()).getAvailable());
	}

	@Test
	@DisplayName("возвращаем вещь по id из БД")
	void getById() {
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		ItemDto created = createItem(owner.getId(), "Тепловизор");

		ItemDto fromDb = itemService.getById(owner.getId(), created.getId());

		assertEquals(created.getId(), fromDb.getId());
		assertEquals("Тепловизор", fromDb.getName());

		ItemDto created2 = createItem(owner.getId(), "Телевизор");
		ItemDto fromDb2 = itemService.getById(owner.getId(), created2.getId());
		assertNotEquals("Тепловизор", fromDb2.getName());
	}

	@Test
	@DisplayName("возвращаем вещи владельца из БД")
	void getByOwner() {
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		ItemDto item = createItem(owner.getId(), "Тепловизор");

		List<ItemDto> items = itemService.getByOwner(owner.getId());

		assertEquals(1, items.size());
		assertEquals(item.getId(), items.getFirst().getId());
	}

	@Test
	@DisplayName("ищем вещи по тексту в БД")
	void search() {
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		ItemDto item = createItem(owner.getId(), "Мегаомметр");

		List<ItemDto> found = itemService.search("МЕГАОММЕТР");

		assertEquals(1, found.size());
		assertEquals(item.getId(), found.getFirst().getId());
	}

	@Test
	@DisplayName("добавляем комментарий после завершенной аренды")
	void addComment() {
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		UserDto booker = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");
		ItemDto item = createItem(owner.getId(), "Тепловизор");
		createPastApprovedBooking(booker.getId(), item.getId());

		CommentDto comment = itemService.addComment(booker.getId(), item.getId(), CommentDto.builder()
				.text("Удобный тепловизор")
				.build());

		assertNotNull(comment.getId());
		assertEquals("Удобный тепловизор", comment.getText());
		assertEquals("Маркус Зоргенфрей", comment.getAuthorName());
		assertEquals(1, itemService.getById(owner.getId(), item.getId()).getComments().size());
	}
}
