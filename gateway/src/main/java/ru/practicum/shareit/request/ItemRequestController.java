package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.util.HeaderConstants;

@Validated
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
	private final ItemRequestClient itemRequestClient;

	@PostMapping
	public ResponseEntity<Object> create(@RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId,
										 @RequestBody @Valid ItemRequestCreateDto dto) {
		return itemRequestClient.create(userId, dto);
	}

	@GetMapping
	public ResponseEntity<Object> getOwn(@RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId) {
		return itemRequestClient.getOwn(userId);
	}

	@GetMapping("/all")
	public ResponseEntity<Object> getAll(@RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId) {
		return itemRequestClient.getAll(userId);
	}

	@GetMapping("/{requestId}")
	public ResponseEntity<Object> getById(@RequestHeader(HeaderConstants.X_SHARER_USER_ID) Long userId,
										  @PathVariable @NotNull(message = "Идентификатор запроса обязателен") Long requestId) {
		return itemRequestClient.getById(userId, requestId);
	}
}
