package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import src.Account;

public class BankingAppTest {

  @Test
  public void testDepositIncreasesBalance() {
    Account acc = new Account("Alice", 101, 1000.0, "pass123");
    boolean success = acc.deposit(500.0);

    assertTrue(success);
    assertEquals(1500.0, acc.getBalance());
  }

  @Test
  public void testWithdrawReducesBalance() {
    Account acc = new Account("Bob", 102, 1000.0, "secret");
    boolean success = acc.withdraw(400.0);

    assertTrue(success);
    assertEquals(600.0, acc.getBalance());
  }

  @Test
  public void testWithdrawFailsIfInsufficientFunds() {
    Account acc = new Account("Charlie", 103, 300.0, "1234");
    boolean success = acc.withdraw(400.0);

    assertFalse(success);
    assertEquals(300.0, acc.getBalance());
  }

  @Test
  public void testCheckPassword() {
    Account acc = new Account("Dave", 104, 0, "myPass");
    assertTrue(acc.checkPassword("myPass"));
    assertFalse(acc.checkPassword("wrongPass"));
  }

  @Test
  public void testDepositFailsOnNonPositiveAmount() {
    Account acc = new Account("Eve", 105, 0, "pass");
    assertFalse(acc.deposit(0));
    assertFalse(acc.deposit(-100));
    assertEquals(0, acc.getBalance());
  }

  @Test
  public void testWithdrawFailsOnNonPositiveAmount() {
    Account acc = new Account("Eve", 105, 0, "pass");
    assertFalse(acc.withdraw(0));
    assertFalse(acc.withdraw(-100));
    assertEquals(0, acc.getBalance());
  }
}