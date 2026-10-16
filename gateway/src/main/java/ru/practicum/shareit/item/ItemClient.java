package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.AbstractBaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends AbstractBaseClient {
	private static final String SERVER_PATH_PREFIX = "/items";

	@Autowired
	public ItemClient(@Value("${server.url}") String serverUrl, RestTemplateBuilder builder) {
		super(
				builder
						.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + SERVER_PATH_PREFIX))
						.requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
						.build()
		);
	}

	public ResponseEntity<Object> create(Long ownerId, ItemDto itemDto) {
		return post(null, ownerId, itemDto);
	}

	public ResponseEntity<Object> update(Long ownerId, Long itemId, ItemDto itemDto) {
		return patch("/" + itemId, ownerId, itemDto);
	}

	public ResponseEntity<Object> getById(Long userId, Long itemId) {
		return get("/" + itemId, userId);
	}

	public ResponseEntity<Object> getByOwner(Long ownerId) {
		return get("", ownerId);
	}

	public ResponseEntity<Object> search(String text) {
		Map<String, Object> parameters = Map.of("text", text);
		return get("/search?text={text}", null, parameters);
	}

	public ResponseEntity<Object> addComment(Long userId, Long itemId, CommentDto commentDto) {
		return post("/" + itemId + "/comment", userId, commentDto);
	}
}
