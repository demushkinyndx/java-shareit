package ru.practicum.shareit.controller_mock_mvc;

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
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.LinkedHashMap;
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

@WebMvcTest(ItemController.class)
@Import(ErrorHandler.class)
class ItemControllerTest {
	private static final String USER_ID_HEADER = ItemController.X_SHARER_USER_ID;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ItemClient itemClient;

	@Test
	@DisplayName("POST /items создание вещи без requestId")
	void createWithoutRequestId() throws Exception {
		Long ownerId = 1L;
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("id", 10L);
		response.put("name", "Тепловизор usb");
		response.put("description", "USB");
		response.put("available", true);
		when(itemClient.create(eq(ownerId), any(ItemDto.class)))
				.thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(response));

		mvc.perform(post("/items")
						.header(USER_ID_HEADER, ownerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Тепловизор usb\",\"description\":\"USB\",\"available\":true}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.name").value("Тепловизор usb"))
				.andExpect(jsonPath("$.requestId").doesNotExist());

		verify(itemClient).create(eq(ownerId), any(ItemDto.class));
	}

	@Test
	@DisplayName("POST /items создание вещи с requestId")
	void createWithRequestId() throws Exception {
		Long ownerId = 1L;
		when(itemClient.create(eq(ownerId), any(ItemDto.class)))
				.thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
						"id", 10L,
						"name", "Тепловизор usb",
						"description", "USB",
						"available", true,
						"requestId", 5L
				)));

		mvc.perform(post("/items")
						.header(USER_ID_HEADER, ownerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Тепловизор usb\",\"description\":\"USB\",\"available\":true,\"requestId\":5}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.requestId").value(5));

		verify(itemClient).create(eq(ownerId), any(ItemDto.class));
	}

	@Test
	@DisplayName("PATCH /items/{id} обновление вещи")
	void update() throws Exception {
		Long ownerId = 1L;
		Long itemId = 10L;
		when(itemClient.update(eq(ownerId), eq(itemId), any(ItemDto.class)))
				.thenReturn(ResponseEntity.ok(Map.of(
						"id", itemId,
						"name", "Тепловизор usb",
						"description", "USB",
						"available", false
				)));

		mvc.perform(patch("/items/{id}", itemId)
						.header(USER_ID_HEADER, ownerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"available\":false}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(false));

		verify(itemClient).update(eq(ownerId), eq(itemId), any(ItemDto.class));
	}

	@Test
	@DisplayName("GET /items/{id} возвращает вещь")
	void getById() throws Exception {
		Long userId = 1L;
		Long itemId = 10L;
		when(itemClient.getById(userId, itemId)).thenReturn(ResponseEntity.ok(Map.of(
				"id", itemId,
				"name", "Тепловизор usb",
				"description", "USB",
				"available", true
		)));

		mvc.perform(get("/items/{id}", itemId).header(USER_ID_HEADER, userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.name").value("Тепловизор usb"));

		verify(itemClient).getById(userId, itemId);
	}

	@Test
	@DisplayName("GET /items вещи владельца")
	void getByOwner() throws Exception {
		Long ownerId = 1L;
		when(itemClient.getByOwner(ownerId)).thenReturn(ResponseEntity.ok(List.of(Map.of(
				"id", 10L,
				"name", "Тепловизор usb",
				"description", "USB",
				"available", true
		))));

		mvc.perform(get("/items").header(USER_ID_HEADER, ownerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("Тепловизор usb"));

		verify(itemClient).getByOwner(ownerId);
	}

	@Test
	@DisplayName("GET /items/search")
	void search() throws Exception {
		when(itemClient.search("узо")).thenReturn(ResponseEntity.ok(List.of(Map.of(
				"id", 10L,
				"name", "новое узо",
				"description", "Описание",
				"available", true
		))));

		mvc.perform(get("/items/search").param("text", "узо"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("новое узо"));

		verify(itemClient).search("узо");
	}

	@Test
	@DisplayName("POST /items/{id}/comment")
	void addComment() throws Exception {
		Long userId = 2L;
		Long itemId = 10L;
		when(itemClient.addComment(eq(userId), eq(itemId), any(CommentDto.class)))
				.thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
						"id", 1L,
						"text", "Отличный тепловизор usb",
						"authorName", "Маркус Зоргенфрей",
						"created", "2026-10-15T12:00:00"
				)));

		mvc.perform(post("/items/{id}/comment", itemId)
						.header(USER_ID_HEADER, userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"text\":\"Отличный тепловизор usb\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.text").value("Отличный тепловизор usb"))
				.andExpect(jsonPath("$.authorName").value("Маркус Зоргенфрей"));

		verify(itemClient).addComment(eq(userId), eq(itemId), any(CommentDto.class));
	}
}
