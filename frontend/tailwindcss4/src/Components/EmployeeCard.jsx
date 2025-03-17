
import React from "react";
import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";

const EmployeeCard = ({ employee }) => {
  const navigate = useNavigate();

  const token = localStorage.getItem("Authorization");
  const decodedToken = jwtDecode(token); 
  const role = decodedToken.roles?.[0];

  const viewCalender = () => {
    if (role === "ROLE_HR") navigate(`/hr/${employee.id}/Mar-2025`);
    else navigate(`/manager/${employee.id}/Mar-2025`);
  };

  const handleUpdateClick = () => {
    navigate(`/hr/updateemployee/${employee.id}`, { state: { employee } });
  };

  return (
    <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
      <div className="flex items-start gap-3">
        <div className="flex-shrink-0 w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center text-white">
          <span className="text-lg font-bold">
            {employee.name
              .split(" ")
              .map((n) => n[0])
              .join("")}
          </span>
        </div>
        <div className="flex-1">
          <h3 className="font-medium text-lg text-black">{employee.name}</h3>
          <p className="text-blue-500">{employee.position}</p>
          <p className="text-gray-600">{employee.department}</p>
          <p className="text-sm text-gray-500">{employee.email}</p>
        </div>
      </div>
      <div className="mt-3 flex space-x-2">
        <button
          onClick={viewCalender}
          className="px-3 py-1 text-xs bg-blue-400 hover:bg-blue-700 rounded text-white"
        >
          View Calendar
        </button>
        {role !== "ROLE_MANAGER" && (
          <button
            onClick={handleUpdateClick}
            className="px-3 py-1 text-xs bg-green-400 hover:bg-green-700 rounded text-white"
          >
            Update Details
          </button>
        )}
      </div>
    </div>
  );
};

export default EmployeeCard;


