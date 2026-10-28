package travelbooking.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.ArrayList;
import java.util.List;

public class ReviewSubmissionRequest {

    @JsonAlias({"targetType", "itemType"})
    private String targetType;

    @JsonAlias({"targetId", "itemId"})
    private String targetId;

    @JsonAlias({"targetName", "itemName"})
    private String targetName;

    @JsonAlias({"authorName", "reviewerName", "name"})
    private String authorName;

    private String authorLocation;

    private Integer rating;

    @JsonAlias({"cleanlinessScore", "cleanlinessRating"})
    private Double cleanlinessScore;

    @JsonAlias({"serviceScore", "serviceRating"})
    private Double serviceScore;

    @JsonAlias({"valueScore", "valueRating"})
    private Double valueScore;

    private String title;

    @JsonAlias({"comment", "comments"})
    private String comment;

    @JsonAlias({"photoUrls", "photoUrl"})
    private Object photos;

    public ReviewSubmissionRequest() {}

    public ReviewSubmissionRequest(String targetType, String targetId, String targetName, String authorName,
                                   String authorLocation, Integer rating, Double cleanlinessScore,
                                   Double serviceScore, Double valueScore, String title, String comment,
                                   List<String> photoUrls) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetName = targetName;
        this.authorName = authorName;
        this.authorLocation = authorLocation;
        this.rating = rating;
        this.cleanlinessScore = cleanlinessScore;
        this.serviceScore = serviceScore;
        this.valueScore = valueScore;
        this.title = title;
        this.comment = comment;
        this.photos = photoUrls;
    }

    public String targetType() { return targetType != null ? targetType : "EXPERIENCE"; }
    public String targetId() { return targetId != null ? targetId : "item-demo"; }
    public String targetName() { return targetName != null ? targetName : "VoyageX Experience"; }
    public String authorName() { return authorName != null ? authorName : "Verified Traveler"; }
    public String authorLocation() { return authorLocation != null ? authorLocation : "Global"; }
    public Integer rating() { return rating; }
    public Double cleanlinessScore() { return cleanlinessScore; }
    public Double serviceScore() { return serviceScore; }
    public Double valueScore() { return valueScore; }
    public String title() { return title != null ? title : "Great Travel Experience"; }
    public String comment() { return comment != null ? comment : ""; }

    @SuppressWarnings("unchecked")
    public List<String> photoUrls() {
        if (photos instanceof List) {
            return (List<String>) photos;
        } else if (photos instanceof String s && !s.isBlank()) {
            return List.of(s);
        }
        return new ArrayList<>();
    }

    public int safeRating() { return (rating != null && rating >= 1 && rating <= 5) ? rating : 5; }
    public double safeCleanliness() { return cleanlinessScore != null ? cleanlinessScore : 5.0; }
    public double safeService() { return serviceScore != null ? serviceScore : 5.0; }
    public double safeValue() { return valueScore != null ? valueScore : 5.0; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getAuthorLocation() { return authorLocation; }
    public void setAuthorLocation(String authorLocation) { this.authorLocation = authorLocation; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public Double getCleanlinessScore() { return cleanlinessScore; }
    public void setCleanlinessScore(Double cleanlinessScore) { this.cleanlinessScore = cleanlinessScore; }
    public Double getServiceScore() { return serviceScore; }
    public void setServiceScore(Double serviceScore) { this.serviceScore = serviceScore; }
    public Double getValueScore() { return valueScore; }
    public void setValueScore(Double valueScore) { this.valueScore = valueScore; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Object getPhotos() { return photos; }
    public void setPhotos(Object photos) { this.photos = photos; }
}
