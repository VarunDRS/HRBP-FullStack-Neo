import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import { LogOut, Lock } from "lucide-react";

import LeaveRequestsPanel from "../components/LeaveRequestsPanel";

const EmployeeDashboard = () => {
  const [employeeName, setEmployeeName] = useState("Employee");
  const [leaveRequests, setLeaveRequests] = useState({});
  const [monthFilter, setMonthFilter] = useState("");
  const [employeeFilter, setEmployeeFilter] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  // Fetch Employee data on component mount
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem("Authorization");
        const decodedToken = jwtDecode(token);
        const userId = decodedToken.userId;  

        // Fetch employee name and info
        const userResponse = await axios.get(
          `http://localhost:8080/users/${userId}`,
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );
        
        // Set Employee name
        setEmployeeName(userResponse.data || "Employee");

        // Fetch leave requests
        const leaveResponse = await axios.get(
          `http://localhost:8080/employee/${userId}`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        // Set leave requests
        if (leaveResponse.data) {
          setLeaveRequests(leaveResponse.data);
        }
      } catch (error) {
        console.error("Error fetching data:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // Get filtered leave requests
  const getFilteredLeaveRequests = () => {
    if (leaveRequests && typeof leaveRequests === 'object' && !Array.isArray(leaveRequests)) {
      if (!monthFilter && !employeeFilter) return leaveRequests;

      const filteredRequests = {};
      Object.entries(leaveRequests).forEach(([employeeName, requests]) => {
        if (employeeFilter && !employeeName.toLowerCase().includes(employeeFilter.toLowerCase())) {
          return;
        }

        const filteredEmployeeRequests = {};
        Object.entries(requests).forEach(([date, status]) => {
          const requestDate = new Date(date);
          if (!monthFilter || (requestDate.getMonth() + 1) === parseInt(monthFilter)) {
            filteredEmployeeRequests[date] = status;
          }
        });

        if (Object.keys(filteredEmployeeRequests).length > 0) {
          filteredRequests[employeeName] = filteredEmployeeRequests;
        }
      });

      return filteredRequests;
    }

    return leaveRequests;
  };

  // Navigation handlers
  const handleUpdatePassword = () => {
    navigate("/users/update-password");
  };

  // Handle apply for leave
  const handleApplyForLeave = () => {
    navigate("/apply-leave");
  };

  return (
    <div className="min-h-screen bg-gray-50 text-gray-800">
      {/* Top navigation bar */}
      <div className="bg-indigo-800 text-white shadow-lg">
        <div className="container mx-auto px-4 py-7 flex justify-between items-center">
          <div className="flex flex-col items-start space-y-1">
            <div className="flex items-center space-x-3">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
              </svg>
              <h1 className="px-115 text-3xl font-bold">Autonaut Employee Portal</h1>
            </div>
            <span className="px-150 text-lg text-gray-300">Welcome, {employeeName}</span>
          </div>
          <button 
            className="px-4 py-2 bg-red-700 hover:bg-red-900 rounded-lg flex items-center text-white"
            onClick={() => {
              localStorage.setItem("Authorization", "null");
              window.location.href = "/login";
            }}
          >
            <LogOut size={16} className="mr-1" />
            Sign Out
          </button>
        </div>
      </div>

      {/* Main content */}
      <div className="container mx-auto px-4 py-6">
        {/* Quick actions section */}
        <div className="mb-6">
          <h2 className="text-xl font-semibold text-gray-700 mb-4">Quick Actions</h2>
          <div className="grid grid-cols-1 md:grid-cols-1 gap-4">
            <button 
              onClick={handleUpdatePassword}
              className="flex items-center justify-center bg-white border border-gray-200 rounded-lg p-4 shadow-sm hover:shadow-md transition duration-150 text-indigo-800"
            >
              <Lock size={16} className="mr-2" />
              Update Password
            </button>
            {/* <button 
              onClick={handleApplyForLeave}
              className="flex items-center justify-center bg-white border border-gray-200 rounded-lg p-4 shadow-sm hover:shadow-md transition duration-150 text-indigo-800"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" />
              </svg>
              Apply for Leave
            </button> */}
          </div>
        </div>

        {/* Search and filters */}
        <div className="bg-white p-4 rounded-lg shadow-sm mb-6">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
            <div className="relative flex-1">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg className="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clipRule="evenodd" />
                </svg>
              </div>
              <input
                type="text"
                className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white placeholder-gray-500 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                placeholder="Search leave requests..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            
            <div className="flex flex-col md:flex-row gap-3 md:items-center">
              <select
                value={monthFilter}
                onChange={(e) => setMonthFilter(e.target.value)}
                className="block w-full md:w-auto pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md"
              >
                <option value="">All Months</option>
                <option value="1">January</option>
                <option value="2">February</option>
                <option value="3">March</option>
                <option value="4">April</option>
                <option value="5">May</option>
                <option value="6">June</option>
                <option value="7">July</option>
                <option value="8">August</option>
                <option value="9">September</option>
                <option value="10">October</option>
                <option value="11">November</option>
                <option value="12">December</option>
              </select>
            </div>
          </div>
        </div>

        {/* Main content grid */}
        <div className="grid grid-cols-1 gap-6">
          {/* Leave Requests Section */}
          <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            <div className="bg-indigo-50 border-b border-gray-200 px-4 py-3 flex justify-between items-center">
              <h3 className="text-lg font-medium text-indigo-800">My Leave Requests</h3>
              <span className="bg-indigo-100 text-indigo-800 text-xs font-medium px-2.5 py-0.5 rounded-full">
                {Object.keys(getFilteredLeaveRequests()).length} Request(s)
              </span>
            </div>
            <div className="p-4 max-h-96 overflow-y-auto">
              {loading ? (
                <div className="flex justify-center items-center h-64">
                  <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-700"></div>
                </div>
              ) : Object.keys(getFilteredLeaveRequests()).length > 0 ? (
                <LeaveRequestsPanel leaveRequests={getFilteredLeaveRequests()} />
              ) : (
                <div className="text-center py-8 text-gray-500">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 mx-auto text-gray-400 mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  <p>No leave requests found with current filters</p>
                </div>
              )}
            </div>
          </div>
          
          {/* Leave Calendar Section - This could be added later */}
          {/* <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            <div className="bg-indigo-50 border-b border-gray-200 px-4 py-3">
              <h3 className="text-lg font-medium text-indigo-800">Leave Calendar</h3>
            </div>
            <div className="p-4 h-64 flex items-center justify-center text-gray-500">
              <div className="text-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 mx-auto text-gray-400 mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                <p>Calendar view coming soon</p>
              </div>
            </div>
          </div>
        </div> */}
        </div >

        {/* Footer */}
        <div className="mt-8 text-center text-sm text-gray-500 pb-6">
          <p>© {new Date().getFullYear()} Autonaut Employee System. All rights reserved.</p>
        </div>
      </div>
    </div>
  );
};

export default EmployeeDashboard;