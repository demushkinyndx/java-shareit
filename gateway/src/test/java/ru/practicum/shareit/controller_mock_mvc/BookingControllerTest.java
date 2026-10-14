package ru.practicum.shareit.controller_mock_mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.ItemController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import(ErrorHandler.class)
class BookingControllerTest {
	private static final String USER_ID_HEADER = ItemController.X_SHARER_USER_ID;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@MockBean
	private BookingClient bookingClient;

	@Test
	@DisplayName("POST /bookings создание бронирования")
	void create() throws Exception {
		Long bookerId = 2L;
		LocalDateTime start = LocalDateTime.of(2026, 10, 20, 10, 0);
		LocalDateTime end = LocalDateTime.of(2026, 10, 22, 10, 0);
		when(bookingClient.create(eq(bookerId), any(BookingCreateDto.class)))
				.thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
						"id", 1L,
						"start", start.toString(),
						"end", end.toString(),
						"status", "WAITING",
						"item", Map.of("id", 10L, "name", "Тепловизор usb"),
						"booker", Map.of("id", bookerId)
				)));

		mvc.perform(post("/bookings")
						.header(USER_ID_HEADER, bookerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(mapper.writeValueAsString(BookingCreateDto.builder()
								.itemId(10L)
								.start(start)
								.end(end)
								.build())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.status").value("WAITING"))
				.andExpect(jsonPath("$.item.name").value("Тепловизор usb"));

		verify(bookingClient).create(eq(bookerId), any(BookingCreateDto.class));
	}

	@Test
	@DisplayName("PATCH /bookings/{id} подтверждение бронирования")
	void approve() throws Exception {
		Long ownerId = 1L;
		Long bookingId = 1L;
		when(bookingClient.approve(ownerId, bookingId, true)).thenReturn(ResponseEntity.ok(Map.of(
				"id", bookingId,
				"status", "APPROVED",
				"item", Map.of("id", 10L, "name", "тепловизор usb"),
				"booker", Map.of("id", 2L)
		)));

		mvc.perform(patch("/bookings/{id}", bookingId)
						.header(USER_ID_HEADER, ownerId)
						.param("approved", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"));

		verify(bookingClient).approve(ownerId, bookingId, true);
	}

	@Test
	@DisplayName("GET /bookings/{id}")
	void getById() throws Exception {
		Long userId = 1L;
		Long bookingId = 1L;
		when(bookingClient.getById(userId, bookingId)).thenReturn(ResponseEntity.ok(Map.of(
				"id", bookingId,
				"status", "WAITING",
				"item", Map.of("id", 10L, "name", "Тепловизор usb"),
				"booker", Map.of("id", 2L)
		)));

		mvc.perform(get("/bookings/{id}", bookingId).header(USER_ID_HEADER, userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.status").value("WAITING"));

		verify(bookingClient).getById(userId, bookingId);
	}

	@Test
	@DisplayName("GET /bookings возвращает бронирования арендатора")
	void getByBooker() throws Exception {
		Long bookerId = 2L;
		when(bookingClient.getByBooker(bookerId, "ALL")).thenReturn(ResponseEntity.ok(List.of(Map.of(
				"id", 1L,
				"status", "WAITING",
				"item", Map.of("id", 10L, "name", "Тепловизор usb"),
				"booker", Map.of("id", bookerId)
		))));

		mvc.perform(get("/bookings").header(USER_ID_HEADER, bookerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1));

		verify(bookingClient).getByBooker(bookerId, "ALL");
	}

	@Test
	@DisplayName("GET /bookings/owner возвращает бронирования владельца")
	void getByOwner() throws Exception {
		Long ownerId = 1L;
		when(bookingClient.getByOwner(ownerId, "ALL")).thenReturn(ResponseEntity.ok(List.of(Map.of(
				"id", 1L,
				"status", "WAITING",
				"item", Map.of("id", 10L, "name", "Тепловизор usb"),
				"booker", Map.of("id", 2L)
		))));

		mvc.perform(get("/bookings/owner").header(USER_ID_HEADER, ownerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1));

		verify(bookingClient).getByOwner(ownerId, "ALL");
	}
}
