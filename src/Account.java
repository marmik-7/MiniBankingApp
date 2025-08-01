package src;

public class Account {
  private String name;
  private int accNo;
  private double balance;
  private String password;

  public Account(String name, int accNo, double balance, String password) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be empty");
    }
    if (balance < 0) {
      throw new IllegalArgumentException("Balance cannot be negative");
    }
    if (password == null || password.trim().isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }

    this.name = name;
    this.accNo = accNo;
    this.balance = balance;
    this.password = password;
  }

  public void display() {
    System.out.println("Account Holder: " + name);
    System.out.println("Account Number: " + accNo);
    System.out.printf("Current Balance: $%.2f%n", balance);
    // Password is intentionally not printed
  }

  public boolean deposit(double amount) {
    if (amount <= 0) {
      System.out.println("Deposit amount must be greater than zero.");
      return false;
    }
    balance += amount;
    System.out.println("Deposited $" + amount);
    System.out.println("New Balance: $" + balance);
    return true;
  }

  public boolean withdraw(double amount) {
    if (amount <= 0) {
      System.out.println("Withdrawal amount must be greater than zero.");
      return false;
    }
    if (balance >= amount) {
      balance -= amount;
      System.out.println("Withdrew $" + amount);
      System.out.println("New Balance: $" + balance);
      return true;
    } else {
      System.out.println("Insufficient funds.");
      return false;
    }
  }

  public String getName() {
    return name;
  }

  public int getAccNo() {
    return accNo;
  }

  public double getBalance() {
    return balance;
  }

  public boolean checkPassword(String input) {
    return this.password.equals(input);
  }

  public String toCSV() {
    return name + "," + accNo + "," + balance + "," + password;
  }
}