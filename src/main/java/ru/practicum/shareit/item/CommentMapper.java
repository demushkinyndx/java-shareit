package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {

	public static CommentDto toCommentDto(Comment comment) {
		return CommentDto.builder()
				.id(comment.getId())
				.text(comment.getText())
				.authorName(comment.getAuthor().getName())
				.created(comment.getCreated())
				.build();
	}

	public static Comment toComment(CommentDto commentDto, Item item, User author) {
		return Comment.builder()
				.text(commentDto.getText())
				.item(item)
				.author(author)
				.created(LocalDateTime.now())
				.build();
	}
}
