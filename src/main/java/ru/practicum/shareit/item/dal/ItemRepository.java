package ru.practicum.shareit.item.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Collection<Item> findByOwnerId(long ownerId);

    @Query("""
            select i
            from Item i
            where (lower(i.name) like lower(concat('%', :text, '%'))
            or lower(i.description) like lower(concat('%', :text, '%')))
            and i.available = true
            """)
    Collection<Item> search(@Param("text") String text);

    Optional<Item> findById(long itemId);
}
