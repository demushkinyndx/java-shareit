package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService implements BookingServiceInterface {
	private final BookingRepository bookingRepository;
	private final UserRepository userRepository;
	private final ItemRepository itemRepository;

	@Override
	@Transactional
	public BookingDto create(Long bookerId, BookingCreateDto bookingCreateDto) {
		User booker = findUser(bookerId);
		Item item = itemRepository.findById(bookingCreateDto.getItemId())
				.orElseThrow(() -> new NotFoundException(
						"Вещь с id=" + bookingCreateDto.getItemId() + " не найдена"));

		validateBookingDates(bookingCreateDto.getStart(), bookingCreateDto.getEnd());

		if (!Boolean.TRUE.equals(item.getAvailable())) {
			throw new BadRequestException("Вещь недоступна для бронирования");
		}
		if (item.getOwner().getId().equals(bookerId)) {
			throw new NotFoundException("Владелец не может бронировать свою вещь");
		}

		Booking booking = bookingRepository.save(BookingMapper.toBooking(bookingCreateDto, item, booker));
		log.info("Пользователь id={} создал бронирование id={}", bookerId, booking.getId());
		return BookingMapper.toBookingDto(booking);
	}

	@Override
	@Transactional
	public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
		Booking booking = findBooking(bookingId);

		if (!booking.getItem().getOwner().getId().equals(ownerId)) {
			throw new ForbiddenException("Подтверждать бронирование может только владелец вещи");
		}
		if (booking.getStatus() != BookingStatus.WAITING) {
			throw new BadRequestException("Статус бронирования уже изменен");
		}

		booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
		log.info("Владелец id={} {} бронирование id={}",
				ownerId, approved ? "подтвердил" : "отклонил", bookingId);
		return BookingMapper.toBookingDto(bookingRepository.save(booking));
	}

	@Override
	public BookingDto getById(Long userId, Long bookingId) {
		checkUserExists(userId);
		Booking booking = findBooking(bookingId);
		Long bookerId = booking.getBooker().getId();
		Long ownerId = booking.getItem().getOwner().getId();
		if (!userId.equals(bookerId) && !userId.equals(ownerId)) {
			throw new NotFoundException("Нет доступа к бронированию id=" + bookingId);
		}
		return BookingMapper.toBookingDto(booking);
	}

	@Override
	public List<BookingDto> getByBooker(Long bookerId, BookingState state) {
		checkUserExists(bookerId);
		LocalDateTime now = LocalDateTime.now();
		List<Booking> bookings = switch (state) {
			case ALL -> bookingRepository.findAllByBooker(bookerId);
			case CURRENT -> bookingRepository.findCurrentByBooker(bookerId, now);
			case PAST -> bookingRepository.findPastByBooker(bookerId, now);
			case FUTURE -> bookingRepository.findFutureByBooker(bookerId, now);
			case WAITING -> bookingRepository.findByBookerAndStatus(bookerId, BookingStatus.WAITING);
			case REJECTED -> bookingRepository.findByBookerAndStatus(bookerId, BookingStatus.REJECTED);
		};
		return bookings.stream().map(BookingMapper::toBookingDto).toList();
	}

	@Override
	public List<BookingDto> getByOwner(Long ownerId, BookingState state) {
		checkUserExists(ownerId);
		if (!itemRepository.existsByOwnerId(ownerId)) {
			throw new NotFoundException("У пользователя id=" + ownerId + " нет вещей");
		}
		LocalDateTime now = LocalDateTime.now();
		List<Booking> bookings = switch (state) {
			case ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId);
			case CURRENT -> bookingRepository.findCurrentByOwner(ownerId, now);
			case PAST -> bookingRepository.findPastByOwner(ownerId, now);
			case FUTURE -> bookingRepository.findFutureByOwner(ownerId, now);
			case WAITING -> bookingRepository.findByOwnerAndStatus(ownerId, BookingStatus.WAITING);
			case REJECTED -> bookingRepository.findByOwnerAndStatus(ownerId, BookingStatus.REJECTED);
		};
		return bookings.stream().map(BookingMapper::toBookingDto).toList();
	}

	private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
		if (start == null || end == null) {
			throw new BadRequestException("Даты бронирования обязательны");
		}
		LocalDateTime now = LocalDateTime.now();
		if (!end.isAfter(start)) {
			throw new BadRequestException("Дата окончания должна быть позже даты начала");
		}
		if (start.isBefore(now)) {
			throw new BadRequestException("Дата начала не может быть в прошлом");
		}
		if (end.isBefore(now)) {
			throw new BadRequestException("Дата окончания не может быть в прошлом");
		}
	}

	private User findUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
	}

	private void checkUserExists(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new NotFoundException("Пользователь с id=" + userId + " не найден");
		}
	}

	private Booking findBooking(Long bookingId) {
		return bookingRepository.findById(bookingId)
				.orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));
	}
}
