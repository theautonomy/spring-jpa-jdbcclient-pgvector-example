package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.entity.ItemVector;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for ItemVector entity with vector similarity search support.
 *
 * <p>This repository demonstrates the same vector operations as ItemRepository but uses the
 * ItemVector entity which employs a custom Hibernate UserType for vector handling.
 */
@Repository
public interface ItemVectorRepository extends JpaRepository<ItemVector, Long> {

    // Standard JPA queries
    List<ItemVector> findByCategory(String category);

    List<ItemVector> findByPriceLessThan(BigDecimal price);

    /**
     * Find items similar to a query vector using L2 distance.
     *
     * <p>Example: findSimilarByL2Distance("[1.0, 0.5, 0.2, 0.1]", 5)
     */
    @Query(
            value =
                    """
            SELECT i.*,
                   i.embedding <-> CAST(:queryVector AS vector) AS distance
            FROM items i
            ORDER BY i.embedding <-> CAST(:queryVector AS vector)
            LIMIT :limit
            """,
            nativeQuery = true)
    List<ItemVector> findSimilarByL2Distance(
            @Param("queryVector") String queryVector, @Param("limit") int limit);

    /**
     * Find items similar to a query vector using cosine distance.
     *
     * <p>Example: findSimilarByCosineDistance("[0.2, 0.1, 0.8, 0.3]", 5)
     */
    @Query(
            value =
                    """
            SELECT i.*,
                   i.embedding <=> CAST(:queryVector AS vector) AS distance
            FROM items i
            ORDER BY i.embedding <=> CAST(:queryVector AS vector)
            LIMIT :limit
            """,
            nativeQuery = true)
    List<ItemVector> findSimilarByCosineDistance(
            @Param("queryVector") String queryVector, @Param("limit") int limit);

    /**
     * Find similar items within a specific category.
     *
     * <p>Example: findSimilarInCategory("Fruit", "[1.0, 0.6, 0.1, 0.0]", 3)
     */
    @Query(
            value =
                    """
            SELECT i.*,
                   i.embedding <-> CAST(:queryVector AS vector) AS distance
            FROM items i
            WHERE i.category = :category
            ORDER BY i.embedding <-> CAST(:queryVector AS vector)
            LIMIT :limit
            """,
            nativeQuery = true)
    List<ItemVector> findSimilarInCategory(
            @Param("category") String category,
            @Param("queryVector") String queryVector,
            @Param("limit") int limit);
}
