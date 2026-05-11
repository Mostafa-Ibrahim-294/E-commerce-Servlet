# E-Commerce Servlet REST API

A fully functional E-commerce REST API built with **Servlet API**, **MySQL**, **Redis**, and **JWT Authentication**.

## 🚀 Features

✅ **JWT Authentication** - Secure token-based authentication  
✅ **Role-Based Authorization** - Admin and User roles  
✅ **Redis Caching** - Product list caching for performance  
✅ **Rate Limiting** - Payment endpoint protection (5 attempts/min)  
✅ **Complete CRUD Operations** - For all modules  
✅ **Shopping Cart** - Add/Remove/View items  
✅ **Order Management** - Place and track orders  
✅ **Payment Processing** - CASH and CARD payment methods  
✅ **Database** - MySQL with proper schema  

## 📋 Project Structure

```
src/main/java/
├── com/ecommerce/
│   ├── config/
│   │   ├── DatabaseConfig.java
│   │   └── RedisConfig.java
│   ├── dao/
│   │   ├── UserDAO.java
│   │   ├── ProductDAO.java (with Redis caching)
│   │   ├── CategoryDAO.java
│   │   ├��─ CartDAO.java
│   │   ├── OrderDAO.java
│   │   └── PaymentDAO.java
│   ├── filter/
│   │   └── JWTFilter.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Product.java
│   │   ├── Category.java
│   │   ├── CartItem.java
│   │   ├── Order.java
│   │   ├── Payment.java
│   │   └── ApiResponse.java
│   ├── servlet/
│   │   ├── AuthServlet.java
│   │   ├── ProductServlet.java
│   │   ├── CartServlet.java
│   │   ├── OrderServlet.java
│   │   └── PaymentServlet.java
│   └── util/
│       ├── JsonUtil.java
│       ├── SecurityUtil.java
│       ├── JWTUtil.java
│       └── RateLimiter.java
```

## 🛠️ Tech Stack

- **Backend**: Servlet API (No Spring Framework)
- **Database**: MySQL 8.0+
- **Caching**: Redis
- **JSON**: Jackson
- **Authentication**: JWT (io.jsonwebtoken)
- **Build Tool**: Maven

## 📦 Dependencies

- javax.servlet-api:4.0.1
- mysql-connector-java:8.0.33
- jedis:4.4.1
- jackson-databind:2.15.2
- jjwt:0.11.5
- slf4j & logback

## 🚀 Quick Start

### Prerequisites
- Java 11+
- MySQL 8.0+
- Redis Server
- Apache Tomcat 9+
- Maven 3.6+

### Setup

1. **Clone Repository**
   ```bash
   git clone https://github.com/Mostafa-Ibrahim-294/E-commerce-Servlet.git
   cd E-commerce-Servlet
   ```

2. **Setup Database**
   ```bash
   mysql -u root -p
   CREATE DATABASE ecommerce;
   USE ecommerce;
   SOURCE database.sql;
   ```

3. **Update Configuration**
   - Edit `src/main/java/com/ecommerce/config/DatabaseConfig.java`
   - Set MySQL credentials and database URL
   - Ensure Redis is running on localhost:6379

4. **Build Project**
   ```bash
   mvn clean install
   ```

5. **Deploy to Tomcat**
   - Copy `target/ecommerce-servlet-1.0-SNAPSHOT.war` to Tomcat's `webapps` folder
   - Start Tomcat: `bin/catalina.sh run`

6. **Access API**
   ```
   http://localhost:8080/ecommerce-servlet-1.0-SNAPSHOT/
   ```

## 📚 API Endpoints

### Authentication
- **POST** `/api/auth?action=register` - Register new user
- **POST** `/api/auth?action=login` - Login user (returns JWT)
- **POST** `/api/auth?action=logout` - Logout user

### Products
- **GET** `/api/products` - Get all products (cached)
- **GET** `/api/products/{id}` - Get single product
- **POST** `/api/products` - Create product (Admin)
- **PUT** `/api/products/{id}` - Update product (Admin)
- **DELETE** `/api/products/{id}` - Delete product (Admin)

### Cart
- **POST** `/api/cart` - Add to cart
- **GET** `/api/cart` - View cart
- **PUT** `/api/cart/{id}` - Update cart item
- **DELETE** `/api/cart/{id}` - Remove from cart

### Orders
- **POST** `/api/orders` - Place order
- **GET** `/api/orders` - View orders

### Payments
- **POST** `/api/payments` - Process payment (with rate limiting)
- **GET** `/api/payments` - View payments

## 🔐 Authentication

All endpoints (except `/api/auth`) require JWT token in header:

```
Authorization: Bearer <your_jwt_token>
```

## 📝 Example Requests

### Register
```bash
curl -X POST http://localhost:8080/ecommerce-servlet-1.0-SNAPSHOT/api/auth?action=register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/ecommerce-servlet-1.0-SNAPSHOT/api/auth?action=login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

### Add to Cart
```bash
curl -X POST http://localhost:8080/ecommerce-servlet-1.0-SNAPSHOT/api/cart \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

## 🎯 Key Features Explained

### JWT Authentication
- Tokens expire after 24 hours
- Contains user ID and role information
- Validated on every protected endpoint

### Redis Caching
- Product list cached for 1 hour
- Cache invalidated on CRUD operations
- Improves performance for frequent reads

### Rate Limiting
- Payment endpoints limited to 5 requests per minute per user
- Prevents duplicate payment attempts
- Returns 429 status when limit exceeded

### Role-Based Access
- **ADMIN**: Full CRUD on all resources
- **USER**: Limited access (view products, manage own cart/orders)

## 🐛 Troubleshooting

**Database Connection Issues**
- Check MySQL is running
- Verify credentials in DatabaseConfig.java
- Ensure database exists

**Redis Connection Issues**
- Check Redis server is running (default: localhost:6379)
- Products will still work without Redis (no caching)

**JWT Token Issues**
- Ensure token is in Authorization header with "Bearer " prefix
- Check token hasn't expired

## 📄 License

MIT License - Feel free to use for learning and projects!

## 👨‍💻 Author

**Mostafa Ibrahim**
- GitHub: [@Mostafa-Ibrahim-294](https://github.com/Mostafa-Ibrahim-294)
