package com.example.user;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.example.user.Repositories.AccountRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract test: every interaction below is validated — request <em>and</em>
 * response — against the checked-in {@code static/openapi.yaml}. If the
 * controllers drift from the published contract (a field renamed, a status code
 * changed) this fails.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OpenApiContractTest {

    private static OpenApiInteractionValidator validator;

    @Autowired
    MockMvc mvc;
    @Autowired
    AccountRepository accounts;

    @BeforeAll
    static void loadSpec() throws Exception {
        String spec = new String(
                new ClassPathResource("static/openapi.yaml").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
        validator = OpenApiInteractionValidator.createForInlineApiSpecification(spec).build();
    }

    @BeforeEach
    void reset() {
        accounts.deleteAll();
    }

    private long register() throws Exception {
        String body = mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"woorim","email":"woorim@example.com",
                                 "displayName":"Woorim","password":"s3cret-pw"}"""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*?\"id\":(\\d+).*", "$1"));
    }

    @Test
    void register_matchesContract() throws Exception {
        mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"woorim","email":"woorim@example.com",
                                 "displayName":"Woorim","password":"s3cret-pw"}"""))
                .andExpect(status().isCreated())
                .andExpect(openApi().isValid(validator));
    }

    @Test
    void duplicateRegister_matchesContractAs400Problem() throws Exception {
        // A contract-valid body that the server rejects on a business rule (the
        // username is taken) — so request validation passes and we're checking
        // the 400 ProblemDetail response shape, not Bean Validation.
        register();
        mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"woorim","email":"other@example.com",
                                 "displayName":"Other","password":"another-pw"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(openApi().isValid(validator));
    }

    @Test
    void getAccount_matchesContract() throws Exception {
        long id = register();
        mvc.perform(get("/api/accounts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(validator));
    }

    @Test
    void getMissingAccount_matchesContractAs404Problem() throws Exception {
        mvc.perform(get("/api/accounts/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(openApi().isValid(validator));
    }

    @Test
    void authenticate_matchesContract() throws Exception {
        register();
        mvc.perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"woorim\",\"password\":\"s3cret-pw\"}"))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(validator));
    }

    @Test
    void authenticateBadPassword_matchesContractAs401Problem() throws Exception {
        register();
        mvc.perform(post("/api/authenticate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"woorim\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(openApi().isValid(validator));
    }

    @Test
    void replaceAndReadRestrictions_matchesContract() throws Exception {
        long id = register();

        mvc.perform(put("/api/accounts/{id}/restrictions", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restrictions\":[\" Diet:Vegan \",\"ALLERGEN:PEANUT\"]}"))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(validator));

        mvc.perform(get("/api/accounts/{id}/restrictions", id))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(validator));
    }

    @Test
    void favoriteRecipesAndAlternatives_matchesContract() throws Exception {
        long id = register();

        mvc.perform(post("/api/accounts/{id}/favorites/recipes/{r}", id, 7))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(validator));

        mvc.perform(get("/api/accounts/{id}/favorites/recipes", id))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(validator));

        mvc.perform(put("/api/accounts/{id}/favorites/alternatives", id).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"alternatives":[{"recipeId":7,"ingredientId":12,"replacementIngredientId":99}]}"""))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(validator));
    }

    @Test
    void restrictionCatalogue_matchesContract() throws Exception {
        mvc.perform(get("/api/restrictions"))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(validator));

        mvc.perform(get("/api/restrictions?kind=diet"))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(validator));

        mvc.perform(get("/api/restrictions/{code}", "diet:vegan"))
                .andExpect(status().isOk())
                .andExpect(openApi().isValid(validator));
    }

    @Test
    void missingRestriction_matchesContractAs404Problem() throws Exception {
        mvc.perform(get("/api/restrictions/{code}", "nonsense:code"))
                .andExpect(status().isNotFound())
                .andExpect(openApi().isValid(validator));
    }
}
