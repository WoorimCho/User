package com.example.user.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * An advisory catalogue entry for a common dietary restriction, e.g.
 * {@code code="diet:vegan"}, {@code label="Vegan"}, {@code kind="diet"}. Seeded
 * by a migration. Accounts may still use codes that aren't listed here.
 */
@Entity
@Table(name = "restriction", indexes = {
        @Index(name = "ix_restriction_kind", columnList = "kind")
})
@Getter
public class Restriction {

    @Id
    @GeneratedValue
    private Long id;

    @Setter
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Setter
    @Column(name = "label", nullable = false, length = 100)
    private String label;

    /** Grouping for filtering: diet, allergen, religious, lifestyle. */
    @Setter
    @Column(name = "kind", nullable = false, length = 40)
    private String kind;

    @Setter
    @Column(name = "description", length = 500)
    private String description;

    protected Restriction() {
        // for JPA
    }
}
