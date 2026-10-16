package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
	private static final String USER_ID_HEADER = "X-Sharer-User-Id";

	private final ItemRequestClient itemRequestClient;

	@PostMapping
	public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) Long userId,
										 @RequestBody @Valid ItemRequestCreateDto dto) {
		return itemRequestClient.create(userId, dto);
	}

	@GetMapping
	public ResponseEntity<Object> getOwn(@RequestHeader(USER_ID_HEADER) Long userId) {
		return itemRequestClient.getOwn(userId);
	}

	@GetMapping("/all")
	public ResponseEntity<Object> getAll(@RequestHeader(USER_ID_HEADER) Long userId) {
		return itemRequestClient.getAll(userId);
	}

	@GetMapping("/{requestId}")
	public ResponseEntity<Object> getById(@RequestHeader(USER_ID_HEADER) Long userId,
										  @PathVariable Long requestId) {
		return itemRequestClient.getById(userId, requestId);
	}
}
