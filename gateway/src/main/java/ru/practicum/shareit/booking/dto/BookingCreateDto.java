package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
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

	@JsonIgnore
	@AssertTrue(message = "Дата окончания должна быть позже даты начала")
	public boolean isEndAfterStart() {
		return start == null || end == null || end.isAfter(start);
	}

	@JsonIgnore
	@AssertTrue(message = "Дата начала не может быть в прошлом")
	public boolean isStartNotInPast() {
		return start == null || !start.isBefore(LocalDateTime.now());
	}

	@JsonIgnore
	@AssertTrue(message = "Дата окончания не может быть в прошлом")
	public boolean isEndNotInPast() {
		return end == null || !end.isBefore(LocalDateTime.now());
	}
}
