package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.OnCreate;

@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
	public static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

	private final ItemClient itemClient;

	@PostMapping
	public ResponseEntity<Object> create(@RequestHeader(X_SHARER_USER_ID) Long ownerId,
										 @RequestBody @Validated(OnCreate.class) ItemDto itemDto) {
		return itemClient.create(ownerId, itemDto);
	}

	@PatchMapping("/{itemId}")
	public ResponseEntity<Object> update(@RequestHeader(X_SHARER_USER_ID) Long ownerId,
										 @PathVariable @NotNull(message = "Идентификатор вещи обязателен") Long itemId,
										 @RequestBody ItemDto itemDto) {
		return itemClient.update(ownerId, itemId, itemDto);
	}

	@GetMapping("/{itemId}")
	public ResponseEntity<Object> getById(@RequestHeader(X_SHARER_USER_ID) Long userId,
										  @PathVariable @NotNull(message = "Идентификатор вещи обязателен") Long itemId) {
		return itemClient.getById(userId, itemId);
	}

	@GetMapping
	public ResponseEntity<Object> getByOwner(@RequestHeader(X_SHARER_USER_ID) Long ownerId) {
		return itemClient.getByOwner(ownerId);
	}

	@GetMapping("/search")
	public ResponseEntity<Object> search(@RequestParam(defaultValue = "") String text) {
		return itemClient.search(text);
	}

	@PostMapping("/{itemId}/comment")
	public ResponseEntity<Object> addComment(@RequestHeader(X_SHARER_USER_ID) Long userId,
											 @PathVariable @NotNull(message = "Идентификатор вещи обязателен") Long itemId,
											 @RequestBody @Valid CommentDto commentDto) {
		return itemClient.addComment(userId, itemId, commentDto);
	}
}
