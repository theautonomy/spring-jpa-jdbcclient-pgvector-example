package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.demo.type.PgVectorType;

import org.hibernate.annotations.Type;

import jakarta.persistence.*;

/**
 * Alternative JPA Entity for the items table using custom Hibernate UserType approach.
 *
 * <p>This entity demonstrates using a custom UserType (PgVectorType) to map the PostgreSQL vector
 * type to a float[] in Java. This approach provides direct integration with the pgvector-java
 * library.
 *
 * <p>Note: This entity maps to the same "items" table as the Item entity, but uses a different
 * approach for handling vector embeddings.
 */
@Entity
@Table(name = "items")
public class ItemVector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String category;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 4-dimensional vector embedding using custom UserType. This demonstrates the pgvector-java
     * library integration with Hibernate.
     */
    @Type(PgVectorType.class)
    @Column(name = "embedding", columnDefinition = "vector(4)")
    private float[] embedding;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Constructors
    public ItemVector() {}

    public ItemVector(String name, String category, BigDecimal price, float[] embedding) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.embedding = embedding;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ItemVector{"
                + "id="
                + id
                + ", name='"
                + name
                + '\''
                + ", category='"
                + category
                + '\''
                + ", price="
                + price
                + ", embedding="
                + (embedding != null ? vectorToString(embedding) : "null")
                + ", createdAt="
                + createdAt
                + '}';
    }

    private String vectorToString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
