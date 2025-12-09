```
==================================================================
 ____  __ __  ____   ____  ______  _____   ___   _      ____  ___  
|    \|  |  ||    \ |    \|      ||     | /   \ | |    |    |/   \ 
|  o  )  |  ||  D  )|  D  )      ||   __||     || |     |  ||     |
|   _/|  |  ||    / |    /|_|  |_||  |_  |  O  || |___  |  ||  O  |
|  |  |  :  ||    \ |    \  |  |  |   _] |     ||     | |  ||     |
|  |  |     ||  .  \|  .  \ |  |  |  |   |     ||     | |  ||     |
|__|   \__,_||__|\_||__|\_| |__|  |__|    \___/ |_____||____|___/ 

===================================================================
```

# 🐾 Purrtfolio - Portfolio Management System

> *Your purr-fect companion for managing stocks with a little extra fluff.*

A full-stack web application for tracking stock portfolios with real-time prices, built with Java and modern web technologies. Purrtfolio combines financial clarity with a warm, pet-friendly charm.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Educational-green.svg)]()

---

## ✨ Features

- 📈 **Portfolio Tracking** - Monitor all your stock holdings in one dashboard
- 💹 **Real-Time Prices** - Live stock data from Twelve Data API
- 🔍 **Stock Search** - Find and explore thousands of NASDAQ stocks
- 🛒 **Buy Stocks** - Quick and easy stock purchases
- 📊 **Profit/Loss Tracking** - See your gains and losses at a glance
- 🔐 **User Authentication** - Secure signup and login system
- 💾 **Data Persistence** - Your portfolio is automatically saved

---

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- Internet connection (for stock price API)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/kaungnyeinlwin/portfolio-management-gpproject.git
   cd portfolio-management-gpproject/webservice
   ```

2. **Build the project**
   ```bash
   mvn clean package
   ```

3. **Run the application**
   ```bash
   java -jar target/spark-hello-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

4. **Access the app**
   ```
   Open your browser: http://localhost:8080
   ```

---

## 🏗️ Architecture

This project follows **MVC (Model-View-Controller)** architecture with Service and Repository layers:

```
📁 src/main/java/org/global/academy/
├── 📄 Server.java          # Application entry point
├── 📂 config/              # Configuration (API keys, constants)
├── 📂 model/               # Domain entities (User, Stock, Portfolio)
├── 📂 controller/          # HTTP request handlers
├── 📂 service/             # Business logic layer
├── 📂 repository/          # Data access layer
└── 📂 dto/                 # Data transfer objects
```

**Technology Stack:**
- **Backend**: Java 21, Spark Framework
- **Frontend**: HTML, CSS, JavaScript
- **Data**: JSON file storage
- **API**: Twelve Data (stock prices)
- **Build**: Maven

📖 **[View Architecture Diagram](ARCHITECTURE_DIAGRAM.md)** for detailed structure

---

## 📱 Usage

### 1. Create an Account
- Navigate to signup page
- Enter username and password
- Click "Sign Up"

### 2. Search for Stocks
- Use the search bar on the buy stocks page
- Search by symbol (e.g., "AAPL") or company name (e.g., "Apple")
- View real-time prices

### 3. Buy Stocks
- Select a stock from search results
- Enter the quantity you want to purchase
- Click "Buy" to add to your portfolio

### 4. Track Your Portfolio
- View your dashboard to see all holdings
- Monitor current prices and profit/loss
- Watch your total portfolio value update in real-time

---

## 🔌 API Endpoints

### Authentication
```
POST   /login              # User login
POST   /signup             # User registration
GET    /logout             # User logout
```

### Portfolio
```
GET    /api/portfolio      # Get user's portfolio with current prices
POST   /api/buy-stock      # Purchase stocks
```

### Stocks
```
GET    /api/stocks?q=<query>   # Search for stocks
```

---

## 📂 Project Structure

```
portfolio-management-gpproject/
├── webservice/
│   ├── src/main/java/org/global/academy/
│   │   ├── Server.java
│   │   ├── config/
│   │   ├── model/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   └── dto/
│   ├── src/main/resources/
│   │   └── public/              # HTML, CSS, JS files
│   ├── data/                    # JSON data storage
│   └── pom.xml
├── ARCHITECTURE_DIAGRAM.md
└── README.md
```

---

## 🎓 What This Project Demonstrates

- ✅ **MVC Architecture** - Proper separation of concerns
- ✅ **RESTful API Design** - Clean, intuitive endpoints
- ✅ **Dependency Injection** - Loose coupling between components
- ✅ **Repository Pattern** - Abstracted data access
- ✅ **Service Layer** - Reusable business logic
- ✅ **External API Integration** - Real-time stock data
- ✅ **Session Management** - User authentication
- ✅ **JSON Persistence** - File-based data storage

---

## 👥 Team

**Project Group 5** - Global Academy

---

## 🐛 Troubleshooting

**Stock prices not loading?**
- Check your internet connection
- Verify the API key in `AppConfig.java`
- The app uses cached prices as fallback

---

## 🚀 Future Enhancements

- [ ] Sell stocks functionality
- [ ] Portfolio performance charts
- [ ] Stock price history graphs
- [ ] Email notifications for price alerts
- [ ] Database integration (PostgreSQL/MySQL)
- [ ] Password hashing (BCrypt)
- [ ] JWT authentication
- [ ] Mobile responsive design

---

## 📚 Documentation

- 📖 **[Architecture Diagram](ARCHITECTURE_DIAGRAM.md)** - Detailed MVC structure
- 📖 **[Wiki](../../wiki)** - User guides and feature documentation
- 📖 **Javadoc** - Generate with `mvn javadoc:javadoc`

---

## 📄 License

This project is for educational purposes.

---

## 💛 Why Purrtfolio?

Purrtfolio blends **financial clarity** with **warm, pet-friendly charm**, making your investing journey both productive and delightful. Like a loyal companion sitting quietly by your side, Purrtfolio is here to guide you—one paw at a time.

![Purrtfolio Cat](https://github.com/user-attachments/assets/14708027-3f79-4edf-a97d-9720c14966a0)

**Happy Investing! 🐾📈**
