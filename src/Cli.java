package src;

import java.util.Scanner;

public class Cli {

  private static final String DIVIDER = "----------------------------------------";

  public static void displayMainMenu() {
    System.out.println("\n" + DIVIDER);
    System.out.println("          Welcome to the Banking App");
    System.out.println(DIVIDER);
    System.out.println("1) Create Account");
    System.out.println("2) Login");
    System.out.println("3) Exit");
    System.out.println("9) Help");
    System.out.print("Choose an option: ");
  }

  public static void displayLoggedInMenu(String userName) {
    System.out.println("\n" + DIVIDER);
    System.out.println("Logged in as: " + userName);
    System.out.println(DIVIDER);
    System.out.println("1) View Account Summary");
    System.out.println("2) Deposit Money");
    System.out.println("3) Withdraw Money");
    System.out.println("4) Transfer Funds");
    System.out.println("5) View Transaction History");
    System.out.println("6) Change Password");
    System.out.println("7) Delete My Account");
    System.out.println("8) Logout");
    System.out.println("9) Help");
    System.out.print("Choose an option: ");
  }

  public static void displayMainHelp() {
    System.out.println("\n" + DIVIDER);
    System.out.println("Main Menu Help");
    System.out.println(DIVIDER);
    System.out.println("- Create Account: Register a new bank account.");
    System.out.println("- Login: Access your account with account number + password.");
    System.out.println("- Exit: Close the app safely.");
    System.out.println("Tip: Use account numbers with exactly 6 digits.");
  }

  public static void displayLoggedInHelp() {
    System.out.println("\n" + DIVIDER);
    System.out.println("Logged-In Menu Help");
    System.out.println(DIVIDER);
    System.out.println("- Deposit/Withdraw update your available balance.");
    System.out.println("- Transfer moves funds to another valid account.");
    System.out.println("- Change Password requires current password verification.");
    System.out.println("- Delete Account permanently removes the account.");
  }

  public static void showSuccess(String message) {
    System.out.println("[SUCCESS] " + message);
  }

  public static void showError(String message) {
    System.out.println("[ERROR] " + message);
  }

  public static void showInfo(String message) {
    System.out.println("[INFO] " + message);
  }

  public static boolean confirm(Scanner sc, String prompt) {
    while (true) {
      System.out.print(prompt + " (y/n): ");
      String input = sc.nextLine().trim().toLowerCase();
      if (input.equals("y") || input.equals("yes")) {
        return true;
      }
      if (input.equals("n") || input.equals("no")) {
        return false;
      }
      showError("Please enter 'y' or 'n'.");
    }
  }

  public static void pressEnterToContinue(Scanner sc) {
    System.out.println("\nPress Enter to continue...");
    sc.nextLine();
  }

  public static int getIntInput(Scanner sc, String prompt) {
    while (true) {
      System.out.print(prompt);
      if (!sc.hasNextInt()) {
        showError("Invalid input. Please enter a whole number.");
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
        showError("Invalid input. Please enter a numeric value.");
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