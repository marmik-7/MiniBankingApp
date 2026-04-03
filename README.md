# MiniBankingApp

A simple command-line banking application built with Java that allows users to create accounts, login, deposit/withdraw funds, transfer money between accounts, and manage their passwords.

## Features

- **Account Management**: Create new accounts with secure password validation
- **User Authentication**: Login with account number and password (max 3 attempts)
- **Financial Operations**: Deposit, withdraw, and transfer funds between accounts
- **Transaction History**: Track all transactions for each account
- **Password Management**: Change account passwords securely
- **Data Persistence**: All accounts and transactions are saved to text files
- **Input Validation**: Comprehensive validation for account numbers, amounts, and passwords

## Project Structure

```
src/
  Account.java          - Account model with deposit/withdraw/transfer logic
  BankingApp.java       - Main application with user interface and business logic
  Transaction.java      - Transaction record model
test/
  BankingAppTest.java   - Unit and integration tests for banking operations
lib/
  junit-platform-console-standalone-1.13.0-M3.jar  - JUnit testing framework
accounts.txt           - Persistent accounts database (generated at runtime)
transactions.txt       - Persistent transaction history (generated at runtime)
```

## Requirements

- Java 21 or higher (OpenJDK/Eclipse Adoptium)
- Maven-compatible build or direct javac compilation

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
   javac -cp "lib/junit-platform-console-standalone-1.13.0-M3.jar" -d out src/*.java test/*.java
   ```

### Option 2: Using VS Code

VS Code with the Java Extension Pack will automatically handle compilation when you save files.

## Running the Application

Execute the main application:

```powershell
java -cp out src.BankingApp
```

Or from within VS Code, use the Run button on the main method.

## Running Tests

Execute all unit and integration tests:

```powershell
java -jar "lib/junit-platform-console-standalone-1.13.0-M3.jar" execute --class-path out --scan-class-path
```

All tests should pass (20 tests on main branch).

## Data Storage

- **accounts.txt**: CSV format with `name,accNo,balance,password` per line
- **transactions.txt**: CSV format with `accNo,type,amount,description` per line

Both files are automatically created and updated during application use. Add them to `.gitignore` to prevent committing user data.

## Error Handling

The application validates all inputs and prevents:
- Negative balances
- Overdrafts
- Invalid account numbers
- Duplicate accounts
- Weak passwords
- Self-transfers

## Contributing

When contributing, ensure:
1. All tests pass: `20/20` on main
2. No new compiler errors
3. Folder structure is preserved (`src/`, `test/`, `lib/`)
4. Runtime files (`accounts.txt`, `transactions.txt`) remain at project root

## License

This is an educational project.
