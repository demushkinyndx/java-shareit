package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestService implements ItemRequestServiceInterface {
	private final ItemRequestRepository itemRequestRepository;
	private final ItemRepository itemRepository;
	private final UserService userService;

	@Override
	@Transactional
	public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
		User requestor = userService.findUser(userId);
		ItemRequest request = itemRequestRepository.save(ItemRequestMapper.toItemRequest(dto, requestor));
		log.info("Пользователь id={} создал запрос вещи id={}", userId, request.getId());
		return ItemRequestMapper.toItemRequestDto(request);
	}

	@Override
	public List<ItemRequestDto> getOwn(Long userId) {
		userService.checkUserExists(userId);
		List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
		return toDto(requests);
	}

	@Override
	public List<ItemRequestDto> getAll(Long userId) {
		userService.checkUserExists(userId);
		List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId);
		return toDto(requests);
	}

	@Override
	public ItemRequestDto getById(Long userId, Long requestId) {
		userService.checkUserExists(userId);
		ItemRequest request = itemRequestRepository.findById(requestId)
				.orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));
		List<Item> items = itemRepository.findByRequestId(requestId);
		return ItemRequestMapper.toItemRequestDto(request, items);
	}

	private List<ItemRequestDto> toDto(List<ItemRequest> requests) {
		if (requests.isEmpty()) {
			return List.of();
		}

		List<Long> requestIds = requests.stream()
				.map(ItemRequest::getId)
				.toList();
		Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds).stream()
				.collect(Collectors.groupingBy(item -> item.getRequest().getId()));

		return requests.stream()
				.map(request -> ItemRequestMapper.toItemRequestDto(
						request,
						itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())))
				.toList();
	}
}
