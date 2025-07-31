import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class BankingApp {
  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    ArrayList<Account> accounts = new ArrayList<>();

    while (true) {
      System.out.println("Welcome to the Banking App!");
      System.out.println("1. Create Account");
      System.out.println("2. Display Accounts");
      System.out.println("3. Deposit Money");
      System.out.println("4. Withdraw Money");
      System.out.println("5. Exit");
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

          Account newAccount = new Account(name, accNo, balance);
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

        case 2:
          if (accounts.isEmpty()) {
            System.out.println("No accounts available.\n");
          } else {
            for (Account acc : accounts) {
              acc.display();
              System.out.println("-----------------------");
            }
          }
          break;

        case 3:
          System.out.print("Enter account number: ");
          int depositAcc = sc.nextInt();
          boolean foundDeposit = false;

          for (Account acc : accounts) {
            if (acc.accNo == depositAcc) {
              System.out.print("Enter the amount to deposit: ");
              double amount = sc.nextDouble();
              acc.deposit(amount);
              foundDeposit = true;
              break;
            }
          }

          if (!foundDeposit) {
            System.out.println("Account not found.");
          }
          break;

        case 4:
          System.out.print("Enter account number: ");
          boolean foundWithdraw = false;
          int withdrawAcc = sc.nextInt();

          for (Account acc : accounts) {
            if (acc.accNo == withdrawAcc) {
              System.out.print("Enter the amount to withdraw: ");
              double amount = sc.nextDouble();
              acc.withdraw(amount);
              foundWithdraw = true;
              break;
            }
          }

          if (!foundWithdraw) {
            System.out.println("Account not found.");
          }
          break;

        case 5:
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