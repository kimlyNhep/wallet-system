A comprehensive digital wallet system with authentication, transaction management, and financial operations. Built with Spring Boot and secured with JWT authentication.

![Postman Collection](https://img.shields.io/badge/Postman-Collection-orange?style=flat-square&logo=postman)

## Features

- 🔐 JWT Authentication & Authorization
- 💰 Wallet Management (Create/View)
- 🎁 Gift Code Generation/Redemption
- 💸 Cross-currency Fund Transfers
- 📊 Transaction History & CSV Export
- 👥 User Role Management

## Prerequisites

- Postman (for API testing)
- Java 21+ (for backend development)
- Maven 3.9+ (build tool)
- Docker (for containerization)

## Quick Start

1. **Import Collection**
   - Download the `Wallet System.postman_collection.json`
   - Import into Postman: *File → Import → Upload Files*

2. **Set Environment Variables**
   Create a Postman environment with:
   ```json
   {
     "url": "http://localhost:8080",
     "auth_token": "your_jwt_token_here"
   }
