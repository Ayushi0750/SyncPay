
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { ThemeProvider } from "./context/ThemeContext";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";
import WalletPage from "./pages/WalletPage";
import SendMoneyPage from "./pages/SendMoneyPage";
import TransactionHistoryPage from "./pages/TransactionHistoryPage";
import OfflineQueuePage from "./pages/OfflineQueuePage";
import SyncPage from "./pages/SyncPage";
import TransactionPage from "./pages/TransactionPage";

import PrivateRoute from "./routes/PrivateRoute";
import Navbar from "./components/Navbar";

function App() {
  return (
    <ThemeProvider>
      <BrowserRouter>
        {/*  Navbar will show on EVERY page */}
        <Navbar />
        
        <div className="main-content">
          <Routes>
            {/*  PUBLIC ROUTES */}
            <Route path="/" element={<LoginPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/*  PROTECTED ROUTES */}
            <Route
              path="/dashboard"
              element={
                <PrivateRoute>
                  <DashboardPage />
                </PrivateRoute>
              }
            />

            <Route
              path="/wallet"
              element={
                <PrivateRoute>
                  <WalletPage />
                </PrivateRoute>
              }
            />

            <Route
              path="/send"
              element={
                <PrivateRoute>
                  <SendMoneyPage />
                </PrivateRoute>
              }
            />

            {/*  TRANSACTIONS (kept both for now, no logic break) */}
            <Route
              path="/transactions"
              element={
                <PrivateRoute>
                  <TransactionPage />
                </PrivateRoute>
              }
            />

            <Route
              path="/history"
              element={
                <PrivateRoute>
                  <TransactionHistoryPage />
                </PrivateRoute>
              }
            />

            <Route
              path="/queue"
              element={
                <PrivateRoute>
                  <OfflineQueuePage />
                </PrivateRoute>
              }
            />

            <Route
              path="/sync"
              element={
                <PrivateRoute>
                  <SyncPage />
                </PrivateRoute>
              }
            />

            {/*  SAFETY FALLBACK */}
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </div>
      </BrowserRouter>
    </ThemeProvider>
  );
}

export default App;