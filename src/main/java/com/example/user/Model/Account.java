package com.example.user.Model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A user account: profile, dietary restrictions, and favourites.
 *
 * <p>{@code favoriteRecipeIds} and the ids inside {@link FavoriteAlternative}
 * reference RecipeCatalogue / IngredientCatalogue by value - no foreign keys across
 * the service boundary. Restrictions are free-text codes; {@link Restriction}
 * is an advisory catalogue of the common ones, not a constraint.
 */
@Entity
@Table(name = "account", uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_account_email", columnNames = "email")
})
@Getter
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Setter
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    /** Optional — nothing is ever sent to it. Unique when present (MySQL lets NULLs repeat). */
    @Setter
    @Column(name = "email", length = 255)
    private String email;

    @Setter
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Setter
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection
    @CollectionTable(name = "account_restriction", joinColumns = @JoinColumn(name = "account_id"))
    @Column(name = "restriction_code", nullable = false, length = 100)
    private Set<String> restrictions = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "account_favorite_recipe", joinColumns = @JoinColumn(name = "account_id"))
    @Column(name = "recipe_id", nullable = false)
    private Set<Long> favoriteRecipeIds = new LinkedHashSet<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoriteAlternative> favoriteAlternatives = new ArrayList<>();

    protected Account() {
        // for JPA
    }

    public Account(String username, String email, String displayName, String passwordHash) {
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void replaceRestrictions(Set<String> codes) {
        restrictions.clear();
        restrictions.addAll(codes);
    }

    public void replaceFavoriteAlternatives(List<FavoriteAlternative> alternatives) {
        favoriteAlternatives.clear();
        alternatives.forEach(alt -> alt.setAccount(this));
        favoriteAlternatives.addAll(alternatives);
    }
}
