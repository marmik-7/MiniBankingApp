package src;

public class Transaction {
  private String type;
  private double amount;
  private String description;

  public Transaction(String type, double amount, String description) {
    // Validation now allows for positive or negative amounts, which is correct for
    // transactions.
    // For a withdrawal, the amount should be negative. For a deposit, it should be
    // positive.
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

  @Override
  public String toString() {
    // Use Math.abs() to format the amount nicely for withdrawals,
    // avoiding a confusing double negative (e.g., "Withdrawal: $-50.00").
    String formattedAmount = String.format("$%.2f", Math.abs(amount));
    return String.format("%s: %s - %s", type, formattedAmount, description);
  }
}