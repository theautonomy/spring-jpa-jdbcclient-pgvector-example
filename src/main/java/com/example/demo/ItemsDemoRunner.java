package com.example.demo;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.entity.Item;
import com.example.demo.entity.ItemVector;
import com.example.demo.service.ItemJdbcService;
import com.example.demo.service.ItemService;
import com.example.demo.service.ItemVectorService;

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
    private final ItemVectorService itemVectorService;

    public ItemsDemoRunner(
            ItemService itemService,
            ItemJdbcService itemJdbcService,
            ItemVectorService itemVectorService) {
        this.itemService = itemService;
        this.itemJdbcService = itemJdbcService;
        this.itemVectorService = itemVectorService;
    }

    @Override
    public void run(String... args) {
        log.info("\n" + "=".repeat(80));
        log.info("ITEMS TABLE DEMO - JPA and JdbcClient Examples");
        log.info("=".repeat(80) + "\n");

        try {
            demonstrateSaveNewItem();
            demonstrateBasicQueries();
            demonstrateVectorSimilarityJPA();
            demonstrateVectorSimilarityJdbc();
            demonstrateFilteredSearch();
            demonstrateDistanceComparison();

            // ItemVector demonstrations using custom UserType
            demonstrateItemVectorSave();
            demonstrateItemVectorQueries();

            log.info("\n" + "=".repeat(80));
            log.info("DEMO COMPLETED - Check the API endpoints at /api/items/*");
            log.info("=".repeat(80) + "\n");

        } catch (Exception e) {
            log.error("Error running items demo", e);
        }
    }

    private void demonstrateSaveNewItem() {
        log.info(">>> 0. SAVE NEW ITEM (JPA)");
        log.info("-".repeat(80));

        // Create a new item with embedding vector
        float[] embedding = {0.9f, 0.7f, 0.3f, 0.2f};
        Item newItem = new Item("Mango", "Fruit", new BigDecimal("3.99"), embedding);

        log.info("Creating new item: {}", newItem.getName());
        log.info("  Category: {}", newItem.getCategory());
        log.info("  Price: ${}", newItem.getPrice());
        log.info("  Embedding: {}", formatVector(embedding));

        // Save the item
        Item savedItem = itemService.saveItem(newItem);

        log.info("Item saved successfully with ID: {}", savedItem.getId());
        log.info("  Created at: {}", savedItem.getCreatedAt());

        // Retrieve the embedding value from the saved item
        float[] retrievedEmbedding = savedItem.getEmbedding();
        log.info("Retrieved embedding: {}", formatVector(retrievedEmbedding));

        // You can also access individual values
        if (retrievedEmbedding != null && retrievedEmbedding.length > 0) {
            log.info("  First dimension value: {}", retrievedEmbedding[0]);
            log.info("  Embedding dimensions: {}", retrievedEmbedding.length);
        }

        log.info("");
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

    private void demonstrateItemVectorSave() {
        log.info(">>> 6. ITEMVECTOR - SAVE WITH CUSTOM USERTYPE");
        log.info("-".repeat(80));
        log.info(
                "This demonstrates using the custom PgVectorType UserType with pgvector-java library");

        // Create a new ItemVector with embedding
        float[] embedding = {0.8f, 0.6f, 0.4f, 0.2f};
        ItemVector newItem =
                new ItemVector("Pineapple", "Fruit", new BigDecimal("4.49"), embedding);

        log.info("Creating new ItemVector: {}", newItem.getName());
        log.info("  Category: {}", newItem.getCategory());
        log.info("  Price: ${}", newItem.getPrice());
        log.info("  Embedding: {}", formatVector(embedding));

        // Save the item using custom UserType
        ItemVector savedItem = itemVectorService.saveItem(newItem);

        log.info("ItemVector saved successfully with ID: {}", savedItem.getId());
        log.info("  Created at: {}", savedItem.getCreatedAt());

        // Retrieve the embedding - the custom UserType handles the conversion
        float[] retrievedEmbedding = savedItem.getEmbedding();
        log.info("Retrieved embedding via custom UserType: {}", formatVector(retrievedEmbedding));

        if (retrievedEmbedding != null && retrievedEmbedding.length > 0) {
            log.info("  First dimension: {}", retrievedEmbedding[0]);
            log.info("  Total dimensions: {}", retrievedEmbedding.length);
        }

        log.info("");
    }

    private void demonstrateItemVectorQueries() {
        log.info(">>> 7. ITEMVECTOR - VECTOR SIMILARITY QUERIES");
        log.info("-".repeat(80));
        log.info("Vector queries work the same way with custom UserType");

        // Find similar items using L2 distance
        String fruitVector = "[1.0, 0.5, 0.2, 0.1]";
        log.info("Query: Find items similar to Apple-like vector using L2 distance");
        log.info("Query vector: {}", fruitVector);

        List<ItemVector> similarItems = itemVectorService.findSimilarItemsL2(fruitVector, 5);
        log.info("Results:");
        similarItems.forEach(
                item ->
                        log.info(
                                "  - {}: {} (category: {}, price: ${})",
                                item.getName(),
                                formatVector(item.getEmbedding()),
                                item.getCategory(),
                                item.getPrice()));

        // Find similar items using cosine distance
        String vegetableVector = "[0.2, 0.1, 0.8, 0.3]";
        log.info("\nQuery: Find items similar to vegetables using cosine distance");
        log.info("Query vector: {}", vegetableVector);

        List<ItemVector> similarVeggies =
                itemVectorService.findSimilarItemsCosine(vegetableVector, 3);
        log.info("Results:");
        similarVeggies.forEach(
                item ->
                        log.info(
                                "  - {}: {} ({})",
                                item.getName(),
                                formatVector(item.getEmbedding()),
                                item.getCategory()));

        // Category-filtered search
        log.info("\nQuery: Find similar fruits only (filtered by category)");
        List<ItemVector> similarFruits =
                itemVectorService.findSimilarInCategory("Fruit", fruitVector, 3);
        log.info("Results:");
        similarFruits.forEach(item -> log.info("  - {}: ${}", item.getName(), item.getPrice()));

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
