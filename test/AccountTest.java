package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import src.Account;
import src.Transaction;

public class AccountTest {

  private static final String TEST_NAME = "TestUser";
  private static final int TEST_ACC_NO = 999;
  private static final double INITIAL_BALANCE = 1000.0;
  private static final String VALID_PASSWORD = "testpass";

  private Account acc;

  @BeforeEach
  public void setUp() {
    acc = new Account(TEST_NAME, TEST_ACC_NO, INITIAL_BALANCE, VALID_PASSWORD);
  }

  @Test
  public void testDepositIncreasesBalance() {
    boolean success = acc.deposit(500.0);
    assertTrue(success, "Deposit should succeed with a positive amount");
    assertEquals(1500.0, acc.getBalance(), "Balance should increase after deposit");
  }

  @Test
  public void testWithdrawReducesBalance() {
    boolean success = acc.withdraw(400.0);
    assertTrue(success, "Withdraw should succeed when balance is sufficient");
    assertEquals(600.0, acc.getBalance(), "Balance should decrease after withdrawal");
  }

  @Test
  public void testWithdrawFailsIfInsufficientFunds() {
    boolean success = acc.withdraw(1200.0);
    assertFalse(success, "Withdraw should fail if amount exceeds balance");
    assertEquals(INITIAL_BALANCE, acc.getBalance(), "Balance should remain unchanged after failed withdrawal");
  }

  @Test
  public void testCheckPassword() {
    assertTrue(acc.checkPassword(VALID_PASSWORD), "Password check should succeed with correct password");
    assertFalse(acc.checkPassword("wrongPass"), "Password check should fail with incorrect password");
  }

  @Test
  public void testDepositFailsOnNonPositiveAmount() {
    double originalBalance = acc.getBalance();
    assertFalse(acc.deposit(0), "Deposit of zero should fail");
    assertFalse(acc.deposit(-100), "Deposit of negative amount should fail");
    assertEquals(originalBalance, acc.getBalance(), "Balance should remain unchanged on failed deposit");
  }

  @Test
  public void testWithdrawFailsOnNonPositiveAmount() {
    double originalBalance = acc.getBalance();
    assertFalse(acc.withdraw(0), "Withdraw of zero should fail");
    assertFalse(acc.withdraw(-100), "Withdraw of negative amount should fail");
    assertEquals(originalBalance, acc.getBalance(), "Balance should remain unchanged on failed withdrawal");
  }

  @Test
  public void testWithdrawExactBalance() {
    boolean success = acc.withdraw(INITIAL_BALANCE);
    assertTrue(success, "Withdrawal should succeed with exact balance");
    assertEquals(0.0, acc.getBalance(), "Balance should be zero after withdrawing the entire amount");
  }

  @Test
  public void testAccountCreationFailsWithInvalidInputs() {
    Assertions.assertAll(
        () -> assertThrows(IllegalArgumentException.class, () -> new Account("", 201, 500, "validPass"), "Empty name"),
        () -> assertThrows(IllegalArgumentException.class, () -> new Account("John", 202, -100, "validPass"),
            "Negative balance"),
        () -> assertThrows(IllegalArgumentException.class, () -> new Account("Jane", 203, 100, null), "Null password"),
        () -> assertThrows(IllegalArgumentException.class, () -> new Account("Frank", 204, 1000, "short"),
            "Short password"));
  }

  @Test
  public void testSetPasswordSuccess() {
    assertDoesNotThrow(() -> acc.setPassword("newPass123"),
        "Setting a valid new password should not throw an exception");
    assertTrue(acc.checkPassword("newPass123"), "Account should update to the new password");
    assertFalse(acc.checkPassword(VALID_PASSWORD), "Old password should no longer work");
  }

  @Test
  public void testSetPasswordFailsWithEmptyPassword() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      acc.setPassword("");
    });
    assertEquals("New password cannot be empty.", exception.getMessage());
    assertTrue(acc.checkPassword("testpass"), "Password should remain unchanged after failed attempt");
  }

  @Test
  public void testSetPasswordFailsWithNullPassword() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      acc.setPassword(null);
    });
    assertEquals("New password cannot be empty.", exception.getMessage());
    assertTrue(acc.checkPassword("testpass"), "Password should remain unchanged after failed attempt");
  }

  @Test
  public void testSetPasswordFailsWithShortPassword() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      acc.setPassword("abc");
    });
    assertEquals("New password must be at least 6 characters long.", exception.getMessage());
    assertTrue(acc.checkPassword("testpass"), "Password should remain unchanged after failed attempt");
  }

  @Test
  public void testToCSVFormat() {
    Account csvAcc = new Account("CSVUser", 111222, 500.75, "csvPass");
    String expectedCSV = "CSVUser,111222,500.75,csvPass";
    assertEquals(expectedCSV, csvAcc.toCSV(), "toCSV should return correctly formatted string");
  }

  @Test
  public void testTransactionCanBeAddedAndRetrieved() {
    Transaction newTransaction = new Transaction("Deposit", 100.0, "Initial deposit");
    acc.addTransaction(newTransaction);
    assertEquals(1, acc.getTransactions().size(), "One transaction should be in the list");
    assertEquals(newTransaction, acc.getTransactions().get(0), "The correct transaction object should be retrieved");
  }
}