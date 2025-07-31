public class Account {
  String name;
  int accNo;
  double balance;

  public Account(String name, int accNo, double balance) {
    this.name = name;
    this.accNo = accNo;
    this.balance = balance;
  }

  public void display() {
    System.out.println("Account Holder: " + name);
    System.out.println("Account Number: " + accNo);
    System.out.println("Current Balance: $" + balance);
  }

  public void deposit(double amount) {
    balance += amount;
    System.out.println("Deposited $" + amount);
    System.out.println("New Balance: $" + balance);
  }

  public void withdraw(double amount) {
    if (balance >= amount) {
      balance -= amount;
      System.out.println("Withdrew $" + amount);
      System.out.println("New Balance: $" + balance);
    } else {
      System.out.println("Insufficient funds.");
    }
  }

  public String toCSV() {
    return name + "," + accNo + "," + balance;
  }
}