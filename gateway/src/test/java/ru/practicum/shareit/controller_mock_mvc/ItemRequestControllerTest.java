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
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.util.HeaderConstants;

import java.util.List;
import java.util.Map;

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
    private static final String USER_ID_HEADER = HeaderConstants.X_SHARER_USER_ID;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    @DisplayName("POST /requests создание запроса вещи")
    void create() throws Exception {
        Long userId = 1L;
        when(itemRequestClient.create(eq(userId), any(ItemRequestCreateDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                        "id", 10L,
                        "description", "Нужен тепловизор usb",
                        "created", "2026-10-15T12:00:00",
                        "items", List.of()
                )));

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Нужен тепловизор usb\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("Нужен тепловизор usb"))
                .andExpect(jsonPath("$.items").isArray());

        verify(itemRequestClient).create(eq(userId), any(ItemRequestCreateDto.class));
    }

    @Test
    @DisplayName("GET /requests свои запросы")
    void getOwn() throws Exception {
        Long userId = 1L;
        when(itemRequestClient.getOwn(userId)).thenReturn(ResponseEntity.ok(List.of(Map.of(
                "id", 10L,
                "description", "Нужен тепловизор usb",
                "created", "2026-10-15T12:00:00",
                "items", List.of(Map.of(
                        "id", 100L,
                        "name", "тепловизор usb",
                        "ownerId", 2L
                ))
        ))));

        mvc.perform(get("/requests").header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].items[0].name").value("тепловизор usb"))
                .andExpect(jsonPath("$[0].items[0].ownerId").value(2));

        verify(itemRequestClient).getOwn(userId);
    }

    @Test
    @DisplayName("GET /requests/all чужие запросы")
    void getAll() throws Exception {
        Long userId = 2L;
        when(itemRequestClient.getAll(userId)).thenReturn(ResponseEntity.ok(List.of(Map.of(
                "id", 10L,
                "description", "Нужно новое узо",
                "created", "2026-10-15T12:00:00",
                "items", List.of()
        ))));

        mvc.perform(get("/requests/all").header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Нужно новое узо"));

        verify(itemRequestClient).getAll(userId);
    }

    @Test
    @DisplayName("GET /requests/{id}")
    void getById() throws Exception {
        Long userId = 2L;
        Long requestId = 10L;
        when(itemRequestClient.getById(userId, requestId)).thenReturn(ResponseEntity.ok(Map.of(
                "id", requestId,
                "description", "Нужен тепловизор usb",
                "created", "2026-10-15T12:00:00",
                "items", List.of()
        )));

        mvc.perform(get("/requests/{id}", requestId).header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.description").value("Нужен тепловизор usb"));

        verify(itemRequestClient).getById(userId, requestId);
    }
}
