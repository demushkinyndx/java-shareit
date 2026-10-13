package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService implements ItemServiceInterface {
	private final ItemRepository itemRepository;
	private final UserRepository userRepository;
	private final BookingRepository bookingRepository;
	private final CommentRepository commentRepository;

	@Override
	@Transactional
	public ItemDto create(Long ownerId, ItemDto itemDto) {
		User owner = findUser(ownerId);
		Item item = itemRepository.save(ItemMapper.toItem(itemDto, owner));
		log.info("Пользователь id={} добавил вещь id={}", ownerId, item.getId());
		return ItemMapper.toItemDto(item);
	}

	@Override
	@Transactional
	public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
		findUser(ownerId);
		Item item = findItem(itemId);
		if (!item.getOwner().getId().equals(ownerId)) {
			throw new ForbiddenException("Пользователь id=" + ownerId + " не владелец вещи id=" + itemId);
		}

		if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
			item.setName(itemDto.getName());
		}
		if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
			item.setDescription(itemDto.getDescription());
		}
		if (itemDto.getAvailable() != null) {
			item.setAvailable(itemDto.getAvailable());
		}

		log.info("Пользователь id={} обновил вещь id={}", ownerId, itemId);
		return ItemMapper.toItemDto(itemRepository.save(item));
	}

	@Override
	public ItemDto getById(Long userId, Long itemId) {
		findUser(userId);
		Item item = findItem(itemId);
		ItemDto itemDto = ItemMapper.toItemDto(item);
		if (item.getOwner().getId().equals(userId)) {
			addBookings(itemDto, itemId);
		}
		itemDto.setComments(commentRepository.findByItemId(itemId).stream()
				.map(CommentMapper::toCommentDto)
				.toList());
		return itemDto;
	}

	@Override
	public List<ItemDto> getByOwner(Long ownerId) {
		findUser(ownerId);
		List<Item> items = itemRepository.findByOwnerId(ownerId);
		if (items.isEmpty()) {
			return List.of();
		}

		List<Long> itemIds = items.stream().map(Item::getId).toList();
		Map<Long, List<Booking>> bookingsByItem = bookingRepository
				.findApprovedByItemIds(itemIds, BookingStatus.APPROVED)
				.stream()
				.collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
		Map<Long, List<Comment>> commentsByItem = commentRepository.findByItemIdIn(itemIds).stream()
				.collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
		LocalDateTime now = LocalDateTime.now();

		return items.stream()
				.map(item -> {
					ItemDto itemDto = ItemMapper.toItemDto(item);
					List<Booking> bookings = bookingsByItem.getOrDefault(item.getId(), List.of());
					itemDto.setLastBooking(toShort(findLast(bookings, now)));
					itemDto.setNextBooking(toShort(findNext(bookings, now)));
					itemDto.setComments(commentsByItem.getOrDefault(item.getId(), List.of()).stream()
							.map(CommentMapper::toCommentDto)
							.toList());
					return itemDto;
				})
				.toList();
	}

	@Override
	public List<ItemDto> search(String text) {
		if (text == null || text.isBlank()) {
			return List.of();
		}
		return itemRepository.search(text).stream()
				.map(ItemMapper::toItemDto)
				.toList();
	}

	@Override
	@Transactional
	public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
		User author = findUser(userId);
		Item item = findItem(itemId);
		LocalDateTime now = LocalDateTime.now();

		if (!bookingRepository.hasPastApprovedBooking(itemId, userId, BookingStatus.APPROVED, now)) {
			throw new BadRequestException(
					"Комментарий можно оставить только после завершённой аренды вещи");
		}

		Comment comment = commentRepository.save(CommentMapper.toComment(commentDto, item, author));
		log.info("Пользователь id={} добавил комментарий id={} к вещи id={}",
				userId, comment.getId(), itemId);
		return CommentMapper.toCommentDto(comment);
	}

	private void addBookings(ItemDto itemDto, Long itemId) {
		LocalDateTime now = LocalDateTime.now();
		var page = PageRequest.of(0, 1);
		itemDto.setLastBooking(toShort(bookingRepository
				.findLastApproved(itemId, BookingStatus.APPROVED, now, page)
				.stream()
				.findFirst()
				.orElse(null)));
		itemDto.setNextBooking(toShort(bookingRepository
				.findNextApproved(itemId, BookingStatus.APPROVED, now, page)
				.stream()
				.findFirst()
				.orElse(null)));
	}

	private Booking findLast(List<Booking> bookings, LocalDateTime now) {
		return bookings.stream()
				.filter(booking -> booking.getStart() != null && booking.getStart().isBefore(now))
				.max(Comparator.comparing(Booking::getStart))
				.orElse(null);
	}

	private Booking findNext(List<Booking> bookings, LocalDateTime now) {
		return bookings.stream()
				.filter(booking -> booking.getStart() != null && booking.getStart().isAfter(now))
				.min(Comparator.comparing(Booking::getStart))
				.orElse(null);
	}

	private BookingShortDto toShort(Booking booking) {
		return BookingMapper.toBookingShortDto(booking);
	}

	private User findUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
	}

	private Item findItem(Long itemId) {
		return itemRepository.findById(itemId)
				.orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
	}
}
