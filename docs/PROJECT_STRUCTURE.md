# Project Structure

## 📁 Directory Organization

```
victuce/
│
├── 📄 README.md                          # Main project documentation
├── 📄 pom.xml                            # Maven configuration
├── 📄 mvnw                               # Maven wrapper (Unix)
├── 📄 mvnw.cmd                           # Maven wrapper (Windows)
├── 📄 .gitignore                         # Git ignore rules
│
├── 📂 src/                               # Source code
│   └── main/
│       ├── java/com/victusstore/
│       │   ├── config/                   # Configuration classes
│       │   │   ├── CorsConfig.java
│       │   │   ├── JwtUtil.java
│       │   │   ├── SecurityConfig.java
│       │   │   └── SwaggerConfig.java
│       │   │
│       │   ├── controller/               # REST Controllers (12 files)
│       │   │   ├── AccountController.java
│       │   │   ├── AdminActivityController.java ⭐ NEW
│       │   │   ├── AdminCouponController.java ⭐ NEW
│       │   │   ├── AuthController.java
│       │   │   ├── CartController.java
│       │   │   ├── CartProductController.java
│       │   │   ├── CategoryController.java
│       │   │   ├── ImageController.java
│       │   │   ├── OrderController.java
│       │   │   ├── ProductController.java
│       │   │   ├── SellerController.java
│       │   │   └── VariantController.java
│       │   │
│       │   ├── model/                    # JPA Entities (10 files)
│       │   │   ├── Account.java
│       │   │   ├── AdminActivity.java ⭐ NEW
│       │   │   ├── Cart.java
│       │   │   ├── CartProduct.java
│       │   │   ├── Category.java
│       │   │   ├── Coupon.java ⭐ NEW
│       │   │   ├── Image.java
│       │   │   ├── Order.java
│       │   │   ├── Product.java
│       │   │   ├── ProductVariant.java
│       │   │   └── Seller.java
│       │   │
│       │   ├── repository/               # Data Repositories (11 files)
│       │   │   ├── AccountRepository.java
│       │   │   ├── AdminActivityRepository.java ⭐ NEW
│       │   │   ├── CartProductRepository.java
│       │   │   ├── CartRepository.java
│       │   │   ├── CategoryRepository.java
│       │   │   ├── CouponRepository.java ⭐ NEW
│       │   │   ├── ImageRepository.java
│       │   │   ├── OrderRepository.java
│       │   │   ├── ProductRepository.java
│       │   │   ├── ProductVariantRepository.java
│       │   │   └── SellerRepository.java
│       │   │
│       │   └── VictusStoreApplication.java
│       │
│       └── resources/
│           └── application.properties    # Application configuration
│
├── 📂 docs/                              # Documentation
│   ├── API_ENDPOINTS_DOCUMENTATION.md   # Complete API reference (64 endpoints)
│   ├── NEW_API_FEATURES_DOCUMENTATION.md # Latest features documentation
│   ├── DATABASE_COMPATIBILITY_REPORT.md # DB schema compatibility analysis
│   ├── MISSING_ENDPOINTS_REPORT.md      # Missing endpoints analysis
│   └── PROJECT_STRUCTURE.md             # This file
│
├── 📂 database/                          # Database Scripts
│   ├── database_complete.sql             # ⭐ USE THIS - Complete DB setup
│   ├── postgre_creation.sql              # Original schema (legacy)
│   ├── postgre_creation_FIXED.sql       # Fixed schema
│   └── admin_tables_creation.sql        # Admin tables only
│
└── 📂 target/                            # Build output (generated, gitignored)

```

## 📊 File Counts

### Java Source Files
- **Controllers**: 12 files
- **Models**: 10 files
- **Repositories**: 11 files
- **Configuration**: 4 files
- **Total Java Files**: 38 files

### Documentation Files
- **4 Markdown files** in `docs/`

### Database Scripts
- **4 SQL files** in `database/`

## 🎯 Key Files Reference

### Setup & Configuration
- `README.md` - Start here! Project overview and quick start
- `pom.xml` - Maven dependencies and build configuration
- `src/main/resources/application.properties` - Database and server configuration

### Database
- `database/database_complete.sql` - **Main database script** (use this for setup)

### Documentation
- `docs/API_ENDPOINTS_DOCUMENTATION.md` - Complete API reference
- `docs/NEW_API_FEATURES_DOCUMENTATION.md` - Latest features guide

## 📝 Notes

- All source code follows standard Maven directory structure
- Documentation is organized in the `docs/` folder
- Database scripts are centralized in the `database/` folder
- Build artifacts in `target/` are excluded from version control

## 🔄 Organization Principles

1. **Separation of Concerns**: Code, docs, and scripts are separated
2. **Standard Maven Structure**: Follows Maven conventions
3. **Easy Navigation**: Clear folder names and structure
4. **Documentation First**: All docs in one place for easy reference

