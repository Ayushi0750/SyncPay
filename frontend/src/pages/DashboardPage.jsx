import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import SimulationPanel from "../components/SimulationPanel";

function DashboardPage() {
  const [loading] = useState(false);
  const [error] = useState("");

  const navigate = useNavigate();

  //logout
  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };

  return (
    <div className="page-container">

      {/* DASHBOARD HEADER */}
      <h1 className="page-title">Dashboard</h1>

      {/* ERROR SECTION */}
      {error && (
        <div className="error-box">
          {error}
        </div>
      )}

      {/* LOADING STATE */}
      {loading && (
        <div className="card">
          <p>Loading dashboard...</p>
        </div>
      )}

      {/* MAIN DASHBOARD */}
      {!loading && (
        <>

          {/*  SIMULATION PANEL */}
          <SimulationPanel />

          {/* WALLET */}
          <div className="card">
            <h3>Wallet</h3>
            <p>Check balance and wallet information</p>
            <br />
            <Link to="/wallet">
              <button>Open Wallet</button>
            </Link>
          </div>

          {/* SEND MONEY */}
          <div className="card">
            <h3>Send Money</h3>
            <p>Transfer funds securely to another user</p>
            <br />
            <Link to="/send">
              <button>Send Money</button>
            </Link>
          </div>

          {/* TRANSACTION HISTORY */}
          <div className="card">
            <h3>Transaction History</h3>
            <p>View all completed transactions</p>
            <br />
            <Link to="/history">
              <button>View History</button>
            </Link>
          </div>

          {/* OFFLINE QUEUE */}
          <div className="card">
            <h3>Offline Queue</h3>
            <p>View transactions waiting for synchronization</p>
            <br />
            <Link to="/queue">
              <button>Open Queue</button>
            </Link>
          </div>

          {/* SYNC CENTER */}
          <div className="card">
            <h3>Sync Center</h3>
            <p>Manage offline-to-online transaction synchronization</p>
            <br />
            <Link to="/sync">
              <button>Open Sync Center</button>
            </Link>
          </div>

          {/* LOGOUT */}
          <div className="card">
            <h3>Account</h3>
            <p>Securely logout from your session</p>
            <br />
            <button onClick={handleLogout}>
              Logout
            </button>
          </div>

        </>
      )}

    </div>
  );
}

export default DashboardPage;