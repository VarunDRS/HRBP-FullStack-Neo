import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";

import Navbar from "../Components/Navbar";
import QuickActions from "../Components/QuickActions";
import SearchFilters from "../Components/SearchFilters";
import LeaveRequestsPanel from "../Components/LeaveRequestsPanel";
import EmployeeInfoPanel from "../components/EmployeeInfoPanel";
import Footer from "../Components/Footer";

const HRDashboard = () => {
  const [hrName, setHrName] = useState("");
  const [leaveRequests, setLeaveRequests] = useState([]);
  const [monthFilter, setMonthFilter] = useState("");
  const [employeeFilter, setEmployeeFilter] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [teamMembers, setTeamMembers] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [pageSize, setPageSize] = useState(4);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  

  // Initial data fetch
  useEffect(() => {
    const fetchData = async () => {
      try {
        const token = localStorage.getItem("Authorization");
        const decodedToken = jwtDecode(token);
        const userId = decodedToken.userId;  
        const role = decodedToken.roles?.[0];

        const userResponse = await axios.get(
          `http://localhost:8080/users/${userId}`,
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );

        setHrName(userResponse.data || "HR Admin");

        const leaveResponse = await axios.get(
          `http://localhost:8080/hr/${userId}`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        if (leaveResponse.data) {
          setLeaveRequests(leaveResponse.data);
        }

        fetchTeamMembers(userId, token, role);
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };

    fetchData();
  }, []);


  // Fetch again on page change or page size change
  useEffect(() => {
    const fetchData = async () => {
      const token = localStorage.getItem("Authorization");
      const decodedToken = jwtDecode(token);
      const userId = decodedToken.userId;  
      const role = decodedToken.roles?.[0];
  
      await fetchTeamMembers(userId, token, role);
    };
  
    fetchData();
  }, [currentPage, pageSize]);

  
  // Fetch on search query change
  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      const token = localStorage.getItem("Authorization");
      const decodedToken = jwtDecode(token);
      const userId = decodedToken.userId;
      const role = decodedToken.roles?.[0];
  
      fetchTeamMembers(userId, token, role, searchQuery.trim() || "%20");
    }, 1000);
  
    return () => clearTimeout(delayDebounceFn);
  }, [searchQuery]);
  
  

  // Fetch team members
  const fetchTeamMembers = async (userId, token, role, search = "%20") => {
    setLoading(true);
    try {
      const response = await axios.get(
        `http://localhost:8080/hr/displayUsers/${userId}/${search.trim() || "%20"}`,
        {
          headers: { Authorization: `Bearer ${token}` },
          params: {
            page: currentPage,
            limit: pageSize,
          },
        }
      );
  
      if (response.data) {
        const formattedEmployees = response.data.map((employee) => ({
          id: employee.userId,
          email: employee.email,
          name: employee.username,
        }));
  
        setTeamMembers(formattedEmployees);
      }
    } catch (error) {
      console.error("Error fetching team members:", error);
    } finally {
      setLoading(false);
    }
  };
  

  // Fetch total pages for pagination
  useEffect(() => {
    const fetchTotalPages = async () => {
      try {
        const token = localStorage.getItem("Authorization");
        const decodedToken = jwtDecode(token);
        const userId = decodedToken.userId;
        
        const response = await axios.get(
          `http://localhost:8080/hr/displayUsers/count/${userId}/${searchQuery.trim() || "%20"}`,
          {
            headers: { Authorization: `Bearer ${token}` },
            params: { 
              limit: pageSize
            },
          }
        );
  
        if (response.data) {
          setTotalPages(response.data.totalPages);
        }
      } catch (error) {
        console.error("Error fetching total pages:", error);
      }
    };
  
    const delayDebounceFn = setTimeout(() => {
      fetchTotalPages();
    }, 1000);
  
    return () => clearTimeout(delayDebounceFn);
  }, [pageSize, searchQuery]); 
  
  

  const getFilteredLeaveRequests = () => {
    if (
      leaveRequests &&
      typeof leaveRequests === "object" &&
      !Array.isArray(leaveRequests)
    ) {
      if (!monthFilter && !employeeFilter) return leaveRequests;

      const filteredRequests = {};
      Object.entries(leaveRequests).forEach(([employeeName, requests]) => {
        if (
          employeeFilter &&
          !employeeName.toLowerCase().includes(employeeFilter.toLowerCase())
        ) {
          return;
        }

        const filteredEmployeeRequests = {};
        Object.entries(requests).forEach(([date, status]) => {
          const requestDate = new Date(date);
          if (
            !monthFilter ||
            requestDate.getMonth() + 1 === parseInt(monthFilter)
          ) {
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

  const handlePrevPage = () => {
    if (currentPage > 1) {
      setCurrentPage((prev) => prev - 1);
    }
  };
  
  const handleNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage((prev) => prev + 1);
    }
  };  

  const handlePageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setCurrentPage(1);
  };

  const handleCreateUser = () => {
    navigate("/hr/create-user");
  };

  const handleUpdateUser = () => {
    navigate("/hr/update-user");
  };

  const handleUpdatePassword = () => {
    navigate("/users/update-password");
  };

  return (
    <div className="min-h-screen bg-gray-50 text-gray-800">

      <Navbar hrName={hrName} />

      <div className="container mx-auto px-4 py-6">

        <QuickActions 
          onCreateUser={handleCreateUser} 
          onUpdatePassword={handleUpdatePassword} 
        />

        <SearchFilters
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
        />

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            <div className="bg-indigo-50 border-b border-gray-200 px-4 py-3 flex justify-between items-center">
              <h3 className="text-lg font-medium text-indigo-800">Leave Requests</h3>
              <span className="bg-indigo-100 text-indigo-800 text-xs font-medium px-2.5 py-0.5 rounded-full">
                {Object.keys(getFilteredLeaveRequests()).length} Employees
              </span>
            </div>
            <div className="p-4 max-h-96 overflow-y-auto">
              {Object.keys(getFilteredLeaveRequests()).length > 0 ? (
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


          <div className="bg-white rounded-lg shadow-sm overflow-hidden">
            <div className="bg-indigo-50 border-b border-gray-200 px-4 py-3 flex justify-between items-center">
            <h3 className="text-lg font-medium text-indigo-800">Team Members</h3>
            <div className="flex items-center">
                <select
                value={pageSize}
                onChange={handlePageSizeChange}
                className="mr-2 text-sm border-gray-300 rounded-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                >
                <option value="4">4 per page</option>
                <option value="8">8 per page</option>
                <option value="12">12 per page</option>
                </select>
                <span className="text-xs font-medium text-gray-600">
                {teamMembers.length > 0 ? 
                    `Showing ${(currentPage - 1) * pageSize + 1}-${Math.min(currentPage * pageSize, totalPages * pageSize)}` : 
                    "No results"}
                </span>
            </div>
            </div>
            <div className="p-4 max-h-96 overflow-y-auto">
            {loading ? (
                <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-700"></div>
                </div>
            ) : teamMembers.length > 0 ? (
                <div>
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
            ) : (
                <div className="text-center py-8 text-gray-500">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 mx-auto text-gray-400 mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
                <p>No team members found</p>
                </div>
            )}
            </div>
            {teamMembers.length > 0 && (
            <div className="bg-gray-50 px-4 py-3 border-t border-gray-200 flex items-center justify-between">
                <div className="flex-1 flex justify-between sm:hidden">
                <button
                    onClick={handlePrevPage}
                    disabled={currentPage === 1}
                    className={`relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md ${
                    currentPage === 1 ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white text-gray-700 hover:bg-gray-50'
                    }`}
                >
                    Previous
                </button>
                <button
                    onClick={handleNextPage}
                    disabled={currentPage === totalPages}
                    className={`ml-3 relative inline-flex items-center px-4 py-2 border border-gray-300 text-sm font-medium rounded-md ${
                    currentPage === totalPages ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white text-gray-700 hover:bg-gray-50'
                    }`}
                >
                    Next
                </button>
                </div>
                <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                <div>
                    <p className="text-sm text-gray-700">
                    Showing <span className="font-medium">{(currentPage - 1) * pageSize + 1}</span> to{" "}
                    <span className="font-medium">{Math.min(currentPage * pageSize, totalPages * pageSize)}</span> of{" "}
                    <span className="font-medium">{totalPages * pageSize}</span> results
                    </p>
                </div>
                <div>
                    <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                    <button
                        onClick={handlePrevPage}
                        disabled={currentPage === 1}
                        className={`relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 text-sm font-medium ${
                        currentPage === 1 ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white text-gray-500 hover:bg-gray-50'
                        }`}
                    >
                        <span className="sr-only">Previous</span>
                        <svg className="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                        <path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
                        </svg>
                    </button>
                    {Array.from({ length: totalPages }, (_, i) => i + 1).map((page) => (
                        <button
                        key={page}
                        onClick={() => setCurrentPage(page)}
                        className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                            page === currentPage
                            ? 'z-10 bg-indigo-50 border-indigo-500 text-indigo-600'
                            : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                        }`}
                        >
                        {page}
                        </button>
                    ))}
                    <button
                        onClick={handleNextPage}
                        disabled={currentPage === totalPages}
                        className={`relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 text-sm font-medium ${
                        currentPage === totalPages ? 'bg-gray-100 text-gray-400 cursor-not-allowed' : 'bg-white text-gray-500 hover:bg-gray-50'
                        }`}
                    >
                        <span className="sr-only">Next</span>
                        <svg className="h-5 w-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                        <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                        </svg>
                    </button>
                    </nav>
                </div>
                </div>
            </div>
            )}
        </div>
        </div>

        <Footer />
      </div>
    </div>
  );
};

export default HRDashboard;



