package src;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class WebBankingServer {
  private static final int PORT = 8080;
  private static final String HOST = "http://localhost:" + PORT;
  private static final Object BANK_LOCK = new Object();

  private static final Bank bank = new Bank();
  private static final Map<String, Integer> sessions = new ConcurrentHashMap<>();

  public static void main(String[] args) throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
    server.createContext("/api/health", new HealthHandler());
    server.createContext("/api/signup", new SignupHandler());
    server.createContext("/api/login", new LoginHandler());
    server.createContext("/api/logout", new LogoutHandler());
    server.createContext("/api/me", new MeHandler());
    server.createContext("/api/me/transactions", new TransactionsHandler());
    server.createContext("/api/me/deposit", new DepositHandler());
    server.createContext("/api/me/withdraw", new WithdrawHandler());
    server.createContext("/api/me/transfer", new TransferHandler());
    server.createContext("/api/me/password", new ChangePasswordHandler());
    server.createContext("/", new StaticHandler());
    server.setExecutor(null);
    server.start();

    System.out.println("MiniBanking web server running at " + HOST);
  }

  private static class SignupHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if (!"POST".equals(exchange.getRequestMethod())) {
        sendMethodNotAllowed(exchange, "POST");
        return;
      }

      String body = readRequestBody(exchange);
      String name = parseJsonString(body, "name");
      Integer accNo = parseJsonInt(body, "accNo");
      String password = parseJsonString(body, "password");
      Double initialBalance = parseJsonDouble(body, "initialBalance");
      if (initialBalance == null) {
        initialBalance = 0.0;
      }

      if (name == null || !isValidName(name)) {
        sendJson(exchange, 400, jsonError("Name is required and may only include letters, spaces, apostrophes, hyphens, and periods."));
        return;
      }
      if (!isSixDigitAccountNumber(accNo)) {
        sendJson(exchange, 400, jsonError("Account number must be exactly 6 digits."));
        return;
      }
      if (!isValidPassword(password)) {
        sendJson(exchange, 400, jsonError("Password must be at least 6 characters long."));
        return;
      }
      if (!isNonNegativeFiniteAmount(initialBalance)) {
        sendJson(exchange, 400, jsonError("Initial balance cannot be negative."));
        return;
      }

      Account newAccount;
      synchronized (BANK_LOCK) {
        if (bank.findAccountByNumber(accNo) != null) {
          sendJson(exchange, 409, jsonError("Account number already exists."));
          return;
        }

        try {
          newAccount = new Account(name, accNo, initialBalance, password);
        } catch (IllegalArgumentException e) {
          sendJson(exchange, 400, jsonError(e.getMessage()));
          return;
        }

        if (initialBalance > 0) {
          newAccount.addTransaction(new Transaction("Deposit", initialBalance, "Initial balance at signup"));
        }

        bank.addAccount(newAccount);
        bank.saveAllAccounts();
        bank.saveAllTransactions();
      }

      String token = UUID.randomUUID().toString();
      sessions.put(token, accNo);
      String response = "{"
          + "\"message\":\"Account created successfully.\"," + "\"token\":\"" + escapeJson(token)
          + "\"," + "\"account\":" + accountSummaryJson(newAccount) + "}";
      sendJson(exchange, 201, response);
    }
  }

  private static class HealthHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if (!"GET".equals(exchange.getRequestMethod())) {
        sendMethodNotAllowed(exchange, "GET");
        return;
      }
      sendJson(exchange, 200, "{\"status\":\"ok\"}");
    }
  }

  private static class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if (!"POST".equals(exchange.getRequestMethod())) {
        sendMethodNotAllowed(exchange, "POST");
        return;
      }

      String body = readRequestBody(exchange);
      Integer accNo = parseJsonInt(body, "accNo");
      String password = parseJsonString(body, "password");

      if (!isSixDigitAccountNumber(accNo) || password == null || password.trim().isEmpty()) {
        sendJson(exchange, 400, jsonError("Missing accNo or password."));
        return;
      }

      Account account;
      synchronized (BANK_LOCK) {
        account = bank.findAccountByNumber(accNo);
      }

      if (account == null || !account.checkPassword(password)) {
        sendJson(exchange, 401, jsonError("Invalid account number or password."));
        return;
      }

      String token = UUID.randomUUID().toString();
      sessions.put(token, accNo);

      String response = "{"
          + "\"token\":\"" + escapeJson(token) + "\"," + "\"account\":" + accountSummaryJson(account)
          + "}";
      sendJson(exchange, 200, response);
    }
  }

  private static class LogoutHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if (!"POST".equals(exchange.getRequestMethod())) {
        sendMethodNotAllowed(exchange, "POST");
        return;
      }

      String token = extractToken(exchange);
      if (token != null) {
        sessions.remove(token);
      }
      sendJson(exchange, 200, "{\"message\":\"Logged out.\"}");
    }
  }

  private static class MeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      String method = exchange.getRequestMethod();
      if (!"GET".equals(method) && !"DELETE".equals(method)) {
        sendMethodNotAllowed(exchange, "GET, DELETE");
        return;
      }

      Account account = authenticate(exchange);
      if (account == null) {
        return;
      }

      if ("GET".equals(method)) {
        sendJson(exchange, 200, accountSummaryJson(account));
        return;
      }

      String body = readRequestBody(exchange);
      String password = parseJsonString(body, "password");
      Boolean confirm = parseJsonBoolean(body, "confirm");
      if (password == null || confirm == null || !confirm) {
        sendJson(exchange, 400, jsonError("Password and confirm=true are required."));
        return;
      }

      if (!account.checkPassword(password)) {
        sendJson(exchange, 401, jsonError("Password verification failed."));
        return;
      }

      synchronized (BANK_LOCK) {
        bank.removeAccount(account);
        bank.saveAllAccounts();
        bank.saveAllTransactions();
      }

      String token = extractToken(exchange);
      if (token != null) {
        sessions.remove(token);
      }

      sendJson(exchange, 200, "{\"message\":\"Account deleted.\"}");
    }
  }

  private static class TransactionsHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if (!"GET".equals(exchange.getRequestMethod())) {
        sendMethodNotAllowed(exchange, "GET");
        return;
      }

      Account account = authenticate(exchange);
      if (account == null) {
        return;
      }

      StringBuilder sb = new StringBuilder();
      sb.append("{\"transactions\":[");
      ArrayList<Transaction> transactions = account.getTransactions();
      for (int i = 0; i < transactions.size(); i++) {
        Transaction t = transactions.get(i);
        if (i > 0) {
          sb.append(",");
        }
        sb.append("{")
            .append("\"type\":\"").append(escapeJson(t.getType())).append("\",")
            .append("\"amount\":").append(t.getAmount()).append(",")
            .append("\"description\":\"").append(escapeJson(t.getDescription())).append("\"")
            .append("}");
      }
      sb.append("]}");

      sendJson(exchange, 200, sb.toString());
    }
  }

  private static class DepositHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if (!"POST".equals(exchange.getRequestMethod())) {
        sendMethodNotAllowed(exchange, "POST");
        return;
      }

      Account account = authenticate(exchange);
      if (account == null) {
        return;
      }

      String body = readRequestBody(exchange);
      Double amount = parseJsonDouble(body, "amount");
      if (!isPositiveFiniteAmount(amount)) {
        sendJson(exchange, 400, jsonError("Amount must be greater than 0."));
        return;
      }

      synchronized (BANK_LOCK) {
        if (!account.deposit(amount)) {
          sendJson(exchange, 400, jsonError("Deposit failed."));
          return;
        }
        account.addTransaction(new Transaction("Deposit", amount, "Deposited via web UI"));
        bank.saveAllAccounts();
        bank.saveAllTransactions();
      }

      sendJson(exchange, 200, "{\"message\":\"Deposit successful.\",\"balance\":" + account.getBalance() + "}");
    }
  }

  private static class WithdrawHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if (!"POST".equals(exchange.getRequestMethod())) {
        sendMethodNotAllowed(exchange, "POST");
        return;
      }

      Account account = authenticate(exchange);
      if (account == null) {
        return;
      }

      String body = readRequestBody(exchange);
      Double amount = parseJsonDouble(body, "amount");
      if (!isPositiveFiniteAmount(amount)) {
        sendJson(exchange, 400, jsonError("Amount must be greater than 0."));
        return;
      }

      synchronized (BANK_LOCK) {
        if (!account.withdraw(amount)) {
          sendJson(exchange, 400, jsonError("Insufficient funds or invalid amount."));
          return;
        }
        account.addTransaction(new Transaction("Withdrawal", -amount, "Withdrew via web UI"));
        bank.saveAllAccounts();
        bank.saveAllTransactions();
      }

      sendJson(exchange, 200,
          "{\"message\":\"Withdrawal successful.\",\"balance\":" + account.getBalance() + "}");
    }
  }

  private static class TransferHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if (!"POST".equals(exchange.getRequestMethod())) {
        sendMethodNotAllowed(exchange, "POST");
        return;
      }

      Account fromAccount = authenticate(exchange);
      if (fromAccount == null) {
        return;
      }

      String body = readRequestBody(exchange);
      Integer toAccNo = parseJsonInt(body, "toAccNo");
      Double amount = parseJsonDouble(body, "amount");

      if (!isSixDigitAccountNumber(toAccNo) || !isPositiveFiniteAmount(amount)) {
        sendJson(exchange, 400, jsonError("toAccNo and amount (> 0) are required."));
        return;
      }
      if (toAccNo == fromAccount.getAccNo()) {
        sendJson(exchange, 400, jsonError("Cannot transfer to same account."));
        return;
      }

      synchronized (BANK_LOCK) {
        Account toAccount = bank.findAccountByNumber(toAccNo);
        if (toAccount == null) {
          sendJson(exchange, 404, jsonError("Recipient account not found."));
          return;
        }

        if (!fromAccount.withdraw(amount)) {
          sendJson(exchange, 400, jsonError("Insufficient funds or invalid amount."));
          return;
        }

        if (!toAccount.deposit(amount)) {
          fromAccount.deposit(amount);
          sendJson(exchange, 500, jsonError("Transfer failed; rollback applied."));
          return;
        }

        fromAccount.addTransaction(new Transaction("Transfer", -amount, "Transfer to account " + toAccNo));
        toAccount.addTransaction(new Transaction("Transfer", amount, "Transfer from account " + fromAccount.getAccNo()));
        bank.saveAllAccounts();
        bank.saveAllTransactions();
      }

      sendJson(exchange, 200, "{\"message\":\"Transfer successful.\",\"balance\":" + fromAccount.getBalance() + "}");
    }
  }

  private static class ChangePasswordHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if (!"POST".equals(exchange.getRequestMethod())) {
        sendMethodNotAllowed(exchange, "POST");
        return;
      }

      Account account = authenticate(exchange);
      if (account == null) {
        return;
      }

      String body = readRequestBody(exchange);
      String currentPassword = parseJsonString(body, "currentPassword");
      String newPassword = parseJsonString(body, "newPassword");

      if (currentPassword == null || newPassword == null) {
        sendJson(exchange, 400, jsonError("currentPassword and newPassword are required."));
        return;
      }
      if (!isValidPassword(newPassword)) {
        sendJson(exchange, 400, jsonError("New password must be at least 6 characters long."));
        return;
      }
      if (currentPassword.equals(newPassword)) {
        sendJson(exchange, 400, jsonError("New password cannot be the same as current password."));
        return;
      }
      if (!account.checkPassword(currentPassword)) {
        sendJson(exchange, 401, jsonError("Current password is incorrect."));
        return;
      }

      synchronized (BANK_LOCK) {
        try {
          account.setPassword(newPassword);
          bank.saveAllAccounts();
        } catch (IllegalArgumentException e) {
          sendJson(exchange, 400, jsonError(e.getMessage()));
          return;
        }
      }

      sendJson(exchange, 200, "{\"message\":\"Password changed successfully.\"}");
    }
  }

  private static class StaticHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      if (!"GET".equals(exchange.getRequestMethod())) {
        sendMethodNotAllowed(exchange, "GET");
        return;
      }

      String requestPath = exchange.getRequestURI().getPath();
      if ("/".equals(requestPath)) {
        requestPath = "/web/index.html";
      } else if (requestPath.startsWith("/web/")) {
        // keep as-is
      } else {
        sendJson(exchange, 404, jsonError("Not found."));
        return;
      }

      Path filePath = Paths.get(".", requestPath.substring(1));
      if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
        sendJson(exchange, 404, jsonError("Static file not found."));
        return;
      }

      String contentType = guessContentType(filePath.toString());
      byte[] payload = Files.readAllBytes(filePath);
      exchange.getResponseHeaders().set("Content-Type", contentType);
      exchange.sendResponseHeaders(200, payload.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(payload);
      }
    }
  }

  private static Account authenticate(HttpExchange exchange) throws IOException {
    String token = extractToken(exchange);
    if (token == null || token.isEmpty()) {
      sendJson(exchange, 401, jsonError("Missing auth token."));
      return null;
    }

    Integer accNo = sessions.get(token);
    if (accNo == null) {
      sendJson(exchange, 401, jsonError("Invalid or expired token."));
      return null;
    }

    Account account;
    synchronized (BANK_LOCK) {
      account = bank.findAccountByNumber(accNo);
    }
    if (account == null) {
      sessions.remove(token);
      sendJson(exchange, 401, jsonError("Session account no longer exists."));
      return null;
    }

    return account;
  }

  private static String extractToken(HttpExchange exchange) {
    String header = exchange.getRequestHeaders().getFirst("X-Auth-Token");
    return header == null ? null : header.trim();
  }

  private static String accountSummaryJson(Account account) {
    return "{"
        + "\"name\":\"" + escapeJson(account.getName()) + "\"," + "\"accNo\":" + account.getAccNo() + ","
        + "\"balance\":" + account.getBalance() + "}";
  }

  private static String readRequestBody(HttpExchange exchange) throws IOException {
    InputStream inputStream = exchange.getRequestBody();
    byte[] bytes = inputStream.readAllBytes();
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
    byte[] payload = json.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    exchange.sendResponseHeaders(statusCode, payload.length);
    try (OutputStream os = exchange.getResponseBody()) {
      os.write(payload);
    }
  }

  private static void sendMethodNotAllowed(HttpExchange exchange, String allowedMethods) throws IOException {
    String method = exchange.getRequestMethod();
    String path = exchange.getRequestURI().getPath();
    exchange.getResponseHeaders().set("Allow", allowedMethods);
    sendJson(exchange, 405,
        "{\"error\":\"Method not allowed.\",\"method\":\"" + escapeJson(method)
            + "\",\"path\":\"" + escapeJson(path) + "\",\"allowed\":\"" + escapeJson(allowedMethods)
            + "\"}");
  }

  private static String jsonError(String message) {
    return "{\"error\":\"" + escapeJson(message) + "\"}";
  }

  private static boolean isValidName(String name) {
    return name != null && Pattern.matches("^[a-zA-Z\\s.'-]+$", name);
  }

  private static boolean isValidPassword(String password) {
    return password != null && !password.trim().isEmpty() && password.length() >= 6;
  }

  private static boolean isSixDigitAccountNumber(Integer accNo) {
    return accNo != null && accNo >= 100000 && accNo <= 999999;
  }

  private static boolean isPositiveFiniteAmount(Double amount) {
    return amount != null && Double.isFinite(amount) && amount > 0;
  }

  private static boolean isNonNegativeFiniteAmount(Double amount) {
    return amount != null && Double.isFinite(amount) && amount >= 0;
  }

  private static String escapeJson(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r");
  }

  private static String guessContentType(String filePath) {
    if (filePath.endsWith(".html")) {
      return "text/html; charset=utf-8";
    }
    if (filePath.endsWith(".css")) {
      return "text/css; charset=utf-8";
    }
    if (filePath.endsWith(".js")) {
      return "application/javascript; charset=utf-8";
    }
    return "application/octet-stream";
  }

  private static Integer parseJsonInt(String json, String key) {
    String raw = parseJsonRawValue(json, key);
    if (raw == null) {
      return null;
    }
    try {
      return Integer.parseInt(raw.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Double parseJsonDouble(String json, String key) {
    String raw = parseJsonRawValue(json, key);
    if (raw == null) {
      return null;
    }
    try {
      return Double.parseDouble(raw.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Boolean parseJsonBoolean(String json, String key) {
    String raw = parseJsonRawValue(json, key);
    if (raw == null) {
      return null;
    }
    if ("true".equalsIgnoreCase(raw.trim())) {
      return true;
    }
    if ("false".equalsIgnoreCase(raw.trim())) {
      return false;
    }
    return null;
  }

  private static String parseJsonString(String json, String key) {
    String raw = parseJsonRawValue(json, key);
    if (raw == null) {
      return null;
    }
    String trimmed = raw.trim();
    if (trimmed.length() < 2 || trimmed.charAt(0) != '"' || trimmed.charAt(trimmed.length() - 1) != '"') {
      return null;
    }
    String unquoted = trimmed.substring(1, trimmed.length() - 1);
    return unquoted
        .replace("\\\"", "\"")
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\\\", "\\");
  }

  private static String parseJsonRawValue(String json, String key) {
    if (json == null || key == null) {
      return null;
    }

    String quotedKey = "\"" + key + "\"";
    int keyPos = json.indexOf(quotedKey);
    if (keyPos < 0) {
      return null;
    }

    int colonPos = json.indexOf(':', keyPos + quotedKey.length());
    if (colonPos < 0) {
      return null;
    }

    int i = colonPos + 1;
    while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
      i++;
    }
    if (i >= json.length()) {
      return null;
    }

    char first = json.charAt(i);
    if (first == '"') {
      int end = i + 1;
      boolean escaped = false;
      while (end < json.length()) {
        char c = json.charAt(end);
        if (c == '"' && !escaped) {
          break;
        }
        escaped = c == '\\' && !escaped;
        if (c != '\\') {
          escaped = false;
        }
        end++;
      }
      if (end >= json.length()) {
        return null;
      }
      return json.substring(i, end + 1);
    }

    int end = i;
    while (end < json.length()) {
      char c = json.charAt(end);
      if (c == ',' || c == '}' || Character.isWhitespace(c)) {
        break;
      }
      end++;
    }

    return json.substring(i, end);
  }
}
