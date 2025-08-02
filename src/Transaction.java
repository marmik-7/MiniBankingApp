package src;

public class Transaction {
  private String type;
  private double amount;
  private String description;

  public Transaction(String type, double amount, String description) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Amount must be positive.");
    }
    if (description == null || description.trim().isEmpty()) {
      throw new IllegalArgumentException("Description cannot be empty.");
    }

    this.type = type;
    this.amount = amount;
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public double getAmount() {
    return amount;
  }

  public String getDescription() {
    return description;
  }

  // toString method for displaying transactions
  @Override
  public String toString() {
    return String.format("%s: $%.2f - %s", type, amount, description);
  }
}