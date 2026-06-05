
import { useEffect, useState } from "react";
import api from "../services/api";

function TransactionHistoryPage() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  
  const fetchHistory = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/transaction/history");
      setTransactions(response.data);

    } catch (error) {
      console.error("Failed to fetch history:", error);

      if (error.response?.status === 401) {
        setError("Session expired. Please login again.");
      } else if (error.response?.status === 403) {
        setError("You are not authorized to view history.");
      } else {
        setError("Failed to load transaction history.");
      }

    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, []);

  
  useEffect(() => {
    const onFocus = () => fetchHistory();
    window.addEventListener("focus", onFocus);

    return () => window.removeEventListener("focus", onFocus);
  }, []);

  
  if (loading) {
    return (
      <div className="page-container">
        <h2>Loading Transaction History...</h2>
      </div>
    );
  }

  
  return (
    <div className="page-container">

      <h1 className="page-title">Transaction History</h1>

      {/* ERROR */}
      {error && (
        <div className="error-box">
          {error}
        </div>
      )}

      {/* EMPTY STATE */}
      {!error && transactions.length === 0 && (
        <div className="card">
          <p>No transactions found.</p>
        </div>
      )}

      {/* TABLE */}
      {!error && transactions.length > 0 && (
        <div className="card">

          <table>
            <thead>
              <tr>
                <th>Transaction ID</th>
                <th>Sender</th>
                <th>Receiver</th>
                <th>Amount</th>
                <th>Status</th>
                <th>Timestamp</th>
              </tr>
            </thead>

            <tbody>
              {transactions.map((tx) => (
                <tr key={tx.transactionId}>
                  <td>{tx.transactionId}</td>
                  <td>{tx.fromEmail}</td>
                  <td>{tx.toEmail}</td>
                  <td>₹{tx.amount}</td>
                  <td>{tx.status}</td>
                  <td>
                    {tx.timestamp
                      ? new Date(tx.timestamp).toLocaleString()
                      : "Unknown"}
                  </td>
                </tr>
              ))}
            </tbody>

          </table>

        </div>
      )}

    </div>
  );
}

export default TransactionHistoryPage;