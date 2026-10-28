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
import travelbooking.TravelbookingApplication;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TravelbookingApplication.class)
public class FeatureApiControllersTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("GET /api/flight-status - Should return active flight tracking list")
    void testFlightStatusList() throws Exception {
        mockMvc.perform(get("/api/flight-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$[0].flightNumber", notNullValue()))
                .andExpect(jsonPath("$[0].status", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/flight-status/AI 101 - Should return detailed status with delayReason and gate")
    void testFlightStatusDetail() throws Exception {
        mockMvc.perform(get("/api/flight-status/AI 101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber", equalTo("AI 101")))
                .andExpect(jsonPath("$.terminal", equalTo("Terminal 3")))
                .andExpect(jsonPath("$.gate", equalTo("Gate 14B")));
    }

    @Test
    @DisplayName("GET /api/pricing/history - Should return 7-day price volatility points")
    void testPricingHistory() throws Exception {
        mockMvc.perform(get("/api/pricing/history")
                        .param("origin", "DEL")
                        .param("destination", "BOM")
                        .param("basePrice", "180"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[0].price", greaterThan(0)))
                .andExpect(jsonPath("$[0].trend", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/pricing/freeze - Should generate 48-hour price freeze quote")
    void testPriceFreeze() throws Exception {
        String payload = """
                {
                  "flightNumber": "AI 101",
                  "airline": "Air India",
                  "route": "DEL → BOM",
                  "departureDate": "2026-11-05",
                  "fare": 200,
                  "travelerEmail": "arun@example.com"
                }
                """;

        mockMvc.perform(post("/api/pricing/freeze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freezeToken", startsWith("PF-")))
                .andExpect(jsonPath("$.validityHours", equalTo(48)))
                .andExpect(jsonPath("$.status", equalTo("ACTIVE")));
    }

    @Test
    @DisplayName("GET /api/cancellations/policy - Should compute policy-based refund quote")
    void testCancellationPolicyQuote() throws Exception {
        mockMvc.perform(get("/api/cancellations/policy")
                        .param("bookingReference", "VX-12345")
                        .param("originalFare", "400")
                        .param("departureDate", "2026-11-20")
                        .param("medicalWaiver", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingReference", equalTo("VX-12345")))
                .andExpect(jsonPath("$.refundPercentage", equalTo(90)))
                .andExpect(jsonPath("$.netRefundAmount", greaterThan(0)));
    }

    @Test
    @DisplayName("POST /api/cancellations/request - Should issue official refund reference")
    void testProcessCancellation() throws Exception {
        String payload = """
                {
                  "bookingReference": "VX-12345",
                  "travelerEmail": "arun@example.com",
                  "reason": "Change of travel plans",
                  "medicalWaiver": false
                }
                """;

        mockMvc.perform(post("/api/cancellations/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundReference", startsWith("RF-")))
                .andExpect(jsonPath("$.status", equalTo("APPROVED")));
    }

    @Test
    @DisplayName("GET /api/seats/map - Should generate interactive cabin layout with rows and aisles")
    void testSeatMap() throws Exception {
        mockMvc.perform(get("/api/seats/map")
                        .param("flightNumber", "AI 101")
                        .param("aircraft", "Airbus A350-900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRows", equalTo(6)))
                .andExpect(jsonPath("$.seats", hasSize(36)))
                .andExpect(jsonPath("$.seats[0].seatNumber", equalTo("1A")));
    }

    @Test
    @DisplayName("GET /api/hotels/hotel-1/rooms - Should return available room tiers and amenities")
    void testHotelRooms() throws Exception {
        mockMvc.perform(get("/api/hotels/hotel-1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].pricePerNight", greaterThan(0)));
    }

    @Test
    @DisplayName("POST /api/hotels/book - Should confirm simulated room reservation")
    void testBookHotel() throws Exception {
        String payload = """
                {
                  "hotelId": "hotel-1",
                  "hotelName": "Astra Caldera Luxury Suites",
                  "roomId": "rm-101",
                  "roomName": "Deluxe Caldera View Suite",
                  "checkInDate": "2026-11-05",
                  "checkOutDate": "2026-11-08",
                  "nights": 3,
                  "guests": 2,
                  "totalAmount": 960,
                  "guestName": "Elena Rostova",
                  "guestEmail": "elena@example.com",
                  "guestPhone": "+49 171 1234567"
                }
                """;

        mockMvc.perform(post("/api/hotels/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingReference", startsWith("HT-")))
                .andExpect(jsonPath("$.status", equalTo("CONFIRMED")));
    }

    @Test
    @DisplayName("GET /api/reviews - Should return curated reviews with multi-criteria scores")
    void testGetReviews() throws Exception {
        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].rating", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("POST /api/reviews - Should submit verified review")
    void testSubmitReview() throws Exception {
        String payload = """
                {
                  "targetType": "HOTEL",
                  "targetId": "hotel-1",
                  "targetName": "Astra Caldera Luxury Suites",
                  "authorName": "Arun Patel",
                  "authorLocation": "Mumbai, India",
                  "rating": 5,
                  "cleanlinessScore": 5.0,
                  "serviceScore": 5.0,
                  "valueScore": 4.8,
                  "title": "Unrivalled Hospitality",
                  "comment": "Superb views and attentive concierge.",
                  "photoUrls": []
                }
                """;

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", startsWith("rev-")))
                .andExpect(jsonPath("$.rating", equalTo(5)));
    }

    @Test
    @DisplayName("POST /api/recommendations - Should return personalized suggestions with reasons")
    void testGetRecommendations() throws Exception {
        String payload = """
                {
                  "travelStyle": "ROMANTIC",
                  "preferredVibe": "BEACH",
                  "budgetRange": "LUXURY",
                  "travelParty": "COUPLE"
                }
                """;

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].recommendationReason", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/newsletter/subscribe - Should validate email and confirm signup")
    void testNewsletterSubscribe() throws Exception {
        String payload = """
                {
                  "email": "traveler@voyagex.example.com"
                }
                """;

        mockMvc.perform(post("/api/newsletter/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.email", equalTo("traveler@voyagex.example.com")));
    }
}
