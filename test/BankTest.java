package test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import src.Account;
import src.Bank;
import src.Transaction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

class BankTest {

  private File accountsFile;
  private File transactionsFile;

  @BeforeEach
  void setUp() throws IOException {
    accountsFile = new File("accounts.txt");
    transactionsFile = new File("transactions.txt");
    if (accountsFile.exists()) {
      Files.delete(accountsFile.toPath());
    }
    if (transactionsFile.exists()) {
      Files.delete(transactionsFile.toPath());
    }
  }

  @AfterEach
  void tearDown() throws IOException {
    if (accountsFile.exists()) {
      Files.delete(accountsFile.toPath());
    }
    if (transactionsFile.exists()) {
      Files.delete(transactionsFile.toPath());
    }
  }

  @Test
  void testAddAndFindAccount() {
    Bank bank = new Bank();
    Account account1 = new Account("Test User 1", 100001, 100.0, "password123");
    Account account2 = new Account("Test User 2", 100002, 200.0, "password456");

    bank.addAccount(account1);
    bank.addAccount(account2);

    assertNotNull(bank.findAccountByNumber(100001));
    assertEquals(account1.getName(), bank.findAccountByNumber(100001).getName());
    assertNotNull(bank.findAccountByNumber(100002));
    assertEquals(account2.getName(), bank.findAccountByNumber(100002).getName());

    assertNull(bank.findAccountByNumber(999999), "Should not find a non-existent account");
  }

  @Test
  void testRemoveAccount() {
    Bank bank = new Bank();
    Account account = new Account("Test User", 100003, 500.0, "password123");
    bank.addAccount(account);
    assertNotNull(bank.findAccountByNumber(100003));

    bank.removeAccount(account);
    assertNull(bank.findAccountByNumber(100003), "Account should be null after removal");
  }

  @Test
  void testGetAccounts() {
    Bank bank = new Bank();
    Account account1 = new Account("Test User 1", 100004, 100.0, "password123");
    Account account2 = new Account("Test User 2", 100005, 200.0, "password456");

    bank.addAccount(account1);
    bank.addAccount(account2);

    ArrayList<Account> accounts = bank.getAccounts();
    assertEquals(2, accounts.size());
    assertEquals(account1.getAccNo(), accounts.get(0).getAccNo());
    assertEquals(account2.getAccNo(), accounts.get(1).getAccNo());
  }

  @Test
  void testAccountFilePersistence() {
    Bank bank = new Bank();
    Account account1 = new Account("Test User 1", 100006, 100.0, "password123");
    Account account2 = new Account("Test User 2", 100007, 200.0, "password456");
    bank.addAccount(account1);
    bank.addAccount(account2);

    bank.saveAllAccounts();

    Bank newBank = new Bank();
    Account loadedAccount1 = newBank.findAccountByNumber(100006);
    assertNotNull(loadedAccount1);
    assertEquals(account1.getName(), loadedAccount1.getName());
    assertEquals(account1.getBalance(), loadedAccount1.getBalance(), 0.001);

    Account loadedAccount2 = newBank.findAccountByNumber(100007);
    assertNotNull(loadedAccount2);
    assertEquals(account2.getName(), loadedAccount2.getName());
    assertEquals(account2.getBalance(), loadedAccount2.getBalance(), 0.001);
  }

  @Test
  void testTransactionFilePersistence() {
    Bank bank = new Bank();
    Account account = new Account("Test User", 100008, 500.0, "password123");
    bank.addAccount(account);
    account.deposit(100);
    account.addTransaction(new Transaction("Deposit", 100.0, "Test deposit"));
    account.withdraw(50);
    account.addTransaction(new Transaction("Withdrawal", -50.0, "Test withdrawal"));

    bank.saveAllAccounts();
    bank.saveAllTransactions();

    Bank newBank = new Bank();
    Account loadedAccount = newBank.findAccountByNumber(100008);

    assertNotNull(loadedAccount);
    assertEquals(2, loadedAccount.getTransactions().size());
    assertEquals("Deposit", loadedAccount.getTransactions().get(0).getType());
    assertEquals("Withdrawal", loadedAccount.getTransactions().get(1).getType());
    assertEquals(100.0, loadedAccount.getTransactions().get(0).getAmount(), 0.001);
    assertEquals(-50.0, loadedAccount.getTransactions().get(1).getAmount(), 0.001);
  }
}