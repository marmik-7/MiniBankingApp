package src;

import java.util.Scanner;

public class Cli {

  public static void displayMainMenu() {
    System.out.println("\n--- Welcome to the Banking App ---");
    System.out.println("1. Create Account");
    System.out.println("2. Login");
    System.out.println("3. Exit");
    System.out.print("Choose an option: ");
  }

  public static void displayLoggedInMenu(String userName) {
    System.out.println("\n--- Logged in as: " + userName + " ---");
    System.out.println("1. View Account Summary");
    System.out.println("2. Deposit Money");
    System.out.println("3. Withdraw Money");
    System.out.println("4. Transfer Funds");
    System.out.println("5. View Transaction History");
    System.out.println("6. Change Password");
    System.out.println("7. Delete My Account");
    System.out.println("8. Logout");
    System.out.print("Choose an option: ");
  }

  public static void pressEnterToContinue(Scanner sc) {
    System.out.println("\nPress Enter to continue...");
    sc.nextLine();
  }

  public static int getIntInput(Scanner sc, String prompt) {
    while (true) {
      System.out.print(prompt);
      if (!sc.hasNextInt()) {
        System.out.println("Invalid input. Please enter a number.");
        sc.next(); // Clear invalid input
        sc.nextLine(); // Clear the rest of the line
      } else {
        int value = sc.nextInt();
        sc.nextLine(); // Clear newline
        return value;
      }
    }
  }

  public static double getDoubleInput(Scanner sc, String prompt) {
    while (true) {
      System.out.print(prompt);
      if (!sc.hasNextDouble()) {
        System.out.println("Invalid input. Please enter a numeric value.");
        sc.next(); // Consume the invalid token
        sc.nextLine(); // Consume the rest of the line
      } else {
        double value = sc.nextDouble();
        sc.nextLine(); // Consume the newline
        return value;
      }
    }
  }
}