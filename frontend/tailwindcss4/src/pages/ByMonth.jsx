import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import { jwtDecode } from "jwt-decode";

const ByMonth = () => {
  const [reportData, setReportData] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const location = useLocation();
  const { userid, month } = useParams();
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(5); 
  const [totalPages, setTotalPages] = useState(1);

  // Convert the month-year format from the URL (Mar-2025) to the format needed by the API (YYYY-MM)
  const getFormattedMonthYear = () => {
    if (!month) return "";

    const [monthName, year] = month.split("-");

    // Get month number from name (Mar -> 03)
    const monthMap = {
      Jan: "01", Feb: "02", Mar: "03", Apr: "04", May: "05", Jun: "06",
      Jul: "07", Aug: "08", Sep: "09", Oct: "10", Nov: "11", Dec: "12",
    };

    const monthNum = monthMap[monthName];

    if (!monthNum || !year) return "";

    return `${year}-${monthNum}`;
  };

  // Get month number from month name
  const getMonthNumber = (monthName) => {
    const monthMap = {
      Jan: 0, Feb: 1, Mar: 2, Apr: 3, May: 4, Jun: 5,
      Jul: 6, Aug: 7, Sep: 8, Oct: 9, Nov: 10, Dec: 11,
    };
    return monthMap[monthName];
  };

  // Format date to the format we need for URL (Mon-YYYY)
  const formatMonthForUrl = (date) => {
    const monthNames = [
      "Jan", "Feb", "Mar", "Apr", "May", "Jun",
      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    ];
    return `${monthNames[date.getMonth()]}-${date.getFullYear()}`;
  };

  // Initialize selectedMonth state from URL params
  const [selectedMonth, setSelectedMonth] = useState(month || formatMonthForUrl(new Date()));
  const [data, setData] = useState(null); // Store fetched data

  const API_BASE_URL = "http://localhost:8080"; // Ensure this matches your backend

  // Main data fetching function
  const fetchData = async () => {
    setLoading(true);
    setError("");
    
    try {
      const token = localStorage.getItem("Authorization");
      if (!token) {
        console.error("No token found, redirecting to login.");
        navigate("/login");
        return;
      }
  
      const decodedToken = jwtDecode(token);
      const role = decodedToken.roles?.[0];
      const userId = decodedToken.userId;
  
      if (!role) {
        console.error("No role found, redirecting to login.");
        navigate("/login");
        return;
      }
  
      // Convert selectedMonth format (e.g., Feb-2025 ➝ 2025-02)
      const [monthStr, year] = selectedMonth.split("-");
      const monthMap = {
        Jan: "01", Feb: "02", Mar: "03", Apr: "04", May: "05", Jun: "06",
        Jul: "07", Aug: "08", Sep: "09", Oct: "10", Nov: "11", Dec: "12"
      };
      const formattedMonthYear = `${year}-${monthMap[monthStr]}`;
      
      console.log("Fetching data for:", formattedMonthYear);
      console.log("Current page:", currentPage);
      console.log("Page size:", pageSize);
  
      // Build the URL with proper pagination parameters
      let url = "";
      if (role === "ROLE_HR") {
        url = `${API_BASE_URL}/hr/bymonth?monthYear=${formattedMonthYear}&page=${currentPage}&limit=${pageSize}`;
      } else if (role === "ROLE_MANAGER") {
        url = `${API_BASE_URL}/manager/bymonth?monthYear=${formattedMonthYear}&userId=${userId}&page=${currentPage}&limit=${pageSize}`;
      }
  
      console.log("Fetching data from:", url);
  
      const response = await fetch(url, {
        headers: { 
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        }
      });
  
      if (response.status === 401 || response.status === 403) {
        console.error("Unauthorized or Forbidden - Redirecting to login.");
        navigate("/login");
        return;
      }
  
      if (!response.ok) {
        const errorText = await response.text();
        console.error("API Error:", errorText);
        throw new Error("Failed to fetch data");
      }
  
      const result = await response.json();
      console.log("Fetched Data:", result);
  
      if (result && Object.keys(result).length > 0) {
        setReportData(result.reportData);
        setData(result);
        
        // Set pagination information
        if (result.totalPages) {
          setTotalPages(result.totalPages);
        } else {
          setTotalPages(Math.ceil(result.totalRecords / pageSize) || 1);
        }
        
        // Update current page if it's provided in the response
        if (result.currentPage) {
          setCurrentPage(result.currentPage);
        }
      } else {
        console.warn("No data found for the specified month and year.");
        setError("No data found for the specified month and year.");
      }
    } catch (error) {
      console.error("Error fetching data:", error);
      setError("Error fetching data. Please try again later.");
    } finally {
      setLoading(false);
    }
  };
  
  // Main useEffect - fetch data when dependencies change
  useEffect(() => {
    fetchData();
  }, [selectedMonth, currentPage, pageSize]);

  // Update selectedMonth when URL params change
  useEffect(() => {
    if (month) {
      setSelectedMonth(month);
      setCurrentPage(1); // Reset to first page when month changes
    }
  }, [month]);

  // Debug useEffect - log data changes
  useEffect(() => {
    console.log("Updated Data in State:", data);
  }, [data]);

  const navigateToMonth = (direction) => {
    const [monthName, yearStr] = selectedMonth.split("-");
    const year = parseInt(yearStr);
    const monthNum = getMonthNumber(monthName);
    
    let newDate;
    if (direction === "prev") {
      newDate = new Date(year, monthNum - 1, 1);
    } else {
      newDate = new Date(year, monthNum + 1, 1);
    }

    const newMonthYear = formatMonthForUrl(newDate);
    setSelectedMonth(newMonthYear);
    setCurrentPage(1); // Reset to first page when changing month
    navigate(generatePath(newMonthYear));
  };

  // Update the jump to month function
  const jumpToMonth = (event) => {
    const selectedMonthYear = event.target.value; // Format: 2025-03
    const [year, monthNum] = selectedMonthYear.split("-");
    const monthNames = [
      "Jan", "Feb", "Mar", "Apr", "May", "Jun",
      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    ];
    const monthName = monthNames[parseInt(monthNum) - 1];
    
    const newMonthYear = `${monthName}-${year}`;
    setSelectedMonth(newMonthYear);
    setCurrentPage(1); // Reset to first page when changing month
    navigate(generatePath(newMonthYear));
  };

  const generatePath = (newMonthYear) => {
    const token = localStorage.getItem("Authorization");
    const decodedToken = jwtDecode(token);
    const role = decodedToken.roles?.[0];
    const userId = decodedToken.userId;

    if (!token) {
      console.error("No token found, redirecting to login.");
      navigate("/login");
      return;
    }
  
    if (!role) {
      console.error("No role found, redirecting to login.");
      navigate("/login");
      return;
    }
    
    return role === "ROLE_HR"
      ? `/hr/monthly/${userId}/${newMonthYear}`
      : `/manager/monthly/${userId}/${newMonthYear}`;
  };

  const handleBackButton = () => {
    const token = localStorage.getItem("Authorization");
    const decodedToken = jwtDecode(token);
    const userId = decodedToken.userId;  
    const roles = decodedToken.roles; 

    if (roles.includes("ROLE_HR")) {
      navigate("/hr");
    } else if (roles.includes("ROLE_EMPLOYEE")) {
      navigate("/employee");
    } else {
      navigate("/manager");
    }
  };

  // Pagination handlers
  const handlePrevPage = () => {
    if (currentPage > 1) {
      setCurrentPage(prevPage => prevPage - 1);
    }
  };
  
  const handleNextPage = () => {
    if (currentPage < totalPages) {
      setCurrentPage(prevPage => prevPage + 1);
    }
  };
  
  const handlePageSizeChange = (e) => {
    setPageSize(parseInt(e.target.value));
    setCurrentPage(1); // Reset to first page when changing page size
  };

  const getAttendanceColor = (code) => {
    switch (code) {
      case "P": return "#4CAF50"; // Planned Leave
      case "U": return "#F44336"; // Unplanned Leave
      case "P*": return "#8BC34A"; // Planned Leave (Second Half)
      case "S": return "#FF9800"; // Sick Leave
      case "W": return "#2196F3"; // Work From Home
      case "T": return "#9C27B0"; // Travelling to HQ
      case "H": return "#E91E63"; // Holiday
      case "E": return "#607D8B"; // Elections
      case "J": return "#00BCD4"; // Joined
      case "P**": return "#CDDC39"; // Planned Leave (First Half)
      default: return "#EEEEEE"; // Default/Empty
    }
  };

  const getAttendanceTooltip = (code) => {
    switch (code) {
      case "P": return "Planned Leave";
      case "U": return "Unplanned Leave";
      case "P*": return "Planned Leave (Second Half)";
      case "S": return "Sick Leave";
      case "W": return "Work From Home";
      case "T": return "Travelling to HQ";
      case "H": return "Holiday";
      case "E": return "Elections";
      case "J": return "Joined";
      case "P**": return "Planned Leave (First Half)";
      default: return "";
    }
  };

  // Add this function to handle the report generation and download
  const handleGenerateReport = async () => {
    try {
      const token = localStorage.getItem("Authorization");
      if (!token) {
        navigate("/login");
        return;
      }
  
      // Decode the token to get role and userId
      const decodedToken = jwtDecode(token);
      const role = decodedToken.roles?.[0]; // Get the user's role (ROLE_HR or ROLE_MANAGER)
      const userId = decodedToken.userId; // Get the user's ID
  
      // Get the formatted month-year
      const formattedMonthYear = getFormattedMonthYear();
      if (!formattedMonthYear) {
        setError("Invalid month format. Please select a valid month.");
        return;
      }
  
      // Dynamically generate the URL based on the role
      let url;
      if (role === "ROLE_HR") {
        url = `http://localhost:8080/hr/bymonthreport?monthYear=${formattedMonthYear}`;
      } else if (role === "ROLE_MANAGER") {
        url = `http://localhost:8080/manager/bymonthreport?monthYear=${formattedMonthYear}&userId=${userId}`;
      } else {
        setError("Unauthorized role. You do not have permission to generate reports.");
        return;
      }
  
      // Make the API request
      const response = await axios.get(url, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        responseType: "blob", // Ensure this is set to "blob"
      });
  
      // Create a URL for the blob
      const urlObject = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = urlObject;
      link.setAttribute("download", `Attendance_Report_${formattedMonthYear}.xlsx`);
      document.body.appendChild(link);
  
      // Trigger the download
      link.click();
  
      // Clean up
      link.remove();
      window.URL.revokeObjectURL(urlObject);
    } catch (error) {
      console.error("Error generating report:", error);
      setError("Failed to generate report. Please try again.");
    }
  };

  const generateCalendar = () => {
    if (!reportData) return null;

    const [year, monthNum] = getFormattedMonthYear().split("-");
    const month = parseInt(monthNum);
    const daysInMonth = new Date(year, month, 0).getDate();

    // Get days of week for column headers
    const weekdays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

    // Generate dates in the format used by the API
    const dates = Array.from({ length: daysInMonth }, (_, i) => {
      const date = new Date(year, month - 1, i + 1);
      return date
        .toLocaleDateString("en-US", { month: "short", day: "2-digit" })
        .replace(" ", "-");
    });

    // Get day of week for each date to show weekends differently
    const dayOfWeeks = dates.map((_, i) => {
      const date = new Date(year, month - 1, i + 1);
      return date.getDay(); // 0 for Sunday, 6 for Saturday
    });

    const monthName = new Date(year, month - 1, 1).toLocaleString("default", {
      month: "long",
    });

    return (
      <div className="bg-white rounded-lg shadow flex flex-col h-full overflow-hidden">
        <div className="bg-blue-50 p-6 border-b border-blue-100 flex-shrink-0">
          <h2 className="text-2xl font-bold text-blue-700 mb-3">
            {monthName} {year} Attendance
          </h2>
          
          <div className="mt-3">
            <h4 className="text-sm font-semibold text-blue-500 mb-2">Legend:</h4>
            <div className="flex flex-wrap gap-3">
              {[
                { code: "P", label: "Planned Leave" },
                { code: "U", label: "Unplanned Leave" },
                { code: "P*", label: "Planned Leave (Second Half)" },
                { code: "S", label: "Sick Leave" },
                { code: "W", label: "Work From Home" },
                { code: "T", label: "Travelling to HQ" },
                { code: "H", label: "Holiday" },
                { code: "E", label: "Elections" },
                { code: "J", label: "Joined" },
                { code: "P**", label: "Planned Leave (First Half)" },
              ].map((item) => (
                <div
                  key={item.code}
                  className="flex items-center gap-2 text-xs bg-white px-2 py-1 rounded-md shadow-sm"
                >
                  <span
                    className="inline-block w-4 h-4 rounded-md shadow-sm"
                    style={{ backgroundColor: getAttendanceColor(item.code) }}
                  ></span>
                  <span className="font-semibold">{item.code}</span>
                  <span className="text-gray-700">{item.label}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="flex-1 overflow-auto">
          <table className="w-full text-sm border-collapse">
            <thead>
              <tr>
                <th className="sticky top-0 left-0 bg-blue-500 text-white z-30 text-left py-4 px-4 font-semibold border border-blue-700">
                  User Name
                </th>
                {dates.map((date, index) => (
                  <th
                    key={date}
                    className={`sticky top-0 z-20 text-center py-3 px-2 min-w-[60px] font-semibold border border-blue-700 ${
                      [0, 6].includes(dayOfWeeks[index])
                        ? "bg-blue-600 text-white"
                        : "bg-blue-500 text-white"
                    }`}
                  >
                    <div className="font-semibold">{date.split("-")[1]}</div>
                    <div className="text-xs opacity-80 mt-1">
                      {weekdays[dayOfWeeks[index]]}
                    </div>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {Object.entries(reportData).map(([username, attendance], rowIndex) => (
                <tr key={username} className="hover:bg-blue-50 transition-colors duration-150">
                  <td className="sticky left-0 bg-white z-10 text-left py-4 px-4 font-medium min-w-[180px] max-w-[250px] whitespace-nowrap overflow-hidden text-ellipsis border border-gray-300 shadow-sm">
                    {username}
                  </td>
                  {dates.map((date, index) => {
                    const attendanceCode = attendance[date] || "";
                    return (
                      <td
                        key={date}
                        className={`text-center py-4 px-2 font-semibold cursor-default transition-all duration-150 hover:scale-105 hover:z-20 hover:shadow-lg border border-gray-300 ${
                          [0, 6].includes(dayOfWeeks[index])
                            ? "bg-gray-50"
                            : ""
                        }`}
                        style={{
                          backgroundColor: attendanceCode
                            ? getAttendanceColor(attendanceCode)
                            : "",
                        }}
                        title={getAttendanceTooltip(attendanceCode)}
                      >
                        {attendanceCode}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };
  
  // Generate a list of months for the dropdown (current year and previous year)
  const generateMonthOptions = () => {
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear();
    const options = [];
  
    // Add months for current year and previous year
    for (let year = currentYear; year >= currentYear - 1; year--) {
      for (let month = 12; month >= 1; month--) {
        const monthStr = month < 10 ? `0${month}` : `${month}`;
        options.push(
          <option key={`${year}-${monthStr}`} value={`${year}-${monthStr}`}>
            {new Date(year, month - 1, 1).toLocaleString("default", {
              month: "long",
              year: "numeric",
            })}
          </option>
        );
      }
    }
  
    return options;
  };
  
  return (
    <>
      <div className="font-sans text-gray-800 w-full h-screen flex flex-col bg-gray-50 overflow-hidden">
        <div className="flex flex-col h-full">
          {/* Fixed header */}
          <header className="bg-blue-500 text-white shadow-lg p-4 z-40">
            <div className="flex justify-between items-center max-w-screen-2xl mx-auto">
              <div className="flex items-center space-x-4">
                <h1 className="text-xl font-bold">Attendance Report System</h1>
              </div>
              
              <button
                onClick={handleBackButton}
                className="py-2 px-4 bg-blue-300 text-white rounded-lg text-sm font-medium cursor-pointer transition-colors hover:bg-blue-600 flex items-center gap-2"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                </svg>
                Back
              </button>
            </div>
          </header>
  
          {/* Month navigation */}
          <div className="bg-white border-b border-gray-200 py-3 px-4 shadow-sm z-30">
            <div className="flex justify-between items-center max-w-screen-2xl mx-auto">
              <div className="text-lg text-gray-700 font-semibold">
                Monthly Attendance View
              </div>
  
              <div className="flex items-center gap-3">
                <button
                  onClick={() => navigateToMonth("prev")}
                  className="py-2 px-4 bg-blue-50 border border-blue-200 text-blue-500 rounded-lg text-sm font-medium cursor-pointer transition-colors hover:bg-blue-100 flex items-center"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-4 w-4 mr-1"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M15 19l-7-7 7-7"
                    />
                  </svg>
                  Previous
                </button>
  
                <select
                  className="py-2 px-4 bg-white border border-blue-300 text-blue-700 rounded-lg text-sm font-medium cursor-pointer focus:ring-2 focus:ring-blue-300 focus:border-blue-500 focus:outline-none"
                  value={getFormattedMonthYear()}
                  onChange={jumpToMonth}
                >
                  {generateMonthOptions()}
                </select>
  
                <button
                  onClick={() => navigateToMonth("next")}
                  className="py-2 px-4 bg-blue-50 border border-blue-200 text-blue-500 rounded-lg text-sm font-medium cursor-pointer transition-colors hover:bg-blue-100 flex items-center"
                >
                  Next
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-4 w-4 ml-1"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M9 5l7 7-7 7"
                    />
                  </svg>
                </button>
                <button
                onClick={handleGenerateReport}
                className="py-2 px-4 bg-green-500 text-white rounded-lg text-sm font-medium cursor-pointer transition-colors hover:bg-green-600 flex items-center gap-2"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-4 w-4"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                  />
                </svg>
                Generate Report
              </button>
              </div>
            </div>
          </div>
  
          {/* Main content */}
          <div className="flex-1 p-4 overflow-hidden">
            <div className="h-full max-w-screen-2xl mx-auto">
              {error && (
                <div className="text-red-500 bg-red-50 border border-red-200 py-3 px-4 rounded-lg text-sm mb-4 flex items-center">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                  </svg>
                  {error}
                </div>
              )}
  
              {loading ? (
                <div className="h-full flex items-center justify-center">
                  <div className="text-center py-16 text-gray-500 flex flex-col items-center">
                    <svg className="animate-spin h-10 w-10 text-blue-500 mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <span className="text-lg font-medium">Loading attendance data...</span>
                  </div>
                </div>
              ) : reportData ? (
                <div className="h-full overflow-hidden border border-gray-300 rounded-lg">{generateCalendar()}</div>
              ) : (
                <div className="h-full flex items-center justify-center">
                  <div className="text-center py-16 text-gray-500 flex flex-col items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 text-gray-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span className="text-lg font-medium">No attendance data found.</span>
                    <p className="mt-2 text-gray-400">Try selecting a different month or check user ID.</p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
      
      {/* Pagination controls - styled to match HRDashboard */}
      {!loading && reportData && (
        <div className="max-w-screen-2xl mx-auto mb-6 px-4">
          <div className="bg-white rounded-lg shadow-sm p-4 border border-gray-200">
            <div className="flex flex-col sm:flex-row justify-between items-center gap-3">
              <div className="flex items-center">
                <span className="text-sm text-gray-600 mr-2">Records per page:</span>
                <select 
                  className="border border-gray-300 rounded py-1 px-2 text-sm"
                  value={pageSize}
                  onChange={handlePageSizeChange}
                >
                  <option value="2">2</option>
                  <option value="5">5</option>
                  <option value="10">10</option>
                  <option value="20">20</option>
                </select>
              </div>
              
              <div className="flex items-center gap-2">
                <button
                  onClick={handlePrevPage}
                  disabled={currentPage === 1}
                  className={`px-3 py-1 rounded-md flex items-center ${
                    currentPage === 1 
                      ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                      : 'bg-blue-50 text-blue-600 hover:bg-blue-100'
                  }`}
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                  </svg>
                  Previous
                </button>
                
                <div className="px-3 py-1 text-sm bg-gray-50 rounded-md border border-gray-200">
                  Page <span className="font-medium">{currentPage}</span> of <span className="font-medium">{totalPages}</span>
                </div>
                
                <button
                  onClick={handleNextPage}
                  disabled={currentPage >= totalPages}
                  className={`px-3 py-1 rounded-md flex items-center ${
                    currentPage >= totalPages 
                      ? 'bg-gray-100 text-gray-400 cursor-not-allowed' 
                      : 'bg-blue-50 text-blue-600 hover:bg-blue-100'
                  }`}
                >
                  Next
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default ByMonth;
