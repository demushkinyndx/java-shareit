package ru.practicum.shareit.controller_mock_mvc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestServiceInterface;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@Import(ErrorHandler.class)
class ItemRequestControllerTest {
	private static final String USER_ID_HEADER = ItemController.X_SHARER_USER_ID;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ItemRequestServiceInterface itemRequestService;

	@Test
	@DisplayName("POST /requests создание запроса вещи")
	void create() throws Exception {
		Long userId = 1L;
		ItemRequestDto response = ItemRequestDto.builder()
				.id(10L)
				.description("Нужен тепловизор usb")
				.created(LocalDateTime.of(2026, 10, 15, 12, 0))
				.items(List.of())
				.build();
		when(itemRequestService.create(eq(userId), any(ItemRequestCreateDto.class))).thenReturn(response);

		mvc.perform(post("/requests")
						.header(USER_ID_HEADER, userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"description\":\"Нужен тепловизор usb\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.description").value("Нужен тепловизор usb"))
				.andExpect(jsonPath("$.items").isArray());

		verify(itemRequestService).create(eq(userId), any(ItemRequestCreateDto.class));
	}

	@Test
	@DisplayName("GET /requests свои запросы")
	void getOwn() throws Exception {
		Long userId =1L;
		when(itemRequestService.getOwn(userId)).thenReturn(List.of(ItemRequestDto.builder()
				.id(10L)
				.description("Нужен тепловизор usb")
				.created(LocalDateTime.of(2026, 10, 15, 12, 0))
				.items(List.of(ItemRequestItemDto.builder()
						.id(100L)
						.name("тепловизор usb")
						.ownerId(2L)
						.build()))
				.build()));

		mvc.perform(get("/requests").header(USER_ID_HEADER, userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(10))
				.andExpect(jsonPath("$[0].items[0].name").value("тепловизор usb"))
				.andExpect(jsonPath("$[0].items[0].ownerId").value(2));

		verify(itemRequestService).getOwn(userId);
	}

	@Test
	@DisplayName("GET /requests/all чужие запросы")
	void getAll() throws Exception {
		Long userId = 2L;
		when(itemRequestService.getAll(userId)).thenReturn(List.of(ItemRequestDto.builder()
				.id(10L)
				.description("Нужно новое узо")
				.created(LocalDateTime.of(2026, 10, 15, 12, 0))
				.items(List.of())
				.build()));

		mvc.perform(get("/requests/all").header(USER_ID_HEADER, userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].description").value("Нужно новое узо"));

		verify(itemRequestService).getAll(userId);
	}

	@Test
	@DisplayName("GET /requests/{id}")
	void getById() throws Exception {
		Long userId = 2L;
		Long requestId = 10L;
		when(itemRequestService.getById(userId, requestId)).thenReturn(ItemRequestDto.builder()
				.id(requestId)
				.description("Нужен тепловизор usb")
				.created(LocalDateTime.of(2026, 10, 15, 12, 0))
				.items(List.of())
				.build());

		mvc.perform(get("/requests/{id}", requestId).header(USER_ID_HEADER, userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.description").value("Нужен тепловизор usb"));

		verify(itemRequestService).getById(userId, requestId);
	}
}
