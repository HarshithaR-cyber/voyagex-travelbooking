package travelbooking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Persisted user account in H2 database.
 * Securely stores PBKDF2 password hashes and user preferences.
 */
@Entity
@Table(name = "user_accounts", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true)
})
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String fullName;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String passwordHash;

    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String salt;

    @Column(nullable = false, length = 50)
    private String membershipTier = "VoyageX Explorer";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_saved_deals", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "deal_id")
    private Set<String> savedDealIds = new HashSet<>();

    @Column(nullable = false)
    private LocalDate createdDate = LocalDate.now();

    public UserAccount() {}

    public UserAccount(String email, String fullName, String passwordHash, String salt, String membershipTier) {
        this.email = email.trim().toLowerCase();
        this.fullName = fullName.trim();
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.membershipTier = membershipTier;
        this.createdDate = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getMembershipTier() {
        return membershipTier;
    }

    public void setMembershipTier(String membershipTier) {
        this.membershipTier = membershipTier;
    }

    public Set<String> getSavedDealIds() {
        return savedDealIds;
    }

    public void setSavedDealIds(Set<String> savedDealIds) {
        this.savedDealIds = savedDealIds != null ? savedDealIds : new HashSet<>();
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getInitials() {
        if (fullName == null || fullName.isBlank()) {
            return "VX";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
