package src;

import java.util.ArrayList;

public class Account {
  private String name;
  private int accNo;
  private double balance;
  private String password;
  private ArrayList<Transaction> transactions;

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
    if (password.length() < 6) {
      throw new IllegalArgumentException("Password must be at least 6 characters long.");
    }

    this.name = name;
    this.accNo = accNo;
    this.balance = balance;
    this.password = password;
    this.transactions = new ArrayList<>();
  }

  public void addTransaction(Transaction transaction) {
    transactions.add(transaction);
  }

  public ArrayList<Transaction> getTransactions() {
    return transactions;
  }

  public void display() {
    System.out.println("Account Holder: " + name);
    System.out.println("Account Number: " + accNo);
    System.out.printf("Current Balance: $%.2f%n", balance);
  }

  public void displayTransactionHistory() {
    if (transactions.isEmpty()) {
      System.out.println("No transaction history available.");
    } else {
      System.out.println("--- Transaction History for Account " + this.getAccNo() + " ---");
      for (Transaction t : transactions) {
        System.out.println(t);
      }
      System.out.println("-------------------------------------------");
    }
  }

  public boolean deposit(double amount) {
    if (amount <= 0) {
      return false;
    }
    balance += amount;
    return true;
  }

  public boolean withdraw(double amount) {
    if (amount <= 0) {
      return false;
    }
    if (balance >= amount) {
      balance -= amount;
      return true;
    } else {
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

  public void setPassword(String newPassword) {
    if (newPassword == null || newPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("New password cannot be empty.");
    }
    if (newPassword.length() < 6) {
      throw new IllegalArgumentException("New password must be at least 6 characters long.");
    }
    this.password = newPassword;
  }

  public String toCSV() {
    return name + "," + accNo + "," + balance + "," + password;
  }
}