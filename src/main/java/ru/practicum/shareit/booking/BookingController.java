package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemController;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
	private final BookingServiceInterface bookingService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookingDto create(@RequestHeader(ItemController.X_SHARER_USER_ID) Long bookerId,
							 @RequestBody @Valid BookingCreateDto bookingCreateDto) {
		return bookingService.create(bookerId, bookingCreateDto);
	}

	@PatchMapping("/{bookingId}")
	public BookingDto approve(@RequestHeader(ItemController.X_SHARER_USER_ID) Long ownerId,
							  @PathVariable Long bookingId,
							  @RequestParam boolean approved) {
		return bookingService.approve(ownerId, bookingId, approved);
	}

	@GetMapping("/owner")
	public List<BookingDto> getByOwner(@RequestHeader(ItemController.X_SHARER_USER_ID) Long ownerId,
									   @RequestParam(defaultValue = "ALL") String state) {
		return bookingService.getByOwner(ownerId, BookingState.from(state));
	}

	@GetMapping("/{bookingId}")
	public BookingDto getById(@RequestHeader(ItemController.X_SHARER_USER_ID) Long userId,
							  @PathVariable Long bookingId) {
		return bookingService.getById(userId, bookingId);
	}

	@GetMapping
	public List<BookingDto> getByBooker(@RequestHeader(ItemController.X_SHARER_USER_ID) Long bookerId,
										@RequestParam(defaultValue = "ALL") String state) {
		return bookingService.getByBooker(bookerId, BookingState.from(state));
	}
}
