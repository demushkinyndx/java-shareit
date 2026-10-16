package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
	private final ItemRequestServiceInterface itemRequestService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ItemRequestDto create(@RequestHeader(ItemController.X_SHARER_USER_ID) Long userId,
								 @RequestBody ItemRequestCreateDto dto) {
		return itemRequestService.create(userId, dto);
	}

	@GetMapping
	public List<ItemRequestDto> getOwn(@RequestHeader(ItemController.X_SHARER_USER_ID) Long userId) {
		return itemRequestService.getOwn(userId);
	}

	@GetMapping("/all")
	public List<ItemRequestDto> getAll(@RequestHeader(ItemController.X_SHARER_USER_ID) Long userId) {
		return itemRequestService.getAll(userId);
	}

	@GetMapping("/{requestId}")
	public ItemRequestDto getById(@RequestHeader(ItemController.X_SHARER_USER_ID) Long userId,
								  @PathVariable Long requestId) {
		return itemRequestService.getById(userId, requestId);
	}
}
