package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    @GetMapping()
    public List<ItemRequestDto> getAllByOwner() {
        return null;
    }

    @GetMapping("/{requestId}")
    public List<ItemRequestDto> getByRequestId(@PathVariable Long requestId) {
        return null;
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll() {
        return null;
    }
}
