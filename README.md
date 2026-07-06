# AutoWash Pro Backend

Backend REST API for AutoWash Pro - Smart Motorcycle Washing Management System.

---

# Tech Stack

| Technology | Version |
|------------|----------|
| Java | 17 |
| Spring Boot | 3.x |
| PostgreSQL | 15 |
| Spring Security | JWT |
| JPA | Hibernate |
| Flyway | Latest |
| Maven | 3.9 |

---

# Architecture

React
    │
 REST API
    │
Spring Boot
    │
JPA / Hibernate
    │
PostgreSQL

---

````md
## 📁 Project Structure

```text
autowash-backend
│
├── pom.xml
├── .env.example
├── .gitignore
│
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── autowash
    │   │
    │   │           ├── AutowashApplication.java
    │   │           │
    │   │           ├── controller          # Presentation Layer
    │   │           │   ├── AuthController.java
    │   │           │   ├── BookingController.java
    │   │           │   ├── LoyaltyController.java
    │   │           │   ├── PaymentController.java
    │   │           │   ├── PromotionController.java
    │   │           │   ├── ServiceCatalogController.java
    │   │           │   ├── SlotController.java
    │   │           │   ├── SurveyController.java
    │   │           │   ├── VehicleController.java
    │   │           │   ├── WashController.java
    │   │           │   └── admin/
    │   │           │
    │   │           ├── service             # Business Layer
    │   │           │   ├── impl/
    │   │           │   └── *.java
    │   │           │
    │   │           ├── repository          # Data Access Layer
    │   │           │   └── *.java
    │   │           │
    │   │           ├── entity              # Persistence Layer
    │   │           │   └── *.java
    │   │           │
    │   │           ├── dto                 # Data Transfer Objects
    │   │           │   ├── request/
    │   │           │   └── response/
    │   │           │
    │   │           ├── enums               # Application Enums
    │   │           ├── config              # Spring Configuration
    │   │           ├── security            # JWT Authentication & Authorization
    │   │           ├── exception           # Global Exception Handling
    │   │           ├── common              # Shared Components
    │   │           │   ├── response/
    │   │           │   └── util/
    │   │           └── scheduler           # Scheduled Jobs
    │   │
    │   └── resources
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       └── db
    │           └── migration
    │               ├── V1__init_schema.sql
    │               ├── V2__add_triggers_functions.sql
    │               ├── V3__add_views.sql
    │               └── V4__seed_data.sql
    │
    └── test
        └── java
            ├── controller
            └── service
```

## 🏗️ Architecture Layers

| Layer | Responsibility |
|-------|----------------|
| **controller** | Handle HTTP requests and responses (Presentation Layer) |
| **service** | Business logic and transaction management (Business Layer) |
| **repository** | Database access using Spring Data JPA (Data Access Layer) |
| **entity** | JPA entities mapped to database tables (Persistence Layer) |
| **dto** | Data Transfer Objects for API communication |
| **enums** | Application-wide enumerations |
| **config** | Spring Boot configuration classes |
| **security** | JWT authentication and authorization |
| **exception** | Global exception handling |
| **common** | Shared utilities, responses and helper classes |
| **scheduler** | Scheduled tasks and cron jobs |
````


---

# Modules

## Authentication

- Register
- Login
- Refresh Token
- Logout

## Vehicle

- CRUD Vehicle

## Booking

- Booking Slot
- Cancel Booking
- Walk-in Booking

## Wash

- Assign Staff
- Start Wash
- Complete Wash

## Loyalty

- Earn Points
- Redeem Rewards
- Tier Management

## Payment

- Invoice
- Payment History

## Promotion

- Promotion Management

## Admin

- Dashboard
- Revenue Report
- Staff Performance

---

# REST APIs

## Auth

POST /auth/register

POST /auth/login

POST /auth/logout

...

## Vehicle

GET /vehicles

POST /vehicles

PUT /vehicles/{id}

DELETE /vehicles/{id}

...

---

# Business Flow

Customer
    │
Book Service
    │
Wash Vehicle
    │
Payment
    │
Earn Loyalty Points

---

# Database

- PostgreSQL
- Flyway Migration
- Trigger
- Function
- Views
- Indexes

---

# Scheduled Jobs

- Generate Booking Slots
- Expire Loyalty Points
- Monthly Tier Review

---

# Deployment

- Render
- PostgreSQL (Supabase)
- Swagger
