package ru.practicum.shareit.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingIntegrationTest extends AbstractIntegrationTest {

	@Test
	@DisplayName("создаем бронирование и сохраняем в БД")
	void create() {
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		UserDto booker = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");
		ItemDto item = createItem(owner.getId(), "Дрель");
		LocalDateTime start = LocalDateTime.now().plusDays(1);
		LocalDateTime end = start.plusDays(2);

		BookingDto created = createBooking(booker.getId(), item.getId(), start, end);

		BookingDto fromDb = bookingService.getById(booker.getId(), created.getId());
		Assertions.assertEquals(BookingStatus.WAITING, fromDb.getStatus());
		assertEquals(item.getId(), fromDb.getItem().getId());
		assertEquals(booker.getId(), fromDb.getBooker().getId());
	}

	@Test
	@DisplayName("подтверждаем бронирование владельцем")
	void approve() {
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		UserDto booker = createUser("Ломас", "lomas@transhumanism.inc");
		ItemDto item = createItem(owner.getId(), "Дрель");
		BookingDto created = createBooking(
				booker.getId(),
				item.getId(),
				LocalDateTime.now().plusDays(1),
				LocalDateTime.now().plusDays(3));

		BookingDto approved = bookingService.approve(owner.getId(), created.getId(), true);

		assertEquals(BookingStatus.APPROVED, approved.getStatus());
		assertEquals(BookingStatus.APPROVED, bookingService.getById(owner.getId(), created.getId()).getStatus());
	}

	@Test
	@DisplayName("возвращаем бронирование по id из БД")
	void getById() {
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		UserDto booker = createUser("Галина Юзефович", "fish@roma-3.ru");
		ItemDto item = createItem(owner.getId(), "Дрель");
		BookingDto created = createBooking(
				booker.getId(),
				item.getId(),
				LocalDateTime.now().plusDays(1),
				LocalDateTime.now().plusDays(2));

		BookingDto fromDb = bookingService.getById(owner.getId(), created.getId());

		assertEquals(created.getId(), fromDb.getId());
		assertEquals(BookingStatus.WAITING, fromDb.getStatus());
	}

	@Test
	@DisplayName("возвращаем бронирования арендатора из БД")
	void getByBooker() {
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		UserDto booker = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");
		ItemDto item = createItem(owner.getId(), "Дрель");
		BookingDto created = createBooking(
				booker.getId(),
				item.getId(),
				LocalDateTime.now().plusDays(1),
				LocalDateTime.now().plusDays(2));

		List<BookingDto> bookings = bookingService.getByBooker(booker.getId(), BookingState.ALL);

		assertEquals(1, bookings.size());
		assertEquals(created.getId(), bookings.getFirst().getId());
	}

	@Test
	@DisplayName("возвращаем бронирования владельца из БД")
	void getByOwner() {
		UserDto owner = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		UserDto booker = createUser("Деньков", "denkoff@sinistra.uk");
		ItemDto item = createItem(owner.getId(), "Тепловизор");
		BookingDto created = createBooking(
				booker.getId(),
				item.getId(),
				LocalDateTime.now().plusDays(1),
				LocalDateTime.now().plusDays(2));

		List<BookingDto> bookings = bookingService.getByOwner(owner.getId(), BookingState.ALL);

		assertEquals(1, bookings.size());
		assertEquals(created.getId(), bookings.getFirst().getId());
	}
}
