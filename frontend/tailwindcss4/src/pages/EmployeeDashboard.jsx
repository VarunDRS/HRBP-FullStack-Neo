import React, { useState, useEffect } from "react";
import axios from "axios";

// Import components
import Header from "../components/Header";
import ActionButtons from "../components/ActionButtons";
import FilterBar from "../components/FilterBar";
import LeaveRequestsPanel from "../components/LeaveRequestsPanel";
import EmployeeInfoPanel from "../components/EmployeeInfoPanel";
import { jwtDecode } from "jwt-decode";

const EmployeeDashboard = () => {
  const [employeeName, setEmployeeName] = useState("Sam Wilson");
  const [teamMembers, setTeamMembers] = useState([]);
  const [leaveRequests, setLeaveRequests] = useState({});
  const [monthFilter, setMonthFilter] = useState("");
  const [employeeFilter, setEmployeeFilter] = useState("");
  const [searchQuery, setSearchQuery] = useState("");

  // Fetch employee data on component mount
  useEffect(() => {
    const fetchData = async () => {
      try {
        const token = localStorage.getItem("Authorization");
        const decodedToken = jwtDecode(token);
        const userId = decodedToken.userId;  
        //const role = decodedToken.roles?.[0];

        console.log("checking")

        // Fetch employee name and info
        const userResponse = await axios.get(`http://localhost:8080/users/${userId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        console.log("Get user successful");
        console.log(userResponse);

        // Set Employee name
        setEmployeeName(userResponse.data || "Employee Admin");

        // Fetch leave requests
        const leaveResponse = await axios.get(
          `http://localhost:8080/employee/${userId}`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        console.log(leaveResponse.data);

        // Set leave requests
        if (leaveResponse.data) {
          setLeaveRequests(leaveResponse.data);
        }
      } catch (error) {
        console.error("Error fetching data:", error);
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

  // Get filtered team members
  const getFilteredTeamMembers = () => {
    let filtered = [...teamMembers];

    if (employeeFilter) {
      filtered = filtered.filter(
        (employee) => employee.id.toString() === employeeFilter
      );
    }

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (employee) =>
          employee.name.toLowerCase().includes(query) ||
          employee.email.toLowerCase().includes(query) ||
          employee.position.toLowerCase().includes(query) ||
          (employee.projects &&
            employee.projects.some((project) =>
              project.toLowerCase().includes(query)
            ))
      );
    }

    return filtered;
  };

  return (
    <div className="min-h-screen w-full flex justify-center bg-blue-100 fixed inset-0 text-white">
      <div className="relative z-10 flex flex-col min-h-screen p-4 lg:p-6 w-full overflow-hidden">
        <Header
          userName={employeeName}
          title="Autonaut Employee Portal"
          userRole="Employee"
        />

        <FilterBar
          employees={teamMembers}
          monthFilter={monthFilter}
          setMonthFilter={setMonthFilter}
          employeeFilter={employeeFilter}
          setEmployeeFilter={setEmployeeFilter}
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          placeholderText="Search by name, position or project..."
        />

        <div className="flex-1 grid grid-cols-1 lg:grid-cols-2 gap-6 w-full">
          <div className="overflow-y-auto max-h-[300px]">
            <LeaveRequestsPanel leaveRequests={getFilteredLeaveRequests()} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default EmployeeDashboard;
