import React, { useState } from "react";
import { Link } from "react-router-dom";
import ThemeToggle from "./ThemeToggle";
import UserProfile from "./UserProfile";
import "../index.css";

const Navbar = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const token = localStorage.getItem("token");
  const isLoggedIn = !!token;

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  return (
    <nav className="syncpay-navbar">
      <div className="navbar-container">
        {/* Logo Section */}
        <div className="navbar-logo">
          <Link to="/" className="logo-link">
            <span className="payment-symbol">⟠</span>
            <div className="logo-text">
              <span className="brand-name">SyncPay</span>
              <span className="brand-tagline">Pay anywhere. Sync everywhere.</span>
            </div>
          </Link>
        </div>

        {/* Right side actions */}
        <div className="navbar-actions">
          <ThemeToggle />
          {isLoggedIn && <UserProfile />}
          
          {/* Mobile Menu Button */}
          {isLoggedIn && (
            <button className="mobile-menu-btn" onClick={toggleMobileMenu}>
              <span className={`hamburger ${isMobileMenuOpen ? "open" : ""}`}>
                <span></span>
                <span></span>
                <span></span>
              </span>
            </button>
          )}
        </div>

        {/* Navigation Links */}
        <div className={`navbar-links ${isMobileMenuOpen ? "active" : ""}`}>
          {/* No logout button here anymore */}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;