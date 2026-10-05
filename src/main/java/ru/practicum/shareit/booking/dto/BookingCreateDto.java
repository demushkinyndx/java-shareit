package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateDto {
	@NotNull(message = "Идентификатор вещи обязателен")
	private Long itemId;

	@NotNull(message = "Дата начала бронирования обязательна")
	private LocalDateTime start;

	@NotNull(message = "Дата окончания бронирования обязательна")
	private LocalDateTime end;
}
