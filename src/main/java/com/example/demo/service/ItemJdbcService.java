package com.example.demo.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.example.demo.entity.Item;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

/**
 * Service demonstrating JdbcClient usage for the items table.
 *
 * <p>JdbcClient is a modern, fluent API introduced in Spring Framework 6.1 that simplifies database
 * operations compared to JdbcTemplate.
 *
 * <p>This service shows various patterns for working with vector embeddings using JdbcClient.
 */
@Service
public class ItemJdbcService {

    private final JdbcClient jdbcClient;

    public ItemJdbcService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /** Find an item by ID. */
    public Optional<Item> findById(Long id) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at
                FROM items
                WHERE id = :id
                """;

        return jdbcClient.sql(sql).param("id", id).query(this::mapRowToItem).optional();
    }

    /** Find all items. */
    public List<Item> findAll() {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at
                FROM items
                ORDER BY name
                """;

        return jdbcClient.sql(sql).query(this::mapRowToItem).list();
    }

    /** Find items by category. */
    public List<Item> findByCategory(String category) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at
                FROM items
                WHERE category = :category
                ORDER BY name
                """;

        return jdbcClient.sql(sql).param("category", category).query(this::mapRowToItem).list();
    }

    /**
     * Find similar items using L2 distance (Euclidean distance). The <-> operator calculates L2
     * distance. Smaller distance = more similar.
     */
    public List<ItemWithDistance> findSimilarByL2Distance(String queryVector, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> :queryVector::vector AS distance
                FROM items
                ORDER BY embedding <-> :queryVector::vector
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /**
     * Find similar items using cosine distance. The <=> operator calculates cosine distance. Best
     * for normalized/directional similarity.
     */
    public List<ItemWithDistance> findSimilarByCosineDistance(String queryVector, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <=> :queryVector::vector AS distance
                FROM items
                ORDER BY embedding <=> :queryVector::vector
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /**
     * Find similar items using inner product. The <#> operator calculates negative inner product.
     * Higher absolute value = more similar.
     */
    public List<ItemWithDistance> findSimilarByInnerProduct(String queryVector, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <#> :queryVector::vector AS distance
                FROM items
                ORDER BY embedding <#> :queryVector::vector
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /** Find items within a specific L2 distance threshold. */
    public List<ItemWithDistance> findWithinDistance(String queryVector, double threshold) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> :queryVector::vector AS distance
                FROM items
                WHERE embedding <-> :queryVector::vector < :threshold
                ORDER BY distance
                """;

        return jdbcClient
                .sql(sql)
                .param("queryVector", queryVector)
                .param("threshold", threshold)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /** Find similar items within a specific category. */
    public List<ItemWithDistance> findSimilarInCategory(
            String category, String queryVector, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> :queryVector::vector AS distance
                FROM items
                WHERE category = :category
                ORDER BY distance
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("category", category)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /** Find similar items under a specific price. */
    public List<ItemWithDistance> findSimilarUnderPrice(
            BigDecimal maxPrice, String queryVector, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> :queryVector::vector AS distance
                FROM items
                WHERE price < :maxPrice
                ORDER BY distance
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("maxPrice", maxPrice)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /** Complex filtering: category, price range, and similarity. */
    public List<ItemWithDistance> findSimilarWithFilters(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String queryVector,
            int limit) {

        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <=> :queryVector::vector AS distance
                FROM items
                WHERE category = :category
                  AND price BETWEEN :minPrice AND :maxPrice
                ORDER BY distance
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("category", category)
                .param("minPrice", minPrice)
                .param("maxPrice", maxPrice)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /** Compare all distance metrics for the same query. */
    public List<ItemWithAllDistances> compareDistanceMetrics(String queryVector, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> :queryVector::vector AS l2_distance,
                       embedding <=> :queryVector::vector AS cosine_distance,
                       embedding <#> :queryVector::vector AS neg_inner_product,
                       embedding <+> :queryVector::vector AS l1_distance
                FROM items
                ORDER BY l2_distance
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithAllDistances)
                .list();
    }

    // ========== CAST() Syntax Examples ==========

    /**
     * Find similar items using L2 distance with CAST() function. This is an alternative to ::vector
     * using SQL-standard CAST() syntax.
     *
     * <p>Example: findSimilarByL2DistanceUsingCast("[1.0, 0.5, 0.2, 0.1]", 5)
     */
    public List<ItemWithDistance> findSimilarByL2DistanceUsingCast(String queryVector, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> CAST(:queryVector AS vector) AS distance
                FROM items
                ORDER BY embedding <-> CAST(:queryVector AS vector)
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /**
     * Find similar items using cosine distance with CAST() function. SQL-standard alternative to
     * ::vector casting.
     *
     * <p>Example: findSimilarByCosineDistanceUsingCast("[0.2, 0.1, 0.8, 0.3]", 5)
     */
    public List<ItemWithDistance> findSimilarByCosineDistanceUsingCast(
            String queryVector, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <=> CAST(:queryVector AS vector) AS distance
                FROM items
                ORDER BY embedding <=> CAST(:queryVector AS vector)
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /**
     * Find similar items using inner product with CAST() function.
     *
     * <p>Example: findSimilarByInnerProductUsingCast("[0.1, 0.2, 0.3, 0.9]", 5)
     */
    public List<ItemWithDistance> findSimilarByInnerProductUsingCast(
            String queryVector, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <#> CAST(:queryVector AS vector) AS distance
                FROM items
                ORDER BY embedding <#> CAST(:queryVector AS vector)
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /**
     * Find items within distance threshold using CAST() function.
     *
     * <p>Example: findWithinDistanceUsingCast("[1.0, 0.5, 0.2, 0.1]", 0.5)
     */
    public List<ItemWithDistance> findWithinDistanceUsingCast(
            String queryVector, double threshold) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> CAST(:queryVector AS vector) AS distance
                FROM items
                WHERE embedding <-> CAST(:queryVector AS vector) < :threshold
                ORDER BY distance
                """;

        return jdbcClient
                .sql(sql)
                .param("queryVector", queryVector)
                .param("threshold", threshold)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    /**
     * Compare all distance metrics using CAST() function. Demonstrates SQL-standard casting for all
     * distance operators.
     *
     * <p>Example: compareDistanceMetricsUsingCast("[1.0, 0.5, 0.2, 0.1]", 5)
     */
    public List<ItemWithAllDistances> compareDistanceMetricsUsingCast(
            String queryVector, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> CAST(:queryVector AS vector) AS l2_distance,
                       embedding <=> CAST(:queryVector AS vector) AS cosine_distance,
                       embedding <#> CAST(:queryVector AS vector) AS neg_inner_product,
                       embedding <+> CAST(:queryVector AS vector) AS l1_distance
                FROM items
                ORDER BY l2_distance
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithAllDistances)
                .list();
    }

    /**
     * Mixed casting example: Uses both CAST() and ::vector in same query. Shows that both methods
     * work identically and can be mixed.
     *
     * <p>Example: compareDistanceMetricsMixedCasting("[1.0, 0.5, 0.2, 0.1]", 5)
     */
    public List<ItemWithAllDistances> compareDistanceMetricsMixedCasting(
            String queryVector, int limit) {
        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <-> CAST(:queryVector AS vector) AS l2_distance,
                       embedding <=> :queryVector::vector AS cosine_distance,
                       embedding <#> CAST(:queryVector AS vector) AS neg_inner_product,
                       embedding <+> :queryVector::vector AS l1_distance
                FROM items
                ORDER BY l2_distance
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithAllDistances)
                .list();
    }

    /**
     * Complex filtered search using CAST() function. Demonstrates CAST() with WHERE clauses and
     * multiple conditions.
     *
     * <p>Example: findSimilarWithFiltersUsingCast("Vegetable", 1.00, 2.00, "[0.2, 0.1, 0.8, 0.3]",
     * 5)
     */
    public List<ItemWithDistance> findSimilarWithFiltersUsingCast(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String queryVector,
            int limit) {

        String sql =
                """
                SELECT id, name, category, price, embedding, created_at,
                       embedding <=> CAST(:queryVector AS vector) AS distance
                FROM items
                WHERE category = :category
                  AND price BETWEEN :minPrice AND :maxPrice
                ORDER BY distance
                LIMIT :limit
                """;

        return jdbcClient
                .sql(sql)
                .param("category", category)
                .param("minPrice", minPrice)
                .param("maxPrice", maxPrice)
                .param("queryVector", queryVector)
                .param("limit", limit)
                .query(this::mapRowToItemWithDistance)
                .list();
    }

    // ========== Insert/Update with CAST() ==========

    /** Insert a new item using CAST() function. Alternative to ::vector for INSERT operations. */
    public int insertUsingCast(Item item) {
        String sql =
                """
                INSERT INTO items (name, category, price, embedding)
                VALUES (:name, :category, :price, CAST(:embedding AS vector))
                """;

        return jdbcClient
                .sql(sql)
                .param("name", item.getName())
                .param("category", item.getCategory())
                .param("price", item.getPrice())
                .param("embedding", vectorToString(item.getEmbedding()))
                .update();
    }

    /** Update an item using CAST() function. Alternative to ::vector for UPDATE operations. */
    public int updateUsingCast(Item item) {
        String sql =
                """
                UPDATE items
                SET name = :name,
                    category = :category,
                    price = :price,
                    embedding = CAST(:embedding AS vector)
                WHERE id = :id
                """;

        return jdbcClient
                .sql(sql)
                .param("id", item.getId())
                .param("name", item.getName())
                .param("category", item.getCategory())
                .param("price", item.getPrice())
                .param("embedding", vectorToString(item.getEmbedding()))
                .update();
    }

    /** Insert a new item. */
    public int insert(Item item) {
        String sql =
                """
                INSERT INTO items (name, category, price, embedding)
                VALUES (:name, :category, :price, :embedding::vector)
                """;

        return jdbcClient
                .sql(sql)
                .param("name", item.getName())
                .param("category", item.getCategory())
                .param("price", item.getPrice())
                .param("embedding", vectorToString(item.getEmbedding()))
                .update();
    }

    /** Update an item. */
    public int update(Item item) {
        String sql =
                """
                UPDATE items
                SET name = :name,
                    category = :category,
                    price = :price,
                    embedding = :embedding::vector
                WHERE id = :id
                """;

        return jdbcClient
                .sql(sql)
                .param("id", item.getId())
                .param("name", item.getName())
                .param("category", item.getCategory())
                .param("price", item.getPrice())
                .param("embedding", vectorToString(item.getEmbedding()))
                .update();
    }

    /** Delete an item by ID. */
    public int deleteById(Long id) {
        String sql = "DELETE FROM items WHERE id = :id";

        return jdbcClient.sql(sql).param("id", id).update();
    }

    /** Count items by category. */
    public List<CategoryCount> countByCategory() {
        String sql =
                """
                SELECT category, COUNT(*) as item_count
                FROM items
                GROUP BY category
                ORDER BY category
                """;

        return jdbcClient
                .sql(sql)
                .query(
                        (rs, rowNum) ->
                                new CategoryCount(
                                        rs.getString("category"), rs.getLong("item_count")))
                .list();
    }

    // Row mappers

    private Item mapRowToItem(ResultSet rs, int rowNum) throws SQLException {
        Item item = new Item();
        item.setId(rs.getLong("id"));
        item.setName(rs.getString("name"));
        item.setCategory(rs.getString("category"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setEmbedding(parseVector(rs.getString("embedding")));
        item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return item;
    }

    private ItemWithDistance mapRowToItemWithDistance(ResultSet rs, int rowNum)
            throws SQLException {
        Item item = mapRowToItem(rs, rowNum);
        double distance = rs.getDouble("distance");
        return new ItemWithDistance(item, distance);
    }

    private ItemWithAllDistances mapRowToItemWithAllDistances(ResultSet rs, int rowNum)
            throws SQLException {
        Item item = mapRowToItem(rs, rowNum);
        return new ItemWithAllDistances(
                item,
                rs.getDouble("l2_distance"),
                rs.getDouble("cosine_distance"),
                rs.getDouble("neg_inner_product"),
                rs.getDouble("l1_distance"));
    }

    // Helper methods

    private float[] parseVector(String vectorString) {
        if (vectorString == null || vectorString.isEmpty()) {
            return null;
        }

        // Remove brackets and split by comma
        String cleaned = vectorString.replaceAll("[\\[\\]]", "").trim();
        String[] parts = cleaned.split(",");
        float[] vector = new float[parts.length];

        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }

        return vector;
    }

    private String vectorToString(float[] vector) {
        if (vector == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    // Result classes

    public record ItemWithDistance(Item item, double distance) {
        @Override
        public String toString() {
            return "ItemWithDistance{" + "item=" + item.getName() + ", distance=" + distance + '}';
        }
    }

    public record ItemWithAllDistances(
            Item item,
            double l2Distance,
            double cosineDistance,
            double negInnerProduct,
            double l1Distance) {

        @Override
        public String toString() {
            return "ItemWithAllDistances{"
                    + "item="
                    + item.getName()
                    + ", l2="
                    + l2Distance
                    + ", cosine="
                    + cosineDistance
                    + ", innerProduct="
                    + negInnerProduct
                    + ", l1="
                    + l1Distance
                    + '}';
        }
    }

    public record CategoryCount(String category, Long count) {}
}
