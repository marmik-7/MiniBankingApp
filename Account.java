public class Account {
  String name;
  int accNo;
  double balance;
  String password;

  public Account(String name, int accNo, double balance, String password) {
    this.name = name;
    this.accNo = accNo;
    this.balance = balance;
    this.password = password;
  }

  public void display() {
    System.out.println("Account Holder: " + name);
    System.out.println("Account Number: " + accNo);
    System.out.println("Current Balance: $" + balance);
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

  public String toCSV() {
    return name + "," + accNo + "," + balance + "," + password;
  }
}