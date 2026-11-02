package com.example.user.Dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/** SECURITY.md N4, at the annotation level (no Spring context, no database). */
class RequestLimitsTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void startValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void stopValidator() {
        factory.close();
    }

    private static Set<String> codes(int count) {
        return IntStream.rangeClosed(1, count).mapToObj(i -> "r" + i)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Set<String> paths(Object bean) {
        return validator.validate(bean).stream().map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Test
    void restrictionsUpToTheMaximumAreValid() {
        assertThat(validator.validate(new RestrictionsRequest(codes(Limits.RESTRICTIONS)))).isEmpty();
        assertThat(validator.validate(new RestrictionsRequest(null))).isEmpty();
        assertThat(validator.validate(new RestrictionsRequest(Set.of()))).isEmpty();
    }

    @Test
    void oneRestrictionTooMany() {
        assertThat(paths(new RestrictionsRequest(codes(Limits.RESTRICTIONS + 1)))).containsExactly("restrictions");
    }

    @Test
    void aRestrictionCodeThatIsTooLong() {
        assertThat(validator.validate(new RestrictionsRequest(Set.of("x".repeat(Limits.RESTRICTION_CODE_LENGTH)))))
                .isEmpty();
        assertThat(paths(new RestrictionsRequest(Set.of("x".repeat(Limits.RESTRICTION_CODE_LENGTH + 1)))))
                .hasSize(1).allMatch(p -> p.startsWith("restrictions"));
    }

    @Test
    void favouriteSubstitutionsAreBounded() {
        List<FavoriteAlternativeRequest> ok = IntStream.rangeClosed(1, Limits.FAVORITE_ALTERNATIVES)
                .mapToObj(i -> new FavoriteAlternativeRequest((long) i, 1L, 2L)).collect(Collectors.toList());
        assertThat(validator.validate(new FavoriteAlternativesRequest(ok))).isEmpty();

        List<FavoriteAlternativeRequest> tooMany = IntStream.rangeClosed(1, Limits.FAVORITE_ALTERNATIVES + 1)
                .mapToObj(i -> new FavoriteAlternativeRequest((long) i, 1L, 2L)).collect(Collectors.toList());
        assertThat(paths(new FavoriteAlternativesRequest(tooMany))).containsExactly("alternatives");
    }
}
