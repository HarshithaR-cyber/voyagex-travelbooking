package travelbooking.service;

import org.springframework.stereotype.Service;
import travelbooking.model.FlightStatusInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FlightStatusService {

    private final Map<String, FlightStatusInfo> flights = new ConcurrentHashMap<>();
    private final List<Map<String, String>> notificationSubscriptions = Collections.synchronizedList(new ArrayList<>());

    public FlightStatusService() {
        initFlightStatuses();
    }

    private void initFlightStatuses() {
        flights.put("AI 101", new FlightStatusInfo(
                "AI 101", "Air India", "DEL (New Delhi)", "BOM (Mumbai)",
                "06:15 AM", "06:15 AM", "08:25 AM", "08:25 AM",
                "ON_TIME", 0, "Flight operating on regular schedule",
                "Terminal 3", "Gate 14B", "Belt 4", "Airbus A350-900"
        ));

        flights.put("6E 293", new FlightStatusInfo(
                "6E 293", "IndiGo", "DEL (New Delhi)", "BOM (Mumbai)",
                "08:45 AM", "09:30 AM", "11:00 AM", "11:45 AM",
                "DELAYED", 45, "Late arrival of inbound aircraft from Kolkata due to morning runway maintenance",
                "Terminal 1D", "Gate 04", "Belt 2", "Airbus A321neo"
        ));

        flights.put("EK 202", new FlightStatusInfo(
                "EK 202", "Emirates", "JFK (New York)", "DXB (Dubai)",
                "11:00 PM", "11:00 PM", "07:45 PM (+1)", "07:45 PM (+1)",
                "BOARDING", 0, "Now boarding zones 1 through 4 at gate B22",
                "Terminal 4", "Gate B22", "Belt 7", "Airbus A380-800"
        ));

        flights.put("BA 178", new FlightStatusInfo(
                "BA 178", "British Airways", "JFK (New York)", "LHR (London)",
                "08:45 PM", "08:45 PM", "08:30 AM (+1)", "08:30 AM (+1)",
                "DEPARTED", 0, "Cruising at 38,000 ft over North Atlantic Corridor",
                "Terminal 8", "Gate 31", "Belt 9", "Boeing 787-9"
        ));

        flights.put("AF 023", new FlightStatusInfo(
                "AF 023", "Air France", "JFK (New York)", "CDG (Paris)",
                "07:30 PM", "07:30 PM", "09:15 AM (+1)", "09:15 AM (+1)",
                "LANDED", 0, "Landed smoothly at Charles de Gaulle Airport Terminal 2E",
                "Terminal 1", "Gate 09", "Belt 5", "Boeing 777-300ER"
        ));

        flights.put("SQ 165", new FlightStatusInfo(
                "SQ 165", "Singapore Airlines", "DEL (New Delhi)", "SIN (Singapore)",
                "09:50 AM", "10:25 AM", "06:05 PM", "06:40 PM",
                "DELAYED", 35, "Air traffic control sequencing delay at Changi approach airspace",
                "Terminal 3", "Gate 18A", "Belt 6", "Airbus A350-900"
        ));

        flights.put("UK 345", new FlightStatusInfo(
                "UK 345", "Vistara", "DEL (New Delhi)", "BOM (Mumbai)",
                "11:30 AM", "11:30 AM", "01:40 PM", "01:40 PM",
                "ON_TIME", 0, "On schedule. Check-in counters open at Terminal 3 Row J",
                "Terminal 3", "Gate 20", "Belt 3", "Boeing 787-9 Dreamliner"
        ));

        flights.put("QR 787", new FlightStatusInfo(
                "QR 787", "Qatar Airways", "DEL (New Delhi)", "DOH (Doha)",
                "03:45 AM", "03:45 AM", "05:40 AM", "05:40 AM",
                "SCHEDULED", 0, "Scheduled on time. Gate assignment confirmed at 01:45 AM",
                "Terminal 3", "Gate 12", "Belt 1", "Boeing 787-9"
        ));
    }

    public List<FlightStatusInfo> getAllStatuses() {
        return new ArrayList<>(flights.values());
    }

    public Optional<FlightStatusInfo> getStatusByFlightNumber(String flightNumber) {
        if (flightNumber == null) return Optional.empty();
        String normalized = flightNumber.trim().toUpperCase().replaceAll("\\s+", " ");
        for (Map.Entry<String, FlightStatusInfo> entry : flights.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(normalized) ||
                entry.getKey().replace(" ", "").equalsIgnoreCase(normalized.replace(" ", ""))) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    public boolean subscribeNotifications(String flightNumber, String contact, String type) {
        notificationSubscriptions.add(Map.of(
                "flightNumber", flightNumber,
                "contact", contact,
                "type", type != null ? type : "EMAIL",
                "subscribedAt", new Date().toString()
        ));
        return true;
    }
}
