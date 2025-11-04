package com.example.demo.config;

import java.util.List;

import com.example.demo.entity.Item;
import com.example.demo.service.ItemJdbcService;
import com.example.demo.service.ItemService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Demo runner that demonstrates both JPA and JdbcClient approaches for querying the items table
 * with vector embeddings.
 *
 * <p>This will run at application startup and show various query examples.
 */
@Component
@Order(100) // Run after other initializers
public class ItemsDemoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ItemsDemoRunner.class);

    private final ItemService itemService;
    private final ItemJdbcService itemJdbcService;

    public ItemsDemoRunner(ItemService itemService, ItemJdbcService itemJdbcService) {
        this.itemService = itemService;
        this.itemJdbcService = itemJdbcService;
    }

    @Override
    public void run(String... args) {
        log.info("\n" + "=".repeat(80));
        log.info("ITEMS TABLE DEMO - JPA and JdbcClient Examples");
        log.info("=".repeat(80) + "\n");

        try {
            demonstrateBasicQueries();
            demonstrateVectorSimilarityJPA();
            demonstrateVectorSimilarityJdbc();
            demonstrateFilteredSearch();
            demonstrateDistanceComparison();

            log.info("\n" + "=".repeat(80));
            log.info("DEMO COMPLETED - Check the API endpoints at /api/items/*");
            log.info("=".repeat(80) + "\n");

        } catch (Exception e) {
            log.error("Error running items demo", e);
        }
    }

    private void demonstrateBasicQueries() {
        log.info(">>> 1. BASIC QUERIES (JPA)");
        log.info("-".repeat(80));

        // Get all items
        List<Item> allItems = itemService.getAllItems();
        log.info("Total items in database: {}", allItems.size());

        // Get items by category
        List<Item> fruits = itemService.findByCategory("Fruit");
        log.info("Fruits: {}", fruits.stream().map(Item::getName).toList());

        List<Item> vegetables = itemService.findByCategory("Vegetable");
        log.info("Vegetables: {}", vegetables.stream().map(Item::getName).toList());

        // Category counts using JdbcClient
        List<ItemJdbcService.CategoryCount> counts = itemService.getCategoryCounts();
        log.info("Category counts:");
        counts.forEach(cc -> log.info("  - {}: {} items", cc.category(), cc.count()));

        log.info("");
    }

    private void demonstrateVectorSimilarityJPA() {
        log.info(">>> 2. VECTOR SIMILARITY SEARCH (JPA)");
        log.info("-".repeat(80));

        // Find items similar to Apple's embedding using L2 distance
        String appleVector = "[1.0, 0.5, 0.2, 0.1]";
        log.info("Query: Find items similar to Apple using L2 distance");
        log.info("Query vector: {}", appleVector);

        List<Item> similarToApple = itemService.findSimilarItemsL2(appleVector, 5);
        log.info("Results:");
        similarToApple.forEach(
                item ->
                        log.info(
                                "  - {}: {} (category: {})",
                                item.getName(),
                                formatVector(item.getEmbedding()),
                                item.getCategory()));

        // Find items similar to vegetables using cosine distance
        String vegetableVector = "[0.2, 0.1, 0.8, 0.3]";
        log.info("\nQuery: Find items similar to vegetables using cosine distance");
        log.info("Query vector: {}", vegetableVector);

        List<Item> similarToVegetables = itemService.findSimilarItemsCosine(vegetableVector, 5);
        log.info("Results:");
        similarToVegetables.forEach(
                item ->
                        log.info(
                                "  - {}: {} (category: {})",
                                item.getName(),
                                formatVector(item.getEmbedding()),
                                item.getCategory()));

        log.info("");
    }

    private void demonstrateVectorSimilarityJdbc() {
        log.info(">>> 3. VECTOR SIMILARITY WITH DISTANCES (JdbcClient)");
        log.info("-".repeat(80));

        // Using JdbcClient to get distance values
        String queryVector = "[1.0, 0.5, 0.2, 0.1]";
        log.info("Query: Find similar items with distance values (L2)");
        log.info("Query vector: {}", queryVector);

        List<ItemJdbcService.ItemWithDistance> results =
                itemJdbcService.findSimilarByL2Distance(queryVector, 5);

        log.info("Results:");
        results.forEach(
                result ->
                        log.info(
                                "  - {}: distance = {:.4f}",
                                result.item().getName(),
                                result.distance()));

        // Find items within distance threshold
        double threshold = 0.5;
        log.info("\nQuery: Find items within L2 distance < {}", threshold);
        List<ItemJdbcService.ItemWithDistance> nearbyItems =
                itemJdbcService.findWithinDistance(queryVector, threshold);

        log.info("Results ({} items found):", nearbyItems.size());
        nearbyItems.forEach(
                result ->
                        log.info(
                                "  - {}: distance = {:.4f}",
                                result.item().getName(),
                                result.distance()));

        log.info("");
    }

    private void demonstrateFilteredSearch() {
        log.info(">>> 4. FILTERED SIMILARITY SEARCH (JPA)");
        log.info("-".repeat(80));

        // Find similar fruits only
        String fruitVector = "[1.0, 0.6, 0.1, 0.0]";
        log.info("Query: Find similar fruits (category filter)");
        log.info("Query vector: {}", fruitVector);

        List<Item> similarFruits = itemService.findSimilarInCategory("Fruit", fruitVector, 3);
        log.info("Results:");
        similarFruits.forEach(item -> log.info("  - {}: ${}", item.getName(), item.getPrice()));

        // Find similar affordable items
        log.info("\nQuery: Find similar items under $3.00 (price filter)");
        String affordableVector = "[0.5, 0.5, 0.5, 0.5]";
        List<Item> affordableItems =
                itemService.findSimilarUnderPrice(
                        new java.math.BigDecimal("3.00"), affordableVector, 5);

        log.info("Results:");
        affordableItems.forEach(
                item ->
                        log.info(
                                "  - {}: ${} ({})",
                                item.getName(),
                                item.getPrice(),
                                item.getCategory()));

        log.info("");
    }

    private void demonstrateDistanceComparison() {
        log.info(">>> 5. COMPARE ALL DISTANCE METRICS (JdbcClient)");
        log.info("-".repeat(80));

        String queryVector = "[1.0, 0.5, 0.2, 0.1]";
        log.info("Query: Compare L2, Cosine, Inner Product, and L1 distances");
        log.info("Query vector: {}", queryVector);

        List<ItemJdbcService.ItemWithAllDistances> results =
                itemService.compareAllDistances(queryVector, 5);

        log.info("Results:");
        log.info(
                String.format(
                        "%-12s %-10s %-10s %-10s %-10s",
                        "Item", "L2", "Cosine", "InnerProd", "L1"));
        log.info("-".repeat(80));

        results.forEach(
                result ->
                        log.info(
                                String.format(
                                        "%-12s %-10.4f %-10.4f %-10.4f %-10.4f",
                                        result.item().getName(),
                                        result.l2Distance(),
                                        result.cosineDistance(),
                                        result.negInnerProduct(),
                                        result.l1Distance())));

        log.info("");
    }

    private String formatVector(float[] vector) {
        if (vector == null) return "null";

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Math.min(vector.length, 4); i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("%.1f", vector[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}
