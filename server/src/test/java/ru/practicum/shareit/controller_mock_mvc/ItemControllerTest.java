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
import ru.practicum.shareit.item.ItemServiceInterface;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.util.HeaderConstants;

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

@WebMvcTest(ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerTest {
	private static final String USER_ID_HEADER = HeaderConstants.X_SHARER_USER_ID;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ItemServiceInterface itemService;

	@Test
	@DisplayName("POST /items создание вещи без requestId")
	void createWithoutRequestId() throws Exception {
		Long ownerId = 1L;
		ItemDto response = ItemDto.builder()
				.id(10L)
				.name("Тепловизор usb")
				.description("USB")
				.available(true)
				.build();
		when(itemService.create(eq(ownerId), any(ItemDto.class))).thenReturn(response);

		mvc.perform(post("/items")
						.header(USER_ID_HEADER, ownerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Тепловизор usb\",\"description\":\"USB\",\"available\":true}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.name").value("Тепловизор usb"))
				.andExpect(jsonPath("$.requestId").doesNotExist());

		verify(itemService).create(eq(ownerId), any(ItemDto.class));
	}

	@Test
	@DisplayName("POST /items создание вещи с requestId")
	void createWithRequestId() throws Exception {
		Long ownerId = 1L;
		Long requestId = 5L;
		ItemDto response = ItemDto.builder()
				.id(10L)
				.name("Тепловизор usb")
				.description("USB")
				.available(true)
				.requestId(requestId)
				.build();
		when(itemService.create(eq(ownerId), any(ItemDto.class))).thenReturn(response);

		mvc.perform(post("/items")
						.header(USER_ID_HEADER, ownerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Тепловизор usb\",\"description\":\"USB\",\"available\":true,\"requestId\":5}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.requestId").value(5));

		verify(itemService).create(eq(ownerId), any(ItemDto.class));
	}

	@Test
	@DisplayName("PATCH /items/{id} обновление вещи")
	void update() throws Exception {
		Long ownerId = 1L;
		Long itemId = 10L;
		ItemDto response = ItemDto.builder()
				.id(itemId)
				.name("Тепловизор usb")
				.description("USB")
				.available(false)
				.build();
		when(itemService.update(eq(ownerId), eq(itemId), any(ItemDto.class))).thenReturn(response);

		mvc.perform(patch("/items/{id}", itemId)
						.header(USER_ID_HEADER, ownerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"available\":false}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(false));

		verify(itemService).update(eq(ownerId), eq(itemId), any(ItemDto.class));
	}

	@Test
	@DisplayName("GET /items/{id} возвращает вещь")
	void getById() throws Exception {
		Long userId = 1L;
		Long itemId = 10L;
		when(itemService.getById(userId, itemId)).thenReturn(ItemDto.builder()
				.id(itemId)
				.name("Тепловизор usb")
				.description("USB")
				.available(true)
				.build());

		mvc.perform(get("/items/{id}", itemId).header(USER_ID_HEADER, userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.name").value("Тепловизор usb"));

		verify(itemService).getById(userId, itemId);
	}

	@Test
	@DisplayName("GET /items вещи владельца")
	void getByOwner() throws Exception {
		Long ownerId = 1L;
		when(itemService.getByOwner(ownerId)).thenReturn(List.of(ItemDto.builder()
				.id(10L)
				.name("Тепловизор usb")
				.description("USB")
				.available(true)
				.build()));

		mvc.perform(get("/items").header(USER_ID_HEADER, ownerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("Тепловизор usb"));

		verify(itemService).getByOwner(ownerId);
	}

	@Test
	@DisplayName("GET /items/search")
	void search() throws Exception {
		when(itemService.search("узо")).thenReturn(List.of(ItemDto.builder()
				.id(10L)
				.name("новое узо")
				.description("Описание")
				.available(true)
				.build()));

		mvc.perform(get("/items/search").param("text", "узо"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("новое узо"));

		verify(itemService).search("узо");
	}

	@Test
	@DisplayName("POST /items/{id}/comment")
	void addComment() throws Exception {
		Long userId = 2L;
		Long itemId = 10L;
		CommentDto response = CommentDto.builder()
				.id(1L)
				.text("Отличный тепловизор usb")
				.authorName("Маркус Зоргенфрей")
				.created(LocalDateTime.of(2026, 10, 15, 12, 0))
				.build();
		when(itemService.addComment(eq(userId), eq(itemId), any(CommentDto.class))).thenReturn(response);

		mvc.perform(post("/items/{id}/comment", itemId)
						.header(USER_ID_HEADER, userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"text\":\"Отличный тепловизор usb\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.text").value("Отличный тепловизор usb"))
				.andExpect(jsonPath("$.authorName").value("Маркус Зоргенфрей"));

		verify(itemService).addComment(eq(userId), eq(itemId), any(CommentDto.class));
	}
}
