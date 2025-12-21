# VictusStore - E-Commerce Backend API

Spring Boot REST API for an e-commerce platform with complete product management, shopping cart, orders, and admin features.

## 🚀 Technology Stack

- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security with JWT authentication
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Maven
- **Libraries**: Lombok, MapStruct

## 📁 Project Structure

```
victuce/
├── src/
│   └── main/
│       ├── java/com/victusstore/
│       │   ├── config/          # Configuration classes
│       │   ├── controller/      # REST Controllers
│       │   ├── model/          # JPA Entities
│       │   ├── repository/     # Data Repositories
│       │   └── VictusStoreApplication.java
│       └── resources/
│           └── application.properties
├── docs/                       # Documentation
│   ├── API_ENDPOINTS_DOCUMENTATION.md
│   ├── NEW_API_FEATURES_DOCUMENTATION.md
│   ├── DATABASE_COMPATIBILITY_REPORT.md
│   └── MISSING_ENDPOINTS_REPORT.md
├── database/                   # Database Scripts
│   ├── database_complete.sql   # Complete database setup (USE THIS)
│   ├── postgre_creation.sql    # Original schema
│   ├── postgre_creation_FIXED.sql
│   └── admin_tables_creation.sql
├── pom.xml
├── mvnw
└── README.md
```

## 🗄️ Database Setup

### Quick Start
Use the complete database script:
```bash
psql -U postgres -f database/database_complete.sql
```

### Manual Setup
1. Run database creation commands from `database/database_complete.sql`
2. Connect to the database
3. Run all table creation commands

## 🔧 Configuration

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://your-database-url
spring.datasource.username=your-username
spring.datasource.password=your-password
server.port=8080
```

## 🏃 Running the Application

### Using Maven Wrapper
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Using Maven
```bash
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080/api`

## 📚 API Documentation

### Swagger UI
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/swagger-ui/index.html

### Complete API Reference
See `docs/API_ENDPOINTS_DOCUMENTATION.md` for all 64 endpoints.

### New Features Documentation
See `docs/NEW_API_FEATURES_DOCUMENTATION.md` for latest updates.

## 📊 API Overview

**Total Endpoints**: 64

### Endpoint Categories:
- Authentication: 3 endpoints
- Account Management: 5 endpoints
- Seller Management: 5 endpoints
- Category Management: 5 endpoints
- Product Management: 5 endpoints
- Product Variant Management: 4 endpoints
- Cart Management: 8 endpoints
- Cart Product Management: 6 endpoints
- Order Management: 7 endpoints
- Image Management: 6 endpoints
- Admin Coupons Management: 8 endpoints
- Admin Activity Logging: 8 endpoints

## ✨ Key Features

- ✅ Complete checkout flow (Cart → Order)
- ✅ Automatic stock management
- ✅ User cart and order history access
- ✅ Full image management for products
- ✅ Cart synchronization with backend
- ✅ Coupon management with validation
- ✅ Complete admin activity audit trail
- ✅ JWT authentication
- ✅ Swagger API documentation

## 🔐 Authentication

JWT tokens are generated on login/register and expire after 24 hours.

Include token in `Authorization` header:
```
Authorization: Bearer <token>
```

## 📖 Documentation Files

All documentation is in the `docs/` folder:

- **CONSISTENCY_REPORT_BACKEND.md** ⭐ **NEW** - Complete backend consistency audit and production readiness report
- **ENDPOINT_MAP_BACKEND.md** ⭐ **NEW** - Comprehensive endpoint mapping table with auth, roles, DTOs, and error codes
- **FEATURES_DOCUMENTATION.md** - Complete features documentation with implementation details
- **PRODUCTION_HARDENING.md** - Production hardening implementation guide
- **PRODUCTION_HARDENING_AUDIT.md** - Production hardening audit and improvements
- **API_ENDPOINTS_DOCUMENTATION.md** - Complete API reference (all 78 endpoints)
- **NEW_API_FEATURES_DOCUMENTATION.md** - Latest features documentation
- **DATABASE_COMPATIBILITY_REPORT.md** - Database schema compatibility analysis
- **MISSING_ENDPOINTS_REPORT.md** - Analysis of missing endpoints (now implemented)
- **PROJECT_STRUCTURE.md** - Project organization and file structure

## 🗂️ Database Scripts

All database scripts are in the `database/` folder:

- **database_complete.sql** - ⭐ **USE THIS** - Complete database setup
- **postgre_creation.sql** - Original schema (legacy)
- **postgre_creation_FIXED.sql** - Fixed schema
- **admin_tables_creation.sql** - Admin tables only

## 🛠️ Development

### Building the Project
```bash
mvn clean install
```

### Running Tests
```bash
mvn test
```

### Packaging
```bash
mvn clean package
```

## 📝 Project Status

✅ **Core Features**: Complete  
✅ **Checkout Flow**: Implemented  
✅ **Admin Features**: Implemented  
✅ **Documentation**: Complete  
✅ **Database Schema**: Complete  

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

This project is part of VictusStore e-commerce platform.

---

**Last Updated**: Latest  
**API Version**: 2.0  
**Spring Boot Version**: 3.1.5

