import { useEffect, useState } from "react";
import api from "../services/api";

const TransactionPage = () => {
  const [transactions, setTransactions] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  
  const fetchTransactions = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/transaction/history");

      console.log("Transactions loaded:", response.data);
      setTransactions(response.data);

    } catch (err) {
      console.error("Error fetching transactions:", err);

      if (err.response?.status === 401 || err.response?.status === 403) {
        setError("Session expired. Please login again.");
      } else {
        setError("Failed to load transactions");
      }

    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, []);

  
  if (loading) {
    return (
      <div className="page-container">
        <h2>Loading transactions...</h2>
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
          <p>No transactions found</p>
        </div>
      )}

      {/* TRANSACTIONS LIST */}
      {!error && transactions.length > 0 && (
        <div>

          {transactions.map((tx) => (
            <div key={tx.transactionId} className="card">

              <p>
                <strong>
                  {tx.type === "SENT" ? "Sent " : "Received "}
                </strong>
              </p>

              <p>Amount: ₹{tx.amount}</p>

              <p>
                {tx.type === "SENT"
                  ? `To: ${tx.counterpartyEmail || tx.toEmail}`
                  : `From: ${tx.counterpartyEmail || tx.fromEmail}`}
              </p>

              <p className="meta-text">
                {tx.timestamp
                  ? new Date(tx.timestamp).toLocaleString()
                  : "Unknown date"}
              </p>

              <p>
                Status:{" "}
                <span
                  className={
                    tx.status === "SUCCESS"
                      ? "status-success"
                      : tx.status === "PENDING"
                      ? "status-pending"
                      : "status-failed"
                  }
                >
                  {tx.status}
                </span>
              </p>

              <p className="meta-text">
                ID: {tx.transactionId}
              </p>

            </div>
          ))}

        </div>
      )}

    </div>
  );
};

export default TransactionPage;