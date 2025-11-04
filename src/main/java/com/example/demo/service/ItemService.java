package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.demo.entity.Item;
import com.example.demo.repository.ItemRepository;

import org.springframework.stereotype.Service;

/**
 * Unified service layer that demonstrates both JPA and JdbcClient approaches for working with the
 * items table.
 *
 * <p>This service primarily uses JPA (via ItemRepository) for simplicity, but you can easily switch
 * to JdbcClient (via ItemJdbcService) for more fine-grained control or performance optimization.
 *
 * <p>Usage examples are provided in the method documentation.
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemJdbcService itemJdbcService;

    public ItemService(ItemRepository itemRepository, ItemJdbcService itemJdbcService) {
        this.itemRepository = itemRepository;
        this.itemJdbcService = itemJdbcService;
    }

    // ========== Standard CRUD Operations (JPA) ==========

    /** Get all items. Uses JPA. */
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    /** Get item by ID. Uses JPA. */
    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    /** Save or update an item. Uses JPA. */
    public Item saveItem(Item item) {
        return itemRepository.save(item);
    }

    /** Delete an item. Uses JPA. */
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    // ========== Basic Queries (JPA) ==========

    /**
     * Find items by category. Uses JPA.
     *
     * <p>Example: findByCategory("Fruit")
     */
    public List<Item> findByCategory(String category) {
        return itemRepository.findByCategory(category);
    }

    /**
     * Find items under a specific price. Uses JPA.
     *
     * <p>Example: findItemsUnderPrice(BigDecimal.valueOf(3.00))
     */
    public List<Item> findItemsUnderPrice(BigDecimal price) {
        return itemRepository.findByPriceLessThan(price);
    }

    // ========== Vector Similarity Search (JPA) ==========

    /**
     * Find items similar to a query vector using L2 distance. Uses JPA with native query.
     *
     * <p>Example: - findSimilarItemsL2("[1.0, 0.5, 0.2, 0.1]", 5) // Find 5 items similar to Apple
     */
    public List<Item> findSimilarItemsL2(String queryVector, int limit) {
        return itemRepository.findSimilarByL2Distance(queryVector, limit);
    }

    /**
     * Find items similar to a query vector using cosine distance. Uses JPA with native query.
     *
     * <p>Example: - findSimilarItemsCosine("[0.2, 0.1, 0.8, 0.3]", 5) // Find vegetables
     */
    public List<Item> findSimilarItemsCosine(String queryVector, int limit) {
        return itemRepository.findSimilarByCosineDistance(queryVector, limit);
    }

    /**
     * Find items similar to a query vector using inner product. Uses JPA with native query.
     *
     * <p>Example: - findSimilarItemsInnerProduct("[0.1, 0.2, 0.3, 0.9]", 5) // Find meat items
     */
    public List<Item> findSimilarItemsInnerProduct(String queryVector, int limit) {
        return itemRepository.findSimilarByInnerProduct(queryVector, limit);
    }

    /**
     * Find items within a distance threshold. Uses JPA with native query.
     *
     * <p>Example: - findItemsWithinDistance("[1.0, 0.5, 0.2, 0.1]", 0.5) // Items very similar to
     * Apple
     */
    public List<Item> findItemsWithinDistance(String queryVector, double threshold) {
        return itemRepository.findWithinDistance(queryVector, threshold);
    }

    // ========== Filtered Similarity Search (JPA) ==========

    /**
     * Find similar items within a specific category. Uses JPA with native query.
     *
     * <p>Example: - findSimilarInCategory("Fruit", "[1.0, 0.6, 0.1, 0.0]", 3) // Similar fruits
     * only
     */
    public List<Item> findSimilarInCategory(String category, String queryVector, int limit) {
        return itemRepository.findSimilarInCategory(category, queryVector, limit);
    }

    /**
     * Find similar items under a specific price. Uses JPA with native query.
     *
     * <p>Example: - findSimilarUnderPrice(3.00, "[0.5, 0.5, 0.5, 0.5]", 5) // Affordable similar
     * items
     */
    public List<Item> findSimilarUnderPrice(BigDecimal maxPrice, String queryVector, int limit) {
        return itemRepository.findSimilarUnderPrice(maxPrice, queryVector, limit);
    }

    /**
     * Find similar items with complex filters. Uses JPA with native query.
     *
     * <p>Example: - findSimilarWithFilters("Vegetable", 1.00, 2.00, "[0.2, 0.1, 0.8, 0.3]", 5) //
     * Find similar vegetables priced between $1-$2
     */
    public List<Item> findSimilarWithFilters(
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String queryVector,
            int limit) {

        return itemRepository.findSimilarWithFilters(
                category, minPrice, maxPrice, queryVector, limit);
    }

    // ========== JdbcClient Examples ==========

    /**
     * Find similar items using JdbcClient with distance information. Uses JdbcClient for more
     * detailed results including distance values.
     *
     * <p>Example: - findSimilarWithJdbcClient("[1.0, 0.5, 0.2, 0.1]", 5)
     */
    public List<ItemJdbcService.ItemWithDistance> findSimilarWithJdbcClient(
            String queryVector, int limit) {
        return itemJdbcService.findSimilarByL2Distance(queryVector, limit);
    }

    /**
     * Compare all distance metrics using JdbcClient. Shows L2, cosine, inner product, and L1
     * distances for the same query.
     *
     * <p>Example: - compareAllDistances("[1.0, 0.5, 0.2, 0.1]", 5)
     */
    public List<ItemJdbcService.ItemWithAllDistances> compareAllDistances(
            String queryVector, int limit) {
        return itemJdbcService.compareDistanceMetrics(queryVector, limit);
    }

    /**
     * Get category counts using JdbcClient.
     *
     * <p>Example: - getCategoryCounts()
     */
    public List<ItemJdbcService.CategoryCount> getCategoryCounts() {
        return itemJdbcService.countByCategory();
    }

    // ========== CAST() Syntax Examples (JdbcClient) ==========

    /**
     * Find similar items using CAST() function instead of ::vector. Demonstrates SQL-standard
     * CAST() syntax with L2 distance.
     *
     * <p>Example: - findSimilarWithCast("[1.0, 0.5, 0.2, 0.1]", 5)
     */
    public List<ItemJdbcService.ItemWithDistance> findSimilarWithCast(
            String queryVector, int limit) {
        return itemJdbcService.findSimilarByL2DistanceUsingCast(queryVector, limit);
    }

    /**
     * Find similar items using CAST() with cosine distance.
     *
     * <p>Example: - findSimilarCosineWithCast("[0.2, 0.1, 0.8, 0.3]", 5)
     */
    public List<ItemJdbcService.ItemWithDistance> findSimilarCosineWithCast(
            String queryVector, int limit) {
        return itemJdbcService.findSimilarByCosineDistanceUsingCast(queryVector, limit);
    }

    /**
     * Find similar items using CAST() with inner product.
     *
     * <p>Example: - findSimilarInnerProductWithCast("[0.1, 0.2, 0.3, 0.9]", 5)
     */
    public List<ItemJdbcService.ItemWithDistance> findSimilarInnerProductWithCast(
            String queryVector, int limit) {
        return itemJdbcService.findSimilarByInnerProductUsingCast(queryVector, limit);
    }

    /**
     * Compare all distance metrics using CAST() function. Alternative to ::vector syntax using
     * SQL-standard CAST().
     *
     * <p>Example: - compareAllDistancesWithCast("[1.0, 0.5, 0.2, 0.1]", 5)
     */
    public List<ItemJdbcService.ItemWithAllDistances> compareAllDistancesWithCast(
            String queryVector, int limit) {
        return itemJdbcService.compareDistanceMetricsUsingCast(queryVector, limit);
    }

    /**
     * Compare distance metrics using mixed casting (CAST() and ::vector). Shows that both casting
     * methods work identically and can be mixed.
     *
     * <p>Example: - compareAllDistancesMixed("[1.0, 0.5, 0.2, 0.1]", 5)
     */
    public List<ItemJdbcService.ItemWithAllDistances> compareAllDistancesMixed(
            String queryVector, int limit) {
        return itemJdbcService.compareDistanceMetricsMixedCasting(queryVector, limit);
    }

    /**
     * Find items within distance threshold using CAST().
     *
     * <p>Example: - findWithinDistanceWithCast("[1.0, 0.5, 0.2, 0.1]", 0.5)
     */
    public List<ItemJdbcService.ItemWithDistance> findWithinDistanceWithCast(
            String queryVector, double threshold) {
        return itemJdbcService.findWithinDistanceUsingCast(queryVector, threshold);
    }

    // ========== Utility Methods ==========

    /**
     * Convert a float array to a PostgreSQL vector string.
     *
     * <p>Example: - toVectorString(new float[]{1.0f, 0.5f, 0.2f, 0.1f}) => "[1.0,0.5,0.2,0.1]"
     */
    public static String toVectorString(float[] vector) {
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

    /**
     * Parse a PostgreSQL vector string to a float array.
     *
     * <p>Example: - parseVectorString("[1.0,0.5,0.2,0.1]") => float[]{1.0f, 0.5f, 0.2f, 0.1f}
     */
    public static float[] parseVectorString(String vectorString) {
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
}
