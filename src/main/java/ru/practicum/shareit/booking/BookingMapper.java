package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingMapper {

	public static BookingDto toBookingDto(Booking booking) {
		return BookingDto.builder()
				.id(booking.getId())
				.start(booking.getStart())
				.end(booking.getEnd())
				.status(booking.getStatus())
				.item(ItemDto.builder()
						.id(booking.getItem().getId())
						.name(booking.getItem().getName())
						.build())
				.booker(UserDto.builder()
						.id(booking.getBooker().getId())
						.build())
				.build();
	}

	public static BookingShortDto toBookingShortDto(Booking booking) {
		if (booking == null) {
			return null;
		}
		return BookingShortDto.builder()
				.id(booking.getId())
				.start(booking.getStart())
				.end(booking.getEnd())
				.bookerId(booking.getBooker().getId())
				.build();
	}

	public static Booking toBooking(BookingCreateDto dto, Item item, User booker) {
		return Booking.builder()
				.start(dto.getStart())
				.end(dto.getEnd())
				.item(item)
				.booker(booker)
				.status(BookingStatus.WAITING)
				.build();
	}
}
