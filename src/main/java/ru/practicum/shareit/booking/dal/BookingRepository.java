package ru.practicum.shareit.booking.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {

    @Query("""
            select b
            from Booking b
            where b.item in :items
            and b.start >= current_timestamp
            and status = 'APPROVED'
            """)
    public Collection<Booking> findFutureBookingsForItems(@Param("items") Collection<Item> items);

    @Query("""
            select b
            from Booking b
            where b.item in :items
            and b.end <= current_timestamp
            and status = 'APPROVED'
            """)
    public Collection<Booking> findPastBookingsForItems(@Param("items") Collection<Item> items);

    @Query("""
            select b
            from Booking b
            where b.item = :item
            and b.start >= current_timestamp
            and status = 'APPROVED'
            order by b.start
            limit 1
            """)
    public Optional<Booking> findNextBooking(@Param("item") Item item);

    @Query("""
            select b
            from Booking b
            where b.item = :item
            and b.end <= current_timestamp
            and status = 'APPROVED'
            order by b.end desc
            limit 1
            """)
    public Optional<Booking> findLastBooking(Item item);

}
