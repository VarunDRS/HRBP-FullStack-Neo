import React from "react";
import { LogOut } from "lucide-react";
import { jwtDecode } from "jwt-decode";
import loginImage from "../assets/logo3.png";

const Navbar = ({ hrName }) => {
  const handleSignOut = () => {
    localStorage.setItem("Authorization", "null");
    window.location.href = "/login";
  };

  const token = localStorage.getItem("Authorization");
  let portalName = "Autonaut EMPLOYEE Portal"; 

  if (token && token !== "null") {
    try {
      const decodedToken = jwtDecode(token);
      const roles = decodedToken?.roles || [];

      if (roles.includes("ROLE_HR")) {
        portalName = "Autonaut HR Portal";
      } else if (roles.includes("ROLE_MANAGER")) {
        portalName = "Autonaut MANAGER Portal";
      }
    } catch (error) {
      console.error("Error decoding token:", error);
    }
  }

  return (
    <div className="bg-indigo-800 text-white shadow-lg">
      <div className="container mx-auto px-4 py-5 flex justify-between items-center">
        <div className="flex flex-col items-start space-y-1">
          <div className="flex items-center space-x-3">
            <img className="h-12 w-auto mt-3" src={loginImage} alt="Company logo" />
            <h1 className="px-125 text-2xl font-bold">{portalName}</h1>
          </div>
          <span className="px-155 text-lg text-gray-300">Welcome, {hrName}</span>
        </div>
        <button 
          className="px-4 py-2 bg-red-700 hover:bg-red-900 rounded-lg flex items-center text-white"
          onClick={handleSignOut}
        >
          <LogOut size={16} className="mr-1" />
          Sign Out
        </button>
      </div>
    </div>
  );
};

export default Navbar;
