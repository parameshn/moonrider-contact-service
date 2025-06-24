Moonrider: Identity Reconciliation Service
This project delivers the backend service for Moonrider Task 1: Identity Reconciliation. Its core function is to intelligently consolidate customer contact information (email and phone number) from various transactions, building a unified and accurate customer identity profile.

Table of Contents
Project Overview
Key Features
Technical Stack
Prerequisites
Local Development Setup
API Documentation
Logging and Monitoring
Project Structure
1. Project Overview
The Identity Reconciliation Service tackles the critical challenge of customer identity resolution within e-commerce. It intelligently processes incoming contact details, identifies existing customer profiles across different purchases, and consolidates fragmented information into a single, comprehensive primary contact record. The service dynamically manages the relationships between primary and associated secondary contacts, ensuring a holistic view of each customer.

2. Key Features
Intelligent Identity Consolidation: Automatically links new contact information (email/phone) to existing customer profiles, preventing data silos.
Dynamic Primary/Secondary Contact Management: Establishes a hierarchical relationship, designating one primary contact and linking all related secondary contact information.
Robust JSON API: Exposes a RESTful /api/identify endpoint for submitting new contact data.
Structured Consolidated Response: Returns a clear JSON payload containing primaryContactId, all associated emails, phoneNumbers, and secondaryContactIds.
API Versioning: Supports clear API evolution with distinct path prefixes (/v1, /v1.1, /v2).
3. Technical Stack
Backend Framework: Spring Boot (Java 17)
Database: H2 Database (in-memory for development/testing, easily configurable for production-grade SQL databases like PostgreSQL)
Build Automation: Apache Maven
API Testing & Documentation: Postman
4. Prerequisites
Ensure the following tools are installed and configured on your development machine:

Java Development Kit (JDK): Version 17 or higher
Apache Maven: Version 3.6.0 or higher
Git: For version control
Postman: For API exploration and testing
5. Local Development Setup
Follow these steps to run the service on your local machine:

Clone the Repository:

Bash

git clone https://github.com/parameshn/moonrider-contact-service.git
cd moonrider-contact-service
Build the Project: Compile the application and resolve dependencies.

Bash

mvn clean install
Run the Spring Boot Application:

Bash

mvn spring-boot:run
The application will typically start on http://localhost:8080. API requests will be accessible at http://localhost:8080/api/identify.

6. API Documentation
The core API for this service is a POST request to /api/identify. A comprehensive Postman collection detailing all API requests for testing the Identity Reconciliation Service, including various scenarios and expected responses, is available here:

Postman Collection Link: Moonrider-Task_1 Identity Reconciliation Service
For basic understanding, here are a couple of fundamental API queries:

6.1. New Primary Contact Creation
This request demonstrates how a new, unique contact (both email and phone number) leads to the creation of a primary contact.

Endpoint: POST /api/identify
Content-Type: application/json
Request Body:
JSON

{
  "email": "doc@timelab.com",
  "phoneNumber": "123456789"
}
Expected Response (Example):
JSON

{
  "primaryContactId": 1,
  "emails": ["doc@timelab.com"],
  "phoneNumbers": ["123456789"],
  "secondaryContactIds": []
}
6.2. Secondary Contact Creation (New Phone for Existing Email)
This request shows how a new phone number, when linked to an existing email, creates a secondary contact associated with the primary contact that owns the email.

Endpoint: POST /api/identify
Content-Type: application/json
Pre-requisite: The contact "doc@timelab.com" must already exist (e.g., from the "New Primary Contact Creation" example).
Request Body:
JSON

{
  "email": "doc@timelab.com",
  "phoneNumber": "987654321"
}
Expected Response (Example):
JSON

{
  "primaryContactId": 1,
  "emails": ["doc@timelab.com"],
  "phoneNumbers": ["123456789", "987654321"],
  "secondaryContactIds": [2]
}
7. Logging and Monitoring
(TODO: Populate this section with specific details about your logging and monitoring setup)

Basic Logging: Spring Boot applications output logs to stdout by default.
Error Reporting: Implement an error reporting mechanism (e.g., Sentry, Bugsnag) to capture and notify about runtime exceptions.
8. Project Structure
.
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── controller/      # REST API endpoints (e.g., IdentifyController)
│   │   │   ├── entity/          # JPA entities (e.g., Contact)
│   │   │   ├── exception/       # Custom exceptions for error handling
│   │   │   ├── repository/      # Spring Data JPA repositories for DB interaction
│   │   │   ├── service/         # Business logic for identity reconciliation
│   │   │   └── DemoApplication.java # Main Spring Boot application entry point
│   │   └── resources/           # Application properties, H2 database config, etc.
│   └── test/                    # Unit and integration tests for the service
├── pom.xml                      # Maven project object model (dependencies, build lifecycle)
├── README.md                    # Project overview and documentation (this file)
├── .gitignore                   # Specifies files/directories to be ignored by Git
