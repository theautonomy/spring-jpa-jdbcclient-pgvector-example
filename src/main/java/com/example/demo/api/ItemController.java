package com.example.demo.api;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.entity.Item;
import com.example.demo.service.ItemJdbcService;
import com.example.demo.service.ItemService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API controller for the items table. Demonstrates both JPA and JdbcClient approaches.
 *
 * <p>Endpoints: - GET /api/items - Get all items - GET /api/items/{id} - Get item by ID - GET
 * /api/items/category/{category} - Get items by category - GET /api/items/search/similar - Find
 * similar items (L2 distance) - GET /api/items/search/similar-cosine - Find similar items (cosine
 * distance) - GET /api/items/search/similar-category - Find similar items in category - GET
 * /api/items/search/compare-distances - Compare all distance metrics - POST /api/items - Create new
 * item - PUT /api/items/{id} - Update item - DELETE /api/items/{id} - Delete item
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Get all items.
     *
     * <p>Example: GET /api/items
     */
    @GetMapping
    public List<Item> getAllItems() {
        return itemService.getAllItems();
    }

    /**
     * Get item by ID.
     *
     * <p>Example: GET /api/items/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService
                .getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get items by category.
     *
     * <p>Example: GET /api/items/category/Fruit
     */
    @GetMapping("/category/{category}")
    public List<Item> getItemsByCategory(@PathVariable String category) {
        return itemService.findByCategory(category);
    }

    /**
     * Find similar items using L2 distance.
     *
     * <p>Example: GET /api/items/search/similar?vector=[1.0,0.5,0.2,0.1]&limit=5
     */
    @GetMapping("/search/similar")
    public List<Item> findSimilarItems(
            @RequestParam String vector, @RequestParam(defaultValue = "5") int limit) {

        return itemService.findSimilarItemsL2(vector, limit);
    }

    /**
     * Find similar items using cosine distance.
     *
     * <p>Example: GET /api/items/search/similar-cosine?vector=[0.2,0.1,0.8,0.3]&limit=5
     */
    @GetMapping("/search/similar-cosine")
    public List<Item> findSimilarItemsCosine(
            @RequestParam String vector, @RequestParam(defaultValue = "5") int limit) {

        return itemService.findSimilarItemsCosine(vector, limit);
    }

    /**
     * Find similar items within a category.
     *
     * <p>Example: GET
     * /api/items/search/similar-category?category=Fruit&vector=[1.0,0.6,0.1,0.0]&limit=3
     */
    @GetMapping("/search/similar-category")
    public List<Item> findSimilarInCategory(
            @RequestParam String category,
            @RequestParam String vector,
            @RequestParam(defaultValue = "3") int limit) {

        return itemService.findSimilarInCategory(category, vector, limit);
    }

    /**
     * Find similar items under a price threshold.
     *
     * <p>Example: GET
     * /api/items/search/similar-price?maxPrice=3.00&vector=[0.5,0.5,0.5,0.5]&limit=5
     */
    @GetMapping("/search/similar-price")
    public List<Item> findSimilarUnderPrice(
            @RequestParam BigDecimal maxPrice,
            @RequestParam String vector,
            @RequestParam(defaultValue = "5") int limit) {

        return itemService.findSimilarUnderPrice(maxPrice, vector, limit);
    }

    /**
     * Find similar items with complex filters.
     *
     * <p>Example: GET
     * /api/items/search/similar-filtered?category=Vegetable&minPrice=1.00&maxPrice=2.00&vector=[0.2,0.1,0.8,0.3]&limit=5
     */
    @GetMapping("/search/similar-filtered")
    public List<Item> findSimilarFiltered(
            @RequestParam String category,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam String vector,
            @RequestParam(defaultValue = "5") int limit) {

        return itemService.findSimilarWithFilters(category, minPrice, maxPrice, vector, limit);
    }

    /**
     * Compare all distance metrics for the same query (using JdbcClient).
     *
     * <p>Example: GET /api/items/search/compare-distances?vector=[1.0,0.5,0.2,0.1]&limit=5
     */
    @GetMapping("/search/compare-distances")
    public List<ItemJdbcService.ItemWithAllDistances> compareDistances(
            @RequestParam String vector, @RequestParam(defaultValue = "5") int limit) {

        return itemService.compareAllDistances(vector, limit);
    }

    /**
     * Get category counts.
     *
     * <p>Example: GET /api/items/stats/categories
     */
    @GetMapping("/stats/categories")
    public List<ItemJdbcService.CategoryCount> getCategoryCounts() {
        return itemService.getCategoryCounts();
    }

    // ========== CAST() Syntax Endpoints ==========

    /**
     * Find similar items using CAST() function (SQL standard syntax). This endpoint demonstrates
     * CAST(:vec AS vector) instead of :vec::vector.
     *
     * <p>Example: GET /api/items/search/similar-cast?vector=[1.0,0.5,0.2,0.1]&limit=5
     */
    @GetMapping("/search/similar-cast")
    public List<ItemJdbcService.ItemWithDistance> findSimilarWithCast(
            @RequestParam String vector, @RequestParam(defaultValue = "5") int limit) {

        return itemService.findSimilarWithCast(vector, limit);
    }

    /**
     * Find similar items using CAST() with cosine distance.
     *
     * <p>Example: GET /api/items/search/similar-cosine-cast?vector=[0.2,0.1,0.8,0.3]&limit=5
     */
    @GetMapping("/search/similar-cosine-cast")
    public List<ItemJdbcService.ItemWithDistance> findSimilarCosineWithCast(
            @RequestParam String vector, @RequestParam(defaultValue = "5") int limit) {

        return itemService.findSimilarCosineWithCast(vector, limit);
    }

    /**
     * Find similar items using CAST() with inner product.
     *
     * <p>Example: GET /api/items/search/similar-innerproduct-cast?vector=[0.1,0.2,0.3,0.9]&limit=5
     */
    @GetMapping("/search/similar-innerproduct-cast")
    public List<ItemJdbcService.ItemWithDistance> findSimilarInnerProductWithCast(
            @RequestParam String vector, @RequestParam(defaultValue = "5") int limit) {

        return itemService.findSimilarInnerProductWithCast(vector, limit);
    }

    /**
     * Compare all distance metrics using CAST() function. Shows that CAST() produces identical
     * results to ::vector.
     *
     * <p>Example: GET /api/items/search/compare-distances-cast?vector=[1.0,0.5,0.2,0.1]&limit=5
     */
    @GetMapping("/search/compare-distances-cast")
    public List<ItemJdbcService.ItemWithAllDistances> compareDistancesWithCast(
            @RequestParam String vector, @RequestParam(defaultValue = "5") int limit) {

        return itemService.compareAllDistancesWithCast(vector, limit);
    }

    /**
     * Compare distance metrics using mixed casting (CAST() and ::vector). Demonstrates that both
     * methods can be used together in the same query.
     *
     * <p>Example: GET /api/items/search/compare-distances-mixed?vector=[1.0,0.5,0.2,0.1]&limit=5
     */
    @GetMapping("/search/compare-distances-mixed")
    public List<ItemJdbcService.ItemWithAllDistances> compareDistancesMixed(
            @RequestParam String vector, @RequestParam(defaultValue = "5") int limit) {

        return itemService.compareAllDistancesMixed(vector, limit);
    }

    /**
     * Find items within distance threshold using CAST().
     *
     * <p>Example: GET /api/items/search/within-distance-cast?vector=[1.0,0.5,0.2,0.1]&threshold=0.5
     */
    @GetMapping("/search/within-distance-cast")
    public List<ItemJdbcService.ItemWithDistance> findWithinDistanceWithCast(
            @RequestParam String vector, @RequestParam double threshold) {

        return itemService.findWithinDistanceWithCast(vector, threshold);
    }

    /**
     * Create a new item.
     *
     * <p>Example: POST /api/items { "name": "Tomato", "category": "Vegetable", "price": 1.30,
     * "embedding": [0.25, 0.15, 0.75, 0.25] }
     */
    @PostMapping
    public Item createItem(@RequestBody Item item) {
        return itemService.saveItem(item);
    }

    /**
     * Update an existing item.
     *
     * <p>Example: PUT /api/items/1
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        return itemService
                .getItemById(id)
                .map(
                        existingItem -> {
                            item.setId(id);
                            return ResponseEntity.ok(itemService.saveItem(item));
                        })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete an item.
     *
     * <p>Example: DELETE /api/items/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        return itemService
                .getItemById(id)
                .map(
                        item -> {
                            itemService.deleteItem(id);
                            return ResponseEntity.ok().<Void>build();
                        })
                .orElse(ResponseEntity.notFound().build());
    }
}
