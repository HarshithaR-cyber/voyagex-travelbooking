# ✈️ VoyageX — Modern Travel & Hospitality Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Render-Deployed-46E3B7?logo=render&logoColor=black)](https://voyagex-travelbooking.onrender.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**VoyageX** is a full-stack, enterprise-grade travel booking web application designed for seamless discovery, booking, and management of flights, luxury hotels, and VIP travel packages. Built with **Spring Boot** and a modern, responsive UI featuring dynamic pricing, interactive seat maps, and authentication guards.

🌐 **Live Demo:** [https://voyagex-travelbooking.onrender.com](https://voyagex-travelbooking.onrender.com)

---

## 🌟 Key Features

### 🔐 1. Authentication & Session Management
- **Pre-Access Auth Guard:** Users are directed to a modern authentication gateway before entering the application.
- **Sign In & Register:** Form validation, secure password hashing, and instant session generation.
- **Password Recovery:** Self-service password reset flow with step-by-step verification.
- **Session Persistence:** Secure session token management (`/api/auth/verify-session`) with clean server-side logout.

### 🛫 2. Flight Discovery & Booking
- **Smart Flight Search:** Dynamic origin/destination search, departure/return dates, passenger count, and cabin class selection (Economy, Premium Economy, Business, First).
- **Interactive Seat Selection:** Real-time cabin seat map with status indicators (available, selected, occupied).
- **Live Flight Status:** Track incoming and outgoing flights with delay and gate information.
- **Dynamic Pricing & Price Freeze:** Real-time fare tracking and option to freeze rates prior to confirmation.

### 🏨 3. Hotel Recommendations & Stays
- **Curated Stays:** Filter by destination, star rating, amenities (Wi-Fi, pool, spa), and price per night.
- **Room Selection:** Browse room tiers (Deluxe, Suite, Ocean View) with transparent pricing and instant booking.

### 💎 4. VIP Deals & Exclusive Packages
- **Curated Luxury Travel:** High-end flight + hotel package bundles with discounts.
- **Category Filtering:** Filter packages by Tropical, European, Urban, or Mountain getaways.

### 🛡️ 5. Booking Management & Cancellations
- **Transparent Refund Calculator:** Instant cancellation quotes based on ticket tier and departure time.
- **One-Click Cancellations:** Process cancellations with refund breakdowns and confirmation records.

### ⭐ 6. Traveler Reviews & Community
- **Verified Feedback:** View passenger reviews, airline ratings, and hotel ratings.
- **Review Submission:** Interactive modal to submit ratings and feedback.

---

## 🏗️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 25, Spring Boot 4.x, Spring Data JPA, Spring Validation |
| **Database** | H2 In-Memory Database (Pre-configured with demo catalog & schema auto-ddl) |
| **Frontend** | HTML5, CSS3 (Glassmorphism, CSS Custom Properties, Responsive Grid), Vanilla JavaScript (ES6+) |
| **Containerization** | Multi-stage Dockerfile with Eclipse Temurin (Alpine / Slim runtime) |
| **CI / CD & Cloud** | Git, GitHub, Render (Continuous Deployment on push to `main`) |

---

## 📂 Project Architecture

```
travelbooking/
├── .mvn/                         # Maven wrapper files
├── src/
│   ├── main/
│   │   ├── java/travelbooking/
│   │   │   ├── controller/      # REST API Controllers (Auth, Flights, Hotels, VIP, etc.)
│   │   │   ├── dto/             # Data Transfer Objects (Requests & Responses)
│   │   │   ├── model/           # JPA Entities and Domain Models
│   │   │   ├── service/         # Business Logic & In-memory Repositories
│   │   │   └── TravelbookingApplication.java # Spring Boot Main Class
│   │   └── resources/
│   │       ├── application.properties # Server port & DB configuration
│   │       └── static/          # Frontend assets
│   │           ├── index.html   # Main Dashboard & Booking Experience
│   │           ├── login.html   # Auth & Account Gateway
│   │           ├── css/
│   │           │   └── style.css # Design system & typography
│   │           └── js/
│   │               ├── app.js   # Flight & booking interactions
│   │               └── main.js  # Main UI logic, search, & auth guard
│   └── test/                    # JUnit test suites
├── Dockerfile                   # Multi-stage production container build
├── .dockerignore                # Container build exclusions
├── pom.xml                      # Maven dependencies & build plugins
└── README.md                    # Project documentation
```

---

## 🚀 Getting Started

### Prerequisites
- **Java Development Kit (JDK):** Version 21 or 25
- **Maven:** Version 3.9+ (or use the included `./mvnw`)
- **Git**

### Local Setup & Running

1. **Clone the repository:**
   ```bash
   git clone https://github.com/HarshithaR-cyber/voyagex-travelbooking.git
   cd voyagex-travelbooking
   ```

2. **Run with Maven:**
   ```bash
   # Windows (PowerShell / Command Prompt)
   ./mvnw.cmd spring-boot:run

   # macOS / Linux
   chmod +x ./mvnw
   ./mvnw spring-boot:run
   ```

3. **Open the application:**
   Navigate to [http://localhost:8080](http://localhost:8080) in your browser.
   *(You will be greeted by the VoyageX Sign-In gateway).*

4. **Demo Credentials:**
   - **Email:** `arun.patel@example.com`
   - **Password:** `voyagex123`
   *(Or click "Create Account" on the login page to register a new account).*

5. **H2 Database Console (Optional):**
   - URL: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
   - JDBC URL: `jdbc:h2:mem:traveldb`
   - User Name: `sa`
   - Password: *(leave blank)*

---

## 🐳 Docker Deployment

The application includes a production-optimized multi-stage `Dockerfile`:

```bash
# 1. Build the Docker image
docker build -t voyagex-travelbooking .

# 2. Run the container
docker run -p 8080:8080 -e PORT=8080 voyagex-travelbooking
```

Access the app at `http://localhost:8080`.

---

## 📡 REST API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Authenticate user and receive session token |
| `POST` | `/api/auth/register` | Create a new user profile |
| `GET` | `/api/auth/verify-session?token={token}` | Validate active session token |
| `POST` | `/api/auth/logout` | Revoke session and log out |
| `POST` | `/api/auth/forgot-password` | Request password reset verification |
| `POST` | `/api/flights/search` | Search flights by origin, destination, dates & class |
| `GET` | `/api/flights/status/{flightNumber}` | Retrieve real-time flight tracking info |
| `POST` | `/api/bookings/flight` | Confirm flight booking reservation |
| `GET` | `/api/vip-deals` | Fetch curated luxury travel deals |
| `GET` | `/api/hotels/recommendations` | Get personalized hotel recommendations |
| `POST` | `/api/cancellations/quote` | Calculate cancellation refund quote |

---

## 🔄 Continuous Integration & Deployment (CI/CD)

This repository is set up with continuous deployment connected to **Render**:
1. Any commit pushed to the `main` branch triggers an automated build.
2. Render uses the multi-stage [Dockerfile](Dockerfile) to compile the code and build a lightweight JRE runtime container.
3. Zero-downtime updates are deployed automatically to `https://voyagex-travelbooking.onrender.com`.

---

## 📄 License
This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
