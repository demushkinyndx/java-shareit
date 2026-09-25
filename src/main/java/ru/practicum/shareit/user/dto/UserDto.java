package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.validation.OnUpdate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
	private Long id;

	@NotBlank(message = "Имя не может быть пустым", groups = OnCreate.class)
	private String name;

	@NotBlank(message = "Email не может быть пустым", groups = OnCreate.class)
	@Email(message = "Некорректный email", groups = {OnCreate.class, OnUpdate.class})
	private String email;
}
