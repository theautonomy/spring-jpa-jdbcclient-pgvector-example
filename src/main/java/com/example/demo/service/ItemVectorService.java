package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.demo.entity.ItemVector;
import com.example.demo.repository.ItemVectorRepository;

import org.springframework.stereotype.Service;

/**
 * Service layer for ItemVector entity demonstrating the custom UserType approach.
 *
 * <p>This service shows how to work with vectors using the pgvector-java library through a custom
 * Hibernate UserType.
 */
@Service
public class ItemVectorService {

    private final ItemVectorRepository itemVectorRepository;

    public ItemVectorService(ItemVectorRepository itemVectorRepository) {
        this.itemVectorRepository = itemVectorRepository;
    }

    // ========== Standard CRUD Operations ==========

    /** Get all items. */
    public List<ItemVector> getAllItems() {
        return itemVectorRepository.findAll();
    }

    /** Get item by ID. */
    public Optional<ItemVector> getItemById(Long id) {
        return itemVectorRepository.findById(id);
    }

    /** Save or update an item. */
    public ItemVector saveItem(ItemVector item) {
        return itemVectorRepository.save(item);
    }

    /** Delete an item. */
    public void deleteItem(Long id) {
        itemVectorRepository.deleteById(id);
    }

    // ========== Basic Queries ==========

    /**
     * Find items by category.
     *
     * <p>Example: findByCategory("Fruit")
     */
    public List<ItemVector> findByCategory(String category) {
        return itemVectorRepository.findByCategory(category);
    }

    /**
     * Find items under a specific price.
     *
     * <p>Example: findItemsUnderPrice(BigDecimal.valueOf(3.00))
     */
    public List<ItemVector> findItemsUnderPrice(BigDecimal price) {
        return itemVectorRepository.findByPriceLessThan(price);
    }

    // ========== Vector Similarity Search ==========

    /**
     * Find items similar to a query vector using L2 distance.
     *
     * <p>Example: findSimilarItemsL2("[1.0, 0.5, 0.2, 0.1]", 5)
     */
    public List<ItemVector> findSimilarItemsL2(String queryVector, int limit) {
        return itemVectorRepository.findSimilarByL2Distance(queryVector, limit);
    }

    /**
     * Find items similar to a query vector using cosine distance.
     *
     * <p>Example: findSimilarItemsCosine("[0.2, 0.1, 0.8, 0.3]", 5)
     */
    public List<ItemVector> findSimilarItemsCosine(String queryVector, int limit) {
        return itemVectorRepository.findSimilarByCosineDistance(queryVector, limit);
    }

    /**
     * Find similar items within a specific category.
     *
     * <p>Example: findSimilarInCategory("Fruit", "[1.0, 0.6, 0.1, 0.0]", 3)
     */
    public List<ItemVector> findSimilarInCategory(String category, String queryVector, int limit) {
        return itemVectorRepository.findSimilarInCategory(category, queryVector, limit);
    }

    // ========== Utility Methods ==========

    /**
     * Convert a float array to a PostgreSQL vector string.
     *
     * <p>Example: toVectorString(new float[]{1.0f, 0.5f, 0.2f, 0.1f}) => "[1.0,0.5,0.2,0.1]"
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
}
