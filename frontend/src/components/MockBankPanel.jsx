import { useState } from "react";
import { creditMockBank, debitMockBank } from "../services/mockBankService";

const MockBankPanel = ({ onSuccess }) => {
  const [amount, setAmount] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState(""); // "success", "error", "warning"
  const [forceFail, setForceFail] = useState(false);

  
  const clearMessageAfterDelay = () => {
    setTimeout(() => {
      setMessage("");
      setMessageType("");
    }, 5000);
  };

  
  const handleCredit = async () => {
    if (!amount || amount <= 0) {
      setMessageType("warning");
      setMessage(" Please enter a valid amount");
      clearMessageAfterDelay();
      return;
    }

    setLoading(true);
    setMessage("");
    setMessageType("");

    const res = await creditMockBank(Number(amount), forceFail);

    if (res.success) {
      setMessageType("success");
      
      if (forceFail) {
        setMessage(` SIMULATION: ₹${amount} transaction failed as expected (Test failure)`);
      } else {
        setMessage(` ₹${amount} added to wallet successfully!`);
      }
      
      setAmount("");
      onSuccess?.();
    } else {
      setMessageType("error");
      
      if (forceFail) {
        setMessage(` SIMULATION MODE: Transaction of ₹${amount} failed as expected.\n\nThis is a TEST failure. Disable "Simulate Failure" to test real transactions.`);
      } else if (res.error?.toLowerCase().includes("insufficient")) {
        setMessage(` Insufficient balance in Mock Bank.\nRequested: ₹${amount}`);
      } else if (res.error?.toLowerCase().includes("network")) {
        setMessage(` NETWORK SIMULATION: Transaction failed.\nAmount: ₹${amount}`);
      } else {
        setMessage(` Transaction failed: ${res.error || "Please try again"}`);
      }
    }

    clearMessageAfterDelay();
    setLoading(false);
  };

 
  const handleDebit = async () => {
    if (!amount || amount <= 0) {
      setMessageType("warning");
      setMessage(" Please enter a valid amount");
      clearMessageAfterDelay();
      return;
    }

    setLoading(true);
    setMessage("");
    setMessageType("");

    const res = await debitMockBank(Number(amount), forceFail);

    if (res.success) {
      setMessageType("success");
      
      if (forceFail) {
        setMessage(` SIMULATION: ₹${amount} withdrawal failed as expected (Test failure)`);
      } else {
        setMessage(` ₹${amount} withdrawn from wallet successfully!`);
      }
      
      setAmount("");
      onSuccess?.();
    } else {
      setMessageType("error");
      
      if (forceFail) {
        setMessage(` SIMULATION MODE: Withdrawal of ₹${amount} failed as expected.\n\nThis is a TEST failure. Disable "Simulate Failure" to test real transactions.`);
      } else if (res.error?.toLowerCase().includes("insufficient")) {
        setMessage(` Insufficient wallet balance.\nRequested: ₹${amount}`);
      } else {
        setMessage(` Withdrawal failed: ${res.error || "Please try again"}`);
      }
    }

    clearMessageAfterDelay();
    setLoading(false);
  };

  
  const getMessageClassName = () => {
    switch (messageType) {
      case "success":
        return "success-message";
      case "error":
        return "error-message";
      case "warning":
        return "warning-message";
      default:
        return "";
    }
  };

  return (
    <div className="card">
      <h3>Mock Bank Operations</h3>

      <input
        type="number"
        placeholder="Enter amount"
        value={amount}
        onChange={(e) => setAmount(e.target.value)}
      />

      {/* FAILURE SIMULATION TOGGLE */}
      <div style={{ marginTop: "10px" }}>
        <label>
          <input
            type="checkbox"
            checked={forceFail}
            onChange={(e) => {
              setForceFail(e.target.checked);
              setMessage("");
              setMessageType("");
              if (e.target.checked) {
                setMessageType("warning");
                setMessage(" SIMULATION MODE ENABLED: All transactions will fail for testing!");
                clearMessageAfterDelay();
              } else {
                setMessageType("success");
                setMessage(" SIMULATION MODE DISABLED: Real transactions will work.");
                clearMessageAfterDelay();
              }
            }}
          />
          {" "}Simulate Failure
        </label>
      </div>

      <br />

      <button onClick={handleCredit} disabled={loading}>
        Add Money
      </button>

      <button
        onClick={handleDebit}
        disabled={loading}
        style={{ marginLeft: "10px" }}
      >
        Withdraw
      </button>

      {message && (
        <p className={getMessageClassName()} style={{ marginTop: "10px", whiteSpace: "pre-line" }}>
          {message}
        </p>
      )}
    </div>
  );
};

export default MockBankPanel;