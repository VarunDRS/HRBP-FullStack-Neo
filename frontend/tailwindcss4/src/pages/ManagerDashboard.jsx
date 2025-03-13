
import React, { useState, useEffect } from "react";
import axios from "axios";
import { jwtDecode } from "jwt-decode";

// Import components
import Header from "../components/Header";
import ActionButtons from "../components/ActionButtons";
import FilterBar from "../components/FilterBar";
import LeaveRequestsPanel from "../components/LeaveRequestsPanel";
import EmployeeInfoPanel from "../components/EmployeeInfoPanel";

const ManagerDashboard = () => {
  const [managerName, setManagerName] = useState("Alex Johnson");
  const [teamMembers, setTeamMembers] = useState([]);
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [monthFilter, setMonthFilter] = useState("");
  const [employeeFilter, setEmployeeFilter] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [pageSize, setPageSize] = useState(2); // Default limit is 2 in your backend
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const token = localStorage.getItem("Authorization");
        const decodedToken = jwtDecode(token);
        const userId = decodedToken.userId;  
        const userRole = decodedToken.roles?.[0];

        const response = await axios.get(`http://localhost:8080/users/${userId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        
        console.log("Manager Name:", response.data);
        setManagerName(response.data);
  
        console.log("Fetching data for manager with ID:", userId);

        const leaveResponse = await axios.get(
          `http://localhost:8080/manager/${userId}`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        console.log("Leave Requests:");
        console.log(leaveResponse.data);

        if (leaveResponse.data) {
          setLeaveRequests(leaveResponse.data);
        }
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };
  
    fetchData();
  }, []);

  useEffect(() => {
    const fetchTotalPages = async () => {
      try {
        const token = localStorage.getItem("Authorization");
        const decodedToken = jwtDecode(token);
        const userId = decodedToken.userId;
        
        const response = await axios.get(
          `http://localhost:8080/manager/displayUsers/count/${userId}`,
          {
            headers: { Authorization: `Bearer ${token}` },
            params: { limit: pageSize },
          }
        );

        if (response.data) {
          setTotalPages(response.data.totalPages);
        }
      } catch (error) {
        console.error("Error fetching total pages:", error);
      }
    };

    fetchTotalPages(); // Call when component loads or dependencies change
  }, [pageSize]);
  
  useEffect(() => {
    const token = localStorage.getItem("Authorization");
    const decodedToken = jwtDecode(token);
    const userId = decodedToken.userId;
    
    fetchTeamMembers(userId, token);
  }, [currentPage, pageSize]);

  

  const fetchTeamMembers = async (userId, token) => {
    setLoading(true);
    try {
      // Use the paginated endpoint that matches your backend
      const response = await axios.get(
        `http://localhost:8080/manager/displayUsers/${userId}`,
        {
          headers: { Authorization: `Bearer ${token}` },
          params: {
            page: currentPage,
            limit: pageSize
          }
        }
      );
      
      console.log(response);

      if (response.data) {
        const formattedEmployees = response.data.map((employee) => ({
          id: employee.userId,
          email: employee.email,
          name: employee.username,
          position: employee.position || "Employee",
          department: employee.department || "General"
        }));
        
        setTeamMembers(formattedEmployees);
        
      }
    } catch (error) {
      console.error("Error fetching team members:", error);
    } finally {
      setLoading(false);
    }
  };

  const getFilteredLeaveRequests = () => {
    // If leaveRequests is an object with employee names as keys
    if (leaveRequests && typeof leaveRequests === 'object' && !Array.isArray(leaveRequests)) {
      // If no filters are applied, return the entire object
      if (!monthFilter && !employeeFilter) return leaveRequests;

      // Filter logic for the new data structure
      const filteredRequests = {};
      Object.entries(leaveRequests).forEach(([employeeName, requests]) => {
        // Employee filter
        if (employeeFilter && !employeeName.toLowerCase().includes(employeeFilter.toLowerCase())) {
          return;
        }

        // Month filter
        const filteredEmployeeRequests = {};
        Object.entries(requests).forEach(([date, status]) => {
          const requestDate = new Date(date);
          // If month filter is applied, check if it matches
          if (!monthFilter || (requestDate.getMonth() + 1) === parseInt(monthFilter)) {
            filteredEmployeeRequests[date] = status;
          }
        });

        // Only add if there are filtered requests
        if (Object.keys(filteredEmployeeRequests).length > 0) {
          filteredRequests[employeeName] = filteredEmployeeRequests;
        }
      });

      return filteredRequests;
    }

    // Fallback to existing filtering if data structure is different
    return leaveRequests;
  };  

  // Page navigation handlers
  const handlePrevPage = () => {
    if (currentPage > 1) {
      setCurrentPage(prev => prev - 1);
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage(prev => prev + 1);
    }
  };

  const handlePageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setCurrentPage(1); // Reset to first page when changing page size
  };

  // Local filtering for search will need to be implemented differently
  // since we're using server-side pagination
  const handleSearch = () => {
    // Reset to first page when searching
    setCurrentPage(1);
    // The actual search will be handled in the next data fetch
  };

  return (
    <div className="min-h-screen w-full flex justify-center bg-blue-100 fixed inset-0 text-white">
      <div className="relative z-10 flex flex-col min-h-screen p-4 lg:p-6 w-full">
        <Header
          userName={managerName}
          title="Autonaut Manager Portal"
          userRole="Manager"
        />

        <FilterBar
          employees={teamMembers}
          monthFilter={monthFilter}
          setMonthFilter={setMonthFilter}
          employeeFilter={employeeFilter}
          setEmployeeFilter={setEmployeeFilter}
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          onSearch={handleSearch}
          placeholderText="Search by name, position or project..."
        />

        <div className="flex-1 grid grid-cols-1 lg:grid-cols-2 gap-6 w-full">
          <div className="overflow-y-auto max-h-[450px]">
            <LeaveRequestsPanel
              leaveRequests={getFilteredLeaveRequests()}
            />
          </div>
          <div className="overflow-y-auto max-h-[450px]">
            <EmployeeInfoPanel
              employees={teamMembers}
              loading={loading}
              currentPage={currentPage}
              totalPages={totalPages}
              pageSize={pageSize}
              onPrevPage={handlePrevPage}
              onNextPage={handleNextPage}
              onPageSizeChange={handlePageSizeChange}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default ManagerDashboard;
