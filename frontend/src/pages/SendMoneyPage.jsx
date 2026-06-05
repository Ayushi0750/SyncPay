
import { useState, useEffect } from "react";
import api from "../services/api";

export default function SendMoneyPage() {
  const [receiverPhone, setReceiverPhone] = useState("");
  const [amount, setAmount] = useState("");

  const [balance, setBalance] = useState(null);

  const [loading, setLoading] = useState(false);
  const [balanceLoading, setBalanceLoading] = useState(false);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const fetchBalance = async () => {
    try {
      setBalanceLoading(true);
      setError("");

      const res = await api.get("/wallet/balance");

      // SAFE: supports both number OR object response
      const walletBalance =
        typeof res.data === "number"
          ? res.data
          : res.data?.balance;

      setBalance(walletBalance ?? 0);

    } catch (err) {
      console.error("Failed to fetch balance:", err);

      if (err.response?.status === 401) {
        setError("Session expired. Please login again.");
      } else if (err.response?.status === 403) {
        setError("You are not authorized to view wallet.");
      } else {
        setError("Failed to load balance.");
      }

    } finally {
      setBalanceLoading(false);
    }
  };

  useEffect(() => {
    fetchBalance();
  }, []);

  
  useEffect(() => {
    const onFocus = () => fetchBalance();
    window.addEventListener("focus", onFocus);

    return () => window.removeEventListener("focus", onFocus);
  }, []);

  
  const validate = () => {
    if (!receiverPhone || !amount) return "Fill all fields";
    if (receiverPhone.trim().length < 10) return "Enter valid phone number";
    if (Number(amount) <= 0) return "Amount must be greater than 0";
    if (balance !== null && Number(amount) > balance)
      return "Insufficient balance";
    return null;
  };

  
  const handleSend = async () => {
    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setLoading(true);
      setError("");
      setSuccess("");

      const res = await api.post("/transaction/send", {
        receiverPhone: receiverPhone.trim(),
        amount: Number(amount),
      });

      setSuccess(
        `Transaction successful! ID: ${res.data.transactionId}`
      );

      setReceiverPhone("");
      setAmount("");

      
      await fetchBalance();

    } catch (err) {
      console.error("Transaction error:", err);

      if (err.response?.status === 400) {
        setError(err.response?.data?.error || "Invalid transaction request");
      } else if (err.response?.status === 403) {
        setError("Transaction blocked by security layer");
      } else if (err.response?.status === 401) {
        setError("Session expired. Please login again.");
      } else {
        setError("Transaction failed. Try again.");
      }

    } finally {
      setLoading(false);
    }
  };

  
  return (
    <div className="page-container">

      <h1 className="page-title">Send Money</h1>

      {/* ERROR */}
      {error && <div className="error-box">{error}</div>}

      {/* SUCCESS */}
      {success && <div className="success-box">{success}</div>}

      {/* WALLET BALANCE */}
      <div className="card">
        <h3>Wallet Balance</h3>

        {balanceLoading ? (
          <p>Loading balance...</p>
        ) : (
          <h2>₹ {balance ?? 0}</h2>
        )}
      </div>

      {/* FORM */}
      <div className="card">

        <div>
          <label>Receiver Phone</label>
          <input
            type="text"
            value={receiverPhone}
            onChange={(e) => setReceiverPhone(e.target.value)}
            placeholder="Enter phone number"
            disabled={loading}
          />
        </div>

        <div>
          <label>Amount</label>
          <input
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="Enter amount"
            disabled={loading}
          />
        </div>

        <button onClick={handleSend} disabled={loading}>
          {loading ? "Processing..." : "Send Money"}
        </button>

      </div>
    </div>
  );
}