# Tesla Stock Analysis Web Application

A comprehensive web application for statistical analysis of Tesla Inc. stock market data built with Java EE, JPA, and MVC architecture.

---

## 1. Project Description

This application enables statistical analysis of Tesla stock market data with the following capabilities:

1. Highest Volume Day Analysis - Identifies the trading day with the highest number of shares sold
2. Price Sorting - Sorts trading days by ascending stock price
3. Highest Volume Year - Determines the year with the highest total share volume
4. Pearson Correlation - Calculates correlation between opening price and trading volume

---

## 2. Technologies Used

### 2.1 Backend
- Java 21 - Programming language
- Jakarta EE 11 - Enterprise platform
- JPA 3.1 (EclipseLink 4.0) - Object-relational mapping
- Apache Derby 10.14.2.0 - Embedded database

### 2.2 Server
- Payara Server 6.2024.11 - Application server

### 2.3 Build Tools
- Maven 3.x - Dependency management and build automation
- JUnit 5 - Unit testing framework

### 2.4 Architecture and Patterns
- MVC (Model-View-Controller) - Application structure
- Servlets - HTTP request handling
- DAO Pattern - Data access abstraction
- Entity Pattern - Domain model representation

---

## 3. Data Format

Input CSV file structure:
```
Date,Open,High,Low,Close,Adj Close,Volume
6/30/2010,1.719333,2.028,1.553333,1.588667,1.588667,257806500
7/1/2010,1.666667,1.728,1.351333,1.592667,1.592667,123282000
```

Fields description:
- Date - Trading date (M/D/YYYY format)
- Open - Opening price
- High - Highest price of the day
- Low - Lowest price of the day
- Close - Closing price
- Adj Close - Adjusted closing price
- Volume - Number of shares traded

---

## 4. Database Schema

### 4.1 ANALYSIS_OPERATIONS Table
```
Column Name     Type            Constraints
ID              BIGINT          PRIMARY KEY, AUTO_INCREMENT
ANALYSIS_TYPE   VARCHAR(50)     NOT NULL
FILE_PATH       VARCHAR(500)    NOT NULL
TIMESTAMP       TIMESTAMP       NOT NULL
RESULT_TEXT     VARCHAR(2000)
RECORDS_COUNT   INTEGER
```

### 4.2 ANALYSIS_RESULT_DETAILS Table
```
Column Name     Type            Constraints
ID              BIGINT          PRIMARY KEY, AUTO_INCREMENT
DETAIL_KEY      VARCHAR(100)    NOT NULL
DETAIL_VALUE    VARCHAR(1000)
OPERATION_ID    BIGINT          FOREIGN KEY → ANALYSIS_OPERATIONS(ID)
```
---

## 5. Viewing Documentation
Open the generated documentation:

[Documentation](TeslaAnalysisWebApp/target/reports/apidocs)
