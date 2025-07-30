public class Account {
  String name;
  int accNo;
  double balance;

  public Account(String name, int accNo, double balance) {
    this.name = name;
    this.accNo = accNo;
    this.balance = balance;
  }

  void display() {
    System.out.println("Account Holder: " + name);
    System.out.println("Account Number: " + accNo);
    System.out.println("Current Balance: $" + balance);
  }
}
