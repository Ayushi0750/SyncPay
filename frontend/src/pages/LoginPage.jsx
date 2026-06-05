import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  const validate = () => {
    if (!email || !password) {
      return "Please enter email and password";
    }

    if (!email.includes("@")) {
      return "Please enter a valid email";
    }

    return null;
  };

  const handleLogin = async () => {
    const validationError = validate();

    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    setError("");

    try {
      const res = await fetch("http://localhost:8081/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));

        throw new Error(
          errorData.message ||
            errorData.error ||
            "Login failed. Please check your credentials."
        );
      }

      const data = await res.json();

      if (!data.token) {
        throw new Error("No token received from server");
      }

      
      localStorage.setItem("token", data.token);
      localStorage.setItem("userEmail", email);
      
      
      const userName = email.split("@")[0];
      localStorage.setItem("userName", userName);

      navigate("/dashboard");

    } catch (err) {
      setError(err.message || "Server error. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !loading) {
      handleLogin();
    }
  };

  return (
    <div className="page-container">

      <h1 className="page-title">Login</h1>

      {error && (
        <div className="error-box">
          {error}
        </div>
      )}

      <div className="card">

        <div>
          <label>Email</label>
          <input
            type="email"
            placeholder="user@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            onKeyDown={handleKeyPress}
            disabled={loading}
          />
        </div>

        <div>
          <label>Password</label>
          <input
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onKeyDown={handleKeyPress}
            disabled={loading}
          />
        </div>

        <button
          onClick={handleLogin}
          disabled={loading}
        >
          {loading ? "Logging in..." : "Login"}
        </button>

      </div>

      <p>
        Don't have an account?{" "}
        <Link to="/register">Register here</Link>
      </p>

    </div>
  );
}