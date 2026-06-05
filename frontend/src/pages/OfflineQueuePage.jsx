import { useEffect, useState } from "react";
import api from "../services/api";

function OfflineQueuePage() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchPendingTransactions = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/queue/pending");
      setTransactions(response.data);

    } catch (error) {
      console.error("Failed to fetch pending transactions:", error);

      if (error.response?.status === 401) {
        setError("Session expired. Please login again.");
      } else if (error.response?.status === 403) {
        setError("You are not allowed to access the queue.");
      } else {
        setError("Failed to load offline queue. Try again.");
      }

    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPendingTransactions();
  }, []);

  
  useEffect(() => {
    const onFocus = () => fetchPendingTransactions();
    window.addEventListener("focus", onFocus);

    return () => window.removeEventListener("focus", onFocus);
  }, []);

  if (loading) {
    return (
      <div className="page-container">
        <h2>Loading Pending Transactions...</h2>
      </div>
    );
  }

  
  return (
    <div className="page-container">

      <h1 className="page-title">Offline Queue</h1>

      {/* ERROR */}
      {error && (
        <div className="error-box">
          {error}
        </div>
      )}

      {/* EMPTY STATE */}
      {!error && transactions.length === 0 && (
        <div className="card">
          <p>No pending transactions found.</p>
        </div>
      )}

      {/* TABLE */}
      {!error && transactions.length > 0 && (
        <div className="card">

          <h3>Pending Transactions</h3>

          {/* MANUAL REFRESH BUTTON (IMPORTANT FOR DEBUGGING) */}
          <button onClick={fetchPendingTransactions}>
            Refresh Queue
          </button>

          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Transaction ID</th>
                <th>Receiver</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Created At</th>
              </tr>
            </thead>

            <tbody>
              {transactions.map((tx) => (
                <tr key={tx.id}>
                  <td>{tx.id}</td>
                  <td>{tx.transactionId}</td>
                  <td>{tx.toEmail}</td>
                  <td>₹{tx.amount}</td>
                  <td>{tx.status}</td>
                  <td>{tx.createdAt}</td>
                </tr>
              ))}
            </tbody>

          </table>

        </div>
      )}

    </div>
  );
}

export default OfflineQueuePage;