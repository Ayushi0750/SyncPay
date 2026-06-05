import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../services/api";

function RegisterPage() {

  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    phone: "",
    password: ""
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const validate = () => {
    if (
      !formData.name ||
      !formData.email ||
      !formData.phone ||
      !formData.password
    ) {
      return "All fields are required";
    }

    if (!formData.email.includes("@")) {
      return "Enter a valid email";
    }

    if (formData.phone.length < 10) {
      return "Enter a valid phone number";
    }

    if (formData.password.length < 4) {
      return "Password must be at least 4 characters";
    }

    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationError = validate();

    if (validationError) {
      setError(validationError);
      return;
    }

    try {
      setLoading(true);
      setError("");
      setSuccess("");

      await api.post(
        "/auth/register",
        formData
      );

      setSuccess(
        "Registration successful! Redirecting to login..."
      );

      setFormData({
        name: "",
        email: "",
        phone: "",
        password: ""
      });

      setTimeout(() => {
        navigate("/login");
      }, 2000);

    } catch (error) {

      if (error.response?.status === 409) {
        setError("User already exists");
      } else if (error.response?.status === 500) {
        setError("Server error. Try again later.");
      } else {
        setError("Registration failed. Please try again.");
      }

    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">

      <h1 className="page-title">Register</h1>

      {error && (
        <div className="error-box">
          {error}
        </div>
      )}

      {success && (
        <div className="success-box">
          {success}
        </div>
      )}

      <div className="card">

        <form onSubmit={handleSubmit}>

          <div>
            <label>Name</label>
            <input
              type="text"
              name="name"
              placeholder="Enter Name"
              value={formData.name}
              onChange={handleChange}
            />
          </div>

          <div>
            <label>Email</label>
            <input
              type="email"
              name="email"
              placeholder="Enter Email"
              value={formData.email}
              onChange={handleChange}
            />
          </div>

          <div>
            <label>Phone</label>
            <input
              type="text"
              name="phone"
              placeholder="Enter Phone"
              value={formData.phone}
              onChange={handleChange}
            />
          </div>

          <div>
            <label>Password</label>
            <input
              type="password"
              name="password"
              placeholder="Enter Password"
              value={formData.password}
              onChange={handleChange}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
          >
            {loading ? "Registering..." : "Register"}
          </button>

        </form>

      </div>

      <p>
        Already have an account?{" "}
        <Link to="/login">Login here</Link>
      </p>

    </div>
  );
}

export default RegisterPage;