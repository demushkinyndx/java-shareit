package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

	@Query("select r from ItemRequest r where r.requestor.id = ?1 order by r.created desc")
	List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(Long requestorId);

	@Query("select r from ItemRequest r where r.requestor.id <> ?1 order by r.created desc")
	List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Long requestorId);
}
