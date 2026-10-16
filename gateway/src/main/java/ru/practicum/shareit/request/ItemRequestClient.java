package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

@Service
public class ItemRequestClient extends BaseClient {
	private static final String SERVER_PATH_PREFIX = "/requests";

	@Autowired
	public ItemRequestClient(@Value("${server.url}") String serverUrl, RestTemplateBuilder builder) {
		super(
				builder
						.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + SERVER_PATH_PREFIX))
						.requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
						.build()
		);
	}

	public ResponseEntity<Object> create(Long userId, ItemRequestCreateDto dto) {
		return post("", userId, dto);
	}

	public ResponseEntity<Object> getOwn(Long userId) {
		return get("", userId);
	}

	public ResponseEntity<Object> getAll(Long userId) {
		return get("/all", userId);
	}

	public ResponseEntity<Object> getById(Long userId, Long requestId) {
		return get("/" + requestId, userId);
	}
}
