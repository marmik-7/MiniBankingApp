package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Bank {
  private ArrayList<Account> accounts;

  public Bank() {
    this.accounts = new ArrayList<>();
    loadAllAccounts();
    loadTransactions();
  }

  public ArrayList<Account> getAccounts() {
    return accounts;
  }

  public Account findAccountByNumber(int accNo) {
    for (Account acc : accounts) {
      if (acc.getAccNo() == accNo) {
        return acc;
      }
    }
    return null;
  }

  public void addAccount(Account newAccount) {
    this.accounts.add(newAccount);
  }

  public void removeAccount(Account account) {
    this.accounts.remove(account);
  }

  public void saveAllAccounts() {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter("accounts.txt"))) {
      for (Account acc : accounts) {
        bw.write(acc.toCSV());
        bw.newLine();
      }
    } catch (IOException e) {
      System.err.println("Error saving accounts: " + e.getMessage());
    }
  }

  public void loadAllAccounts() {
    try (BufferedReader br = new BufferedReader(new FileReader("accounts.txt"))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] parts = line.split(",");
        if (parts.length == 4) {
          String name = parts[0];
          int accNo = Integer.parseInt(parts[1]);
          double balance = Double.parseDouble(parts[2]);
          String password = parts[3];
          this.accounts.add(new Account(name, accNo, balance, password));
        }
      }
      System.out.println("Loaded existing accounts from file.");
    } catch (IOException e) {
      System.out.println("No previous accounts found (or error reading file). Starting fresh.");
    } catch (NumberFormatException e) {
      System.err
          .println("Error reading account data: Invalid number format in file. Some accounts might not be loaded.");
    }
  }

  public void saveAllTransactions() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter("transactions.txt"))) {
      for (Account acc : accounts) {
        for (Transaction t : acc.getTransactions()) {
          writer.write(acc.getAccNo() + "," + t.getType() + "," + t.getAmount() + "," + t.getDescription());
          writer.newLine();
        }
      }
    } catch (IOException e) {
      System.err.println("Error saving transactions: " + e.getMessage());
    }
  }

  public void loadTransactions() {
    try (BufferedReader reader = new BufferedReader(new FileReader("transactions.txt"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",", 4);
        if (parts.length == 4) {
          int accNo = Integer.parseInt(parts[0]);
          String type = parts[1];
          double amount = Double.parseDouble(parts[2]);
          String description = parts[3];

          Account acc = findAccountByNumber(accNo);
          if (acc != null) {
            acc.addTransaction(new Transaction(type, amount, description));
          } else {
            System.err.println(
                "Warning: Transaction found for account " + accNo + ", but account no longer exists. Skipping.");
          }
        }
      }
    } catch (IOException e) {
      System.err.println("No transactions file found or error reading transactions.");
    }
  }
}