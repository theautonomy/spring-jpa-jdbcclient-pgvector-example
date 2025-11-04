package com.example.demo.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.demo.service.VectorCastingExamples;

import org.springframework.web.bind.annotation.*;

/**
 * REST API controller demonstrating different vector casting approaches.
 *
 * <p>Endpoints showcase: - CAST(:string AS vector) - :string::vector - Direct string usage -
 * Complex queries with casting
 */
@RestController
@RequestMapping("/api/vector-casting")
public class VectorCastingController {

    private final VectorCastingExamples vectorCastingExamples;

    public VectorCastingController(VectorCastingExamples vectorCastingExamples) {
        this.vectorCastingExamples = vectorCastingExamples;
    }

    /**
     * Method 1: Using CAST() function Most SQL-standard approach
     *
     * <p>Example: GET /api/vector-casting/cast-function?vector=[1.0,0.5,0.2,0.1]&limit=5
     */
    @GetMapping("/cast-function")
    public List<VectorCastingExamples.ItemWithDistance> useCastFunction(
            @RequestParam String vector, @RequestParam(defaultValue = "5") int limit) {

        return vectorCastingExamples.findSimilarUsingCastFunction(vector, limit);
    }

    /**
     * Method 2: Using ::vector PostgreSQL operator Most common in PostgreSQL
     *
     * <p>Example: GET /api/vector-casting/colon-cast?vector=[1.0,0.5,0.2,0.1]&limit=5
     */
    @GetMapping("/colon-cast")
    public List<VectorCastingExamples.ItemWithDistance> useColonCast(
            @RequestParam String vector, @RequestParam(defaultValue = "5") int limit) {

        return vectorCastingExamples.findSimilarUsingColonCast(vector, limit);
    }

    /**
     * Method 3: Using direct string (auto-cast) Less explicit but works
     *
     * <p>Example: GET /api/vector-casting/direct-string?vector=[1.0,0.5,0.2,0.1]&limit=5
     */
    @GetMapping("/direct-string")
    public List<VectorCastingExamples.ItemWithDistance> useDirectString(
            @RequestParam String vector, @RequestParam(defaultValue = "5") int limit) {

        return vectorCastingExamples.findSimilarUsingDirectString(vector, limit);
    }

    /**
     * Compare all distance metrics using mixed casting approaches
     *
     * <p>Example: GET /api/vector-casting/compare-distances?vector=[1.0,0.5,0.2,0.1]&limit=5
     */
    @GetMapping("/compare-distances")
    public List<VectorCastingExamples.ItemWithAllDistances> compareDistances(
            @RequestParam String vector, @RequestParam(defaultValue = "5") int limit) {

        return vectorCastingExamples.compareDistancesWithCasting(vector, limit);
    }

    /**
     * Complex search with filters and casting
     *
     * <p>Example: GET
     * /api/vector-casting/complex-search?vector=[0.2,0.1,0.8,0.3]&category=Vegetable&minPrice=1.00&maxPrice=2.00&maxDistance=1.0&limit=5
     */
    @GetMapping("/complex-search")
    public List<VectorCastingExamples.ItemWithDistance> complexSearch(
            @RequestParam String vector,
            @RequestParam String category,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam double maxDistance,
            @RequestParam(defaultValue = "5") int limit) {

        return vectorCastingExamples.complexSearchWithCasting(
                vector, category, minPrice, maxPrice, maxDistance, limit);
    }

    /**
     * Find items near category center (uses AVG with casting)
     *
     * <p>Example: GET /api/vector-casting/category-center?category=Fruit&limit=3
     */
    @GetMapping("/category-center")
    public List<VectorCastingExamples.ItemWithDistance> findNearCategoryCenter(
            @RequestParam String category, @RequestParam(defaultValue = "3") int limit) {

        return vectorCastingExamples.findItemsNearCategoryCenter(category, limit);
    }

    /**
     * Compare distances to multiple vectors
     *
     * <p>Example: GET
     * /api/vector-casting/multi-compare?v1=[1.0,0.5,0.2,0.1]&v2=[0.2,0.1,0.8,0.3]&v3=[0.1,0.2,0.3,0.9]&limit=5
     */
    @GetMapping("/multi-compare")
    public List<Object[]> compareWithMultipleVectors(
            @RequestParam String v1,
            @RequestParam String v2,
            @RequestParam String v3,
            @RequestParam(defaultValue = "5") int limit) {

        return vectorCastingExamples.compareWithMultipleVectors(v1, v2, v3, limit);
    }

    /**
     * Insert item using vector casting
     *
     * <p>Example: POST /api/vector-casting/insert { "name": "Strawberry", "category": "Fruit",
     * "price": 2.50, "embedding": "[0.95, 0.55, 0.15, 0.05]" }
     */
    @PostMapping("/insert")
    public Map<String, Object> insertItem(@RequestBody InsertRequest request) {
        int rowsAffected =
                vectorCastingExamples.insertItemWithVectorCast(
                        request.name(), request.category(), request.price(), request.embedding());

        return Map.of(
                "success",
                rowsAffected > 0,
                "rowsAffected",
                rowsAffected,
                "message",
                "Item inserted with vector casting");
    }

    /**
     * Update item embedding using vector casting
     *
     * <p>Example: PUT /api/vector-casting/update/1 { "embedding": "[1.0, 0.6, 0.3, 0.1]" }
     */
    @PutMapping("/update/{id}")
    public Map<String, Object> updateItemEmbedding(
            @PathVariable Long id, @RequestBody UpdateRequest request) {

        int rowsAffected = vectorCastingExamples.updateItemWithVectorCast(id, request.embedding());

        return Map.of(
                "success",
                rowsAffected > 0,
                "rowsAffected",
                rowsAffected,
                "message",
                "Item embedding updated with vector casting");
    }

    /**
     * Get examples of different casting syntaxes
     *
     * <p>Example: GET /api/vector-casting/examples
     */
    @GetMapping("/examples")
    public Map<String, Object> getCastingExamples() {
        return Map.of(
                "description", "Different ways to cast strings to vectors in PostgreSQL",
                "methods",
                        List.of(
                                Map.of(
                                        "method", "CAST() function",
                                        "syntax", "CAST('[1.0, 0.5, 0.2, 0.1]' AS vector)",
                                        "pros", "SQL standard, explicit, portable",
                                        "cons", "More verbose",
                                        "example",
                                                "SELECT embedding <-> CAST(:vec AS vector) FROM items"),
                                Map.of(
                                        "method", "PostgreSQL :: operator",
                                        "syntax", "':vec'::vector or :vec::vector",
                                        "pros", "Concise, PostgreSQL idiomatic",
                                        "cons", "PostgreSQL-specific",
                                        "example", "SELECT embedding <-> :vec::vector FROM items"),
                                Map.of(
                                        "method", "Direct string",
                                        "syntax", ":vec (relies on implicit casting)",
                                        "pros", "Shortest syntax",
                                        "cons", "Less explicit, may not always work",
                                        "example", "SELECT embedding <-> :vec FROM items"),
                                Map.of(
                                        "method", "Array to vector",
                                        "syntax", "vector(ARRAY[1.0, 0.5, 0.2, 0.1])",
                                        "pros", "Type-safe array construction",
                                        "cons", "Most verbose",
                                        "example",
                                                "SELECT embedding <-> vector(ARRAY[1.0, 0.5]) FROM items")),
                "recommendations",
                        List.of(
                                "Use CAST() for maximum portability and clarity",
                                "Use ::vector for PostgreSQL-specific code (most common)",
                                "Always use parameterized queries to prevent SQL injection",
                                "Both CAST() and ::vector work identically for pgvector"),
                "testEndpoints",
                        Map.of(
                                "castFunction",
                                        "/api/vector-casting/cast-function?vector=[1.0,0.5,0.2,0.1]&limit=5",
                                "colonCast",
                                        "/api/vector-casting/colon-cast?vector=[1.0,0.5,0.2,0.1]&limit=5",
                                "directString",
                                        "/api/vector-casting/direct-string?vector=[1.0,0.5,0.2,0.1]&limit=5",
                                "compareDistances",
                                        "/api/vector-casting/compare-distances?vector=[1.0,0.5,0.2,0.1]&limit=5"));
    }

    // Request DTOs
    public record InsertRequest(String name, String category, BigDecimal price, String embedding) {}

    public record UpdateRequest(String embedding) {}
}
