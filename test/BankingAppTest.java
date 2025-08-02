package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

import src.Account;

public class BankingAppTest {

  Account acc;

  @BeforeEach
  public void setUp() {
    acc = new Account("TestUser", 999, 1000.0, "testpass");
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
    Account acc = new Account("Charlie", 103, 300.0, "charlieP");
    boolean success = acc.withdraw(400.0);
    assertFalse(success, "Withdraw should fail if amount exceeds balance");
    assertEquals(300.0, acc.getBalance(), "Balance should remain unchanged after failed withdrawal");
  }

  @Test
  public void testCheckPassword() {
    Account acc = new Account("Dave", 104, 0, "myPass12");
    assertTrue(acc.checkPassword("myPass12"), "Password check should succeed with correct password");
    assertFalse(acc.checkPassword("wrongPass"), "Password check should fail with incorrect password");
  }

  @Test
  public void testDepositFailsOnNonPositiveAmount() {
    Account acc = new Account("Eve", 105, 0, "evePass1");
    assertFalse(acc.deposit(0), "Deposit of zero should fail");
    assertFalse(acc.deposit(-100), "Deposit of negative amount should fail");
    assertEquals(0, acc.getBalance(), "Balance should remain unchanged on failed deposit");
  }

  @Test
  public void testWithdrawFailsOnNonPositiveAmount() {
    Account acc = new Account("Eve", 105, 0, "evePass2");
    assertFalse(acc.withdraw(0), "Withdraw of zero should fail");
    assertFalse(acc.withdraw(-100), "Withdraw of negative amount should fail");
    assertEquals(0, acc.getBalance(), "Balance should remain unchanged on failed withdrawal");
  }

  @Test
  public void testAccountCreationFailsWithEmptyName() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new Account("", 201, 500, "validPass");
    });
    // Removed period from the end of the expected message
    assertEquals("Name cannot be empty", exception.getMessage());
  }

  @Test
  public void testAccountCreationFailsWithNegativeBalance() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new Account("John", 202, -100, "validPass");
    });
    // Removed period from the end of the expected message
    assertEquals("Balance cannot be negative", exception.getMessage());
  }

  @Test
  public void testAccountCreationFailsWithNullPassword() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new Account("Jane", 203, 100, null);
    });
    // Removed period from the end of the expected message
    assertEquals("Password cannot be null or empty", exception.getMessage());
  }

  @Test
  public void testAccountCreationFailsWithShortPassword() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      new Account("Frank", 204, 1000, "short");
    });
    assertEquals("Password must be at least 6 characters long.", exception.getMessage());
  }

  @Test
  public void testSetPasswordSuccess() {
    assertDoesNotThrow(() -> acc.setPassword("newPass123"),
        "Setting a valid new password should not throw an exception");
    assertTrue(acc.checkPassword("newPass123"), "Account should update to the new password");
    assertFalse(acc.checkPassword("testpass"), "Old password should no longer work");
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
}