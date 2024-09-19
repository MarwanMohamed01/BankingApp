# BankingApp

## Overview

**BankingApp** is a comprehensive banking application that allows users to manage their accounts, wallets, and transactions. It supports functionalities such as creating and managing wallets, depositing and withdrawing funds, and tracking transactions.

## Features

- **User Authentication**: Secure user registration and login.
- **Wallet Management**: Create, update, and delete wallets.
- **Transaction Management**: Deposit and withdraw funds, view transaction history.
- **KYC (Know Your Customer)**: Verify user identity through document uploads.
- **Email Notifications**: Receive notifications for deposits and withdrawals.

## Technologies

- **Java**: The programming language used for application development.
- **Spring Boot**: Framework for building the application.
- **Spring Security**: For authentication and authorization.
- **JPA/Hibernate**: For database interactions.
- **PostgreSQL/MySQL**: Database for storing user and transaction data.

## Setup

### Prerequisites

- Java 17 or higher
- Maven
- A relational database (PostgreSQL or MySQL)

### Installation

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/MarwanMohamed01/BankingApp.git
   cd BankingApp
   ```

2. **Configure Database**:

   Update `application.yml` with your database configurations.

3. **Build the Application**:

   ```bash
   mvn clean install
   ```

4. **Run the Application**:

   ```bash
   mvn spring-boot:run
   ```

## Usage

1. **Register a User**: Use the `/register` endpoint to create a new user.
2. **Login**: Authenticate using the `/authenticate` endpoint.
3. **Manage Wallets**: Create and manage wallets via the `/wallets` endpoints.
4. **Make Transactions**: Deposit and withdraw funds through the `/transactions` endpoints.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## Contact

For any questions or issues, please contact [Marwan Mohamed](mailto:marwan.mohamed.abdallah@gmail.com).
