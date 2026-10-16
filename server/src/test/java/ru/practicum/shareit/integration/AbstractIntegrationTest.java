package ru.practicum.shareit.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingServiceInterface;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemServiceInterface;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestServiceInterface;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserServiceInterface;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

	@Autowired
	protected UserServiceInterface userService;

	@Autowired
	protected ItemServiceInterface itemService;

	@Autowired
	protected BookingServiceInterface bookingService;

	@Autowired
	protected ItemRequestServiceInterface itemRequestService;

	@Autowired
	protected ItemRepository itemRepository;

	@Autowired
	protected BookingRepository bookingRepository;

	protected UserDto createUser(String name, String email) {
		String uniqueEmail = email.replace("@", "+" + UUID.randomUUID() + "@");
		return userService.create(UserDto.builder()
				.name(name)
				.email(uniqueEmail)
				.build());
	}

	protected ItemDto createItem(Long ownerId, String name) {
		return itemService.create(ownerId, ItemDto.builder()
				.name(name)
				.description("Описание")
				.available(true)
				.build());
	}

	protected ItemRequestDto createRequest(Long userId, String description) {
		return itemRequestService.create(userId, ItemRequestCreateDto.builder()
				.description(description)
				.build());
	}

	protected BookingDto createBooking(Long bookerId, Long itemId, LocalDateTime start, LocalDateTime end) {
		return bookingService.create(bookerId, BookingCreateDto.builder()
				.itemId(itemId)
				.start(start)
				.end(end)
				.build());
	}

	protected void createPastApprovedBooking(Long bookerId, Long itemId) {
		User booker = userService.findUser(bookerId);
		Item item = itemRepository.findById(itemId).orElseThrow();
		LocalDateTime end = LocalDateTime.now().minusDays(1);
		LocalDateTime start = end.minusDays(2);
		bookingRepository.save(Booking.builder()
				.item(item)
				.booker(booker)
				.start(start)
				.end(end)
				.status(BookingStatus.APPROVED)
				.build());
	}
}
