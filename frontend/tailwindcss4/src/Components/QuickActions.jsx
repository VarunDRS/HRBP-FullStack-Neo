import React from "react";
import { jwtDecode } from "jwt-decode";

const QuickActions = ({ onCreateUser, onUpdatePassword }) => {
  const token = localStorage.getItem("Authorization");
  let roles = [];

  if (token && token !== "null") {
    try {
      const decodedToken = jwtDecode(token);
      roles = decodedToken?.roles || [];
    } catch (error) {
      console.error("Error decoding token:", error);
    }
  }

  const isHR = roles.includes("ROLE_HR");
  const isManagerOrEmployee = roles.includes("ROLE_MANAGER") || roles.includes("ROLE_EMPLOYEE");

  return (
    <div className="mb-6">
      <h2 className="text-xl font-semibold text-gray-700 mb-4">Quick Actions</h2>
      <div className={`grid ${isHR ? "grid-cols-1 md:grid-cols-2 gap-4" : "grid-cols-1"}`}>
        {/* Show both buttons for HR */}
        {isHR && (
          <button
            onClick={onCreateUser}
            className="flex items-center justify-center bg-white border border-gray-200 rounded-lg p-4 shadow-sm hover:shadow-md transition duration-150 text-indigo-800"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
              <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6zM16 7a1 1 0 10-2 0v1h-1a1 1 0 100 2h1v1a1 1 0 102 0v-1h1a1 1 0 100-2h-1V7z" />
            </svg>
            Create User
          </button>
        )}

        {/* Update Password button for everyone */}
        <button
          onClick={onUpdatePassword}
          className={`flex items-center justify-center bg-white border border-gray-200 rounded-lg p-4 shadow-sm hover:shadow-md transition duration-150 text-indigo-800 ${
            isManagerOrEmployee ? "col-span-1 w-full" : ""
          }`}
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
            <path fillRule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clipRule="evenodd" />
          </svg>
          Update Password
        </button>
      </div>
    </div>
  );
};

export default QuickActions;
