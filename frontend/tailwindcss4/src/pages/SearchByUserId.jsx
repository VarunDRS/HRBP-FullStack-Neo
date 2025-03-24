import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { format } from "date-fns";

const EnhancedCalendarView = () => {
  const { userId, month: routeMonth } = useParams();
  const navigate = useNavigate();
  const [attendanceData, setAttendanceData] = useState(null);
  const [error, setError] = useState(null);
  const [currentMonth, setCurrentMonth] = useState(routeMonth || "Mar-2025"); // Default value if no route param
  const [isLoading, setIsLoading] = useState(true);
  const [employeeName, setEmployeeName] = useState("");
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteDate, setDeleteDate] = useState("");
  const [showMonthDropdown, setShowMonthDropdown] = useState(false);
  const [message, setMessage] = useState("");


  const [reportReady, setReportReady] = useState(
    JSON.parse(localStorage.getItem("reportReady")) || {}
  );

  const handleGenerateReport = (userId) => {
    generateReport(userId);
  };
  
  const handleDownloadReport = (userId) => {
    downloadReport(userId);
  };

  const [fromMonth, setFromMonth] = useState("");
  const [toMonth, setToMonth] = useState("");
  const [months, setMonths] = useState([]);

  const token = localStorage.getItem("Authorization");
  const decodedToken = jwtDecode(token);
  const userRole = decodedToken.roles?.[0];
  const [showFromMonthDropdown, setShowFromMonthDropdown] = useState(false);
  const [showToMonthDropdown, setShowToMonthDropdown] = useState(false);

  useEffect(() => {
    const now = new Date();
    const generatedMonths = [];

    for (let i = 0; i < 24; i++) {
      const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const formattedMonth = date.toISOString().slice(0, 7); // "YYYY-MM"
      generatedMonths.push(formattedMonth);
    }

    generatedMonths.reverse(); // Ensure months are in ascending order

    setMonths(generatedMonths);

    // Ensure current month is in the list
    const currentMonth = now.toISOString().slice(0, 7);
    if (!generatedMonths.includes(currentMonth)) {
      generatedMonths.push(currentMonth);
    }

    setFromMonth(currentMonth);
    setToMonth(currentMonth);
  }, []);

  // Function to handle the delete operation
  const handleDeleteEntry = async () => {
    try {
      const response = await fetch(
        `http://localhost:8080/hr/deleteEntry/${userId}/${deleteDate}`,
        {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (response.ok) {
        toast.success("Entry deleted successfully");
        fetchAttendanceData(currentMonth); // Assuming you have a function to fetch attendance data
      } else {
        toast.error("No such entry exists for the user");
      }
    } catch (error) {
      console.log("Error response:", error.response);

      if (error.response && error.response.data) {
        const { statuscode, message, data } = error.response.data;

        if (statuscode === 400) {
          toast.error(message);
        } else {
          toast.error("Unexpected error occurred: " + message);
        }
      } else if (error.message) {
        toast.error("Error: " + error.message);
      } else {
        toast.error("An unexpected error occurred. Please try again.");
      }
    }

    setShowDeleteModal(false);
    setDeleteDate("");
  };

  // Function to fetch attendance data
  const fetchAttendanceData = (month) => {
    setIsLoading(true);
    const token = localStorage.getItem("Authorization");

    if (!token) {
      console.error("No token found, redirecting to login.");
      navigate("/login");
      return;
    }

    try {
      const decodedToken = jwtDecode(token);
      const role = decodedToken.roles?.[0];
      const roleEndpoints = {
        ROLE_HR: "hr",
        ROLE_MANAGER: "manager",
        ROLE_EMPLOYEE: "employee",
      };

      // Determine API path based on role
      const rolePath = roleEndpoints[role] || "employee"; // Default to "employee" if role is undefined
      const apiUrl = `http://localhost:8080/${rolePath}/${userId}/${month}`;

      fetch(apiUrl, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
        .then((response) => response.json())
        .then((data) => {
          setAttendanceData(data);
          if (data && Object.keys(data).length > 0) {
            setEmployeeName(Object.keys(data)[0]);
          }
          setIsLoading(false);
        })
        .catch((error) => {
          console.error("Error fetching attendance data:", error);
          setError("Failed to load attendance data.");
          setIsLoading(false);
        });
    } catch (error) {
      console.error("Error decoding token:", error);
      navigate("/login");
    }
  };

  const handleGraphClick = () => {
    const token = localStorage.getItem("Authorization");
    const decodedToken = jwtDecode(token);
    const role = decodedToken.roles?.[0];

    console.log("hiiiiii" + userId);

    if (role === "ROLE_HR") navigate(`/hr/graph/${userId}/${currentMonth}`);
    else if (role === "ROLE_MANAGER")
      navigate(`/manager/graph/${userId}/${currentMonth}`);
    else navigate(`/employee/graph/${userId}/${currentMonth}`);
  };

  useEffect(() => {
    fetchAttendanceData(currentMonth);
  }, [userId, currentMonth, navigate]);

  // Function to get days in month
  const getDaysInMonth = (monthStr) => {
    const [month, year] = monthStr.split("-");
    const monthMap = {
      Jan: 0,
      Feb: 1,
      Mar: 2,
      Apr: 3,
      May: 4,
      Jun: 5,
      Jul: 6,
      Aug: 7,
      Sep: 8,
      Oct: 9,
      Nov: 10,
      Dec: 11,
    };
    const monthIndex = monthMap[month];
    const yearNum = parseInt(year);
    return new Date(yearNum, monthIndex + 1, 0).getDate();
  };

  // Function to get first day of month (0 = Sunday, 1 = Monday, etc.)
  const getFirstDayOfMonth = (monthStr) => {
    const [month, year] = monthStr.split("-");
    const monthMap = {
      Jan: 0,
      Feb: 1,
      Mar: 2,
      Apr: 3,
      May: 4,
      Jun: 5,
      Jul: 6,
      Aug: 7,
      Sep: 8,
      Oct: 9,
      Nov: 10,
      Dec: 11,
    };
    const monthIndex = monthMap[month];
    const yearNum = parseInt(year);
    return new Date(yearNum, monthIndex, 1).getDay();
  };

  // Function to check if a date is a weekend
  const isWeekend = (day, monthStr) => {
    const [month, year] = monthStr.split("-");
    const monthMap = {
      Jan: 0,
      Feb: 1,
      Mar: 2,
      Apr: 3,
      May: 4,
      Jun: 5,
      Jul: 6,
      Aug: 7,
      Sep: 8,
      Oct: 9,
      Nov: 10,
      Dec: 11,
    };
    const monthIndex = monthMap[month];
    const yearNum = parseInt(year);
    const date = new Date(yearNum, monthIndex, day);
    const dayOfWeek = date.getDay();
    return dayOfWeek === 0 || dayOfWeek === 6; // 0 is Sunday, 6 is Saturday
  };

  // Function to check if a date is today
  const isToday = (day, monthStr) => {
    const today = new Date();
    const [month, year] = monthStr.split("-");
    const monthMap = {
      Jan: 0,
      Feb: 1,
      Mar: 2,
      Apr: 3,
      May: 4,
      Jun: 5,
      Jul: 6,
      Aug: 7,
      Sep: 8,
      Oct: 9,
      Nov: 10,
      Dec: 11,
    };
    const monthIndex = monthMap[month];
    const yearNum = parseInt(year);

    return (
      today.getDate() === day &&
      today.getMonth() === monthIndex &&
      today.getFullYear() === yearNum
    );
  };

  // Function to change month
  const changeMonth = (increment) => {
    const [month, year] = currentMonth.split("-");
    const monthMap = {
      Jan: 0,
      Feb: 1,
      Mar: 2,
      Apr: 3,
      May: 4,
      Jun: 5,
      Jul: 6,
      Aug: 7,
      Sep: 8,
      Oct: 9,
      Nov: 10,
      Dec: 11,
    };
    const reverseMonthMap = [
      "Jan",
      "Feb",
      "Mar",
      "Apr",
      "May",
      "Jun",
      "Jul",
      "Aug",
      "Sep",
      "Oct",
      "Nov",
      "Dec",
    ];

    let monthIndex = monthMap[month];
    let yearNum = parseInt(year);

    monthIndex += increment;

    if (monthIndex > 11) {
      monthIndex = 0;
      yearNum += 1;
    } else if (monthIndex < 0) {
      monthIndex = 11;
      yearNum -= 1;
    }

    const newMonth = `${reverseMonthMap[monthIndex]}-${yearNum}`;
    setCurrentMonth(newMonth);

    // Get user role from token
    const token = localStorage.getItem("Authorization");
    if (!token) {
      console.error("No token found, redirecting to login.");
      navigate("/login");
      return;
    }

    try {
      const decodedToken = jwtDecode(token);
      const role = decodedToken.roles?.[0];

      const rolePaths = {
        ROLE_HR: "hr",
        ROLE_MANAGER: "manager",
        ROLE_EMPLOYEE: "employee",
      };

      const rolePath = rolePaths[role] || "employee"; // Default to employee if role is unknown
      navigate(`/${rolePath}/${userId}/${newMonth}`);
    } catch (error) {
      console.error("Error decoding token:", error);
      navigate("/login");
    }
  };

  const handleMonthSelection = (monthStr) => {
    setCurrentMonth(monthStr);

    const token = localStorage.getItem("Authorization");
    if (!token) {
      console.error("No token found, redirecting to login.");
      navigate("/login");
      return;
    }

    try {
      const decodedToken = jwtDecode(token);
      const role = decodedToken.roles?.[0];

      const rolePaths = {
        ROLE_HR: "hr",
        ROLE_MANAGER: "manager",
        ROLE_EMPLOYEE: "employee",
      };

      const rolePath = rolePaths[role] || "employee"; // Default to employee if role is unknown
      navigate(`/${rolePath}/${userId}/${monthStr}`);
      setShowMonthDropdown(false);
    } catch (error) {
      console.error("Error decoding token:", error);
      navigate("/login");
    }
  };

  const generateMonthOptions = () => {
    const monthNames = [
      "Jan",
      "Feb",
      "Mar",
      "Apr",
      "May",
      "Jun",
      "Jul",
      "Aug",
      "Sep",
      "Oct",
      "Nov",
      "Dec",
    ];
    const currentYear = new Date().getFullYear();
    const years = [currentYear - 1, currentYear, currentYear + 1];

    return years
      .map((year) => monthNames.map((month) => `${month}-${year}`))
      .flat();
  };

  // Sync state when URL changes
  useEffect(() => {
    if (routeMonth && routeMonth !== currentMonth) {
      setCurrentMonth(routeMonth);
    }
  }, [routeMonth, currentMonth]);

  // Function to format date for API
  const formatDateForApi = (day) => {
    const [month, year] = currentMonth.split("-");
    const paddedDay = day < 10 ? `0${day}` : day;
    return `${month}-${paddedDay}`;
  };

  // Status icons and labels
  const getStatusIcon = (status) => {
    const baseIconClass = "w-6 h-6 transition-all duration-300 ease-in-out";
    const textClass = "text-xs font-medium mt-1 transition-all duration-300";

    const statusConfig = {
      W: {
        icon: (
          <svg
            className={`${baseIconClass} text-blue-600 hover:text-blue-700`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
            ></path>
          </svg>
        ),
        label: "WFH",
        textColor: "text-blue-800",
      },
      P: {
        icon: (
          <svg
            className={`${baseIconClass} text-amber-600 hover:text-amber-700`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
            ></path>
          </svg>
        ),
        label: "PL",
        textColor: "text-amber-800",
      },
      "?": {
        icon: (
          <svg
            className={`${baseIconClass} text-orange-600 hover:text-orange-700`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            ></path>
          </svg>
        ),
        label: "UL",
        textColor: "text-orange-800",
      },
      "P**": {
        icon: (
          <svg
            className={`${baseIconClass} text-amber-600 hover:text-amber-700`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
            ></path>
          </svg>
        ),
        label: "PL²",
        textColor: "text-amber-800",
      },
      "P*": {
        icon: (
          <svg
            className={`${baseIconClass} text-amber-600 hover:text-amber-700`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
            ></path>
          </svg>
        ),
        label: "PL¹",
        textColor: "text-amber-800",
      },
      S: {
        icon: (
          <svg
            className={`${baseIconClass} text-purple-600 hover:text-purple-700`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
            ></path>
          </svg>
        ),
        label: "SL",
        textColor: "text-purple-800",
      },
      T: {
        icon: (
          <svg
            className={`${baseIconClass} text-indigo-600 hover:text-indigo-700`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"
            ></path>
          </svg>
        ),
        label: "HQ",
        textColor: "text-indigo-800",
      },
      H: {
        icon: (
          <svg
            className={`${baseIconClass} text-teal-600 hover:text-teal-700`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"
            ></path>
          </svg>
        ),
        label: "HOL",
        textColor: "text-teal-800",
      },
      E: {
        icon: (
          <svg
            className={`${baseIconClass} text-cyan-600 hover:text-cyan-700`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
            ></path>
          </svg>
        ),
        label: "EL",
        textColor: "text-cyan-800",
      },
      J: {
        icon: (
          <svg
            className={`${baseIconClass} text-lime-600 hover:text-lime-700`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"
            ></path>
          </svg>
        ),
        label: "JOIN",
        textColor: "text-lime-800",
      },
      Present: {
        icon: (
          <svg
            className={`${baseIconClass} text-green-600 hover:text-green-700`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
            ></path>
          </svg>
        ),
        label: "✓",
        textColor: "text-green-800",
      },
      A: {
        icon: (
          <svg
            className={`${baseIconClass} text-red-600 hover:text-red-700`}
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
            ></path>
          </svg>
        ),
        label: "ABS",
        textColor: "text-red-800",
      },
    };
    // Default/fallback icon
    const defaultConfig = {
      icon: (
        <svg
          className={`${baseIconClass} text-gray-600 hover:text-gray-700`}
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth="2"
            d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          ></path>
        </svg>
      ),
      label: "?",
      textColor: "text-gray-600",
    };

    // Normalize the status input
    const normalizedStatus = status.trim();

    const config = statusConfig[normalizedStatus] || defaultConfig;

    return (
      <div className="flex flex-col items-center justify-center group">
        <div className="transform group-hover:scale-110 transition-transform duration-200">
          {config.icon}
        </div>
        <div className={`${textClass} ${config.textColor} font-semibold`}>
          {config.label}
        </div>
      </div>
    );
  };

  // Generate the calendar
  const generateCalendar = () => {
    if (!attendanceData) return null;

    const daysInMonth = getDaysInMonth(currentMonth);
    const firstDay = getFirstDayOfMonth(currentMonth);
    const records = attendanceData[employeeName] || {};

    let cells = [];

    // Add empty cells for days before the first day of the month
    for (let i = 0; i < firstDay; i++) {
      cells.push(<div key={`empty-${i}`} className="h-20 bg-gray-50"></div>);
    }

    // Add cells for each day of the month
    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = formatDateForApi(day);
      const status = records[dateStr] || "";
      const weekend = isWeekend(day, currentMonth);
      const today = isToday(day, currentMonth);

      // Determine cell styles based on status and day type
      let cellClass = "relative h-20 p-0.5 border transition-all duration-200 ";
      let dayClass = "absolute top-1 right-2 text-xs ";
      let contentClass = "mt-3 text-center text-xs ";

      if (today) {
        cellClass += "ring-2 ring-blue-500 ";
        dayClass +=
          "font-bold bg-blue-500 text-white rounded-full w-5 h-5 flex items-center justify-center ";
      } else {
        dayClass += "font-medium text-black ";
      }

      if (weekend) {
        cellClass += "bg-gray-50 ";
        if (!today) dayClass += "text-gray-500 ";
      } else {
        cellClass += "bg-white ";
      }

      // Status-specific styling
      if (status === "WFH") {
        contentClass += "text-blue-800 ";
        if (!weekend) cellClass += "bg-blue-50 hover:bg-blue-100 ";
      } else if (
        status === "Planned Leave" ||
        status === "Planned Leave (First Half)" ||
        status === "Planned Leave (Second Half)"
      ) {
        contentClass += "text-amber-800 ";
        if (!weekend) cellClass += "bg-amber-50 hover:bg-amber-100 ";
      } else if (status === "Unplanned Leave") {
        contentClass += "text-orange-800 ";
        if (!weekend) cellClass += "bg-orange-50 hover:bg-orange-100 ";
      } else if (status === "Sick Leave") {
        contentClass += "text-purple-800 ";
        if (!weekend) cellClass += "bg-purple-50 hover:bg-purple-100 ";
      } else if (status === "Travelling to HQ") {
        contentClass += "text-indigo-800 ";
        if (!weekend) cellClass += "bg-indigo-50 hover:bg-indigo-100 ";
      } else if (status === "Holiday") {
        contentClass += "text-teal-800 ";
        if (!weekend) cellClass += "bg-teal-50 hover:bg-teal-100 ";
      } else if (status === "Elections") {
        contentClass += "text-cyan-800 ";
        if (!weekend) cellClass += "bg-cyan-50 hover:bg-cyan-100 ";
      } else if (status === "Joined") {
        contentClass += "text-lime-800 ";
        if (!weekend) cellClass += "bg-lime-50 hover:bg-lime-100 ";
      } else if (status === "Present") {
        contentClass += "text-green-800 ";
        if (!weekend) cellClass += "bg-green-50 hover:bg-green-100 ";
      } else if (status === "Absent") {
        contentClass += "text-red-800 ";
        if (!weekend) cellClass += "bg-red-50 hover:bg-red-100 ";
      } else {
        if (!weekend) cellClass += "hover:bg-gray-100 ";
        contentClass += "text-gray-400 ";
      }

      cellClass += "hover:shadow-sm ";

      cells.push(
        <div key={day} className={cellClass}>
          <div className={dayClass}>{day}</div>

          <div className={contentClass}>
            {status ? (
              <>
                {getStatusIcon(status)}
                <div className="text-[10px] font-medium mt-1">{status}</div>
              </>
            ) : weekend ? (
              <div className="mt-3 text-[10px] text-gray-400">Weekend</div>
            ) : (
              <div className="text-[10px] text-gray-400 mt-3">
                Regular Workday
              </div>
            )}
          </div>
        </div>
      );
    }

    return cells;
  };


  
  const generateReport = (userId) => {
    const token = localStorage.getItem("Authorization");
  
    if (!token) {
      console.error("No token found, redirecting to login.");
      navigate("/login");
      return;
    }
  
    const decodedToken = jwtDecode(token);
    const role = decodedToken.roles?.[0];
    const roleEndpoints = {
      ROLE_HR: "hr",
      ROLE_MANAGER: "manager",
      ROLE_EMPLOYEE: "employee",
    };
    const rolePath = roleEndpoints[role] || "employee";
  
    const formattedFromMonth = format(new Date(fromMonth), "MMM-yyyy");
    const formattedToMonth = format(new Date(toMonth), "MMM-yyyy");
  
    // Establish SSE connection
    const eventSource = new EventSource(
      `http://localhost:8080/${rolePath}/events/${userId}/${formattedFromMonth}/${formattedToMonth}`
    );
  
    eventSource.onmessage = (event) => {
      console.log("SSE Message:", event.data);
  
      if (event.data === "Generating Excel...") {
        toast.dismiss();
        const toastId = toast.info("Generating Excel...");
        setTimeout(() => {
          toast.dismiss(toastId);
        }, 1000);
      }
  
      if (event.data === "Error generating report") {
        toast.dismiss();
        toast.error("Error generating report", { duration: 3000 });
      }
  
      if (event.data === "Report Ready") {
        setReportReady((prev) => {
          const updatedReady = { ...prev, [userId]: true };
          localStorage.setItem("reportReady", JSON.stringify(updatedReady));
          return updatedReady;
        });
  
        toast.dismiss();
        toast(
          (t) => (
            <div className="flex items-center justify-between gap-4">
              <span className="text-black">Report is ready!</span>
              <button
                onClick={() => {
                  toast.dismiss(t.id);
                  downloadReport(userId); // Pass userId to download function
                }}
                className="px-3 py-1 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
              >
                Download
              </button>
            </div>
          ),
          {
            duration: Infinity,
            closeButton: true,
            autoClose: false,
            position: "top-right",
          }
        );
  
        eventSource.close();
      }
    };
  
    eventSource.onerror = (error) => {
      console.error("SSE Error:", error);
      eventSource.close();
    };
  
    // Close SSE connection after some time
    setTimeout(() => {
      eventSource.close();
    }, 10000);
  };
  
  const downloadReport = (userId) => {
    const token = localStorage.getItem("Authorization");
    const decodedToken = jwtDecode(token);
    const role = decodedToken.roles?.[0];
  
    const roleEndpoints = {
      ROLE_HR: "hr",
      ROLE_MANAGER: "manager",
      ROLE_EMPLOYEE: "employee",
    };
  
    const rolePath = roleEndpoints[role] || "employee";
  
    const formattedFromMonth = format(new Date(fromMonth), "MMM-yyyy");
    const formattedToMonth = format(new Date(toMonth), "MMM-yyyy");
  
    fetch(
      `http://localhost:8080/${rolePath}/download/${userId}/${formattedFromMonth}/${formattedToMonth}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    )
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to download file");
        }
  
        // Extract filename correctly
        const contentDisposition = response.headers.get("Content-Disposition");
        let filename = `Attendance_${userId}_from_${formattedFromMonth}_to_${formattedToMonth}.xlsx`; // Default if not found
  
        if (contentDisposition) {
          const match = contentDisposition.match(/filename="(.+?)"/);
          if (match && match[1]) {
            filename = match[1];
          }
        }
  
        return response.blob().then((blob) => ({ blob, filename }));
      })
      .then(({ blob, filename }) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
      })
      .catch((error) => {
        console.error("Error downloading report:", error);
        setMessage("Failed to download report.");
      });
  
    setReportReady((prev) => {
      const updatedReady = { ...prev, [userId]: false };
      localStorage.setItem("reportReady", JSON.stringify(updatedReady));
      return updatedReady;
    });
  };
  
  
  

  const handleBackButton = () => {
    const token = localStorage.getItem("Authorization");
    const decodedToken = jwtDecode(token);
    const roles = decodedToken.roles;

    if (roles.includes("ROLE_HR")) {
      navigate("/hr");
    } else if (roles.includes("ROLE_EMPLOYEE")) {
      navigate("/employee");
    } else {
      navigate("/manager");
    }
  };

  return (
    <div className="h-screen w-full flex flex-col bg-gray-100 overflow-hidden">
      {/* Header */}
      <div className="bg-gradient-to-r from-blue-700 to-blue-500 text-white py-4 px-6 shadow-lg">
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-bold">Attendance Dashboard</h1>
          <div className="flex flex-col text-right pl-200">
            <span className="font-medium text-lg">{employeeName}</span>
            <span className="text-blue-100 text-sm">Employee ID: {userId}</span>
          </div>
          <button
            onClick={handleBackButton}
            className="py-2 px-4 bg-blue-300 text-white rounded-lg text-sm font-medium cursor-pointer transition-colors hover:bg-blue-600 flex items-center gap-2"
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
                d="M10 19l-7-7m0 0l7-7m-7 7h18"
              />
            </svg>
            Back
          </button>
        </div>
      </div>

      {/* Main content area */}
      <div className="flex flex-1 overflow-hidden">
        {/* Calendar container - takes majority of screen */}
        <div className="flex-1 flex flex-col overflow-hidden p-4">
          {/* Month navigation and download button */}
          <div className="bg-white rounded-lg shadow-md p-4 mb-4 flex justify-between items-center">
            <div className="flex items-center space-x-2">
              <button
                onClick={() => changeMonth(-1)}
                className="p-2 rounded-full hover:bg-gray-100 focus:outline-none transition-colors"
                aria-label="Previous month"
              >
                <svg
                  className="w-6 h-6 text-blue-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M15 19l-7-7 7-7"
                  ></path>
                </svg>
              </button>

              <div className="relative">
                <button
                  onClick={() => setShowMonthDropdown(!showMonthDropdown)}
                  className="px-4 py-2 bg-white border border-gray-300 rounded-md mx-2 hover:bg-gray-50 transition duration-300 ease-in-out flex items-center"
                >
                  {currentMonth}
                  <svg
                    className="w-4 h-4 ml-2"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M19 9l-7 7-7-7"
                    ></path>
                  </svg>
                </button>
                {showMonthDropdown && (
                  <div className="absolute right-0 mt-2 py-2 w-40 bg-white border border-gray-300 rounded-md shadow-lg z-10 max-h-60 overflow-y-auto">
                    {generateMonthOptions().map((month) => (
                      <button
                        key={month}
                        onClick={() => handleMonthSelection(month)}
                        className={`block px-4 py-2 text-left w-full hover:bg-gray-100 ${
                          currentMonth === month
                            ? "bg-blue-100 font-medium"
                            : ""
                        }`}
                      >
                        {month}
                      </button>
                    ))}
                  </div>
                )}
                      
              </div>

              <button
                onClick={() => changeMonth(1)}
                className="p-2 rounded-full hover:bg-gray-100 focus:outline-none transition-colors"
                aria-label="Next month"
              >
                <svg
                  className="w-6 h-6 text-blue-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M9 5l7 7-7 7"
                  ></path>
                </svg>
              </button>
            </div>

            <div className="flex items-center space-x-3">
              {userRole === "ROLE_HR" && (
                <button
                  onClick={() => setShowDeleteModal(true)}
                  className="px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-700 transition-colors flex items-center shadow-sm mr-3"
                >
                  <svg
                    className="w-5 h-5 mr-2"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth="2"
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                    ></path>
                  </svg>
                  Delete Entry
                </button>
              )}
              <button
                onClick={handleGraphClick}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors flex items-center shadow-sm"
              >
                <svg
                  className="w-5 h-5 mr-2"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M7 12l3-3m0 0l3 3m-3-3v7m6-6v6m-6 2h9a2 2 0 002-2v-5a2 2 0 00-2-2h-2"
                  ></path>
                </svg>
                View Graph
              </button>

              {/* Ensure Consistent Height by Wrapping in a Fixed Container */}
              {/* Ensure Consistent Height by Wrapping in a Fixed Container */}
              <div className="flex items-center space-x-4 min-h-[48px]">
    {
      <div className="flex items-center space-x-2">
        {/* From Month Dropdown */}
        <div className="relative">
          <button
            onClick={() => setShowFromMonthDropdown(!showFromMonthDropdown)}
            className="py-2 px-4 bg-white border border-blue-300 text-blue-700 rounded-lg text-sm font-medium cursor-pointer focus:ring-2 focus:ring-blue-300 focus:border-blue-500 focus:outline-none flex items-center"
          >
            From Month: {fromMonth}
            <svg className="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7"></path>
            </svg>
          </button>
          {showFromMonthDropdown && (
            <div className="absolute left-0 mt-2 py-2 w-40 bg-white border border-gray-300 rounded-md shadow-lg z-10 max-h-60 overflow-y-auto">
              {months.map((month) => (
                <button
                  key={month}
                  onClick={() => {
                    if (months.indexOf(month) > months.indexOf(toMonth)) {
                      alert("From Month cannot be greater than To Month!");
                    } else {
                      setFromMonth(month);
                      setShowFromMonthDropdown(false);
                    }
                  }}
                  className={`block px-4 py-2 text-left w-full hover:bg-blue-50 ${fromMonth === month ? "bg-blue-100 font-medium text-blue-700" : ""}`}
                >
                  {month}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* To Month Dropdown */}
        <div className="relative">
          <button
            onClick={() => setShowToMonthDropdown(!showToMonthDropdown)}
            className="py-2 px-4 bg-white border border-blue-300 text-blue-700 rounded-lg text-sm font-medium cursor-pointer focus:ring-2 focus:ring-blue-300 focus:border-blue-500 focus:outline-none flex items-center"
          >
            To Month: {toMonth}
            <svg className="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7"></path>
            </svg>
          </button>
          {showToMonthDropdown && (
            <div className="absolute left-0 mt-2 py-2 w-40 bg-white border border-gray-300 rounded-md shadow-lg z-10 max-h-60 overflow-y-auto">
              {months.map((month) => (
                <button
                  key={month}
                  onClick={() => {
                    if (months.indexOf(fromMonth) > months.indexOf(month)) {
                      alert("To Month cannot be less than From Month!");
                    } else {
                      setToMonth(month);
                      setShowToMonthDropdown(false);
                    }
                  }}
                  className={`block px-4 py-2 text-left w-full hover:bg-blue-50 ${toMonth === month ? "bg-blue-100 font-medium text-blue-700" : ""}`}
                >
                  {month}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Generate Report Button */}
        <button
          onClick={() => handleGenerateReport(userId)}
          className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors flex items-center shadow-sm"
        >
          <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
          </svg>
          Generate Report
        </button>
      </div>
    }
  </div>
            </div>
          </div>

          {/* Calendar */}
          <div className="flex-1 bg-white rounded-lg shadow-md overflow-hidden flex flex-col">
            {error && (
              <div className="m-4 p-3 bg-red-100 text-red-700 rounded-md">
                {error}
              </div>
            )}

            {isLoading ? (
              <div className="flex-1 flex justify-center items-center">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
              </div>
            ) : (
              <div className="flex-1 flex flex-col overflow-hidden">
                <div className="grid grid-cols-7 bg-gray-50 border-b">
                  {[
                    "Sunday",
                    "Monday",
                    "Tuesday",
                    "Wednesday",
                    "Thursday",
                    "Friday",
                    "Saturday",
                  ].map((day, index) => (
                    <div
                      key={day}
                      className={`text-center py-3 font-semibold ${
                        index === 0 || index === 6
                          ? "text-red-500"
                          : "text-gray-700"
                      }`}
                    >
                      <span className="hidden md:inline">{day}</span>
                      <span className="md:hidden">{day.substring(0, 3)}</span>
                    </div>
                  ))}
                </div>

                <div className="flex-1 overflow-y-auto">
                  <div className="grid grid-cols-7 auto-rows-fr">
                    {generateCalendar()}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Legend sidebar */}
        <div className="w-64 bg-white shadow-lg p-4 border-l hidden md:block">
          <h3 className="text-lg font-semibold mb-4 text-gray-800 border-b pb-2">
            Attendance Codes
          </h3>
          <div className="space-y-3">
            <div className="flex items-center">
              <svg
                className="w-5 h-5 mr-3 text-green-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                ></path>
              </svg>
              <span className="text-gray-700">Present (✓)</span>
            </div>
            <div className="flex items-center">
              <svg
                className="w-5 h-5 mr-3 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
                ></path>
              </svg>
              <span className="text-gray-700">Work From Home (W)</span>
            </div>
            <div className="flex items-center">
              <svg
                className="w-5 h-5 mr-3 text-amber-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                ></path>
              </svg>
              <span className="text-gray-700">Planned Leave (P)</span>
            </div>
            <div className="flex items-center">
              <svg
                className="w-5 h-5 mr-3 text-orange-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                ></path>
              </svg>
              <span className="text-gray-700">Unplanned Leave (?)</span>
            </div>
            <div className="flex items-center">
              <svg
                className="w-5 h-5 mr-3 text-purple-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
                ></path>
              </svg>
              <span className="text-gray-700">Sick Leave (S)</span>
            </div>
            <div className="flex items-center">
              <svg
                className="w-5 h-5 mr-3 text-indigo-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                ></path>
              </svg>
              <span className="text-gray-700">Travelling to HQ (T)</span>
            </div>
            <div className="flex items-center">
              <svg
                className="w-5 h-5 mr-3 text-teal-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z"
                ></path>
              </svg>
              <span className="text-gray-700">Holiday (H)</span>
            </div>
            <div className="flex items-center">
              <svg
                className="w-5 h-5 mr-3 text-cyan-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                ></path>
              </svg>
              <span className="text-gray-700">Elections (E)</span>
            </div>
            <div className="flex items-center">
              <svg
                className="w-5 h-5 mr-3 text-lime-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"
                ></path>
              </svg>
              <span className="text-gray-700">Joined (J)</span>
            </div>
            <div className="flex items-center">
              <svg
                className="w-5 h-5 mr-3 text-red-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
                ></path>
              </svg>
              <span className="text-gray-700">Absent (A)</span>
            </div>
          </div>
        </div>
      </div>

      {showDeleteModal && (
        <div className="fixed inset-0 backdrop-blur-md flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-96 max-w-md shadow-lg">
            <h3 className="text-lg font-semibold mb-4">
              Delete Attendance Entry
            </h3>
            <p className="text-gray-600 mb-4">
              Please select the date of the entry you want to delete:
            </p>

            <div className="mb-4">
              <input
                type="date"
                className="w-full p-2 border border-gray-300 rounded"
                value={deleteDate}
                onChange={(e) => setDeleteDate(e.target.value)}
              />
            </div>

            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setShowDeleteModal(false)}
                className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-100"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteEntry}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
                disabled={!deleteDate}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default EnhancedCalendarView;
