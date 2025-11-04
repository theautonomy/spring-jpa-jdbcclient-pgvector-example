package com.example.demo.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.example.demo.entity.Item;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 * Examples demonstrating different ways to cast strings to vectors in SQL queries.
 *
 * <p>PostgreSQL pgvector supports multiple casting syntaxes: 1. CAST(string AS vector) 2.
 * string::vector 3. Direct string literal '[1.0, 0.5, 0.2, 0.1]'
 */
@Service
public class VectorCastingExamples {

    private final JdbcClient jdbcClient;
    private final EntityManager entityManager;

    public VectorCastingExamples(JdbcClient jdbcClient, EntityManager entityManager) {
        this.jdbcClient = jdbcClient;
        this.entityManager = entityManager;
    }

    // ========== Method 1: Using CAST() Syntax ==========

    /** Method 1a: Using CAST() with JdbcClient Most explicit and SQL-standard approach */
    public List<ItemWithDistance> findSimilarUsingCastFunction(String vectorString, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> CAST(:vectorString AS vector) AS distance
                FROM items
                ORDER BY embedding <-> CAST(:vectorString AS vector)
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("vectorString", vectorString)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /** Method 1b: Using CAST() with JPA native query */
    @SuppressWarnings("unchecked")
    public List<Object[]> findSimilarUsingCastJPA(String vectorString, int limit) {
        String jpql =
                """
                SELECT i, (i.embedding <-> CAST(:vectorString AS vector)) AS distance
                FROM Item i
                ORDER BY (i.embedding <-> CAST(:vectorString AS vector))
                """;

        Query query = entityManager.createQuery(jpql);
        query.setParameter("vectorString", vectorString);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    // ========== Method 2: Using :: PostgreSQL Cast Operator ==========

    /**
     * Method 2a: Using ::vector with JdbcClient PostgreSQL-specific shorthand, most common in
     * PostgreSQL
     */
    public List<ItemWithDistance> findSimilarUsingColonCast(String vectorString, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> :vectorString::vector AS distance
                FROM items
                ORDER BY embedding <-> :vectorString::vector
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("vectorString", vectorString)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /**
     * Method 2b: Using ::vector with text concatenation (less safe) Shows how to build SQL
     * dynamically (not recommended for production)
     */
    public List<ItemWithDistance> findSimilarUsingTextConcatenation(
            String vectorString, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> '%s'::vector AS distance
                FROM items
                ORDER BY embedding <-> '%s'::vector
                LIMIT %d
                """
                        .formatted(vectorString, vectorString, limit);

        return jdbcClient.sql(sql).query(this::mapRowToItemWithDistance).list();
    }

    // ========== Method 3: Using Direct String Literals ==========

    /**
     * Method 3: Using string literal directly (pgvector auto-casts) Works in some contexts but less
     * explicit
     */
    public List<ItemWithDistance> findSimilarUsingDirectString(String vectorString, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> :vectorString AS distance
                FROM items
                ORDER BY embedding <-> :vectorString
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("vectorString", vectorString)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    // ========== Method 4: Using pgvector Constructor Function ==========

    /** Method 4: Using vector constructor with array Creates vector from array of floats */
    public List<ItemWithDistance> findSimilarUsingVectorConstructor(
            float[] vectorArray, int limit) {
        // Convert float array to PostgreSQL array syntax: ARRAY[1.0, 0.5, 0.2, 0.1]
        String arrayString = convertToPostgresArray(vectorArray);

        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> vector(:arrayString) AS distance
                FROM items
                ORDER BY embedding <-> vector(:arrayString)
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("arrayString", arrayString)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    // ========== Complex Examples with Multiple Operators ==========

    /** Example 5: Compare all distance metrics using different cast methods */
    public List<ItemWithAllDistances> compareDistancesWithCasting(String vectorString, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> CAST(:vec AS vector) AS l2_distance,
                       embedding <=> :vec::vector AS cosine_distance,
                       embedding <#> CAST(:vec AS vector) AS neg_inner_product,
                       embedding <+> :vec::vector AS l1_distance
                FROM items
                ORDER BY l2_distance
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("vec", vectorString)
                .param("limit", limit)
                .query(this::mapRowToItemWithAllDistances)
                .list();
    }

    /** Example 6: Insert with vector casting */
    public int insertItemWithVectorCast(
            String name, String category, BigDecimal price, String vectorString) {
        // Method 1: Using CAST
        String sql1 =
                """
                INSERT INTO items (name, category, price, embedding)
                VALUES (:name, :category, :price, CAST(:embedding AS vector))
                """;

        return jdbcClient
                .sql(sql1)
                .param("name", name)
                .param("category", category)
                .param("price", price)
                .param("embedding", vectorString)
                .update();
    }

    /** Example 7: Update with vector casting */
    public int updateItemWithVectorCast(Long id, String vectorString) {
        // Method 2: Using ::vector
        String sql =
                """
                UPDATE items
                SET embedding = :embedding::vector
                WHERE id = :id
                """;

        return jdbcClient.sql(sql).param("id", id).param("embedding", vectorString).update();
    }

    /** Example 8: Parameterized search with filters and casting */
    public List<ItemWithDistance> complexSearchWithCasting(
            String vectorString,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            double maxDistance,
            int limit) {

        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> CAST(:vector AS vector) AS distance
                FROM items
                WHERE category = :category
                  AND price BETWEEN :minPrice AND :maxPrice
                  AND embedding <-> :vector::vector < :maxDistance
                ORDER BY distance
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("vector", vectorString)
                .param("category", category)
                .param("minPrice", minPrice)
                .param("maxPrice", maxPrice)
                .param("maxDistance", maxDistance)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /** Example 9: Using subquery with vector casting */
    public List<ItemWithDistance> findItemsNearCategoryCenter(String category, int limit) {
        String sql =
                """
                WITH category_center AS (
                    SELECT AVG(embedding) AS center_vector
                    FROM items
                    WHERE category = :category
                )
                SELECT i.id, i.name, i.category, i.price, i.embedding, i.created_at,
                       i.embedding <-> CAST(cc.center_vector AS vector) AS distance
                FROM items i
                CROSS JOIN category_center cc
                WHERE i.category = :category
                ORDER BY distance
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("category", category)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /** Example 10: Multiple vector comparisons in one query */
    public List<Object[]> compareWithMultipleVectors(
            String vector1, String vector2, String vector3, int limit) {

        String sql =
                """
                SELECT
                    name,
                    embedding <-> :v1::vector AS dist_to_v1,
                    embedding <-> :v2::vector AS dist_to_v2,
                    embedding <-> :v3::vector AS dist_to_v3,
                    LEAST(
                        embedding <-> :v1::vector,
                        embedding <-> :v2::vector,
                        embedding <-> :v3::vector
                    ) AS min_distance
                FROM items
                ORDER BY min_distance
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("v1", vector1)
                .param("v2", vector2)
                .param("v3", vector3)
                .param("limit", limit)
                .query(
                        (rs, rowNum) ->
                                new Object[] {
                                    rs.getString("name"),
                                    rs.getDouble("dist_to_v1"),
                                    rs.getDouble("dist_to_v2"),
                                    rs.getDouble("dist_to_v3"),
                                    rs.getDouble("min_distance")
                                })
                .list();
    }

    // ========== Helper Methods ==========

    private String convertToPostgresArray(float[] array) {
        StringBuilder sb = new StringBuilder("ARRAY[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private ItemWithDistance mapRowToItemWithDistance(ResultSet rs, int rowNum)
            throws SQLException {
        Item item = new Item();
        item.setId(rs.getLong("id"));
        item.setName(rs.getString("name"));
        item.setCategory(rs.getString("category"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setEmbedding(parseVector(rs.getString("embedding")));
        item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        double distance = rs.getDouble("distance");
        return new ItemWithDistance(item, distance);
    }

    private ItemWithAllDistances mapRowToItemWithAllDistances(ResultSet rs, int rowNum)
            throws SQLException {
        Item item = new Item();
        item.setId(rs.getLong("id"));
        item.setName(rs.getString("name"));
        item.setCategory(rs.getString("category"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setEmbedding(parseVector(rs.getString("embedding")));
        item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        return new ItemWithAllDistances(
                item,
                rs.getDouble("l2_distance"),
                rs.getDouble("cosine_distance"),
                rs.getDouble("neg_inner_product"),
                rs.getDouble("l1_distance"));
    }

    private float[] parseVector(String vectorString) {
        if (vectorString == null || vectorString.isEmpty()) {
            return null;
        }
        String cleaned = vectorString.replaceAll("[\\[\\]]", "").trim();
        String[] parts = cleaned.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }
        return vector;
    }

    // Result classes
    public record ItemWithDistance(Item item, double distance) {}

    public record ItemWithAllDistances(
            Item item,
            double l2Distance,
            double cosineDistance,
            double negInnerProduct,
            double l1Distance) {}
}
