# 📈 Stock Trading Backend

## 📝 Project Overview
This project is a **Spring Boot-based stock trading backend** designed to handle user authentication, stock order management, and real-time stock price updates. The application features JWT authentication, an in-memory H2 database for demonstration purposes, and scheduled stock price updates every 20 seconds.

## 🛠️ Technologies Used
- **Java 17** – Primary language
- **Spring Boot** – Backend framework
- **Spring Security** – JWT authentication
- **H2 Database** – In-memory database
- **JPA (Hibernate)** – ORM for database interactions
- **Jackson** – JSON serialization/deserialization
- **Lombok** – Reduces boilerplate code
- **Maven** – Dependency management

## 🚀 How to Build and Run the App (Without IntelliJ IDEA)

### **1️⃣ Install Java & Maven**
Ensure you have **Java 17** and **Maven** installed:
```bash
java -version
mvn -version
```
If not installed, download and install them from:
- [Java 17](https://adoptium.net/)
- [Maven](https://maven.apache.org/download.cgi)

### **2️⃣ Clone the Repository**
```bash
git clone https://github.com/your-username/your-repo.git
cd your-repo
```

### **3️⃣ Configure Environment Variables**
Create an `application.properties` file inside `src/main/resources/` and set the required properties:
```properties
server.port=8080
jwt.secret=YOUR_SECRET_KEY
jwt.expiration=3600000  # 1 hour in milliseconds
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

### **4️⃣ Build the Project Using CLI**
To build the project from the command line, navigate to the root directory of the project and execute:
```bash
mvn clean install
```
This will compile the source code, run tests, and generate a `.jar` file inside the `target/` directory.

### **5️⃣ Run the Application Using CLI**
Once the build is complete, run the application with:
```bash
java -jar target/your-app-name.jar
```
This will start the Spring Boot application on the default port `8080`. You should see logs indicating that the server has started successfully.

To verify that the application is running, open a browser or use `curl` to test:
```bash
curl -X GET http://localhost:8080/api/stocks
```

## 👤 Test Accounts
Use the following demo accounts to test authentication and API features:

| Username   | Password     
|------------|-------------|
| `johndoe1`    | `password1` |
| `johndoe2`    | `password2` | 

All accounts can be used to log in and receive a JWT token, which is required for accessing protected API endpoints.

## 📡 API Endpoints & Testing Guide

### **🛠️ User Authentication**
#### **1. User Login**
**Endpoint:** `POST /api/users/login`
**Description:** Logs in a user and returns a JWT token.
**Request Body:**
```json
{
  "username": "user1",
  "password": "password1"
}
```
**Response:**
```json
{
  "token": "your-jwt-token"
}
```
**Testing:** Use Postman or cURL:
```bash
curl -X POST http://localhost:8080/api/users/login \
     -H "Content-Type: application/json" \
     -d '{"username": "user1", "password": "password1"}'
```

#### **2. User info**
**Endpoint:** `GET /api/users/info`
**Description:** Returns info for currently logged-in user.
**Request Headers:**
```http
Authorization: Bearer your-jwt-token
```
**Response:**
```json
{
    "username": "johndoe02",
    "balance": 100000.00,
    "stocks": [
        {
            "stock": "AAPL",
            "quantity": 10
        },
        {
            "stock": "TSLA",
            "quantity": 10
        },
        {
            "stock": "GOOGL",
            "quantity": 10
        },
        {
            "stock": "AMZN",
            "quantity": 10
        }
    ]
}
```
**Testing:** Use Postman or cURL:
```bash
curl -X POST http://localhost:8080/api/users/login \
     -H "Content-Type: application/json" \
```

### **📊 Stock Order Management**
#### **3. Place a New Order**
**Endpoint:** `POST /api/orders/place`
**Description:** Places a new buy/sell order (Requires authentication).
**Request Headers:**
```http
Authorization: Bearer your-jwt-token
```
**Request Body:**
```json
{
  "stockSymbol": "AAPL",
  "orderType": "BUY",
  "quantity": 10
}
```
**Response:**
```json
{
  "message": "Order has been placed"
}
```
**Testing:**
```bash
curl -X POST http://localhost:8080/api/orders/place \
     -H "Authorization: Bearer your-jwt-token" \
     -H "Content-Type: application/json" \
     -d '{"stockSymbol": "AAPL", "orderType": "BUY", "quantity": 10}'
```

### **📈 Stock Prices**
#### **4. Get Stock Prices**
**Endpoint:** `GET /api/orders/revoke/{orderId}`
**Description:** Revokes placed order.
**Response:**
```json
{
  "message": "Order has been revoked"
}
```
**Testing:**
```bash
curl -X POST http://localhost:8080/api/orders/revoke/{orderId} \
     -H "Authorization: Bearer your-jwt-token" \
     -H "Content-Type: application/json" \
```

## 📌 Additional Notes
- The application updates stock prices **every 5 seconds** via a scheduled task.
- **JWT Authentication:** All protected endpoints require the `Authorization: Bearer <token>` header.
- **Swagger Documentation:** Accessible at `http://localhost:8080/swagger-ui/` when running.

---

🚀 **Enjoy trading with our Stock Trading API!** 🚀

