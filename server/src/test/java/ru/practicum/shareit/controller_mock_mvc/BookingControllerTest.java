package ru.practicum.shareit.controller_mock_mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingServiceInterface;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.util.HeaderConstants;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

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
	private static final String USER_ID_HEADER = HeaderConstants.X_SHARER_USER_ID;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@MockBean
	private BookingServiceInterface bookingService;

	@Test
	@DisplayName("POST /bookings создание бронирования")
	void create() throws Exception {
		Long bookerId = 2L;
		LocalDateTime start = LocalDateTime.now().plusDays(1);
		LocalDateTime end = start.plusDays(2);
		BookingDto response = BookingDto.builder()
				.id(1L)
				.start(start)
				.end(end)
				.status(BookingStatus.WAITING)
				.item(ItemDto.builder().id(10L).name("Тепловизор usb").build())
				.booker(UserDto.builder().id(bookerId).build())
				.build();
		when(bookingService.create(eq(bookerId), any(BookingCreateDto.class))).thenReturn(response);

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

		verify(bookingService).create(eq(bookerId), any(BookingCreateDto.class));
	}

	@Test
	@DisplayName("PATCH /bookings/{id} подтверждение бронирования")
	void approve() throws Exception {
		Long ownerId = 1L;
		Long bookingId = 1L;
		BookingDto response = BookingDto.builder()
				.id(bookingId)
				.status(BookingStatus.APPROVED)
				.item(ItemDto.builder().id(10L).name("тепловизор usb").build())
				.booker(UserDto.builder().id(2L).build())
				.build();
		when(bookingService.approve(ownerId, bookingId, true)).thenReturn(response);

		mvc.perform(patch("/bookings/{id}", bookingId)
						.header(USER_ID_HEADER, ownerId)
						.param("approved", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("APPROVED"));

		verify(bookingService).approve(ownerId, bookingId, true);
	}

	@Test
	@DisplayName("GET /bookings/{id}")
	void getById() throws Exception {
		Long userId = 1L;
		Long bookingId = 1L;
		when(bookingService.getById(userId, bookingId)).thenReturn(BookingDto.builder()
				.id(bookingId)
				.status(BookingStatus.WAITING)
				.item(ItemDto.builder().id(10L).name("Тепловизор usb").build())
				.booker(UserDto.builder().id(2L).build())
				.build());

		mvc.perform(get("/bookings/{id}", bookingId).header(USER_ID_HEADER, userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.status").value("WAITING"));

		verify(bookingService).getById(userId, bookingId);
	}

	@Test
	@DisplayName("GET /bookings возвращает бронирования арендатора")
	void getByBooker() throws Exception {
		Long bookerId = 2L;
		when(bookingService.getByBooker(bookerId, BookingState.ALL)).thenReturn(List.of(BookingDto.builder()
				.id(1L)
				.status(BookingStatus.WAITING)
				.item(ItemDto.builder().id(10L).name("Тепловизор usb").build())
				.booker(UserDto.builder().id(bookerId).build())
				.build()));

		mvc.perform(get("/bookings").header(USER_ID_HEADER, bookerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1));

		verify(bookingService).getByBooker(bookerId, BookingState.ALL);
	}

	@Test
	@DisplayName("GET /bookings/owner возвращает бронирования владельца")
	void getByOwner() throws Exception {
		Long ownerId = 1L;
		when(bookingService.getByOwner(ownerId, BookingState.ALL)).thenReturn(List.of(BookingDto.builder()
				.id(1L)
				.status(BookingStatus.WAITING)
				.item(ItemDto.builder().id(10L).name("Тепловизор usb").build())
				.booker(UserDto.builder().id(2L).build())
				.build()));

		mvc.perform(get("/bookings/owner").header(USER_ID_HEADER, ownerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(1));

		verify(bookingService).getByOwner(ownerId, BookingState.ALL);
	}
}
