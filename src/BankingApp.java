package src;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern; // Import Pattern for regex validation

public class BankingApp {

  static Account loggedInAccount = null;

  // Helper method for pausing execution
  private static void pressEnterToContinue(Scanner sc) {
    System.out.println("\nPress Enter to continue...");
    sc.nextLine(); // Consume the newline
  }

  // New helper method to validate password format
  // This method only checks format, does not interact with user directly
  private static boolean isValidPasswordFormat(String password) {
    return password != null && !password.trim().isEmpty() && password.length() >= 6;
  }

  // New helper method to prompt and get a valid password string from user
  private static String getValidatedPasswordInput(Scanner sc, String promptMessage) {
    String password;
    while (true) {
      System.out.print(promptMessage);
      password = sc.nextLine();
      if (isValidPasswordFormat(password)) {
        return password;
      } else {
        if (password.trim().isEmpty()) {
          System.out.println("Error: Password cannot be empty.");
        } else { // Implicitly means length < 6
          System.out.println("Error: Password must be at least 6 characters long.");
        }
      }
    }
  }

  public static Account findAccountByNumber(ArrayList<Account> accounts, int accNo) {
    for (Account acc : accounts) {
      if (acc.getAccNo() == accNo)
        return acc;
    }
    return null;
  }

  public static void createAccount(ArrayList<Account> accounts, Scanner sc) {
    String name;
    int accNo;
    double balance;
    String password;

    // Input validation for name
    while (true) {
      System.out.print("Enter account holder's name: ");
      name = sc.nextLine();
      if (name.trim().isEmpty()) {
        System.out.println("Error: Name cannot be empty.");
      } else if (!Pattern.matches("^[a-zA-Z\\s.'-]+$", name)) { // Regex to allow letters, spaces, apostrophes, hyphens
        System.out.println(
            "Error: Name contains invalid characters. Please use only letters, spaces, apostrophes, or hyphens.");
      } else {
        break;
      }
    }

    // Input validation for account number
    while (true) {
      System.out.print("Enter account number (6 digits): ");
      if (!sc.hasNextInt()) {
        System.out.println("Invalid input. Account number must be numeric.");
        sc.next(); // Clear invalid input
        sc.nextLine(); // Clear the rest of the line
        continue;
      }
      accNo = sc.nextInt();
      sc.nextLine(); // Clear newline

      if (String.valueOf(accNo).length() != 6) {
        System.out.println("Error: Account number must be exactly 6 digits.");
      } else if (findAccountByNumber(accounts, accNo) != null) {
        System.out.println("Account number already exists. Please use a different number.\n");
      } else {
        break;
      }
    }

    // Input validation for initial balance
    while (true) {
      System.out.print("Enter initial balance: ");
      if (!sc.hasNextDouble()) {
        System.out.println("Invalid input. Balance must be numeric.");
        sc.next(); // Clear invalid input
        sc.nextLine(); // Clear the rest of the line
        continue;
      }
      balance = sc.nextDouble();
      sc.nextLine(); // Clear newline
      if (balance < 0) {
        System.out.println("Error: Initial balance cannot be negative.");
      } else {
        break;
      }
    }

    // Use the new helper method for password input
    password = getValidatedPasswordInput(sc, "Enter password (min 6 characters): ");

    try {
      Account newAccount = new Account(name, accNo, balance, password);
      accounts.add(newAccount);
      saveAllAccounts(accounts);
      saveAllTransactions(accounts);
      System.out.println("Account created successfully and saved to file!\n");
    } catch (IllegalArgumentException e) {
      // This catch block is mostly a fallback now because getValidatedPasswordInput
      // handles most issues,
      // but it catches any edge cases or future validations added to Account
      // constructor.
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

  public static void saveAllAccounts(ArrayList<Account> accounts) {
    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter("accounts.txt"));
      for (Account acc : accounts) {
        bw.write(acc.toCSV());
        bw.newLine();
      }
      bw.close();
    } catch (IOException e) {
      System.out.println("Error saving accounts: " + e.getMessage());
    }
  }

  public static boolean verifyPassword(Account acc, Scanner sc) {
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

  public static Account login(ArrayList<Account> accounts, Scanner sc) {
    int accNo;
    while (true) {
      System.out.print("Enter account number: ");
      if (!sc.hasNextInt()) {
        System.out.println("Invalid input. Please enter a valid numeric account number.");
        sc.next(); // Clear invalid input
        sc.nextLine(); // Clear the rest of the line
        continue;
      }
      accNo = sc.nextInt();
      sc.nextLine(); // Clear newline

      if (String.valueOf(accNo).length() != 6) {
        System.out.println("Account number must be exactly 6 digits. Please try again.");
      } else {
        break;
      }
    }

    Account acc = findAccountByNumber(accounts, accNo);

    if (acc != null) {
      if (verifyPassword(acc, sc)) {
        System.out.println("Login successful. Welcome, " + acc.getName() + "!");
        return acc;
      }
    } else {
      System.out.println("Account not found for number: " + accNo);
    }

    return null;
  }

  public static void logout() {
    System.out.println("You have been logged out.\n");
  }

  public static void transferFunds(Account loggedInAccount, ArrayList<Account> accounts, Scanner sc) {
    // Step 1: Prompt for the recipient's account number
    System.out.print("Enter recipient's account number (6 digits): ");
    int recipientAccNo = sc.nextInt();

    // Validate account number (6 digits)
    if (String.valueOf(recipientAccNo).length() != 6) {
      System.out.println("Invalid account number. It should be a 6-digit number.");
      return;
    }

    // Step 2: Find the recipient account
    Account recipientAccount = findAccountByNumber(accounts, recipientAccNo);
    if (recipientAccount == null) {
      System.out.println("Recipient account not found.");
      return;
    }

    // Step 3: Prompt for the transfer amount
    System.out.print("Enter amount to transfer: ");
    double transferAmount = sc.nextDouble();

    // Step 4: Validate the amount
    if (transferAmount <= 0) {
      System.out.println("Amount must be greater than 0.");
      return;
    }

    if (loggedInAccount.getBalance() < transferAmount) {
      System.out.println("Insufficient funds for transfer.");
      return;
    }

    // Step 5: Perform the transaction
    boolean withdrawalSuccess = loggedInAccount.withdraw(transferAmount);
    if (!withdrawalSuccess) {
      System.out.println("Withdrawal failed.");
      return;
    }

    boolean depositSuccess = recipientAccount.deposit(transferAmount);
    if (!depositSuccess) {
      System.out.println("Deposit failed.");
      return;
    }

    // Step 6: Log transactions
    Transaction senderTransaction = new Transaction(
        "Transfer",
        -transferAmount,
        "Transfer to account " + recipientAccNo);
    loggedInAccount.addTransaction(senderTransaction);

    Transaction recipientTransaction = new Transaction(
        "Transfer",
        transferAmount,
        "Transfer from account " + loggedInAccount.getAccNo());
    recipientAccount.addTransaction(recipientTransaction);

    // Step 7: Save all changes
    try {
      saveAllAccounts(accounts);
      saveAllTransactions(accounts);
    } catch (Exception e) {
      System.out.println("Error saving accounts or transactions: " + e.getMessage());
    }

    System.out.println("Transfer successful!");
    System.out.printf("Transferred $%.2f from account %s to account %s.%n", transferAmount, loggedInAccount.getAccNo(),
        recipientAccNo);
  }

  // New method to save all transactions to a file
  // This method will be called after each transaction to keep a record
  public static void saveAllTransactions(ArrayList<Account> accounts) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter("transactions.txt"))) {
      for (Account acc : accounts) {
        for (Transaction t : acc.getTransactions()) {
          // Format: accNo,type,amount,description (description should not contain commas
          // or handle escaping)
          writer.write(acc.getAccNo() + "," + t.getType() + "," + t.getAmount() + "," + t.getDescription());
          writer.newLine();
        }
      }
    } catch (IOException e) {
      System.out.println("Error saving transactions: " + e.getMessage());
    }
  }

  public static void loadTransactions(ArrayList<Account> accounts) {
    try (BufferedReader reader = new BufferedReader(new FileReader("transactions.txt"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",", 4); // limit 4 to preserve commas in description if any
        if (parts.length == 4) {
          int accNo = Integer.parseInt(parts[0]);
          String type = parts[1];
          double amount = Double.parseDouble(parts[2]);
          String description = parts[3];

          Account acc = findAccountByNumber(accounts, accNo);
          if (acc != null) {
            acc.addTransaction(new Transaction(type, amount, description));
          }
        }
      }
    } catch (IOException e) {
      System.out.println("No transactions file found or error reading transactions.");
    }
  }

  // New method for changing password
  public static void changePassword(ArrayList<Account> accounts, Scanner sc) {
    System.out.println("\n--- Change Password ---");
    // 1. Verify current password
    if (!verifyPassword(loggedInAccount, sc)) {
      System.out.println("Current password verification failed. Password not changed.");
      return;
    }

    // 2. Get new password with validation
    String newPassword;
    String confirmNewPassword;
    while (true) {
      newPassword = getValidatedPasswordInput(sc, "Enter new password (min 6 characters): ");

      // Additional check: New password cannot be the same as the old password
      if (loggedInAccount.checkPassword(newPassword)) {
        System.out.println("Error: New password cannot be the same as your current password.");
        continue; // Re-prompt for new password
      }

      // 3. Confirm new password
      System.out.print("Confirm new password: ");
      confirmNewPassword = sc.nextLine();

      if (newPassword.equals(confirmNewPassword)) {
        break; // Passwords match and are valid format
      } else {
        System.out.println("Error: New passwords do not match. Please try again.");
      }
    }

    // 4. Set the new password using the Account class method, handling potential
    // exceptions
    try {
      loggedInAccount.setPassword(newPassword);
      saveAllAccounts(accounts);
      System.out.println("Password changed successfully!");
    } catch (IllegalArgumentException e) {
      // This catch block would ideally not be hit if getValidatedPasswordInput
      // and the "same as old" check are perfect, but it's a defensive measure
      System.out.println("Failed to change password: " + e.getMessage());
    }
  }

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    ArrayList<Account> accounts = new ArrayList<>();

    try {
      BufferedReader br = new BufferedReader(new java.io.FileReader("accounts.txt"));
      String line;

      while ((line = br.readLine()) != null) {
        String[] parts = line.split(",");
        if (parts.length == 4) {
          String name = parts[0];
          int accNo = Integer.parseInt(parts[1]);
          double balance = Double.parseDouble(parts[2]);
          String password = parts[3];
          accounts.add(new Account(name, accNo, balance, password));
        }
      }
      br.close();
      System.out.println("Loaded existing accounts from file.");
    } catch (IOException e) {
      System.out.println("No previous accounts found (or error reading file). Starting fresh.");
    } catch (NumberFormatException e) {
      System.out
          .println("Error reading account data: Invalid number format in file. Some accounts might not be loaded.");
      // Optionally, you might want to log the specific line that caused the error
    }

    while (true) {
      if (loggedInAccount == null) {
        System.out.println("\n--- Welcome to the Banking App ---");
        System.out.println("1. Create Account");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
        if (!sc.hasNextInt()) {
          System.out.println("Invalid input. Please enter a number.");
          sc.next(); // Clear invalid input
          sc.nextLine(); // Clear the rest of the line
          continue;
        }
        int choice = sc.nextInt();
        sc.nextLine(); // Clear newline

        switch (choice) {
          case 1:
            createAccount(accounts, sc);
            pressEnterToContinue(sc);
            break;
          case 2:
            loggedInAccount = login(accounts, sc);
            if (loggedInAccount == null) { // If login failed, prompt to continue
              pressEnterToContinue(sc);
            }
            break;
          case 3:
            System.out.println("Exiting the Banking App. Goodbye!");
            sc.close();
            System.exit(0);
            break;
          default:
            System.out.println("Invalid choice. Please enter a number between 1 and 3.");
            pressEnterToContinue(sc);
        }

      } else {
        System.out.println("\n--- Logged in as: " + loggedInAccount.getName() + " ---");
        System.out.println("1. View Account Summary");
        System.out.println("2. Deposit Money");
        System.out.println("3. Withdraw Money");
        System.out.println("4. Transfer Funds");
        System.out.println("5. View Transaction History");
        System.out.println("6. Change Password");
        System.out.println("7. Delete My Account");
        System.out.println("8. Logout");

        System.out.print("Choose an option: ");
        if (!sc.hasNextInt()) {
          System.out.println("Invalid input. Please enter a number from the menu options.");
          sc.next(); // Clear invalid input
          sc.nextLine(); // Clear the rest of the line
          continue;
        }
        int choice = sc.nextInt();
        sc.nextLine(); // Clear newline

        switch (choice) {
          case 1:
            loggedInAccount.display();
            pressEnterToContinue(sc);
            break;

          case 2:
            System.out.print("Enter amount to deposit: ");
            if (!sc.hasNextDouble()) {
              System.out.println("Invalid amount. Please enter a numeric value.");
              sc.next();
              sc.nextLine();
              pressEnterToContinue(sc);
              break;
            }
            double depAmt = sc.nextDouble();
            sc.nextLine();
            if (depAmt <= 0) {
              System.out.println("Invalid amount. Must be greater than 0.");
            } else {
              if (loggedInAccount.deposit(depAmt)) {
                loggedInAccount.addTransaction(
                    new Transaction("Deposit", depAmt, "Deposited to account " + loggedInAccount.getAccNo()));
                saveAllAccounts(accounts);
                saveAllTransactions(accounts);
                System.out.println("Deposit successful.");
              }
            }
            pressEnterToContinue(sc);
            break;

          case 3:
            System.out.print("Enter amount to withdraw: ");
            if (!sc.hasNextDouble()) {
              System.out.println("Invalid amount. Please enter a numeric value.");
              sc.next();
              sc.nextLine();
              pressEnterToContinue(sc);
              break;
            }
            double wdAmt = sc.nextDouble();
            sc.nextLine();
            if (wdAmt <= 0) {
              System.out.println("Invalid amount. Must be greater than 0.");
            } else if (loggedInAccount.withdraw(wdAmt)) {
              loggedInAccount.addTransaction(
                  new Transaction("Withdrawal", wdAmt, "Withdrew from account " + loggedInAccount.getAccNo()));
              saveAllAccounts(accounts);
              saveAllTransactions(accounts);
              System.out.println("Withdrawal successful.");
            }
            pressEnterToContinue(sc);
            break;

          case 4: // Transfer Funds
            transferFunds(loggedInAccount, accounts, sc);
            break;

          case 5:
            ArrayList<Transaction> history = loggedInAccount.getTransactions();
            if (history.isEmpty()) {
              System.out.println("No transaction history available.");
            } else {
              System.out.println("--- Transaction History ---");
              for (Transaction t : history) {
                System.out.println(t);
              }
              System.out.println("---------------------------");
            }
            pressEnterToContinue(sc);
            break;

          case 6: // Handle Change Password
            changePassword(accounts, sc);
            pressEnterToContinue(sc);
            break;

          case 7:
            System.out.println("To delete your account, please verify your password.");
            if (verifyPassword(loggedInAccount, sc)) {
              System.out.print("Are you sure you want to delete your account? (yes/no): ");
              String confirm = sc.nextLine();
              if (confirm.equalsIgnoreCase("yes")) {
                accounts.remove(loggedInAccount);
                saveAllAccounts(accounts);
                saveAllTransactions(accounts);
                System.out.println("Your account has been deleted.");
                loggedInAccount = null; // Log out the user
              } else {
                System.out.println("Account deletion cancelled.");
              }
            } else {
              System.out.println("Password verification failed. Account not deleted.");
            }
            pressEnterToContinue(sc);
            break;

          case 8: // Logout
            System.out.println("Logging out...");
            logout();
            loggedInAccount = null;
            pressEnterToContinue(sc); // Prompt after logout
            break;

          default:
            System.out.println("Invalid choice. Please enter a number between 1 and 5.");
            pressEnterToContinue(sc);
        }
      }
    }
  }
}