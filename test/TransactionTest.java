package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import src.Transaction;

class TransactionTest {

  @Test
  void testConstructorAndGetters() {
    String type = "Deposit";
    double amount = 50.0;
    String description = "Initial deposit";
    Transaction transaction = new Transaction(type, amount, description);

    assertEquals(type, transaction.getType());
    assertEquals(amount, transaction.getAmount());
    assertEquals(description, transaction.getDescription());
  }

  @Test
  void testToStringMethod() {
    String type = "Withdrawal";
    double amount = -25.50;
    String description = "ATM withdrawal";
    Transaction transaction = new Transaction(type, amount, description);

    assertEquals("Withdrawal: $25.50 - ATM withdrawal", transaction.toString());
  }

  @Test
  void testToStringMethodWithDeposit() {
    String type = "Deposit";
    double amount = 150.0;
    String description = "Paycheck deposit";
    Transaction transaction = new Transaction(type, amount, description);

    assertEquals("Deposit: $150.00 - Paycheck deposit", transaction.toString());
  }

  @Test
  void testConstructorThrowsExceptionForInvalidDescription() {
    // Test with null description
    IllegalArgumentException thrownForNull = assertThrows(
        IllegalArgumentException.class,
        () -> new Transaction("Deposit", 100.0, null),
        "Expected constructor to throw IllegalArgumentException for null description");
    assertTrue(thrownForNull.getMessage().contains("Description cannot be empty."));

    // Test with empty description
    IllegalArgumentException thrownForEmpty = assertThrows(
        IllegalArgumentException.class,
        () -> new Transaction("Withdrawal", 50.0, " "),
        "Expected constructor to throw IllegalArgumentException for empty description");
    assertTrue(thrownForEmpty.getMessage().contains("Description cannot be empty."));
  }
}