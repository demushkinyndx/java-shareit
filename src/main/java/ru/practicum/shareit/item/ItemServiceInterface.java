package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemServiceInterface {
	ItemDto create(Long ownerId, ItemDto itemDto);

	ItemDto update(Long ownerId, Long itemId, ItemDto itemDto);

	ItemDto getById(Long userId, Long itemId);

	List<ItemDto> getByOwner(Long ownerId);

	List<ItemDto> search(String text);

	CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}
