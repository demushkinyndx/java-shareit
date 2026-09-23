package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
	private final ItemRepository itemRepository;
	private final UserRepository userRepository;

	@Override
	public ItemDto create(Long ownerId, ItemDto itemDto) {
		User owner = findUser(ownerId);
		Item item = itemRepository.save(ItemMapper.toItem(itemDto, owner));
		log.info("Пользователь id={} добавил вещь id={}", ownerId, item.getId());
		return ItemMapper.toItemDto(item);
	}

	@Override
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
		return ItemMapper.toItemDto(itemRepository.update(item));
	}

	@Override
	public ItemDto getById(Long itemId) {
		return ItemMapper.toItemDto(findItem(itemId));
	}

	@Override
	public List<ItemDto> getByOwner(Long ownerId) {
		findUser(ownerId);
		return itemRepository.findByOwnerId(ownerId).stream()
				.map(ItemMapper::toItemDto)
				.toList();
	}

	@Override
	public List<ItemDto> search(String text) {
		if (text == null || text.isBlank()) {
			return List.of();
		}
		return itemRepository.searchAvailable(text).stream()
				.map(ItemMapper::toItemDto)
				.toList();
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
