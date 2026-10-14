package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.validation.OnCreate;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
	private Long id;

	@NotBlank(message = "Название не может быть пустым", groups = OnCreate.class)
	private String name;

	@NotBlank(message = "Описание не может быть пустым", groups = OnCreate.class)
	private String description;

	@NotNull(message = "Статус доступности обязателен", groups = OnCreate.class)
	private Boolean available;

	private BookingShortDto lastBooking;

	private BookingShortDto nextBooking;

	private List<CommentDto> comments;

	private Long requestId;
}
