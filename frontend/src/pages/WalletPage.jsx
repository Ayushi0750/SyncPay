


import { useEffect, useState } from "react";
import { fetchWalletBalance } from "../services/syncService";
import MockBankPanel from "../components/MockBankPanel";

const WalletPage = () => {
  const [balance, setBalance] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  
  const loadBalance = async () => {
    try {
      setLoading(true);
      setError("");

      const result = await fetchWalletBalance();

      if (result?.success) {
        const value =
          typeof result.data === "number"
            ? result.data
            : result.data?.balance;

        setBalance(value ?? 0);
      } else {
        setError("Failed to fetch wallet balance");
      }
    } catch (err) {
      console.error(err);
      setError("Something went wrong while loading wallet");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBalance();
  }, []);

  if (loading) {
    return (
      <div className="page-container">
        <h2>Loading wallet...</h2>
      </div>
    );
  }

  
  return (
    <div className="page-container">

      <h1 className="page-title">My Wallet</h1>

      {error && <div className="error-box">{error}</div>}

      {/* BALANCE CARD */}
      <div className="card">
        <h3>Wallet Balance</h3>
        <p className="balance-amount">₹ {balance ?? 0}</p>
        <button onClick={loadBalance}>Refresh Balance</button>
      </div>

      {/* MOCK BANK MODULE (CLEAN SEPARATION) */}
      <MockBankPanel onSuccess={loadBalance} />

    </div>
  );
};

export default WalletPage;