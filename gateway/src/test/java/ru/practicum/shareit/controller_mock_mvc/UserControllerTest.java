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
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(ErrorHandler.class)
class UserControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private UserClient userClient;

	@Test
	@DisplayName("POST /users")
	void create() throws Exception {
		when(userClient.create(any(UserDto.class))).thenReturn(ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of(
						"id", 1L,
						"name", "Маркус Зоргенфрей",
						"email", "marcus@transhumanism.inc"
				)));

		mvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Маркус Зоргенфрей\",\"email\":\"marcus@transhumanism.inc\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Маркус Зоргенфрей"))
				.andExpect(jsonPath("$.email").value("marcus@transhumanism.inc"));

		verify(userClient).create(any(UserDto.class));
	}

	@Test
	@DisplayName("PATCH /users/{id}")
	void update() throws Exception {
		Long userId = 1L;
		when(userClient.update(eq(userId), any(UserDto.class))).thenReturn(ResponseEntity.ok(Map.of(
				"id", userId,
				"name", "Мардук Забаба Шам Иддин",
				"email", "marcus@transhumanism.inc"
		)));

		mvc.perform(patch("/users/{id}", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Мардук Забаба Шам Иддин\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Мардук Забаба Шам Иддин"));

		verify(userClient).update(eq(userId), any(UserDto.class));
	}

	@Test
	@DisplayName("GET /users/{id}")
	void getById() throws Exception {
		Long userId = 1L;
		when(userClient.getById(userId)).thenReturn(ResponseEntity.ok(Map.of(
				"id", userId,
				"name", "Ломас",
				"email", "lomas@transhumanism.inc"
		)));

		mvc.perform(get("/users/{id}", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Ломас"));

		verify(userClient).getById(userId);
	}

	@Test
	@DisplayName("GET /users")
	void getAll() throws Exception {
		when(userClient.getAll()).thenReturn(ResponseEntity.ok(List.of(
				Map.of("id", 1L, "name", "Галина Юзефович", "email", "fish@roma-3.ru"),
				Map.of("id", 2L, "name", "Деньков", "email", "denkoff@sinistra.uk")
		)));

		mvc.perform(get("/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].name").value("Галина Юзефович"))
				.andExpect(jsonPath("$[1].name").value("Деньков"));

		verify(userClient).getAll();
	}

	@Test
	@DisplayName("DELETE /users/{id}")
	void deleteUser() throws Exception {
		Long userId = 1L;
		when(userClient.delete(userId)).thenReturn(ResponseEntity.ok().build());

		mvc.perform(delete("/users/{id}", userId))
				.andExpect(status().isOk());

		verify(userClient).delete(userId);
	}
}
