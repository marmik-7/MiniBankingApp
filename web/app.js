const state = {
  token: localStorage.getItem("mb_token") || "",
  account: null,
};

const loginView = document.getElementById("login-view");
const dashboardView = document.getElementById("dashboard-view");
const loginFeedback = document.getElementById("login-feedback");
const heroFeedback = document.getElementById("hero-feedback");
const depositFeedback = document.getElementById("deposit-feedback");
const withdrawFeedback = document.getElementById("withdraw-feedback");
const transferFeedback = document.getElementById("transfer-feedback");
const transactionsFeedback = document.getElementById("transactions-feedback");
const passwordFeedback = document.getElementById("password-feedback");
const deleteFeedback = document.getElementById("delete-feedback");
const sessionAccount = document.getElementById("session-account");
const authTitle = document.getElementById("auth-title");
const authSubtitle = document.getElementById("auth-subtitle");
const loginForm = document.getElementById("login-form");
const signupForm = document.getElementById("signup-form");
const showLoginBtn = document.getElementById("show-login-btn");
const showSignupBtn = document.getElementById("show-signup-btn");

function setAuthMode(mode) {
  const isLogin = mode === "login";
  loginForm.classList.toggle("hidden", !isLogin);
  signupForm.classList.toggle("hidden", isLogin);
  showLoginBtn.classList.toggle("is-active", isLogin);
  showSignupBtn.classList.toggle("is-active", !isLogin);

  if (isLogin) {
    authTitle.textContent = "Sign in to your account";
    authSubtitle.textContent = "Use your existing 6-digit account number and password.";
  } else {
    authTitle.textContent = "Create your account";
    authSubtitle.textContent = "Start with a new 6-digit account number and secure password.";
  }
  setFeedback(loginFeedback, "");
}

function formatMoney(value) {
  return new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(value || 0);
}

function setFeedback(el, message, isError = false) {
  el.textContent = message || "";
  el.classList.toggle("error", !!isError);
}

function isSixDigitAccountNumber(value) {
  return Number.isInteger(value) && value >= 100000 && value <= 999999;
}

function isPositiveFiniteAmount(value) {
  return Number.isFinite(value) && value > 0;
}

function isNonNegativeFiniteAmount(value) {
  return Number.isFinite(value) && value >= 0;
}

function inferAllowedMethods(requestPath) {
  const known = {
    "/api/signup": "POST",
    "/api/login": "POST",
    "/api/logout": "POST",
    "/api/me": "GET, DELETE",
    "/api/me/transactions": "GET",
    "/api/me/deposit": "POST",
    "/api/me/withdraw": "POST",
    "/api/me/transfer": "POST",
    "/api/me/password": "POST",
    "/api/health": "GET",
  };
  return known[requestPath] || "unknown";
}

async function api(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };
  if (state.token) {
    headers["X-Auth-Token"] = state.token;
  }

  const response = await fetch(path, {
    ...options,
    headers,
  });

  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    if (response.status === 405) {
      const detail = payload.error || "Method not allowed.";
      const sentMethod = (options.method || "GET").toUpperCase();
      const method = payload.method || sentMethod;
      const requestPath = payload.path || path;
      const allowed = payload.allowed || response.headers.get("Allow") || inferAllowedMethods(requestPath);
      throw new Error(`${detail} Received ${method} on ${requestPath}. Expected: ${allowed}.`);
    }
    throw new Error(payload.error || "Request failed");
  }
  return payload;
}

function showLogin() {
  dashboardView.classList.add("hidden");
  loginView.classList.remove("hidden");
  loginView.classList.add("show");
  setFeedback(heroFeedback, "");
  if (sessionAccount) {
    sessionAccount.textContent = "-";
  }
}

function showDashboard() {
  loginView.classList.add("hidden");
  loginView.classList.remove("show");
  dashboardView.classList.remove("hidden");
}

function clearDashboardFeedback() {
  setFeedback(heroFeedback, "");
  setFeedback(depositFeedback, "");
  setFeedback(withdrawFeedback, "");
  setFeedback(transferFeedback, "");
  setFeedback(transactionsFeedback, "");
  setFeedback(passwordFeedback, "");
  setFeedback(deleteFeedback, "");
}

function resetDashboardForms() {
  document.getElementById("deposit-form").reset();
  document.getElementById("withdraw-form").reset();
  document.getElementById("transfer-form").reset();
  document.getElementById("password-form").reset();
  document.getElementById("delete-form").reset();
}

function renderAccount() {
  if (!state.account) {
    return;
  }
  document.getElementById("welcome-text").textContent = `Welcome, ${state.account.name}`;
  document.getElementById("account-number").textContent = state.account.accNo;
  document.getElementById("account-balance").textContent = formatMoney(state.account.balance);
  if (sessionAccount) {
    sessionAccount.textContent = state.account.accNo;
  }
}

function renderTransactions(transactions) {
  const list = document.getElementById("transactions-list");
  list.innerHTML = "";

  if (!transactions || transactions.length === 0) {
    const li = document.createElement("li");
    li.textContent = "No transactions yet.";
    list.appendChild(li);
    return;
  }

  transactions
    .slice()
    .reverse()
    .forEach((t) => {
      const li = document.createElement("li");
      const amount = formatMoney(Math.abs(t.amount));
      li.innerHTML = `<strong>${t.type}</strong> <span>${amount}</span><div class="meta">${t.description}</div>`;
      list.appendChild(li);
    });
}

async function refreshAccount() {
  const account = await api("/api/me", { method: "GET" });
  state.account = account;
  renderAccount();
}

async function refreshTransactions() {
  const data = await api("/api/me/transactions", { method: "GET" });
  renderTransactions(data.transactions || []);
}

async function bootstrapSession() {
  if (!state.token) {
    showLogin();
    return;
  }

  try {
    await refreshAccount();
    await refreshTransactions();
    showDashboard();
  } catch (err) {
    localStorage.removeItem("mb_token");
    state.token = "";
    showLogin();
    setFeedback(loginFeedback, "Session expired. Please login again.", true);
  }
}

showLoginBtn.addEventListener("click", () => setAuthMode("login"));
showSignupBtn.addEventListener("click", () => setAuthMode("signup"));

loginForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  setFeedback(loginFeedback, "");

  const accNo = Number(document.getElementById("login-acc-no").value);
  const password = document.getElementById("login-password").value;

  if (!isSixDigitAccountNumber(accNo)) {
    setFeedback(loginFeedback, "Account number must be exactly 6 digits.", true);
    return;
  }
  if (!password || password.trim().length === 0) {
    setFeedback(loginFeedback, "Password is required.", true);
    return;
  }

  try {
    const payload = await api("/api/login", {
      method: "POST",
      body: JSON.stringify({ accNo, password }),
    });
    state.token = payload.token;
    localStorage.setItem("mb_token", state.token);
    state.account = payload.account;
    renderAccount();
    await refreshTransactions();
    showDashboard();
    setFeedback(heroFeedback, "Signed in successfully.");
  } catch (err) {
    setFeedback(loginFeedback, err.message, true);
  }
});

signupForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  setFeedback(loginFeedback, "");

  const name = document.getElementById("signup-name").value.trim();
  const accNo = Number(document.getElementById("signup-acc-no").value);
  const initialBalanceInput = document.getElementById("signup-initial-balance").value;
  const initialBalance = initialBalanceInput === "" ? 0 : Number(initialBalanceInput);
  const password = document.getElementById("signup-password").value;

  if (!/^[a-zA-Z\s.'-]+$/.test(name)) {
    setFeedback(loginFeedback, "Name contains invalid characters.", true);
    return;
  }
  if (!isSixDigitAccountNumber(accNo)) {
    setFeedback(loginFeedback, "Account number must be exactly 6 digits.", true);
    return;
  }
  if (!isNonNegativeFiniteAmount(initialBalance)) {
    setFeedback(loginFeedback, "Initial balance must be a valid non-negative number.", true);
    return;
  }
  if (!password || password.length < 6) {
    setFeedback(loginFeedback, "Password must be at least 6 characters long.", true);
    return;
  }

  try {
    const payload = await api("/api/signup", {
      method: "POST",
      body: JSON.stringify({ name, accNo, password, initialBalance }),
    });
    state.token = payload.token;
    localStorage.setItem("mb_token", state.token);
    state.account = payload.account;
    renderAccount();
    await refreshTransactions();
    showDashboard();
    signupForm.reset();
    setFeedback(heroFeedback, "Account created and signed in.");
  } catch (err) {
    setFeedback(loginFeedback, err.message, true);
  }
});

document.getElementById("logout-btn").addEventListener("click", async () => {
  if (!window.confirm("Logout from your account?")) {
    return;
  }

  try {
    await api("/api/logout", { method: "POST" });
  } catch (err) {
    // Ignore logout failure and clear session locally.
  }

  clearDashboardFeedback();
  resetDashboardForms();
  state.token = "";
  state.account = null;
  localStorage.removeItem("mb_token");
  showLogin();
  setFeedback(loginFeedback, "Logged out successfully.");
});

document.getElementById("reload-transactions").addEventListener("click", async () => {
  setFeedback(transactionsFeedback, "");
  try {
    await refreshTransactions();
    setFeedback(transactionsFeedback, "Transactions refreshed.");
  } catch (err) {
    setFeedback(transactionsFeedback, err.message, true);
  }
});

document.getElementById("deposit-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  setFeedback(depositFeedback, "");
  const amount = Number(document.getElementById("deposit-amount").value);
  if (!isPositiveFiniteAmount(amount)) {
    setFeedback(depositFeedback, "Deposit amount must be a valid number greater than 0.", true);
    return;
  }
  try {
    await api("/api/me/deposit", {
      method: "POST",
      body: JSON.stringify({ amount }),
    });
    await refreshAccount();
    await refreshTransactions();
    e.target.reset();
    setFeedback(depositFeedback, `Deposited ${formatMoney(amount)}.`);
  } catch (err) {
    setFeedback(depositFeedback, err.message, true);
  }
});

document.getElementById("withdraw-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  setFeedback(withdrawFeedback, "");
  const amount = Number(document.getElementById("withdraw-amount").value);
  if (!isPositiveFiniteAmount(amount)) {
    setFeedback(withdrawFeedback, "Withdrawal amount must be a valid number greater than 0.", true);
    return;
  }
  try {
    await api("/api/me/withdraw", {
      method: "POST",
      body: JSON.stringify({ amount }),
    });
    await refreshAccount();
    await refreshTransactions();
    e.target.reset();
    setFeedback(withdrawFeedback, `Withdrew ${formatMoney(amount)}.`);
  } catch (err) {
    setFeedback(withdrawFeedback, err.message, true);
  }
});

document.getElementById("transfer-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  setFeedback(transferFeedback, "");
  const toAccNo = Number(document.getElementById("transfer-to").value);
  const amount = Number(document.getElementById("transfer-amount").value);

  if (!isSixDigitAccountNumber(toAccNo)) {
    setFeedback(transferFeedback, "Recipient account number must be exactly 6 digits.", true);
    return;
  }
  if (state.account && Number(state.account.accNo) === toAccNo) {
    setFeedback(transferFeedback, "You cannot transfer to the same account.", true);
    return;
  }
  if (!isPositiveFiniteAmount(amount)) {
    setFeedback(transferFeedback, "Transfer amount must be a valid number greater than 0.", true);
    return;
  }

  if (!window.confirm(`Transfer ${formatMoney(amount)} to account ${toAccNo}?`)) {
    return;
  }

  try {
    await api("/api/me/transfer", {
      method: "POST",
      body: JSON.stringify({ toAccNo, amount }),
    });
    await refreshAccount();
    await refreshTransactions();
    e.target.reset();
    setFeedback(transferFeedback, `Transferred ${formatMoney(amount)} to ${toAccNo}.`);
  } catch (err) {
    setFeedback(transferFeedback, err.message, true);
  }
});

document.getElementById("password-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  setFeedback(passwordFeedback, "");
  const currentPassword = document.getElementById("current-password").value;
  const newPassword = document.getElementById("new-password").value;
  if (!currentPassword || !newPassword) {
    setFeedback(passwordFeedback, "Both current and new password are required.", true);
    return;
  }
  if (newPassword.length < 6) {
    setFeedback(passwordFeedback, "New password must be at least 6 characters long.", true);
    return;
  }
  if (newPassword === currentPassword) {
    setFeedback(passwordFeedback, "New password cannot be the same as current password.", true);
    return;
  }
  try {
    await api("/api/me/password", {
      method: "POST",
      body: JSON.stringify({ currentPassword, newPassword }),
    });
    e.target.reset();
    setFeedback(passwordFeedback, "Password changed successfully.");
  } catch (err) {
    setFeedback(passwordFeedback, err.message, true);
  }
});

document.getElementById("delete-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  setFeedback(deleteFeedback, "");
  const password = document.getElementById("delete-password").value;

  if (!password || password.trim().length === 0) {
    setFeedback(deleteFeedback, "Password is required to delete account.", true);
    return;
  }

  if (!window.confirm("Delete this account permanently? This cannot be undone.")) {
    return;
  }

  try {
    await api("/api/me", {
      method: "DELETE",
      body: JSON.stringify({ password, confirm: true }),
    });
    state.token = "";
    state.account = null;
    localStorage.removeItem("mb_token");
    showLogin();
    setFeedback(loginFeedback, "Account deleted.");
  } catch (err) {
    setFeedback(deleteFeedback, err.message, true);
  }
});

bootstrapSession();
setAuthMode("signup");
