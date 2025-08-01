package src;

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class BankingApp {

  static Account loggedInAccount = null;

  public static Account findAccountByNumber(ArrayList<Account> accounts, int accNo) {
    for (Account acc : accounts) {
      if (acc.getAccNo() == accNo)
        return acc;
    }
    return null;
  }

  public static Account promptAndFindAccount(ArrayList<Account> accounts, Scanner sc) {
    System.out.print("Enter account number: ");
    int accNo = sc.nextInt();
    sc.nextLine(); // consume newline
    return findAccountByNumber(accounts, accNo);
  }

  public static void createAccount(ArrayList<Account> accounts, Scanner sc) {
    System.out.print("Enter account holder's name: ");
    sc.nextLine(); // to consume leftover newline
    String name = sc.nextLine();
    System.out.print("Enter account number: ");
    int accNo = sc.nextInt();
    System.out.print("Enter initial balance: ");
    double balance = sc.nextDouble();
    System.out.print("Enter password: ");
    String password = sc.next();

    if (findAccountByNumber(accounts, accNo) != null) {
      System.out.println("Account number already exists. Please use a different number.\n");
      return;
    }

    Account newAccount = new Account(name, accNo, balance, password);
    accounts.add(newAccount);

    try {
      BufferedWriter bw = new BufferedWriter(new FileWriter("accounts.txt", true));
      bw.write(newAccount.toCSV());
      bw.newLine();
      bw.close();
      System.out.println("Account saved to file.");
    } catch (IOException e) {
      System.out.println("Error writing to file: " + e.getMessage());
    }

    System.out.println("Account created successfully!\n");
  }

  public static void viewAllAccounts(ArrayList<Account> accounts) {
    if (accounts.isEmpty()) {
      System.out.println("No accounts available.\n");
    } else {
      int count = 1;
      for (Account acc : accounts) {
        System.out.println("Account " + count + ":");
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
      String inputPass = sc.next();

      if (acc.checkPassword(inputPass)) {
        return true;
      } else {
        attempts++;
        System.out.println("Wrong Password. Try again.");
      }
    }

    System.out.println("Too many failed attempts.");
    return false;
  }

  public static Account login(ArrayList<Account> accounts, Scanner sc) {
    System.out.print("Enter account number: ");
    int accNo = sc.nextInt();
    Account acc = findAccountByNumber(accounts, accNo);

    if (acc != null) {
      if (verifyPassword(acc, sc)) {
        System.out.println("Login successful. Welcome, " + acc.getName() + "!");
        return acc;
      }
    } else {
      System.out.println("Account not found.");
    }

    return null;
  }

  public static void logout() {
    System.out.println("You have been logged out.\n");
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
    }

    while (true) {
      if (loggedInAccount == null) {
        System.out.println("\n--- Welcome to the Banking App ---");
        System.out.println("1. Create Account");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");

        int choice = sc.nextInt();
        sc.nextLine(); // Clear newline

        switch (choice) {
          case 1:
            createAccount(accounts, sc);
            break;
          case 2:
            loggedInAccount = login(accounts, sc);
            break;
          case 3:
            System.out.println("Exiting the Banking App. Goodbye!");
            sc.close();
            System.exit(0);
            break;
          default:
            System.out.println("Invalid choice. Please try again.");
        }

      } else {
        System.out.println("\n--- Logged in as: " + loggedInAccount.getName() + " ---");
        System.out.println("1. View My Details");
        System.out.println("2. Deposit Money");
        System.out.println("3. Withdraw Money");
        System.out.println("4. Check Balance");
        System.out.println("5. Delete My Account");
        System.out.println("6. Logout");

        System.out.print("Choose an option: ");
        int choice = sc.nextInt();
        sc.nextLine(); // Clear newline

        switch (choice) {
          case 1:
            loggedInAccount.display();
            break;

          case 2:
            System.out.print("Enter amount to deposit: ");
            double depAmt = sc.nextDouble();
            sc.nextLine();
            if (depAmt <= 0) {
              System.out.println("Invalid amount. Must be greater than 0.");
            } else if (loggedInAccount.deposit(depAmt)) {
              saveAllAccounts(accounts);
              System.out.println("Deposit successful.");
            }
            break;

          case 3:
            System.out.print("Enter amount to withdraw: ");
            double wdAmt = sc.nextDouble();
            sc.nextLine();
            if (wdAmt <= 0) {
              System.out.println("Invalid amount. Must be greater than 0.");
            } else if (loggedInAccount.withdraw(wdAmt)) {
              saveAllAccounts(accounts);
              System.out.println("Withdrawal successful.");
            }
            break;

          case 4:
            System.out.println("Your current balance is: $" + loggedInAccount.getBalance());
            break;

          case 5:
            // Confirm password
            if (verifyPassword(loggedInAccount, sc)) {
              System.out.print("Are you sure you want to delete your account? (yes/no): ");
              String confirm = sc.nextLine();
              if (confirm.equalsIgnoreCase("yes")) {
                accounts.remove(loggedInAccount);
                saveAllAccounts(accounts);
                System.out.println("Your account has been deleted.");
                loggedInAccount = null;
              } else {
                System.out.println("Account deletion cancelled.");
              }
            }
            break;

          case 6:
            logout();
            loggedInAccount = null;
            break;

          default:
            System.out.println("Invalid choice. Please try again.");
        }
      }
    }
  }
}