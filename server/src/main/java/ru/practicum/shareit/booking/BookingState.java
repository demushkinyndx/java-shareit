package ru.practicum.shareit.booking;

import ru.practicum.shareit.exception.BadRequestException;

public enum BookingState {
	ALL,
	CURRENT,
	PAST,
	FUTURE,
	WAITING,
	REJECTED;

	public static BookingState from(String value) {
		try {
			return BookingState.valueOf(value);
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new BadRequestException("Неизвестное значение фильтра: " + value);
		}
	}
}
