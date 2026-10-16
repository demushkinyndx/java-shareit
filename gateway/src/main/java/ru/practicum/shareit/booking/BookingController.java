package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.item.ItemController;

@Validated
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> create(@RequestHeader(ItemController.X_SHARER_USER_ID) Long userId,
										 @RequestBody @Valid BookingCreateDto bookingCreateDto) {
		return bookingClient.create(userId, bookingCreateDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approve(@RequestHeader(ItemController.X_SHARER_USER_ID) Long userId,
										  @PathVariable @NotNull(message = "Идентификатор бронирования обязателен") Long bookingId,
										  @RequestParam @NotNull(message = "Параметр approved обязателен") Boolean approved) {
		return bookingClient.approve(userId, bookingId, approved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getById(@RequestHeader(ItemController.X_SHARER_USER_ID) Long userId,
										  @PathVariable @NotNull(message = "Идентификатор бронирования обязателен") Long bookingId) {
		return bookingClient.getById(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getByBooker(@RequestHeader(ItemController.X_SHARER_USER_ID) Long userId,
											  @RequestParam(defaultValue = "ALL") String state) {
		return bookingClient.getByBooker(userId, state);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getByOwner(@RequestHeader(ItemController.X_SHARER_USER_ID) Long userId,
											 @RequestParam(defaultValue = "ALL") String state) {
		return bookingClient.getByOwner(userId, state);
	}
}
