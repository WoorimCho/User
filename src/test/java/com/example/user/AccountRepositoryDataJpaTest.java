package com.example.user;

import com.example.user.Model.Account;
import com.example.user.Repositories.AccountRepository;
import com.example.user.Repositories.RestrictionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-layer test against the real (Testcontainers) MySQL: case-insensitive
 * account lookups, and the restriction catalogue that migration V2 seeds.
 */
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class AccountRepositoryDataJpaTest {

    @Autowired
    AccountRepository accounts;
    @Autowired
    RestrictionRepository restrictions;

    @Test
    void accountLookupsAreCaseInsensitive() {
        accounts.save(new Account("Woorim", "woorim@example.com", "Woorim", "hash"));

        assertThat(accounts.existsByUsernameIgnoreCase("woorim")).isTrue();
        assertThat(accounts.existsByEmailIgnoreCase("WOORIM@EXAMPLE.COM")).isTrue();
        assertThat(accounts.findByUsernameIgnoreCase("WOORIM")).isPresent();
        assertThat(accounts.findByEmailIgnoreCase("woorim@example.com")).isPresent();
        assertThat(accounts.findByUsernameIgnoreCase("ghost")).isEmpty();
    }

    @Test
    void restrictionSeedIsQueryableByKindAndCode() {
        assertThat(restrictions.findByCodeIgnoreCase("DIET:VEGAN"))
                .get().extracting(r -> r.getLabel()).isEqualTo("Vegan");

        var diet = restrictions.findByKindIgnoreCaseOrderByLabelAsc("diet");
        assertThat(diet).extracting(r -> r.getCode()).contains("diet:vegan", "diet:keto");
        assertThat(diet).allSatisfy(r -> assertThat(r.getKind()).isEqualTo("diet"));
        // ordered by label
        assertThat(diet).extracting(r -> r.getLabel()).isSorted();
    }
}
