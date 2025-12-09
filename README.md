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
# Purrtfolio: Manage Your Assets with Claw-ver Confidence

## 🐱 Stop Chasing Lasers, Start Chasing Returns!

Tired of feeling like your finances are tangled up in yarn? Welcome to **Purrtfolio**, the investment management web application that brings clarity, comfort, and pawsome returns to your financial future. We believe that managing money should feel as effortless as a cat nap—safe, secure, and deeply satisfying.

Built with Java and modern web technologies, Purrtfolio is a full-stack portfolio management system that lets you track stocks, monitor your investments, and watch your wealth grow in real-time!

---

## 🚀 Features

- ✅ **User Authentication** - Secure signup and login system
- ✅ **Portfolio Management** - Track your stock holdings in one place
- ✅ **Real-Time Stock Prices** - Live data from Twelve Data API
- ✅ **Stock Search** - Find and explore thousands of NASDAQ stocks
- ✅ **Buy Stocks** - Purchase stocks and build your portfolio
- ✅ **Profit/Loss Tracking** - See your gains and losses at a glance
- ✅ **Responsive Dashboard** - Beautiful, easy-to-use interface

---

## 🏗️ Architecture

This project follows professional **MVC (Model-View-Controller)** architecture with additional Service and Repository layers:

```
📁 Project Structure
├── model/          # Domain entities (User, Stock, Portfolio)
├── controller/     # HTTP request handlers
├── service/        # Business logic layer
├── repository/     # Data access layer
├── dto/            # Data transfer objects
└── config/         # Application configuration
```

### Technology Stack

- **Backend**: Java 21, Spark Framework
- **Frontend**: HTML, CSS, JavaScript
- **Data Storage**: JSON files
- **External API**: Twelve Data (stock prices)
- **Build Tool**: Maven

---

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- Internet connection (for stock price API)

---

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd portfolio-management-gpproject/webservice
```

### 2. Build the Project
```bash
mvn clean package
```

### 3. Run the Application
```bash
java -jar target/spark-hello-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Or using Maven:
```bash
mvn exec:java -Dexec.mainClass="org.global.academy.Server"
```

### 4. Access the Application
Open your browser and navigate to:
```
http://localhost:8080
```

---

## 📱 How to Use

### 1. **Sign Up**
- Navigate to the signup page
- Create your account with a username and password
- You'll be automatically logged in

### 2. **Search for Stocks**
- Use the search bar to find stocks by symbol or company name
- View real-time prices for thousands of NASDAQ stocks

### 3. **Buy Stocks**
- Select a stock and specify the quantity
- Add it to your portfolio with one click

### 4. **Track Your Portfolio**
- View all your holdings on the dashboard
- See current prices, gains/losses, and total portfolio value
- Watch your wealth grow in real-time!

---

## 🎯 Why Choose Purrtfolio?

At Purrtfolio, we offer more than just portfolio management; we offer a cat-alyst for true financial growth, backed by sophisticated, data-driven strategies.

### 🐾 Key Benefits

* **Tailored Investment Strategies**: Every cat is unique, and so is every portfolio. We craft strategies that are purr-fectly matched to your long-term goals and risk tolerance. We focus on diversification, ensuring your assets have nine lives.

* **Real-Time Tracking**: Keep a watchful eye on your nest egg with our easy-to-use digital dashboard. No more guessing games—you'll see your growth in real time, making you feel feline good about your financial health.

* **Claw-ver Technology**: Built with professional MVC architecture, our system is maintainable, scalable, and follows industry best practices. Your data is in capable paws.

* **Low Maintenance, High Reward**: We handle the complex daily decisions so you don't have to. Set it and forget it, just like your human setting out your favorite kibble. Your wealth is in capable paws.

* **Zero Hidden Fees**: We are open book! Unlike that mysterious midnight meowing, our pricing structure is crystal clear and straightforward.

---

## 📂 Project Structure

```
portfolio-management-gpproject/
├── webservice/
│   ├── src/main/java/org/global/academy/
│   │   ├── Server.java                 # Main application
│   │   ├── config/                     # Configuration
│   │   ├── model/                      # Domain models
│   │   ├── controller/                 # HTTP controllers
│   │   ├── service/                    # Business logic
│   │   ├── repository/                 # Data access
│   │   └── dto/                        # Data transfer objects
│   ├── src/main/resources/
│   │   └── public/                     # HTML, CSS, JS files
│   ├── data/                           # JSON data files
│   └── pom.xml                         # Maven configuration
├── ARCHITECTURE_DIAGRAM.md             # Architecture documentation
└── README.md                           # This file
```

---

## 🧪 API Endpoints

### Authentication
- `POST /login` - User login
- `POST /signup` - User registration
- `GET /logout` - User logout

### Portfolio
- `GET /api/portfolio` - Get user's portfolio with current prices
- `POST /api/buy-stock` - Purchase stocks

### Stocks
- `GET /api/stocks?q=<query>` - Search for stocks

---

## This project demonstrates:

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

## 📄 License

This project is for educational purposes.

---

## 🐱 Ready to Land on Your Feet?

It's time to stop swatting at shadows and build a portfolio that's the cat's meow. Join the thousands of users who are already enjoying un-fur-gettable financial freedom.

Start using Purrtfolio today, and let's get this purrtfolio purring!

![300px-Thumbs_Up_Crying_Cat](https://github.com/user-attachments/assets/14708027-3f79-4edf-a97d-9720c14966a0)

---


**Stock prices not loading?**
- Check your internet connection
- Verify the Twelve Data API key in `AppConfig.java`
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
- [ ] Mobile responsive design improvements

---

**Happy Investing! 🐾📈**
