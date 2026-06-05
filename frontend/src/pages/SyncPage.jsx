import { useEffect, useRef, useState } from "react";
import {
  syncTransaction,
  fetchWalletBalance,
  fetchTransactionHistory
} from "../services/syncService";

function SyncPage() {

  const [pendingCount, setPendingCount] = useState(0);
  const [progress, setProgress] = useState("");
  const [successCount, setSuccessCount] = useState(0);
  const [failedCount, setFailedCount] = useState(0);

  const [syncing, setSyncing] = useState(false);

  const [failedQueue, setFailedQueue] = useState([]);
  const [dlqQueue, setDlqQueue] = useState([]);

  const [autoSyncEnabled, setAutoSyncEnabled] = useState(false);

  const [walletBalance, setWalletBalance] = useState(null);
  const [transactionHistory, setTransactionHistory] = useState([]);

  const [showWallet, setShowWallet] = useState(true);
  const [showDLQ, setShowDLQ] = useState(false);

  const [error, setError] = useState("");

  const processedRef = useRef(new Set());
  const autoSyncLock = useRef(false);

  
  const getQueue = () =>
    JSON.parse(localStorage.getItem("offlineQueue")) || [];

  const saveQueue = (queue) =>
    localStorage.setItem("offlineQueue", JSON.stringify(queue));

  const getFailedQueue = () =>
    JSON.parse(localStorage.getItem("failedQueue")) || [];

  const saveFailedQueue = (queue) =>
    localStorage.setItem("failedQueue", JSON.stringify(queue));

  const getDLQ = () =>
    JSON.parse(localStorage.getItem("dlqQueue")) || [];

  const saveDLQ = (queue) =>
    localStorage.setItem("dlqQueue", JSON.stringify(queue));

  
  useEffect(() => {
    refreshUI();

    const savedAutoSync =
      JSON.parse(localStorage.getItem("autoSyncEnabled"));

    if (savedAutoSync) setAutoSyncEnabled(true);

  }, []);

  const refreshUI = () => {
    const queue = getQueue();
    const failed = getFailedQueue();

    setPendingCount(queue.length);
    setFailedCount(failed.length);
    setFailedQueue(failed);
    setDlqQueue(getDLQ());
  };

  
  const runPostSyncUpdates = async () => {
    try {
      const wallet = await fetchWalletBalance();
      const history = await fetchTransactionHistory();

      if (wallet.success) setWalletBalance(wallet.data.balance);
      if (history.success) setTransactionHistory(history.data);

    } catch {
      setError("Failed to refresh post-sync data");
    }
  };

  
  const handleSync = async () => {
    const queue = getQueue();

    if (queue.length === 0) return;

    
    processedRef.current = new Set();

    setSyncing(true);
    setError("");
    setProgress("");
    setSuccessCount(0);
    setFailedCount(0);

    let success = [];
    let failed = [];

    for (let i = 0; i < queue.length; i++) {

      const tx = queue[i];

      if (processedRef.current.has(tx.transactionId)) continue;
      processedRef.current.add(tx.transactionId);

      setProgress(`Syncing ${i + 1}/${queue.length}`);

      try {
        const response = await syncTransaction(tx);

        if (response?.success) {
          success.push(tx);
        } else {
          failed.push({ ...tx, retryCount: 0 });
        }

      } catch {
        failed.push({ ...tx, retryCount: 0 });
      }
    }

    const updatedQueue =
      queue.filter(tx =>
        !success.some(s => s.transactionId === tx.transactionId)
      );

    saveQueue(updatedQueue);

    const mergedFailed = [...getFailedQueue(), ...failed];
    saveFailedQueue(mergedFailed);

    setSuccessCount(success.length);
    setFailedCount(mergedFailed.length);
    setPendingCount(updatedQueue.length);

    setSyncing(false);
    setProgress("Sync Completed");

    refreshUI();
    await runPostSyncUpdates();
  };

  const handleRetryFailed = async () => {
    const failed = getFailedQueue();

    if (failed.length === 0) return;

    setSyncing(true);
    setProgress("");

    let stillFailed = [];

    for (let i = 0; i < failed.length; i++) {

      const tx = failed[i];
      const retry = tx.retryCount || 0;

      setProgress(`Retrying ${i + 1}/${failed.length}`);

      if (retry >= 3) {
        const dlq = getDLQ();
        dlq.push({ ...tx, reason: "MAX_RETRY_EXCEEDED" });
        saveDLQ(dlq);
        continue;
      }

      await new Promise(r =>
        setTimeout(r, Math.pow(2, retry) * 1000)
      );

      try {
        const response = await syncTransaction(tx);

        if (!response?.success) {
          stillFailed.push({ ...tx, retryCount: retry + 1 });
        }

      } catch {
        stillFailed.push({ ...tx, retryCount: retry + 1 });
      }
    }

    saveFailedQueue(stillFailed);

    setFailedQueue(stillFailed);
    setFailedCount(stillFailed.length);

    setSyncing(false);
    setProgress("Retry Completed");

    refreshUI();
  };

  useEffect(() => {

    if (!autoSyncEnabled) return;

    const interval = setInterval(async () => {

      const queue = getQueue();

      if (queue.length === 0 || syncing) return;

      if (autoSyncLock.current) return;
      autoSyncLock.current = true;

      const tx = queue[0];

      try {
        const response = await syncTransaction(tx);

        if (response?.success) {
          const updated = queue.slice(1);
          saveQueue(updated);

          setPendingCount(updated.length);
          setSuccessCount(prev => prev + 1);

          await runPostSyncUpdates();
          setProgress("Auto-synced 1 transaction");

        } else {
          setFailedCount(prev => prev + 1);
        }

      } catch {
        setFailedCount(prev => prev + 1);
      } finally {
        autoSyncLock.current = false;
      }

    }, 3000);

    return () => clearInterval(interval);

  }, [autoSyncEnabled, syncing]);

  const toggleAutoSync = () => {
    const newState = !autoSyncEnabled;
    setAutoSyncEnabled(newState);
    localStorage.setItem("autoSyncEnabled", JSON.stringify(newState));
  };

  return (
    <div className="page-container">

      <h1 className="page-title">Sync Center</h1>

      {error && <div className="error-box">{error}</div>}

      <div className="card">
        <p>Pending: {pendingCount}</p>
        <p>Failed: {failedCount}</p>
        <p>Success: {successCount}</p>
      </div>

      <div className="card">
        <h3 onClick={() => setShowWallet(!showWallet)}>
          Wallet Balance (toggle)
        </h3>

        {showWallet && (
          <p>₹ {walletBalance ?? "Not loaded"}</p>
        )}
      </div>

      <div className="card">
        <p>{progress}</p>

        <button onClick={handleSync} disabled={syncing}>
          {syncing ? "Syncing..." : "Sync Transactions"}
        </button>

        <button onClick={handleRetryFailed} disabled={syncing}>
          Retry Failed
        </button>

        <button onClick={toggleAutoSync}>
          {autoSyncEnabled ? "Stop Auto Sync" : "Start Auto Sync"}
        </button>
      </div>

      <div className="card">

        <h3 onClick={() => setShowDLQ(!showDLQ)}>
          Dead Letter Queue ({dlqQueue.length})
        </h3>

        {showDLQ && dlqQueue.map((tx, i) => (
          <div key={i}>
            <p>{tx.transactionId}</p>
            <p>{tx.reason}</p>
          </div>
        ))}

      </div>

    </div>
  );
}

export default SyncPage;


