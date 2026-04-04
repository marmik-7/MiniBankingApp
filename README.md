# MiniBankingApp

A Java banking app with both a command-line experience and a browser-based UI. It supports account creation, login, deposit/withdraw, transfers, password changes, transaction history, and account deletion.

## Features

- **Account Management**: Create new accounts with secure password validation
- **User Authentication**: Login with account number and password (max 3 attempts)
- **Financial Operations**: Deposit, withdraw, and transfer funds between accounts
- **Transaction History**: Track all transactions for each account
- **Password Management**: Change account passwords securely
- **Data Persistence**: All accounts and transactions are saved to text files
- **Input Validation**: Comprehensive validation for account numbers, amounts, and passwords
- **Web Experience**: Built-in REST API + browser UI for account operations
- **Section-Level Feedback**: Action results and validation messages appear beside each form in the web UI

## Project Structure

```
src/
  Account.java          - Account model with deposit/withdraw/transfer logic
  Bank.java             - Bank data model and persistence (refactored)
  BankingApp.java       - Main application orchestrator
  Cli.java              - CLI/UI helper methods (refactored)
  WebBankingServer.java - REST API + static web server (new)
  Transaction.java      - Transaction record model
web/
  index.html            - Web app shell
  styles.css            - Responsive UI styles
  app.js                - Frontend API integration
test/
  AccountTest.java      - Unit tests for Account class
  BankTest.java         - Unit tests for Bank persistence (refactored)
  TransactionTest.java  - Unit tests for Transaction class (refactored)
lib/
  junit-platform-console-standalone-1.13.0-M3.jar  - JUnit testing framework
accounts.txt           - Persistent accounts database (generated at runtime)
transactions.txt       - Persistent transaction history (generated at runtime)
```

## Requirements

- Java 21 or higher (OpenJDK/Eclipse Adoptium)
- Direct `javac` compilation (VS Code Java extension also supported)

## Setup & Building

### Option 1: Using javac (Direct Compilation)

1. Navigate to the project root:
   ```powershell
   cd MiniBankingApp
   ```

2. Create the output directory:
   ```powershell
   mkdir out
   ```

3. Compile all source and test files:
   ```powershell
  javac --add-modules jdk.httpserver -cp "lib/junit-platform-console-standalone-1.13.0-M3.jar" -d out src/*.java test/*.java
   ```

### Option 2: Using VS Code

VS Code with the Java Extension Pack will automatically handle compilation when you save files.

## Running the Application

Execute the main application:

```powershell
java -cp out src.BankingApp
```

Or from within VS Code, use the Run button on the main method.

## Running the Web App (REST API + UI)

1. Compile with the HTTP server module enabled:

```powershell
javac --add-modules jdk.httpserver -cp "lib/junit-platform-console-standalone-1.13.0-M3.jar" -d out src/*.java test/*.java
```

2. Start the web server:

```powershell
java --add-modules jdk.httpserver -cp out src.WebBankingServer
```

3. Open in browser:

```text
http://localhost:8080
```

### Web Dashboard Layout

- Move Money spans full width
- Transactions is left column
- Session is right column
- Security is left column (below Transactions)
- Delete Account is right column (below Session)

## Running Tests

Execute all unit and integration tests:

```powershell
java -jar "lib/junit-platform-console-standalone-1.13.0-M3.jar" execute --class-path out --scan-class-path
```

Note: compile first so the `out` directory contains up-to-date classes.

## Error Handling

The application validates all inputs and prevents:
- Negative balances
- Overdrafts
- Invalid account numbers
- Duplicate accounts
- Weak passwords

## Contributing

When contributing, ensure:
1. All tests (26/26) pass
2. No new compiler errors
3. Folder structure is preserved (`src/`, `test/`, `lib/`, `web/`)
4. Runtime files (`accounts.txt`, `transactions.txt`) remain at project root

## License

This is an educational project.
