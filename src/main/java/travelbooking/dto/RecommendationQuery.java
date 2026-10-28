package travelbooking.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class RecommendationQuery {

    @JsonAlias({"travelStyle", "style"})
    private String travelStyle;   // ROMANTIC, ADVENTURE, LUXURY, FAMILY, SOLO

    @JsonAlias({"preferredVibe", "settingVibe", "vibe"})
    private String preferredVibe; // BEACH, CITY, MOUNTAIN, CULTURAL

    @JsonAlias({"budgetRange", "budgetPreference", "budget"})
    private String budgetRange;   // BUDGET, MID_RANGE, LUXURY

    @JsonAlias({"travelParty", "companionType", "travelerCount"})
    private String travelParty;    // COUPLE, FAMILY, SOLO, FRIENDS

    public RecommendationQuery() {}

    public RecommendationQuery(String travelStyle, String preferredVibe, String budgetRange, String travelParty) {
        this.travelStyle = travelStyle;
        this.preferredVibe = preferredVibe;
        this.budgetRange = budgetRange;
        this.travelParty = travelParty;
    }

    public String travelStyle() { return travelStyle; }
    public String preferredVibe() { return preferredVibe; }
    public String budgetRange() { return budgetRange; }
    public String travelParty() { return travelParty; }

    public String getTravelStyle() { return travelStyle; }
    public void setTravelStyle(String travelStyle) { this.travelStyle = travelStyle; }
    public String getPreferredVibe() { return preferredVibe; }
    public void setPreferredVibe(String preferredVibe) { this.preferredVibe = preferredVibe; }
    public String getBudgetRange() { return budgetRange; }
    public void setBudgetRange(String budgetRange) { this.budgetRange = budgetRange; }
    public String getTravelParty() { return travelParty; }
    public void setTravelParty(String travelParty) { this.travelParty = travelParty; }
}
