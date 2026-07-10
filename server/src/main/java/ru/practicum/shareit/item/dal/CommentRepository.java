package ru.practicum.shareit.item.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
            select c
            from Comment c
            join fetch c.item
            where c.item in :items
            """)
    List<Comment> findAllByItemIn(@Param("items") Collection<Item> items);

    @Query("""
            select c
            from Comment c
            join fetch c.item
            where c.item = :item
            """)
    List<Comment> findAllByItem(@Param("item") Item item);
}
