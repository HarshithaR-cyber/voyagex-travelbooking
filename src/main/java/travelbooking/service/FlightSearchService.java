package travelbooking.service;

import org.springframework.stereotype.Service;
import travelbooking.dto.*;
import travelbooking.model.FlightOffer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class FlightSearchService {

    private final List<AirportDto> airports = new ArrayList<>();

    public FlightSearchService() {
        initAirports();
    }

    private void initAirports() {
        // Popular Indian destinations
        airports.add(new AirportDto("DEL", "New Delhi", "India", "Indira Gandhi International Airport", true));
        airports.add(new AirportDto("BOM", "Mumbai", "India", "Chhatrapati Shivaji Maharaj International Airport", true));
        airports.add(new AirportDto("BLR", "Bengaluru", "India", "Kempegowda International Airport", true));
        airports.add(new AirportDto("GOI", "Goa", "India", "Manohar / Dabolim International Airport", true));
        airports.add(new AirportDto("MAA", "Chennai", "India", "Chennai International Airport", true));
        airports.add(new AirportDto("CCU", "Kolkata", "India", "Netaji Subhash Chandra Bose International Airport", true));
        airports.add(new AirportDto("HYD", "Hyderabad", "India", "Rajiv Gandhi International Airport", true));
        airports.add(new AirportDto("COK", "Kochi", "India", "Cochin International Airport", true));

        // Popular International destinations
        airports.add(new AirportDto("DXB", "Dubai", "United Arab Emirates", "Dubai International Airport", false));
        airports.add(new AirportDto("SIN", "Singapore", "Singapore", "Singapore Changi Airport", false));
        airports.add(new AirportDto("LHR", "London", "United Kingdom", "Heathrow Airport", false));
        airports.add(new AirportDto("CDG", "Paris", "France", "Charles de Gaulle Airport", false));
        airports.add(new AirportDto("JFK", "New York", "United States", "John F. Kennedy International Airport", false));
        airports.add(new AirportDto("HND", "Tokyo", "Japan", "Tokyo Haneda Airport", false));
        airports.add(new AirportDto("BKK", "Bangkok", "Thailand", "Suvarnabhumi Airport", false));
        airports.add(new AirportDto("DPS", "Bali", "Indonesia", "Ngurah Rai International Airport", false));
        airports.add(new AirportDto("FCO", "Rome", "Italy", "Leonardo da Vinci-Fiumicino Airport", false));
        airports.add(new AirportDto("JTR", "Santorini", "Greece", "Santorini National Airport", false));
    }

    public List<AirportDto> getAllAirports() {
        return Collections.unmodifiableList(airports);
    }

    /**
     * Validates flight search criteria and returns a map of validation error messages, or empty if valid.
     */
    public Map<String, String> validateCriteria(FlightSearchCriteria criteria) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (criteria.getOrigin() == null || criteria.getOrigin().trim().isEmpty()) {
            errors.put("origin", "Please enter a departure city or airport.");
        }

        if (criteria.getDestination() == null || criteria.getDestination().trim().isEmpty()) {
            errors.put("destination", "Please enter an arrival city or airport.");
        }

        if (criteria.getOrigin() != null && criteria.getDestination() != null) {
            String originCode = extractCodeOrName(criteria.getOrigin());
            String destCode = extractCodeOrName(criteria.getDestination());
            if (originCode.equalsIgnoreCase(destCode)) {
                errors.put("destination", "Departure and arrival destinations cannot be the same.");
            }
        }

        if (criteria.getDepartDate() == null) {
            errors.put("departDate", "Departure date is required.");
        } else if (criteria.getDepartDate().isBefore(LocalDate.now())) {
            errors.put("departDate", "Departure date cannot be in the past.");
        }

        if ("roundTrip".equalsIgnoreCase(criteria.getTripType()) && criteria.getReturnDate() != null) {
            if (criteria.getDepartDate() != null && criteria.getReturnDate().isBefore(criteria.getDepartDate())) {
                errors.put("returnDate", "Return date cannot be earlier than departure date.");
            }
        }

        if (criteria.getPassengers() < 1 || criteria.getPassengers() > 9) {
            errors.put("passengers", "Passenger count must be between 1 and 9.");
        }

        return errors;
    }

    /**
     * Executes the flight search using realistic mock inventory matching the criteria.
     */
    public FlightSearchResponse searchFlights(FlightSearchCriteria criteria) {
        String originCity = normalizeCity(criteria.getOrigin(), "New York");
        String originCode = extractCode(criteria.getOrigin(), "JFK");
        String destCity = normalizeCity(criteria.getDestination(), "Paris");
        String destCode = extractCode(criteria.getDestination(), "CDG");

        int passengerCount = Math.max(1, criteria.getPassengers());
        String cabin = (criteria.getCabinClass() != null && !criteria.getCabinClass().isBlank())
                ? criteria.getCabinClass() : "Economy";

        // Generate base flight catalogue for this route
        List<FlightOffer> generatedOffers = generateFlightsForRoute(
                originCode, originCity, destCode, destCity,
                criteria.getDepartDate(), passengerCount, cabin
        );

        // Extract list of all unique airlines present before filtering
        List<String> availableAirlines = generatedOffers.stream()
                .map(FlightOffer::airline)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Apply filters
        List<FlightOffer> filtered = generatedOffers.stream()
                .filter(f -> {
                    if (criteria.getAirline() != null && !criteria.getAirline().isBlank() && !"all".equalsIgnoreCase(criteria.getAirline())) {
                        return f.airline().equalsIgnoreCase(criteria.getAirline());
                    }
                    return true;
                })
                .filter(f -> {
                    if (Boolean.TRUE.equals(criteria.getNonStopOnly())) {
                        return f.isNonStop();
                    }
                    return true;
                })
                .filter(f -> {
                    if (criteria.getMaxPrice() != null && criteria.getMaxPrice() > 0) {
                        return f.price() <= criteria.getMaxPrice();
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Apply sorting
        String sort = criteria.getSortBy() != null ? criteria.getSortBy().toLowerCase() : "price_asc";
        switch (sort) {
            case "price_desc" -> filtered.sort(Comparator.comparingInt(FlightOffer::price).reversed());
            case "duration", "duration_asc" -> filtered.sort(Comparator.comparingInt(FlightOffer::durationMinutes));
            case "departure", "departure_asc" -> filtered.sort(Comparator.comparing(FlightOffer::departureTime));
            case "price_asc" -> filtered.sort(Comparator.comparingInt(FlightOffer::price));
            default -> filtered.sort(Comparator.comparingInt(FlightOffer::price));
        }

        int minPrice = generatedOffers.stream().mapToInt(FlightOffer::price).min().orElse(150);
        int maxPrice = generatedOffers.stream().mapToInt(FlightOffer::price).max().orElse(1200);

        return new FlightSearchResponse(
                true,
                originCity + " (" + originCode + ")",
                destCity + " (" + destCode + ")",
                criteria.getDepartDate(),
                criteria.getReturnDate(),
                passengerCount,
                cabin,
                criteria.getTripType(),
                filtered.size(),
                filtered,
                availableAirlines,
                minPrice,
                maxPrice,
                "Demonstration data for simulation purposes only. Not connected to live airline inventory."
        );
    }

    /**
     * Confirms a simulated flight booking and produces a reference number.
     */
    public BookingResponse createBooking(BookingRequest req) {
        String ref = "VX-" + ThreadLocalRandom.current().nextInt(10000, 99999);
        String name = (req.passengerName() != null && !req.passengerName().isBlank()) ? req.passengerName() : "Valued Traveler";
        String email = (req.passengerEmail() != null && !req.passengerEmail().isBlank()) ? req.passengerEmail() : "traveler@example.com";
        String route = (req.origin() != null ? req.origin() : "Origin") + " → " + (req.destination() != null ? req.destination() : "Destination");

        return new BookingResponse(
                true,
                ref,
                "CONFIRMED",
                name,
                email,
                req.flightNumber() != null ? req.flightNumber() : "VX 100",
                req.airline() != null ? req.airline() : "VoyageX Partner Airlines",
                route,
                req.departureDate() != null ? req.departureDate() : LocalDate.now().plusDays(3).toString(),
                req.departureTime() != null ? req.departureTime() : "10:00 AM",
                req.safePassengers(),
                req.cabinClass() != null ? req.cabinClass() : "Economy",
                req.safeTotalFare() > 0 ? req.safeTotalFare() : 450,
                "$",
                "Your simulated flight booking has been confirmed! An e-ticket summary has been generated for demonstration.",
                "DEMO ONLY: No actual reservation was made with any airline, and no payment was charged."
        );
    }

    private List<FlightOffer> generateFlightsForRoute(String origCode, String origCity,
                                                     String destCode, String destCity,
                                                     LocalDate date, int passengers, String cabin) {
        // Base distance and duration estimate
        int baseDuration = calculateBaseDurationMinutes(origCode, destCode);
        int baseFare = calculateBasePrice(origCode, destCode, cabin);

        // Predefined airlines pool based on Indian vs International routes
        boolean isIndianRoute = isIndianAirport(origCode) || isIndianAirport(destCode);

        List<AirlineProfile> candidateAirlines = new ArrayList<>();
        if (isIndianRoute) {
            candidateAirlines.add(new AirlineProfile("Air India", "AI", "AI", "Airbus A350-900", 1.0));
            candidateAirlines.add(new AirlineProfile("IndiGo", "6E", "6E", "Airbus A321neo", 0.88));
            candidateAirlines.add(new AirlineProfile("Vistara", "UK", "UK", "Boeing 787-9 Dreamliner", 1.05));
            candidateAirlines.add(new AirlineProfile("Emirates", "EK", "EK", "Boeing 777-300ER", 1.25));
            candidateAirlines.add(new AirlineProfile("Singapore Airlines", "SQ", "SQ", "Airbus A350-900", 1.30));
            candidateAirlines.add(new AirlineProfile("Qatar Airways", "QR", "QR", "Boeing 787-9", 1.22));
            candidateAirlines.add(new AirlineProfile("British Airways", "BA", "BA", "Airbus A350-1000", 1.28));
        } else {
            candidateAirlines.add(new AirlineProfile("Air France", "AF", "AF", "Boeing 777-300ER", 1.0));
            candidateAirlines.add(new AirlineProfile("Delta Air Lines", "DL", "DL", "Airbus A350-900", 0.98));
            candidateAirlines.add(new AirlineProfile("Emirates", "EK", "EK", "Airbus A380-800", 1.15));
            candidateAirlines.add(new AirlineProfile("British Airways", "BA", "BA", "Boeing 787-9", 1.08));
            candidateAirlines.add(new AirlineProfile("Singapore Airlines", "SQ", "SQ", "Airbus A350-900", 1.20));
            candidateAirlines.add(new AirlineProfile("Lufthansa", "LH", "LH", "Airbus A350-900", 1.10));
            candidateAirlines.add(new AirlineProfile("Qatar Airways", "QR", "QR", "Boeing 777-300ER", 1.14));
        }

        List<FlightOffer> offers = new ArrayList<>();
        String[] departureTimes = {"06:15 AM", "08:45 AM", "11:30 AM", "02:15 PM", "05:50 PM", "08:30 PM", "10:45 PM"};

        int idCounter = 101;
        for (int i = 0; i < candidateAirlines.size(); i++) {
            AirlineProfile profile = candidateAirlines.get(i);
            String depTime = departureTimes[i % departureTimes.length];

            boolean isNonstop = (i % 3 != 1); // 2 out of 3 are nonstop
            int durationMins = isNonstop ? baseDuration : (baseDuration + 110 + (i * 15 % 45));
            String durationStr = formatDuration(durationMins);
            String arrTime = calculateArrivalTime(depTime, durationMins);

            String layover = isNonstop ? "Non-stop" : getLayoverCity(origCode, destCode, i);
            int price = (int) Math.round(baseFare * profile.fareMultiplier * (isNonstop ? 1.05 : 0.92));
            int totalFare = price * passengers;
            int seatsLeft = 2 + (i * 3 % 7);

            String badge = switch (i) {
                case 0 -> "Fastest Flight";
                case 1 -> "Lowest Fare";
                case 2 -> "Best Value";
                case 3 -> "Top Rated";
                default -> isNonstop ? "Direct Route" : "Popular Layover";
            };

            int flightNumInt = 100 + (Math.abs((profile.code + origCode + destCode).hashCode()) % 800) + i * 5;
            String flightNo = profile.code + " " + flightNumInt;

            offers.add(new FlightOffer(
                    "FL-" + idCounter++,
                    profile.name,
                    profile.code,
                    flightNo,
                    origCode,
                    origCity,
                    destCode,
                    destCity,
                    depTime,
                    arrTime,
                    durationStr,
                    durationMins,
                    isNonstop,
                    layover,
                    price,
                    totalFare,
                    "$",
                    cabin,
                    seatsLeft,
                    profile.aircraft,
                    "Standard Schedule"
            ));
        }

        // Compute genuine rankings
        int minPrice = offers.stream().mapToInt(FlightOffer::price).min().orElse(0);
        int minDuration = offers.stream().filter(FlightOffer::isNonStop).mapToInt(FlightOffer::durationMinutes).min().orElse(0);

        List<FlightOffer> rankedOffers = new ArrayList<>();
        for (FlightOffer o : offers) {
            String badge = o.isNonStop() ? "Direct Nonstop" : "Convenient Layover";
            String cat = "POPULAR";

            if (o.price() == minPrice) {
                badge = "Lowest Fare";
                cat = "CHEAPEST";
            } else if (o.durationMinutes() == minDuration && o.isNonStop()) {
                badge = "Fastest Route";
                cat = "FASTEST";
            } else if (o.isNonStop() && o.price() <= minPrice * 1.15) {
                badge = "Best Overall";
                cat = "BEST_OVERALL";
            } else if (o.isNonStop()) {
                badge = "Direct Nonstop";
                cat = "NONSTOP";
            }

            if ("business".equalsIgnoreCase(cabin) || "first".equalsIgnoreCase(cabin)) {
                cat = "PREMIUM";
            }

            String baggage = switch (cabin.toLowerCase()) {
                case "business" -> "Cabin: 14kg • Checked: 2x 32kg • Lounge Access";
                case "first", "first class" -> "Cabin: 18kg • Checked: 3x 32kg • Chauffeur & VIP Lounge";
                case "premium economy" -> "Cabin: 10kg • Checked: 2x 23kg • Priority Boarding";
                default -> "Cabin: 7kg • Checked: 23kg included";
            };

            String conditions = switch (cabin.toLowerCase()) {
                case "business", "first", "first class" -> "Lie-flat bed • 100% Refundable • Priority boarding";
                case "premium economy" -> "Extra legroom • Free seat selection • Flexible rebooking";
                default -> "Standard seat selection • 24h change window • In-flight snack";
            };

            rankedOffers.add(new FlightOffer(
                    o.id(), o.airline(), o.airlineCode(), o.flightNumber(),
                    o.originCode(), o.originCity(), o.destinationCode(), o.destinationCity(),
                    o.departureTime(), o.arrivalTime(), o.duration(), o.durationMinutes(),
                    o.isNonStop(), o.layoverInfo(), o.price(), o.totalFare(),
                    o.currency(), o.cabinClass(), o.seatsAvailable(), o.aircraft(),
                    badge, baggage, conditions, cat
            ));
        }

        return rankedOffers;
    }

    private int calculateBaseDurationMinutes(String orig, String dest) {
        String pair = orig.toUpperCase() + "-" + dest.toUpperCase();
        return switch (pair) {
            case "DEL-BOM", "BOM-DEL" -> 130; // 2h 10m
            case "DEL-BLR", "BLR-DEL" -> 165; // 2h 45m
            case "DEL-GOI", "GOI-DEL" -> 150; // 2h 30m
            case "BOM-GOI", "GOI-BOM" -> 75;  // 1h 15m
            case "DEL-DXB", "DXB-DEL" -> 225; // 3h 45m
            case "BOM-DXB", "DXB-BOM" -> 200; // 3h 20m
            case "DEL-SIN", "SIN-DEL" -> 330; // 5h 30m
            case "DEL-LHR", "LHR-DEL" -> 560; // 9h 20m
            case "DEL-CDG", "CDG-DEL" -> 545; // 9h 05m
            case "JFK-CDG", "CDG-JFK" -> 465; // 7h 45m
            case "JFK-LHR", "LHR-JFK" -> 420; // 7h 00m
            case "JFK-HND", "HND-JFK" -> 830; // 13h 50m
            default -> 180 + (Math.abs(pair.hashCode()) % 360);
        };
    }

    private int calculateBasePrice(String orig, String dest, String cabin) {
        int duration = calculateBaseDurationMinutes(orig, dest);
        int base = Math.max(95, (int) Math.round(duration * 0.72));

        double cabinFactor = switch (cabin.toLowerCase()) {
            case "premium economy" -> 1.45;
            case "business", "business class" -> 2.60;
            case "first", "first class" -> 4.20;
            default -> 1.0;
        };

        return (int) Math.round(base * cabinFactor);
    }

    private String getLayoverCity(String orig, String dest, int index) {
        String[] hubs = isIndianAirport(orig) || isIndianAirport(dest)
                ? new String[]{"1 stop in Mumbai (BOM &bull; 1h 45m layover)", "1 stop in Dubai (DXB &bull; 2h 10m layover)", "1 stop in Doha (DOH &bull; 1h 30m layover)"}
                : new String[]{"1 stop in London (LHR &bull; 1h 50m layover)", "1 stop in Frankfurt (FRA &bull; 2h 15m layover)", "1 stop in Paris (CDG &bull; 1h 40m layover)"};
        return hubs[index % hubs.length];
    }

    private boolean isIndianAirport(String code) {
        return airports.stream()
                .anyMatch(a -> a.code().equalsIgnoreCase(code) && a.isIndian());
    }

    private String formatDuration(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return h + "h " + (m < 10 ? "0" + m : m) + "m";
    }

    private String calculateArrivalTime(String depTimeStr, int durationMinutes) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
            LocalTime dep = LocalTime.parse(depTimeStr.trim(), formatter);
            LocalTime arr = dep.plusMinutes(durationMinutes);
            int dayOffset = (dep.getHour() * 60 + dep.getMinute() + durationMinutes) / (24 * 60);
            String arrFormatted = arr.format(formatter);
            if (dayOffset > 0) {
                arrFormatted += " (+" + dayOffset + ")";
            }
            return arrFormatted;
        } catch (Exception e) {
            return "Next Day";
        }
    }

    private String extractCodeOrName(String input) {
        if (input == null) return "";
        int start = input.indexOf('(');
        int end = input.indexOf(')');
        if (start != -1 && end > start) {
            return input.substring(start + 1, end).trim();
        }
        return input.trim();
    }

    private String extractCode(String input, String fallback) {
        String extracted = extractCodeOrName(input);
        if (extracted.length() == 3) {
            return extracted.toUpperCase();
        }
        for (AirportDto a : airports) {
            if (a.city().equalsIgnoreCase(input.trim()) || a.name().equalsIgnoreCase(input.trim()) || a.code().equalsIgnoreCase(input.trim())) {
                return a.code();
            }
        }
        return fallback;
    }

    private String normalizeCity(String input, String fallback) {
        if (input == null || input.isBlank()) return fallback;
        int idx = input.indexOf('(');
        if (idx != -1) {
            return input.substring(0, idx).trim();
        }
        for (AirportDto a : airports) {
            if (a.code().equalsIgnoreCase(input.trim())) {
                return a.city();
            }
        }
        return input.trim();
    }

    private record AirlineProfile(String name, String code, String prefix, String aircraft, double fareMultiplier) {}
}
