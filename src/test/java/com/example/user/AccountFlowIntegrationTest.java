package com.example.user;

import com.example.user.Repositories.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "internal-auth.enabled=false")
class AccountFlowIntegrationTest {

    private static final Pattern ID = Pattern.compile("\"id\":(\\d+)");

    @Autowired
    MockMvc mvc;
    @Autowired
    AccountRepository accounts;

    @BeforeEach
    void reset() {
        accounts.deleteAll();
    }

    @Test
    void registerThenAuthenticate() throws Exception {
        String created = register("woorim", "woorim@example.com", "Woorim", "s3cret-pw");
        assertThat(created).contains("\"username\":\"woorim\"");
        assertThat(created).doesNotContain("password");   // no hash, no plaintext

        // good credentials, by username and by email
        mvc.perform(auth("woorim", "s3cret-pw")).andExpect(status().isOk());
        mvc.perform(auth("woorim@example.com", "s3cret-pw")).andExpect(status().isOk());
        // wrong password, unknown identifier
        mvc.perform(auth("woorim", "nope")).andExpect(status().isUnauthorized());
        mvc.perform(auth("ghost", "whatever")).andExpect(status().isUnauthorized());
    }

    @Test
    void emailIsOptional() throws Exception {
        // no email field at all
        String created = mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"noemail\",\"displayName\":\"No Email\",\"password\":\"s3cret-pw\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertThat(created).contains("\"email\":null");

        // login by username still works; there's no email to log in with
        mvc.perform(auth("noemail", "s3cret-pw")).andExpect(status().isOk());

        // a second account with no email doesn't collide on uk_account_email
        mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content(body("noemail2", "", "No Email 2", "s3cret-pw")))
                .andExpect(status().isCreated());
    }

    @Test
    void duplicateUsernameIsRejected() throws Exception {
        register("dup", "a@example.com", "A", "password1");
        mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(
                        body("dup", "b@example.com", "B", "password2")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePasswordInvalidatesTheOldOne() throws Exception {
        long id = firstId(register("pwuser", "pw@example.com", "Pw", "old-password"));

        mvc.perform(put("/api/accounts/" + id + "/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrong\",\"newPassword\":\"new-password\"}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(put("/api/accounts/" + id + "/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"old-password\",\"newPassword\":\"new-password\"}"))
                .andExpect(status().isNoContent());

        mvc.perform(auth("pwuser", "old-password")).andExpect(status().isUnauthorized());
        mvc.perform(auth("pwuser", "new-password")).andExpect(status().isOk());
    }

    @Test
    void restrictionsAreNormalisedAndReplaced() throws Exception {
        long id = firstId(register("ruser", "r@example.com", "R", "password1"));

        mvc.perform(put("/api/accounts/" + id + "/restrictions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restrictions\":[\" Diet:Vegan \",\"ALLERGEN:PEANUT\"]}"))
                .andExpect(status().isOk());

        String list = mvc.perform(get("/api/accounts/" + id + "/restrictions"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(list).contains("diet:vegan", "allergen:peanut").doesNotContain("Diet:Vegan");

        mvc.perform(put("/api/accounts/" + id + "/restrictions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"restrictions\":[]}"))
                .andExpect(status().isOk());
        assertThat(mvc.perform(get("/api/accounts/" + id + "/restrictions"))
                .andReturn().getResponse().getContentAsString()).isEqualTo("[]");
    }

    @Test
    void favoriteRecipesAndAlternatives() throws Exception {
        long id = firstId(register("fuser", "f@example.com", "F", "password1"));

        mvc.perform(post("/api/accounts/" + id + "/favorites/recipes/7")).andExpect(status().isOk());
        mvc.perform(post("/api/accounts/" + id + "/favorites/recipes/9")).andExpect(status().isOk());
        assertThat(getBody("/api/accounts/" + id + "/favorites/recipes")).contains("7", "9");

        mvc.perform(delete("/api/accounts/" + id + "/favorites/recipes/7")).andExpect(status().isOk());
        assertThat(getBody("/api/accounts/" + id + "/favorites/recipes")).contains("9").doesNotContain("7");

        // prefer ingredient 99 in place of 12, in recipe 7
        String alts = mvc.perform(put("/api/accounts/" + id + "/favorites/alternatives")
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"alternatives":[{"recipeId":7,"ingredientId":12,"replacementIngredientId":99}]}"""))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(alts).contains("\"recipeId\":7", "\"replacementIngredientId\":99");

        long altId = firstId(alts);
        mvc.perform(delete("/api/accounts/" + id + "/favorites/alternatives/" + altId))
                .andExpect(status().isNoContent());
        assertThat(getBody("/api/accounts/" + id + "/favorites/alternatives")).isEqualTo("[]");
    }

    @Test
    void restrictionCatalogueFilters() throws Exception {
        assertThat(getBody("/api/restrictions?kind=diet")).contains("diet:vegan").doesNotContain("allergen:peanut");
        assertThat(getBody("/api/restrictions")).contains("diet:vegan", "allergen:peanut", "religious:halal");
        mvc.perform(get("/api/restrictions/nonsense:code")).andExpect(status().isNotFound());
    }

    // --- helpers ---

    private String register(String username, String email, String displayName, String password) throws Exception {
        return mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON)
                        .content(body(username, email, displayName, password)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private static String body(String username, String email, String displayName, String password) {
        return "{\"username\":\"" + username + "\",\"email\":\"" + email + "\",\"displayName\":\""
                + displayName + "\",\"password\":\"" + password + "\"}";
    }

    private org.springframework.test.web.servlet.RequestBuilder auth(String identifier, String password) {
        return post("/api/authenticate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"identifier\":\"" + identifier + "\",\"password\":\"" + password + "\"}");
    }

    private String getBody(String uri) throws Exception {
        return mvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private static long firstId(String json) {
        Matcher m = ID.matcher(json);
        assertThat(m.find()).as("an id in %s", json).isTrue();
        return Long.parseLong(m.group(1));
    }
}
