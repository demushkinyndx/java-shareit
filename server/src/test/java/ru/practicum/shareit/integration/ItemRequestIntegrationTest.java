package ru.practicum.shareit.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRequestIntegrationTest extends AbstractIntegrationTest {

	@Test
	@DisplayName("создаем запрос вещи и сохраняем в БД")
	void create() {
		UserDto requestor = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");

		ItemRequestDto created = createRequest(requestor.getId(), "Нужен usb тепловизор");

		ItemRequestDto fromDb = itemRequestService.getById(requestor.getId(), created.getId());
		assertEquals(created.getId(), fromDb.getId());
		assertEquals("Нужен usb тепловизор", fromDb.getDescription());
		assertTrue(fromDb.getItems().isEmpty());
	}

	@Test
	@DisplayName("возвращаем свои запросы из БД вместе с ответами")
	void getOwn() {
		UserDto requestor = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		ItemRequestDto request = createRequest(requestor.getId(), "Нужен usb тепловизор");
		itemService.create(owner.getId(), ItemDto.builder()
				.name("Тепловизор")
				.description("usb")
				.available(true)
				.requestId(request.getId())
				.build());

		List<ItemRequestDto> own = itemRequestService.getOwn(requestor.getId());

		assertEquals(1, own.size());
		assertEquals(request.getId(), own.getFirst().getId());
		assertEquals(1, own.getFirst().getItems().size());
		assertEquals("Тепловизор", own.getFirst().getItems().getFirst().getName());
		assertEquals(owner.getId(), own.getFirst().getItems().getFirst().getOwnerId());
	}

	@Test
	@DisplayName("возвращаем чужие запросы из БД")
	void getAll() {
		UserDto requestor = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");
		UserDto other = createUser("Ломас", "lomas@transhumanism.inc");
		ItemRequestDto request = createRequest(requestor.getId(), "Нужен мегаомметр");

		List<ItemRequestDto> forOther = itemRequestService.getAll(other.getId());
		List<ItemRequestDto> forRequestor = itemRequestService.getAll(requestor.getId());

		assertTrue(forOther.stream().anyMatch(r -> r.getId().equals(request.getId())));
		assertTrue(forRequestor.stream().noneMatch(r -> r.getId().equals(request.getId())));
	}

	@Test
	@DisplayName("возвращаем запрос по id из БД")
	void getById() {
		UserDto requestor = createUser("Галина Юзефович", "fish@roma-3.ru");
		UserDto viewer = createUser("Деньков", "denkoff@sinistra.uk");
		ItemRequestDto created = createRequest(requestor.getId(), "Нужен usb тепловизор");

		ItemRequestDto fromDb = itemRequestService.getById(viewer.getId(), created.getId());

		assertEquals(created.getId(), fromDb.getId());
		assertEquals("Нужен usb тепловизор", fromDb.getDescription());
	}
}
