package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemRequestMapper {

	public static ItemRequest toItemRequest(ItemRequestCreateDto dto, User requestor) {
		return ItemRequest.builder()
				.description(dto.getDescription())
				.requestor(requestor)
				.created(LocalDateTime.now())
				.build();
	}

	public static ItemRequestDto toItemRequestDto(ItemRequest request) {
		return toItemRequestDto(request, Collections.emptyList());
	}

	public static ItemRequestDto toItemRequestDto(ItemRequest request, List<Item> items) {
		List<ItemRequestItemDto> itemDtos = items == null
				? List.of()
				: items.stream().map(ItemRequestMapper::toItemRequestItemDto).toList();

		return ItemRequestDto.builder()
				.id(request.getId())
				.description(request.getDescription())
				.created(request.getCreated())
				.items(itemDtos)
				.build();
	}

	public static ItemRequestItemDto toItemRequestItemDto(Item item) {
		return ItemRequestItemDto.builder()
				.id(item.getId())
				.name(item.getName())
				.ownerId(item.getOwner().getId())
				.build();
	}
}
