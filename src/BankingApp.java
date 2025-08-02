package src;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.regex.Pattern; // Import Pattern for regex validation

public class BankingApp {

  private static Account loggedInAccount = null;
  private static Bank bank = new Bank(); // Initialize the bank instance
  private static Scanner sc = new Scanner(System.in);

  private static boolean isValidPasswordFormat(String password) {
    return password != null && !password.trim().isEmpty() && password.length() >= 6;
  }

  private static String getValidatedPasswordInput(String promptMessage) {
    String password;
    while (true) {
      System.out.print(promptMessage);
      password = sc.nextLine();
      if (isValidPasswordFormat(password)) {
        return password;
      } else {
        if (password.trim().isEmpty()) {
          System.out.println("Error: Password cannot be empty.");
        } else {
          System.out.println("Error: Password must be at least 6 characters long.");
        }
      }
    }
  }

  public static void createAccount() {
    String name;
    int accNo;
    double balance;
    String password;

    while (true) {
      System.out.print("Enter account holder's name: ");
      name = sc.nextLine();
      if (name.trim().isEmpty()) {
        System.out.println("Error: Name cannot be empty.");
      } else if (!Pattern.matches("^[a-zA-Z\\s.'-]+$", name)) {
        System.out.println("Error: Name contains invalid characters.");
      } else {
        break;
      }
    }

    while (true) {
      accNo = Cli.getIntInput(sc, "Enter account number (6 digits): ");
      if (String.valueOf(accNo).length() != 6) {
        System.out.println("Error: Account number must be exactly 6 digits.");
      } else if (bank.findAccountByNumber(accNo) != null) {
        System.out.println("Account number already exists. Please use a different number.\n");
      } else {
        break;
      }
    }

    balance = Cli.getDoubleInput(sc, "Enter initial balance: ");
    while (balance < 0) {
      System.out.println("Error: Initial balance cannot be negative.");
      balance = Cli.getDoubleInput(sc, "Enter initial balance: ");
    }

    password = getValidatedPasswordInput("Enter password (min 6 characters): ");

    try {
      Account newAccount = new Account(name, accNo, balance, password);
      bank.addAccount(newAccount);
      bank.saveAllAccounts();
      System.out.println("Account created successfully!\n");
    } catch (IllegalArgumentException e) {
      System.out.println("Error creating account: " + e.getMessage());
    }
  }

  public static void viewAllAccounts(ArrayList<Account> accounts) {
    if (accounts.isEmpty()) {
      System.out.println("No accounts available.\n");
    } else {
      int count = 1;
      for (Account acc : accounts) {
        System.out.println("--- Account " + count + " ---");
        acc.display();
        System.out.println("-----------------------");
        count++;
      }
    }
  }

  private static boolean verifyPassword(Account acc) {
    final int MAX_PASSWORD_ATTEMPTS = 3;
    int attempts = 0;

    while (attempts < MAX_PASSWORD_ATTEMPTS) {
      System.out.print("Enter password: ");
      String inputPass = sc.nextLine();

      if (acc.checkPassword(inputPass)) {
        return true;
      } else {
        attempts++;
        System.out.println("Wrong Password. Attempts left: " + (MAX_PASSWORD_ATTEMPTS - attempts));
      }
    }
    System.out.println("Too many failed attempts.");
    return false;
  }

  public static void login() {
    int accNo = Cli.getIntInput(sc, "Enter account number: ");
    while (String.valueOf(accNo).length() != 6) {
      System.out.println("Account number must be exactly 6 digits. Please try again.");
      accNo = Cli.getIntInput(sc, "Enter account number: ");
    }

    Account acc = bank.findAccountByNumber(accNo);
    if (acc != null) {
      if (verifyPassword(acc)) {
        loggedInAccount = acc;
        System.out.println("Login successful. Welcome, " + acc.getName() + "!");
      }
    } else {
      System.out.println("Account not found for number: " + accNo);
    }
  }

  public static void logout() {
    loggedInAccount = null;
    System.out.println("You have been logged out.\n");
  }

  public static void transferFunds() {
    int recipientAccNo = Cli.getIntInput(sc, "Enter recipient's account number (6 digits): ");
    Account recipientAccount = bank.findAccountByNumber(recipientAccNo);

    if (recipientAccount == null) {
      System.out.println("Recipient account not found.");
      return;
    }

    double transferAmount = Cli.getDoubleInput(sc, "Enter amount to transfer: ");
    if (transferAmount <= 0) {
      System.out.println("Amount must be greater than 0.");
      return;
    }
    if (loggedInAccount.getBalance() < transferAmount) {
      System.out.println("Insufficient funds for transfer.");
      return;
    }

    if (loggedInAccount.withdraw(transferAmount)) {
      if (recipientAccount.deposit(transferAmount)) {
        loggedInAccount
            .addTransaction(new Transaction("Transfer", -transferAmount, "Transfer to account " + recipientAccNo));
        recipientAccount.addTransaction(
            new Transaction("Transfer", transferAmount, "Transfer from account " + loggedInAccount.getAccNo()));
        bank.saveAllAccounts();
        bank.saveAllTransactions();
        System.out.println("Transfer successful!");
        System.out.printf("Transferred $%.2f from account %s to account %s.%n", transferAmount,
            loggedInAccount.getAccNo(), recipientAccNo);
      } else {
        System.err
            .println("Transfer failed due to an error in the recipient's account. Your funds have been returned.");
        loggedInAccount.deposit(transferAmount); // Rollback
      }
    } else {
      System.err.println("Withdrawal failed.");
    }
  }

  public static void changePassword() {
    System.out.println("\n--- Change Password ---");
    if (!verifyPassword(loggedInAccount)) {
      System.out.println("Current password verification failed. Password not changed.");
      return;
    }

    String newPassword;
    String confirmNewPassword;
    while (true) {
      newPassword = getValidatedPasswordInput("Enter new password (min 6 characters): ");
      if (loggedInAccount.checkPassword(newPassword)) {
        System.out.println("Error: New password cannot be the same as your current password.");
        continue;
      }
      System.out.print("Confirm new password: ");
      confirmNewPassword = sc.nextLine();
      if (newPassword.equals(confirmNewPassword)) {
        break;
      } else {
        System.out.println("Error: New passwords do not match. Please try again.");
      }
    }

    try {
      loggedInAccount.setPassword(newPassword);
      bank.saveAllAccounts();
      System.out.println("Password changed successfully!");
    } catch (IllegalArgumentException e) {
      System.out.println("Failed to change password: " + e.getMessage());
    }
  }

  public static void deleteAccount() {
    System.out.println("To delete your account, please verify your password.");
    if (verifyPassword(loggedInAccount)) {
      System.out.print("Are you sure you want to delete your account? (yes/no): ");
      String confirm = sc.nextLine();
      if (confirm.equalsIgnoreCase("yes")) {
        bank.removeAccount(loggedInAccount);
        bank.saveAllAccounts();
        System.out.println("Your account has been deleted.");
        loggedInAccount = null;
      } else {
        System.out.println("Account deletion cancelled.");
      }
    } else {
      System.out.println("Password verification failed. Account not deleted.");
    }
  }

  public static void main(String[] args) {
    while (true) {
      if (loggedInAccount == null) {
        Cli.displayMainMenu();
        int choice = Cli.getIntInput(sc, "");
        switch (choice) {
          case 1:
            createAccount();
            Cli.pressEnterToContinue(sc);
            break;
          case 2:
            login();
            if (loggedInAccount == null) {
              Cli.pressEnterToContinue(sc);
            }
            break;
          case 3:
            System.out.println("Exiting the Banking App. Goodbye!");
            sc.close();
            System.exit(0);
            break;
          default:
            System.out.println("Invalid choice. Please enter a number between 1 and 3.");
            Cli.pressEnterToContinue(sc);
        }
      } else {
        Cli.displayLoggedInMenu(loggedInAccount.getName());
        int choice = Cli.getIntInput(sc, "");
        switch (choice) {
          case 1:
            loggedInAccount.display();
            Cli.pressEnterToContinue(sc);
            break;
          case 2:
            double depAmt = Cli.getDoubleInput(sc, "Enter amount to deposit: ");
            if (depAmt <= 0) {
              System.out.println("Invalid amount. Must be greater than 0.");
            } else {
              loggedInAccount.deposit(depAmt);
              loggedInAccount.addTransaction(
                  new Transaction("Deposit", depAmt, "Deposited to account " + loggedInAccount.getAccNo()));
              bank.saveAllAccounts();
              bank.saveAllTransactions();
              System.out.println("Deposit successful.");
              System.out.printf("New Balance: $%.2f%n", loggedInAccount.getBalance());
            }
            Cli.pressEnterToContinue(sc);
            break;
          case 3:
            double wdAmt = Cli.getDoubleInput(sc, "Enter amount to withdraw: ");
            if (wdAmt <= 0) {
              System.out.println("Invalid amount. Must be greater than 0.");
            } else if (loggedInAccount.withdraw(wdAmt)) {
              loggedInAccount.addTransaction(
                  new Transaction("Withdrawal", -wdAmt, "Withdrew from account " + loggedInAccount.getAccNo()));
              bank.saveAllAccounts();
              bank.saveAllTransactions();
              System.out.println("Withdrawal successful.");
              System.out.printf("New Balance: $%.2f%n", loggedInAccount.getBalance());
            }
            Cli.pressEnterToContinue(sc);
            break;
          case 4:
            transferFunds();
            Cli.pressEnterToContinue(sc);
            break;
          case 5:
            loggedInAccount.displayTransactionHistory();
            Cli.pressEnterToContinue(sc);
            break;
          case 6:
            changePassword();
            Cli.pressEnterToContinue(sc);
            break;
          case 7:
            deleteAccount();
            Cli.pressEnterToContinue(sc);
            break;
          case 8:
            logout();
            Cli.pressEnterToContinue(sc);
            break;
          default:
            System.out.println("Invalid choice. Please enter a number from the menu options.");
            Cli.pressEnterToContinue(sc);
        }
      }
    }
  }
}