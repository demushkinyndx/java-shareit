package ru.practicum.shareit.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    @DisplayName("сериализуем BookingDto с датами и статусом")
    void serialize() throws Exception {
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2026, 10, 20, 10, 0, 0))
                .end(LocalDateTime.of(2026, 10, 22, 12, 30, 0))
                .status(BookingStatus.WAITING)
                .item(ItemDto.builder()
                        .id(10L)
                        .name("Тепловизор usb")
                        .build())
                .booker(UserDto.builder()
                        .id(2L)
                        .name("Маркус Зоргенфрей")
                        .build())
                .build();

        var result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id", 1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-10-20T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-10-22T12:30:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Тепловизор usb");
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Маркус Зоргенфрей");
    }

    @Test
    @DisplayName("десериализуем BookingDto из JSON с датами")
    void deserialize() throws Exception {
        BookingDto dto = json.parseObject("{\"id\": 1, \"start\": \"2026-10-20T10:00:00\", \"end\": \"2026-10-22T12:30:00\", \"status\": \"APPROVED\", \"item\": {\"id\": 10, \"name\": \"Тепловизор usb\"}, \"booker\": {\"id\": 2, \"name\": \"Ломас\"}}");

        assertThat(dto.getId()).isEqualTo(Long.valueOf(1));
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 10, 20, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 10, 22, 12, 30, 0));
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(dto.getItem().getName()).isEqualTo("Тепловизор usb");
        assertThat(dto.getBooker().getName()).isEqualTo("Ломас");
    }
}
