package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.entity.Item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for Item entity with vector similarity search support.
 *
 * <p>Demonstrates various query patterns: - Standard CRUD operations (inherited from JpaRepository)
 * - Native queries for vector similarity searches (L2, cosine, inner product) - Filtered similarity
 * searches
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Standard JPA queries
    List<Item> findByCategory(String category);

    List<Item> findByPriceLessThan(BigDecimal price);

    List<Item> findByCategoryAndPriceBetween(
            String category, BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find items similar to a query vector using L2 distance. Smaller distance = more similar.
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
    List<Item> findSimilarByL2Distance(
            @Param("queryVector") String queryVector, @Param("limit") int limit);

    /**
     * Find items similar to a query vector using cosine distance. Best for normalized/directional
     * similarity.
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
    List<Item> findSimilarByCosineDistance(
            @Param("queryVector") String queryVector, @Param("limit") int limit);

    /**
     * Find items similar to a query vector using inner product. Higher absolute value = more
     * similar.
     *
     * <p>Example: findSimilarByInnerProduct("[0.1, 0.2, 0.3, 0.9]", 5)
     */
    @Query(
            value =
                    """
            SELECT i.*,
                   i.embedding <#> CAST(:queryVector AS vector) AS distance
            FROM items i
            ORDER BY i.embedding <#> CAST(:queryVector AS vector)
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Item> findSimilarByInnerProduct(
            @Param("queryVector") String queryVector, @Param("limit") int limit);

    /**
     * Find items within a specific L2 distance threshold.
     *
     * <p>Example: findWithinDistance("[1.0, 0.5, 0.2, 0.1]", 0.5)
     */
    @Query(
            value =
                    """
            SELECT i.*
            FROM items i
            WHERE i.embedding <-> CAST(:queryVector AS vector) < :threshold
            ORDER BY i.embedding <-> CAST(:queryVector AS vector)
            """,
            nativeQuery = true)
    List<Item> findWithinDistance(
            @Param("queryVector") String queryVector, @Param("threshold") double threshold);

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
    List<Item> findSimilarInCategory(
            @Param("category") String category,
            @Param("queryVector") String queryVector,
            @Param("limit") int limit);

    /**
     * Find similar items under a specific price.
     *
     * <p>Example: findSimilarUnderPrice(3.00, "[0.5, 0.5, 0.5, 0.5]", 5)
     */
    @Query(
            value =
                    """
            SELECT i.*,
                   i.embedding <-> CAST(:queryVector AS vector) AS distance
            FROM items i
            WHERE i.price < :maxPrice
            ORDER BY i.embedding <-> CAST(:queryVector AS vector)
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Item> findSimilarUnderPrice(
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("queryVector") String queryVector,
            @Param("limit") int limit);

    /**
     * Complex filtering: category, price range, and similarity.
     *
     * <p>Example: findSimilarWithFilters("Vegetable", 1.00, 2.00, "[0.2, 0.1, 0.8, 0.3]", 5)
     */
    @Query(
            value =
                    """
            SELECT i.*,
                   i.embedding <=> CAST(:queryVector AS vector) AS distance
            FROM items i
            WHERE i.category = :category
              AND i.price BETWEEN :minPrice AND :maxPrice
            ORDER BY i.embedding <=> CAST(:queryVector AS vector)
            LIMIT :limit
            """,
            nativeQuery = true)
    List<Item> findSimilarWithFilters(
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("queryVector") String queryVector,
            @Param("limit") int limit);
}
