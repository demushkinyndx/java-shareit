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
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserServiceInterface;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

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
	private UserServiceInterface userService;

	@Test
	@DisplayName("POST /users")
	void create() throws Exception {
		UserDto response = UserDto.builder()
				.id(1L)
				.name("Маркус Зоргенфрей")
				.email("marcus@transhumanism.inc")
				.build();
		when(userService.create(any(UserDto.class))).thenReturn(response);

		mvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Маркус Зоргенфрей\",\"email\":\"marcus@transhumanism.inc\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Маркус Зоргенфрей"))
				.andExpect(jsonPath("$.email").value("marcus@transhumanism.inc"));

		verify(userService).create(any(UserDto.class));
	}

	@Test
	@DisplayName("PATCH /users/{id}")
	void update() throws Exception {
		Long userId = 1L;
		UserDto response = UserDto.builder()
				.id(userId)
				.name("Мардук Забаба Шам Иддин")
				.email("marcus@transhumanism.inc")
				.build();
		when(userService.update(eq(userId), any(UserDto.class))).thenReturn(response);

		mvc.perform(patch("/users/{id}", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Мардук Забаба Шам Иддин\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Мардук Забаба Шам Иддин"));

		verify(userService).update(eq(userId), any(UserDto.class));
	}

	@Test
	@DisplayName("GET /users/{id}")
	void getById() throws Exception {
		Long userId = 1L;
		when(userService.getById(userId)).thenReturn(UserDto.builder()
				.id(userId)
				.name("Ломас")
				.email("lomas@transhumanism.inc")
				.build());

		mvc.perform(get("/users/{id}", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Ломас"));

		verify(userService).getById(userId);
	}

	@Test
	@DisplayName("GET /users")
	void getAll() throws Exception {
		when(userService.getAll()).thenReturn(List.of(
				UserDto.builder().id(1L).name("Галина Юзефович").email("fish@roma-3.ru").build(),
				UserDto.builder().id(1L).name("Деньков").email("denkoff@sinistra.uk").build()
		));

		mvc.perform(get("/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].name").value("Галина Юзефович"))
				.andExpect(jsonPath("$[1].name").value("Деньков"));

		verify(userService).getAll();
	}

	@Test
	@DisplayName("DELETE /users/{id}")
	void deleteUser() throws Exception {
		Long userId = 1L;

		mvc.perform(delete("/users/{id}", userId))
				.andExpect(status().isOk());

		verify(userService).delete(userId);
	}
}
