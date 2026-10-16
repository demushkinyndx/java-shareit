package ru.practicum.shareit.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserServiceInterface;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

	@Mock
	private ItemRequestRepository itemRequestRepository;

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private UserServiceInterface userService;

	@InjectMocks
	private ItemRequestService itemRequestService;

	@Test
	@DisplayName("создаем запрос вещи и возвращаем его id")
	void create() {
		Long requestorId = 1L;
		Long requestId = 10L;
		User requestor = User.builder()
				.id(requestorId)
				.name("Маркус Зоргенфрей")
				.email("marcus@transhumanism.inc")
				.build();
		ItemRequestCreateDto dto = ItemRequestCreateDto.builder()
				.description("Нужна мощная дрель")
				.build();

		when(userService.findUser(requestorId)).thenReturn(requestor);
		when(itemRequestRepository.save(any(ItemRequest.class))).thenAnswer(invocation -> {
			ItemRequest request = invocation.getArgument(0);
			request.setId(requestId);
			return request;
		});

		ItemRequestDto result = itemRequestService.create(requestorId, dto);

		assertEquals(requestId, result.getId());
		assertEquals("Нужна мощная дрель", result.getDescription());
		assertNotNull(result.getCreated());
		assertTrue(result.getItems().isEmpty());
		verify(itemRequestRepository).save(any(ItemRequest.class));
	}

	@Test
	@DisplayName("возвращаем свои запросы вместе с ответами")
	void getOwn() {
		Long requestorId = 1L;
		Long ownerId = 2L;
		Long requestId = Long.valueOf(10);
		Long itemId = 100L;
		User requestor = User.builder()
				.id(requestorId)
				.name("Маркус Зоргенфрей")
				.email("marcus@transhumanism.inc")
				.build();
		User owner = User.builder()
				.id(ownerId)
				.name("Порфирий Петрович")
				.email("porfiry@roma-3.ru")
				.build();
		ItemRequest request = ItemRequest.builder()
				.id(requestId)
				.description("Нужна мощная дрель")
				.requestor(requestor)
				.created(LocalDateTime.now())
				.build();
		Item item = Item.builder()
				.id(itemId)
				.name("Дрель")
				.description("Мощная")
				.available(true)
				.owner(owner)
				.request(request)
				.build();

		when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(requestorId))
				.thenReturn(List.of(request));
		when(itemRepository.findByRequestIdIn(List.of(requestId))).thenReturn(List.of(item));

		List<ItemRequestDto> result = itemRequestService.getOwn(requestorId);

		assertEquals(1, result.size());
		assertEquals(requestId, result.getFirst().getId());
		assertEquals(1, result.getFirst().getItems().size());
		assertEquals(itemId, result.getFirst().getItems().getFirst().getId());
		assertEquals("Дрель", result.getFirst().getItems().getFirst().getName());
		assertEquals(ownerId, result.getFirst().getItems().getFirst().getOwnerId());
		verify(userService).checkUserExists(requestorId);
	}

	@Test
	@DisplayName("возвращаем запрос по id вместе с ответами")
	void getById() {
		Long requestorId = 1L;
		Long ownerId = 2L;
		Long requestId = 10L;
		Long itemId = 100L;
		User requestor = User.builder()
				.id(requestorId)
				.name("Ломас")
				.email("lomas@transhumanism.inc")
				.build();
		User owner = User.builder()
				.id(ownerId)
				.name("Порфирий Петрович")
				.email("porfiry@roma-3.ru")
				.build();
		ItemRequest request = ItemRequest.builder()
				.id(requestId)
				.description("Нужна мощная дрель")
				.requestor(requestor)
				.created(LocalDateTime.now())
				.build();
		Item item = Item.builder()
				.id(itemId)
				.name("Дрель")
				.description("Мощная")
				.available(true)
				.owner(owner)
				.request(request)
				.build();

		when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
		when(itemRepository.findByRequestId(requestId)).thenReturn(List.of(item));

		ItemRequestDto result = itemRequestService.getById(ownerId, requestId);

		assertEquals(requestId, result.getId());
		assertEquals("Нужна мощная дрель", result.getDescription());
		assertEquals(1, result.getItems().size());
		assertEquals(itemId, result.getItems().getFirst().getId());
		verify(userService).checkUserExists(ownerId);
	}

	@Test
	@DisplayName("не находим запрос по несуществующему id")
	void getByIdNotFound() {
		Long userId = 1L;
		Long missingRequestId = 99L;

		when(itemRequestRepository.findById(missingRequestId)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> itemRequestService.getById(userId, missingRequestId));
		verify(userService).checkUserExists(userId);
	}
}
