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
- RabbitMq
- Caching
- Concurrency
- Securiry

## Quick Start

1. **Import Collection**

   - Download the `Wallet System.postman_collection.json`
   - Import into Postman: _File → Import → Upload Files_

2. **Set Environment Variables**
   Create a Postman environment with:

   ```json
   {
     "url": "http://localhost:1111"
   }
   ```

3. **Run Project**

   ```
   docker-compose up --build
   ```

### Note

. /api/wallet/v1/account/{id}

=> you only query your own wallet

. /api/transaction/v1/history

=> just add token I will listing the user from token
