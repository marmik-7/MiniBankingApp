import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class BankingApp {

  public static Account findAccountByNumber(ArrayList<Account> accounts, int accNo) {
    for (Account acc : accounts) {
      if (acc.getAccNo() == accNo)
        return acc;
    }
    return null;
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
    int attempts = 0;

    while (attempts < 3) {
      System.out.print("Enter password: ");
      String inputPass = sc.next();

      if (acc.checkPassword(inputPass)) {
        return true; // ✅ correct password
      } else {
        attempts++;
        System.out.println("Wrong Password. Try again.");
      }
    }

    System.out.println("Too many failed attempts.");
    return false; // ❌ after 3 tries
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
      System.out.println("Welcome to the Banking App!");
      System.out.println("1. Create Account");
      System.out.println("2. Display Accounts");
      System.out.println("3. Deposit Money");
      System.out.println("4. Withdraw Money");
      System.out.println("5. Delete Account");
      System.out.println("6. Check Balance");
      System.out.println("7. Exit");
      System.out.print("Choose an option: ");
      int choice = sc.nextInt();
      sc.nextLine();

      switch (choice) {
        case 1:
          System.out.print("Enter account holder's name: ");
          String name = sc.nextLine();
          System.out.print("Enter account number: ");
          int accNo = sc.nextInt();
          System.out.print("Enter initial balance: ");
          double balance = sc.nextDouble();
          System.out.println("Enter password: ");
          String password = sc.next();

          if (findAccountByNumber(accounts, accNo) != null) {
            System.out.println("Account number already exists. Please use a different number.\n");
          } else {
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
            break;
          }
          break;

        case 2:
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
          break;

        case 3:
          System.out.print("Enter account number: ");
          int depositAcc = sc.nextInt();
          Account acc = findAccountByNumber(accounts, depositAcc);

          if (acc != null) {
            if (verifyPassword(acc, sc)) {
              System.out.print("Enter the amount to deposit: ");
              double amount = sc.nextDouble();
              if (acc.deposit(amount)) {
                saveAllAccounts(accounts);
                System.out.println("Deposit successful.");
              }
            }
          } else {
            System.out.println("Account not found.");
          }
          break;

        case 4:
          System.out.print("Enter account number: ");
          int withdrawAcc = sc.nextInt();
          acc = findAccountByNumber(accounts, withdrawAcc);

          if (acc != null) {
            if (verifyPassword(acc, sc)) {
              System.out.print("Enter the amount to withdraw: ");
              double amount = sc.nextDouble();
              if (acc.withdraw(amount)) {
                saveAllAccounts(accounts);
                System.out.println("Withdrawal successful.");
              }
            }
          } else {
            System.out.println("Account not found.");
          }
          break;

        case 5:
          System.out.print("Enter account number to delete: ");
          int delAccNo = sc.nextInt();
          acc = findAccountByNumber(accounts, delAccNo);

          if (acc != null) {
            if (verifyPassword(acc, sc)) {
              accounts.remove(acc);
              saveAllAccounts(accounts);
              System.out.println("Account deleted successfully.");
            }
          } else {
            System.out.println("Account not found.");
          }
          break;

        case 6:
          System.out.print("Enter account number to check balance: ");
          int checkBal = sc.nextInt();
          acc = findAccountByNumber(accounts, checkBal);

          if (acc != null) {
            if (verifyPassword(acc, sc)) {
              System.out.println("Your current balance is: $" + acc.getBalance());
            }
          } else {
            System.out.println("Account not found.");
          }
          break;

        case 7:
          System.out.println("Exiting the Banking App. Goodbye!");
          sc.close();
          System.exit(0);
          break;

        default:
          System.out.println("Invalid choice. Please try again.\n");
          break;
      }
    }
  }
}