package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShareItTests {
	private static final String USER_ID_HEADER = "X-Sharer-User-Id";
	private static final AtomicInteger EMAIL_COUNTER = new AtomicInteger();

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Test
	@DisplayName("создаем юзера и возвращаем его id")
	void createUser() throws Exception {
		mvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Маркус Зоргенфрей\",\"email\":\"marcus@transhumanism.inc\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.email").value("marcus@transhumanism.inc"));
	}

	@Test
	@DisplayName("обновляем имя пользователя")
	void updateUser() throws Exception {
		long userId = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");

		mvc.perform(patch("/users/{id}", userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Мардук Забаба Шам Иддин\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Мардук Забаба Шам Иддин"));
	}

	@Test
	@DisplayName("возвращаем пользователя по id")
	void getUserById() throws Exception {
		long userId = createUser("Ломас", "lomas@transhumanism.inc");

		mvc.perform(get("/users/{id}", userId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(userId));
	}

	@Test
	@DisplayName("возвращаем список пользователей")
	void getAllUsers() throws Exception {
		createUser("Галина Юзефович", "fish@roma-3.ru");

		mvc.perform(get("/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isNotEmpty());
	}

	@Test
	@DisplayName("удаляем пользователя")
	void deleteUser() throws Exception {
		long userId = createUser("Деньков", "denkoff@sinistra.uk");

		mvc.perform(delete("/users/{id}", userId))
				.andExpect(status().isOk());
		mvc.perform(get("/users/{id}", userId))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("создаем вещь от имени хозяина из заголовка")
	void createItem() throws Exception {
		long ownerId = createUser("Порфирий Петрович", "porfiry@roma-3.ru");

		mvc.perform(post("/items")
						.header(USER_ID_HEADER, ownerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Дрель\",\"description\":\"Мощная\",\"available\":true}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name").value("Дрель"));
	}

	@Test
	@DisplayName("меняем доступность вещи владельцем")
	void updateItem() throws Exception {
		long ownerId = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		long itemId = createItem(ownerId, "Дрель");

		mvc.perform(patch("/items/{id}", itemId)
						.header(USER_ID_HEADER, ownerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"available\":false}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(false));
	}

	@Test
	@DisplayName("возвращаем по itemId")
	void getItemById() throws Exception {
		long itemId = createItem(createUser("Порфирий Петрович", "porfiry@roma-3.ru"), "Дрель");

		mvc.perform(get("/items/{id}", itemId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(itemId));
	}

	@Test
	@DisplayName("возвращаем только вещи владельца")
	void getItemsByOwner() throws Exception {
		long ownerId = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		long itemId = createItem(ownerId, "Дрель");

		mvc.perform(get("/items").header(USER_ID_HEADER, ownerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(itemId));
	}

	@Test
	@DisplayName("ищем вещь по названию")
	void searchItems() throws Exception {
		long itemId = createItem(createUser("Порфирий Петрович", "porfiry@roma-3.ru"), "Уникальный-граммофон");

		mvc.perform(get("/items/search").param("text", "уникальный-ГРАММОФОН"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(itemId));
	}

	private long createUser(String name, String email) throws Exception {
		String uniqueEmail = email.replace("@", "+" + EMAIL_COUNTER.incrementAndGet() + "@");
		String response = mvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"" + name + "\",\"email\":\"" + uniqueEmail + "\"}"))
				.andReturn().getResponse().getContentAsString();
		return mapper.readTree(response).get("id").asLong();
	}

	private long createItem(long ownerId, String name) throws Exception {
		String response = mvc.perform(post("/items")
						.header(USER_ID_HEADER, ownerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"" + name + "\",\"description\":\"Описание\",\"available\":true}"))
				.andReturn().getResponse().getContentAsString();
		return mapper.readTree(response).get("id").asLong();
	}
}
