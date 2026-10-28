package travelbooking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import travelbooking.TravelbookingApplication;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TravelbookingApplication.class)
class FlightApiControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    void testGetAirportsIncludesIndianAndInternational() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        mockMvc.perform(get("/api/flights/airports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))))
                .andExpect(jsonPath("$[?(@.code == 'DEL')].city").value("New Delhi"))
                .andExpect(jsonPath("$[?(@.code == 'BOM')].city").value("Mumbai"))
                .andExpect(jsonPath("$[?(@.code == 'DXB')].city").value("Dubai"))
                .andExpect(jsonPath("$[?(@.code == 'LHR')].city").value("London"));
    }

    @Test
    void testSearchFlightsValidInputReturnsSuccess() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        LocalDate tomorrow = LocalDate.now().plusDays(2);
        LocalDate returnDate = LocalDate.now().plusDays(9);

        mockMvc.perform(get("/api/flights/search")
                        .param("origin", "New Delhi (DEL)")
                        .param("destination", "Dubai (DXB)")
                        .param("departDate", tomorrow.toString())
                        .param("returnDate", returnDate.toString())
                        .param("passengers", "2")
                        .param("cabinClass", "Economy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalResults", greaterThan(0)))
                .andExpect(jsonPath("$.flights", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.flights[0].price", greaterThan(0)))
                .andExpect(jsonPath("$.flights[0].totalFare", greaterThan(0)))
                .andExpect(jsonPath("$.disclaimer", containsString("Demonstration data")));
    }

    @Test
    void testSearchFlightsSameOriginAndDestinationReturnsBadRequest() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        LocalDate tomorrow = LocalDate.now().plusDays(2);

        mockMvc.perform(get("/api/flights/search")
                        .param("origin", "New Delhi (DEL)")
                        .param("destination", "DEL")
                        .param("departDate", tomorrow.toString())
                        .param("passengers", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.destination", containsString("cannot be the same")));
    }

    @Test
    void testSearchFlightsPastDateReturnsBadRequest() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        LocalDate pastDate = LocalDate.now().minusDays(2);

        mockMvc.perform(get("/api/flights/search")
                        .param("origin", "DEL")
                        .param("destination", "BOM")
                        .param("departDate", pastDate.toString())
                        .param("passengers", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.departDate", containsString("cannot be in the past")));
    }

    @Test
    void testSearchFlightsInvalidPassengerCountReturnsBadRequest() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        LocalDate tomorrow = LocalDate.now().plusDays(2);

        mockMvc.perform(get("/api/flights/search")
                        .param("origin", "DEL")
                        .param("destination", "BOM")
                        .param("departDate", tomorrow.toString())
                        .param("passengers", "12"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.passengers", containsString("between 1 and 9")));
    }

    @Test
    void testSearchFlightsNonStopFilter() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        LocalDate tomorrow = LocalDate.now().plusDays(2);

        mockMvc.perform(get("/api/flights/search")
                        .param("origin", "DEL")
                        .param("destination", "BOM")
                        .param("departDate", tomorrow.toString())
                        .param("passengers", "1")
                        .param("nonStopOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flights[*].isNonStop", everyItem(is(true))));
    }

    @Test
    void testConfirmSimulatedBookingSuccess() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        String jsonPayload = """
                {
                    "flightId": "FL-101",
                    "airline": "Air India",
                    "flightNumber": "AI 101",
                    "origin": "New Delhi (DEL)",
                    "destination": "Dubai (DXB)",
                    "departureDate": "2026-11-05",
                    "departureTime": "08:15 AM",
                    "arrivalTime": "11:00 AM",
                    "passengers": 2,
                    "cabinClass": "Economy",
                    "totalFare": 480,
                    "passengerName": "Arjun Sharma",
                    "passengerEmail": "arjun.sharma@example.com",
                    "passengerPhone": "+91 98765 43210"
                }
                """;

        mockMvc.perform(post("/api/flights/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.bookingReference", startsWith("VX-")))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.passengerName").value("Arjun Sharma"))
                .andExpect(jsonPath("$.disclaimer", containsString("DEMO ONLY")));
    }

    @Test
    void testConfirmSimulatedBookingValidationFailure() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        String invalidPayload = """
                {
                    "flightId": "FL-101",
                    "airline": "Air India",
                    "passengerName": "",
                    "passengerEmail": "invalid-email",
                    "passengers": 1,
                    "totalFare": 240
                }
                """;

        mockMvc.perform(post("/api/flights/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
