
import React from "react";
import { Users, ChevronLeft, ChevronRight } from "lucide-react";
import EmployeeCard from "./EmployeeCard";
import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";

const EmployeeInfoPanel = ({ 
  employees, 
  loading, 
  currentPage, 
  totalPages, 
  pageSize, 
  onPrevPage, 
  onNextPage, 
  onPageSizeChange 
}) => {
  const navigate = useNavigate();

  const token = localStorage.getItem("Authorization");
  const decodedToken = jwtDecode(token);
  const userId = decodedToken.userId;  
  const role = decodedToken.roles?.[0];
  
  const showMonthCalender = () => {
    if(role === "ROLE_HR") navigate(`/hr/monthly/${userId}/Mar-2025`);
    else navigate(`/manager/monthly/${userId}/Mar-2025`);
  };

  return (
    <>
      {/* Header with Employee Information and View Month Button */}
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-semibold text-gray-800 flex items-center">
          <Users size={20} className="mr-2 text-blue-600" />
          Employee Information
        </h2>
        <button 
          onClick={showMonthCalender}
          className="px-4 py-1 text-sm text-white bg-green-600 rounded-md hover:bg-green-700 transition">
          View Month
        </button>
      </div>

      <div
        className="flex-1 overflow-y-auto pr-2 [&::-webkit-scrollbar]:w-2
            [&::-webkit-scrollbar-track]:rounded-full
            [&::-webkit-scrollbar-track]:bg-gray-100
            [&::-webkit-scrollbar-thumb]:rounded-full
            [&::-webkit-scrollbar-thumb]:bg-gray-300
            dark:[&::-webkit-scrollbar-track]:bg-neutral-500
            dark:[&::-webkit-scrollbar-thumb]:bg-neutral-400"
      >
        {loading ? (
          <div className="flex justify-center items-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
          </div>
        ) : (
          <div
            className="grid grid-cols-1 xl:grid-cols-2 gap-4"
            style={{
              scrollbarColor: "#D1D5DB #F3F4F6",
              scrollbarWidth: "thin",
            }}
          >
            {employees && employees.length > 0 ? (
              employees.map((employee) => (
                <EmployeeCard key={employee.id} employee={employee} />
              ))
            ) : (
              <div className="col-span-full flex flex-col items-center justify-center h-64 text-gray-400">
                <Users size={48} className="mb-2 opacity-50" />
                <p>No employees found</p>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Pagination Controls */}
      <div className="flex items-center justify-between mt-4 text-gray-700">
        <div className="flex items-center">
          <label htmlFor="pageSize" className="mr-2 text-sm">Show:</label>
          <select 
            id="pageSize" 
            value={pageSize} 
            onChange={onPageSizeChange}
            className="border rounded px-2 py-1 text-sm bg-white"
          >
            <option value="2">2</option>
            <option value="4">4</option>
            <option value="6">6</option>
            <option value="8">8</option>
          </select>
        </div>
        
        <div className="flex items-center">
          <span className="text-sm mr-4">
            Page {currentPage} of {totalPages || 1}
          </span>
          <div className="flex space-x-1">
            <button 
              onClick={onPrevPage} 
              disabled={currentPage === 1}
              className={`p-1 rounded ${currentPage === 1 ? 'text-gray-400' : 'text-blue-600 hover:bg-blue-100'}`}
            >
              <ChevronLeft size={20} />
            </button>
            <button 
              onClick={onNextPage} 
              disabled={currentPage === totalPages}
              className={`p-1 rounded ${currentPage === totalPages ? 'text-gray-400' : 'text-blue-600 hover:bg-blue-100'}`}
            >
              <ChevronRight size={20} />
            </button>
          </div>
        </div>
      </div>
    </>
  );
};

export default EmployeeInfoPanel;
