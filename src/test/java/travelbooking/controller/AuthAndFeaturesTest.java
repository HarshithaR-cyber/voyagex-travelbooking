package travelbooking.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthAndFeaturesTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /api/auth/register should create a new user and return user DTO")
    void testRegisterSuccess() throws Exception {
        String uniqueEmail = "traveler." + System.currentTimeMillis() + "@voyagex.test";
        String body = """
            {
              "fullName": "Kavita Rao",
              "email": "%s",
              "password": "securePassword123",
              "confirmPassword": "securePassword123",
              "membershipTier": "VoyageX Plus"
            }
            """.formatted(uniqueEmail);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.fullName").value("Kavita Rao"))
                .andExpect(jsonPath("$.user.email").value(uniqueEmail))
                .andExpect(jsonPath("$.user.initials").value("KR"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.user.salt").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/auth/register with duplicate email should return 400 Bad Request")
    void testRegisterDuplicateEmail() throws Exception {
        String body = """
            {
              "fullName": "Arun Duplicate",
              "email": "arun.patel@example.com",
              "password": "password123",
              "confirmPassword": "password123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("POST /api/auth/register with mismatched passwords should return 400 Bad Request")
    void testRegisterMismatchedPasswords() throws Exception {
        String body = """
            {
              "fullName": "Test Mismatch",
              "email": "mismatch@example.com",
              "password": "password123",
              "confirmPassword": "differentPassword456"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Passwords do not match")));
    }

    @Test
    @DisplayName("POST /api/auth/login with valid credentials should authenticate and return user info")
    void testLoginSuccess() throws Exception {
        String body = """
            {
              "email": "arun.patel@example.com",
              "password": "voyagex123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.user.fullName").value("Arun Patel"))
                .andExpect(jsonPath("$.user.initials").value("AP"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid password should return 401 Unauthorized")
    void testLoginInvalidPassword() throws Exception {
        String body = """
            {
              "email": "arun.patel@example.com",
              "password": "wrongPassword123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Invalid email or password")));
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password should return success response")
    void testForgotPassword() throws Exception {
        String body = """
            {
              "email": "arun.patel@example.com"
            }
            """;

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message", containsString("password reset")));
    }

    @Test
    @DisplayName("POST /api/auth/deals/save should toggle saved deal state")
    void testToggleSaveDeal() throws Exception {
        String body = """
            {
              "email": "arun.patel@example.com",
              "dealId": "deal-6"
            }
            """;

        mockMvc.perform(post("/api/auth/deals/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.dealId").value("deal-6"));
    }

    @Test
    @DisplayName("GET /api/hotels should return categorized hotel recommendations with filtering")
    void testGetHotelsWithFilter() throws Exception {
        mockMvc.perform(get("/api/hotels")
                        .param("category", "TOP_RATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", containsString("Astra Caldera")))
                .andExpect(jsonPath("$[0].rating", is(9.8)))
                .andExpect(jsonPath("$[0].availabilityStatus").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/deals should return curated VIP travel deals across categories")
    void testGetVipDeals() throws Exception {
        mockMvc.perform(get("/api/deals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(6))))
                .andExpect(jsonPath("$[0].title").isNotEmpty())
                .andExpect(jsonPath("$[0].discountPercentage", greaterThan(0)))
                .andExpect(jsonPath("$[0].benefits", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("POST /api/deals/{id}/validate should calculate accurate pricing and savings")
    void testValidateVipDeal() throws Exception {
        mockMvc.perform(post("/api/deals/deal-1/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"travelers\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.travelers").value(2))
                .andExpect(jsonPath("$.unitPrice").value(620))
                .andExpect(jsonPath("$.totalDiscounted").value(1240))
                .andExpect(jsonPath("$.totalSavings").value(540));
    }

    @Test
    @DisplayName("POST /api/auth/membership/upgrade should update tier without requiring payments")
    void testUpgradeMembership() throws Exception {
        String body = """
            {
              "email": "arun.patel@example.com",
              "tier": "VoyageX Elite"
            }
            """;

        mockMvc.perform(post("/api/auth/membership/upgrade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.tier").value("VoyageX Elite"));
    }
}
