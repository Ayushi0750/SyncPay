import React, { useState, useEffect, useRef } from "react";
import "../index.css";

const UserProfile = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [user, setUser] = useState(null);
  const dropdownRef = useRef(null);

  useEffect(() => {
   
    const email = localStorage.getItem("userEmail") || "user@example.com";
    const name = localStorage.getItem("userName") || "SyncPay User";
    setUser({ email, name });
  }, []);

  
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const toggleDropdown = () => {
    setIsOpen(!isOpen);
  };

  return (
    <div className="user-profile" ref={dropdownRef}>
      <button className="profile-btn" onClick={toggleDropdown}>
        <div className="profile-avatar">
          <span className="avatar-text">
            {user?.name?.charAt(0).toUpperCase() || "U"}
          </span>
        </div>
        <span className="profile-name">{user?.name?.split(" ")[0] || "User"}</span>
        <span className={`dropdown-arrow ${isOpen ? "open" : ""}`}>▼</span>
      </button>

      {isOpen && (
        <div className="profile-dropdown">
          {/* User Info Section */}
          <div className="dropdown-header">
            <div className="dropdown-avatar">
              <span className="dropdown-avatar-text">
                {user?.name?.charAt(0).toUpperCase() || "U"}
              </span>
            </div>
            <div className="dropdown-user-info">
              <div className="dropdown-user-name">{user?.name || "User"}</div>
              <div className="dropdown-user-email">{user?.email || "user@example.com"}</div>
            </div>
          </div>
          
          <div className="dropdown-divider"></div>
          
          {/* Account Info Section */}
          <div className="dropdown-section">
            <div className="dropdown-section-title">Account Information</div>
            <div className="dropdown-info-item">
              <span className="info-label">Email:</span>
              <span className="info-value">{user?.email || "Not set"}</span>
            </div>
            <div className="dropdown-info-item">
              <span className="info-label">Member since:</span>
              <span className="info-value">{new Date().getFullYear()}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserProfile;