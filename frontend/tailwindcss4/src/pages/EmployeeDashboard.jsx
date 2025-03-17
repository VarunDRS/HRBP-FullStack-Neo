import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import { LogOut, Lock } from "lucide-react";

import Navbar from "../components/Navbar";
import QuickActions from "../components/QuickActions";
import SearchFilters from "../components/SearchFilters";
import LeaveRequestsPanel from "../components/LeaveRequestsPanel";
import EmployeeInfoPanel from "../components/EmployeeInfoPanel";
import Footer from "../components/Footer";

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

        console.log(leaveResponse);

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
      {/* Navbar Component */}
      <Navbar hrName={employeeName} />

      {/* Main content */}
      <div className="container mx-auto px-4 py-6">
        {/* Quick Actions Component */}
        <QuickActions 
          onUpdatePassword={handleUpdatePassword} 
        />

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
        </div >

        {/* Footer */}
        <Footer />
      </div>
    </div>
  );
};

export default EmployeeDashboard;