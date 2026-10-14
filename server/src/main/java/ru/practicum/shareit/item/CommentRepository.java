package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Item.Comment, Long> {

    List<Item.Comment> findByItemId(Long itemId);

    List<Item.Comment> findByItemIdIn(Collection<Long> itemIds);
}
