package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemRequestApiTest {
	private static final String USER_ID_HEADER = "X-Sharer-User-Id";
	private static final AtomicInteger EMAIL_COUNTER = new AtomicInteger();

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Test
	@DisplayName("создаем запрос вещи и возвращаем его id")
	void createRequest() throws Exception {
		long userId = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");

		mvc.perform(post("/requests")
						.header(USER_ID_HEADER, userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"description\":\"Нужен usb тепловизор\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.description").value("Нужен usb тепловизор"))
				.andExpect(jsonPath("$.created").exists())
				.andExpect(jsonPath("$.items").isArray());
	}

	@Test
	@DisplayName("возвращаем свои запросы и чужие отдельно")
	void getOwnAndAll() throws Exception {
		long requestorId = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");
		long otherId = createUser("Ломас", "lomas@transhumanism.inc");
		long requestId = createRequest(requestorId, "Нужен термоанемометр");

		mvc.perform(get("/requests").header(USER_ID_HEADER, requestorId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(requestId));

		mvc.perform(get("/requests/all").header(USER_ID_HEADER, otherId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id == " + requestId + ")]").exists());

		mvc.perform(get("/requests/all").header(USER_ID_HEADER, requestorId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id == " + requestId + ")]").doesNotExist());
	}

	@Test
	@DisplayName("возвращаем запрос по id")
	void getById() throws Exception {
		long requestorId = createUser("Галина Юзефович", "fish@roma-3.ru");
		long viewerId = createUser("Деньков", "denkoff@sinistra.uk");
		long requestId = createRequest(requestorId, "Нужен usb тепловизор");

		mvc.perform(get("/requests/{id}", requestId).header(USER_ID_HEADER, viewerId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(requestId))
				.andExpect(jsonPath("$.description").value("Нужен usb тепловизор"))
				.andExpect(jsonPath("$.items").isEmpty());
	}

	@Test
	@DisplayName("создаем вещь с requestId и без него")
	void createItemWithAndWithoutRequestId() throws Exception {
		long requestorId = createUser("Маркус Зоргенфрей", "marcus@transhumanism.inc");
		long ownerId = createUser("Порфирий Петрович", "porfiry@roma-3.ru");
		long requestId = createRequest(requestorId, "Нужен usb тепловизор");

		mvc.perform(post("/items")
						.header(USER_ID_HEADER, ownerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Термоанемометр\",\"description\":\"Описание\",\"available\":true}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.requestId").doesNotExist());

		mvc.perform(post("/items")
						.header(USER_ID_HEADER, ownerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Тепловизор\",\"description\":\"USB\",\"available\":true,"
								+ "\"requestId\":" + requestId + "}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Тепловизор"))
				.andExpect(jsonPath("$.requestId").value(requestId));

		mvc.perform(get("/requests/{id}", requestId).header(USER_ID_HEADER, requestorId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].name").value("Тепловизор"))
				.andExpect(jsonPath("$.items[0].ownerId").value(ownerId));
	}

	@Test
	@DisplayName("не создаем вещь с несуществующим requestId")
	void createItemWithUnknownRequestId() throws Exception {
		long ownerId = createUser("Порфирий Петрович", "porfiry@roma-3.ru");

		mvc.perform(post("/items")
						.header(USER_ID_HEADER, ownerId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"Тепловизор\",\"description\":\"USB\",\"available\":true,\"requestId\":99999}"))
				.andExpect(status().isNotFound());
	}

	private long createUser(String name, String email) throws Exception {
		String uniqueEmail = email.replace("@",
				"+" + EMAIL_COUNTER.incrementAndGet() + "-" + UUID.randomUUID() + "@");
		String response = mvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"" + name + "\",\"email\":\"" + uniqueEmail + "\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return mapper.readTree(response).get("id").asLong();
	}

	private long createRequest(long userId, String description) throws Exception {
		String response = mvc.perform(post("/requests")
						.header(USER_ID_HEADER, userId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"description\":\"" + description + "\"}"))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return mapper.readTree(response).get("id").asLong();
	}
}
