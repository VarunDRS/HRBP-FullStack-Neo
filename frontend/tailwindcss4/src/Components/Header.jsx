import React from "react";
import { LogOut, Lock } from "lucide-react";
import loginImage from "../assets/logo3.png";
import { useLocation, useNavigate } from "react-router-dom";

const Header = ({ userName, title, userRole }) => {
  const location = useLocation();
  const path = location.pathname;
  
  // Determine portal type based on path or provided title
  const getPortalTitle = () => {
    if (title) return title;
    
    if (path.includes("/manager")) {
      return "Autonaut Manager Portal";
    } else if (path.includes("/employee")) {
      return "Autonaut Employee Portal";
    } else {
      return "Autonaut HR Portal";
    }
  };

  // Determine role based on explicit prop or path
  const getRole = () => {
    if (userRole) return userRole;
    
    if (path.includes("/manager")) {
      return "Manager";
    } else if (path.includes("/employee")) {
      return "Employee";
    } else {
      return "HR";
    }
  };

  const navigate = useNavigate();
  const role = getRole();
  const isManager = role === "Manager" || role === "Employee";

  const handleUpdatePassword = () => {
    navigate("/users/update-password");
  };

  return (
    <header className="bg-white rounded-lg p-4 mb-6 flex justify-between items-center w-full shadow-sm">
      <div className="flex flex-col items-center mb-4">
        <div className="flex items-center justify-center">
          <img
            className="h-16 w-auto mr-2"
            src={loginImage}
            alt="Company logo"
          />
          <h className="pl-85 text-4xl font-bold text-gray-800">
            {getPortalTitle()}
          </h>
        </div>
        <p className="text-gray-600 pl-125">
          Welcome back, {userName || "User"}! {userName ? `(${role})` : ""}
        </p>
      </div>

      <div className="flex items-center space-x-4">
        {isManager && (
          <button 
            onClick={handleUpdatePassword}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg flex items-center text-white"
          >
            <Lock size={16} className="mr-1" />
            Update Password
          </button>
        )}
        <button 
              className="px-4 py-2 bg-red-600 hover:bg-red-700 rounded-lg flex items-center text-white"
              onClick={() => {
                localStorage.setItem("Authorization", "null");
                window.location.href = "/login";
              }}
            >
              <LogOut size={16} className="mr-1" />
              Sign Out
          </button>

      </div>
    </header>
  );
};

export default Header;