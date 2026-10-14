package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	@Query("select b from Booking b where b.booker.id = ?1 order by b.start desc")
	List<Booking> findAllByBooker(Long bookerId);

	@Query("select b from Booking b " +
			"where b.booker.id = ?1 and b.start < ?2 and b.end > ?2 " +
			"order by b.start desc")
	List<Booking> findCurrentByBooker(Long bookerId, LocalDateTime now);

	@Query("select b from Booking b where b.booker.id = ?1 and b.end < ?2 order by b.start desc")
	List<Booking> findPastByBooker(Long bookerId, LocalDateTime now);

	@Query("select b from Booking b where b.booker.id = ?1 and b.start > ?2 order by b.start desc")
	List<Booking> findFutureByBooker(Long bookerId, LocalDateTime now);

	@Query("select b from Booking b where b.booker.id = ?1 and b.status = ?2 order by b.start desc")
	List<Booking> findByBookerAndStatus(Long bookerId, BookingStatus status);

	List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

	@Query("select b from Booking b " +
			"where b.item.owner.id = ?1 and b.start < ?2 and b.end > ?2 " +
			"order by b.start desc")
	List<Booking> findCurrentByOwner(Long ownerId, LocalDateTime now);

	@Query("select b from Booking b where b.item.owner.id = ?1 and b.end < ?2 order by b.start desc")
	List<Booking> findPastByOwner(Long ownerId, LocalDateTime now);

	@Query("select b from Booking b where b.item.owner.id = ?1 and b.start > ?2 order by b.start desc")
	List<Booking> findFutureByOwner(Long ownerId, LocalDateTime now);

	@Query("select b from Booking b where b.item.owner.id = ?1 and b.status = ?2 order by b.start desc")
	List<Booking> findByOwnerAndStatus(Long ownerId, BookingStatus status);

	@Query("select b from Booking b " +
			"where b.item.id = ?1 and b.status = ?2 and b.start < ?3 " +
			"order by b.start desc")
	List<Booking> findLastApproved(Long itemId, BookingStatus status, LocalDateTime now, Pageable pageable);

	@Query("select b from Booking b " +
			"where b.item.id = ?1 and b.status = ?2 and b.start > ?3 " +
			"order by b.start asc")
	List<Booking> findNextApproved(Long itemId, BookingStatus status, LocalDateTime now, Pageable pageable);

	@Query("select b from Booking b " +
			"where b.item.id in ?1 and b.status = ?2")
	List<Booking> findApprovedByItemIds(Collection<Long> itemIds, BookingStatus status);

	@Query("select count(b) > 0 from Booking b " +
			"where b.item.id = ?1 and b.booker.id = ?2 and b.status = ?3 and b.end < ?4")
	boolean hasPastApprovedBooking(Long itemId, Long bookerId, BookingStatus status, LocalDateTime now);
}
