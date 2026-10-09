package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingServiceInterface {
	BookingDto create(Long bookerId, BookingCreateDto bookingCreateDto);

	BookingDto approve(Long ownerId, Long bookingId, boolean approved);

	BookingDto getById(Long userId, Long bookingId);

	List<BookingDto> getByBooker(Long bookerId, BookingState state);

	List<BookingDto> getByOwner(Long ownerId, BookingState state);
}
