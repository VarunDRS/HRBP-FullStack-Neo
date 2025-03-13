import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import { ArrowLeft, ArrowRight, PieChartIcon, Calendar, User } from 'lucide-react';
import axios from "axios";
import { jwtDecode } from "jwt-decode";
import { useNavigate  } from "react-router-dom";

const EmployeeAttendanceChart = () => {
  const { userId, month } = useParams();
  const [attendanceData, setAttendanceData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentMonth, setCurrentMonth] = useState(month || 'Mar-2025');
  const navigate = useNavigate();

  // Parse the month string and convert to API format (YYYY-MM)
  const parseMonth = (monthStr) => {
    const [monthName, year] = monthStr.split('-');
    const monthMap = {
      'Jan': '01', 'Feb': '02', 'Mar': '03', 'Apr': '04', 'May': '05', 'Jun': '06',
      'Jul': '07', 'Aug': '08', 'Sep': '09', 'Oct': '10', 'Nov': '11', 'Dec': '12'
    };
    return `${year}-${monthMap[monthName]}`;
  };

  // Format API month to display format (MMM-YYYY)
  const formatMonth = (apiMonth) => {
    const [year, month] = apiMonth.split('-');
    const monthMap = {
      '01': 'Jan', '02': 'Feb', '03': 'Mar', '04': 'Apr', '05': 'May', '06': 'Jun',
      '07': 'Jul', '08': 'Aug', '09': 'Sep', '10': 'Oct', '11': 'Nov', '12': 'Dec'
    };
    return `${monthMap[month]}-${year}`;
  };

  // Navigate to previous month
  const goToPreviousMonth = () => {
    const [monthName, year] = currentMonth.split('-');
    const monthMap = {
      'Jan': { prev: 'Dec', yearOffset: -1 },
      'Feb': { prev: 'Jan', yearOffset: 0 },
      'Mar': { prev: 'Feb', yearOffset: 0 },
      'Apr': { prev: 'Mar', yearOffset: 0 },
      'May': { prev: 'Apr', yearOffset: 0 },
      'Jun': { prev: 'May', yearOffset: 0 },
      'Jul': { prev: 'Jun', yearOffset: 0 },
      'Aug': { prev: 'Jul', yearOffset: 0 },
      'Sep': { prev: 'Aug', yearOffset: 0 },
      'Oct': { prev: 'Sep', yearOffset: 0 },
      'Nov': { prev: 'Oct', yearOffset: 0 },
      'Dec': { prev: 'Nov', yearOffset: 0 }
    };
    
    const prevMonth = monthMap[monthName].prev;
    const prevYear = parseInt(year) + monthMap[monthName].yearOffset;
    
    setCurrentMonth(`${prevMonth}-${prevYear}`);
  };

  // Navigate to next month
  const goToNextMonth = () => {
    const [monthName, year] = currentMonth.split('-');
    const monthMap = {
      'Jan': { next: 'Feb', yearOffset: 0 },
      'Feb': { next: 'Mar', yearOffset: 0 },
      'Mar': { next: 'Apr', yearOffset: 0 },
      'Apr': { next: 'May', yearOffset: 0 },
      'May': { next: 'Jun', yearOffset: 0 },
      'Jun': { next: 'Jul', yearOffset: 0 },
      'Jul': { next: 'Aug', yearOffset: 0 },
      'Aug': { next: 'Sep', yearOffset: 0 },
      'Sep': { next: 'Oct', yearOffset: 0 },
      'Oct': { next: 'Nov', yearOffset: 0 },
      'Nov': { next: 'Dec', yearOffset: 0 },
      'Dec': { next: 'Jan', yearOffset: 1 }
    };
    
    const nextMonth = monthMap[monthName].next;
    const nextYear = parseInt(year) + monthMap[monthName].yearOffset;
    
    setCurrentMonth(`${nextMonth}-${nextYear}`);
  };

  // Color mapping for different attendance types
  const typeColorMap = {
    'WFH': '#3B82F6', // blue
    'Planned Leave': '#10B981', // green
    'UnPlanned Leave': '#F59E0B', // amber
    'Sick Leave': '#EF4444', // red
    'Travelling to HQ': '#8B5CF6', // purple
    'Holiday': '#EC4899', // pink
    'Elections': '#6366F1', // indigo
    'Joined': '#14B8A6', // teal
    'Planned Leave (First Half)': '#84CC16', // lime
    'Planned Leave (Second Half)': '#22C55E' // emerald
  };

  // Map short codes to full names
  const typeNameMap = {
    'P': 'Planned Leave',
    'U': 'UnPlanned Leave',
    'P*': 'Planned Leave (Second Half)',
    'S': 'Sick Leave',
    'W': 'WFH',
    'T': 'Travelling to HQ',
    'H': 'Holiday',
    'E': 'Elections',
    'J': 'Joined',
    'P**': 'Planned Leave (First Half)'
  };

  useEffect(() => {
    const fetchData = async () => {
        const token = localStorage.getItem("Authorization");
        setLoading(true);
        try {
          const apiMonth = parseMonth(currentMonth);
      
          const response = await axios.get(
            `http://localhost:8080/graph?userid=${userId}&month=${apiMonth}`,
            {
              headers: { Authorization: `Bearer ${token}` },
            }
          );
      
          const data = response.data;
          setAttendanceData(data);
        } catch (err) {
          setError(err.message);
          console.error('Error fetching attendance data:', err);
        } finally {
          setLoading(false);
        }
    };

    fetchData();
  }, [userId, currentMonth]);

  const getWorkingDaysInMonth = (monthStr) => {
    const [monthName, year] = monthStr.split('-');
    const monthMap = {
      'Jan': 0, 'Feb': 1, 'Mar': 2, 'Apr': 3, 'May': 4, 'Jun': 5,
      'Jul': 6, 'Aug': 7, 'Sep': 8, 'Oct': 9, 'Nov': 10, 'Dec': 11
    };
    
    const monthIndex = monthMap[monthName];
    const date = new Date(year, monthIndex + 1, 0); // Last date of the month
    const totalDays = date.getDate();
    let workingDays = 0;
  
    for (let day = 1; day <= totalDays; day++) {
      const currentDate = new Date(year, monthIndex, day);
      const dayOfWeek = currentDate.getDay();
      if (dayOfWeek !== 0 && dayOfWeek !== 6) { // Exclude weekends
        workingDays++;
      }
    }
    
    return workingDays;
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
  
  // Updated function to calculate total days and regular workdays
  const getTotalDays = (data) => {
    if (!data || !data.typeCounts) return 0;
  
    const totalRequests = Object.values(data.typeCounts).reduce((sum, count) => sum + count, 0);
    const totalWorkingDays = getWorkingDaysInMonth(currentMonth);
    const regularWorkdays = totalWorkingDays - totalRequests;
  
    return {
      totalRequests,
      totalWorkingDays,
      regularWorkdays: Math.max(regularWorkdays, 0) // Ensure no negative values
    };
  };
  
  const totalDaysInfo = attendanceData ? getTotalDays(attendanceData) : { totalRequests: 0, totalWorkingDays: 0, regularWorkdays: 0 };
  
  // Transform data for Pie chart including Regular workdays
  const transformData = (data) => {
    if (!data || !data.typeCounts) return [];
    
    const transformedData = Object.entries(data.typeCounts).map(([type, count]) => ({
      name: typeNameMap[type] || type,
      value: count,
      color: typeColorMap[typeNameMap[type] || type] || `#${Math.floor(Math.random()*16777215).toString(16)}`
    }));
  
    if (totalDaysInfo.regularWorkdays > 0) {
      transformedData.push({
        name: 'Regular Workday',
        value: totalDaysInfo.regularWorkdays,
        color: '#34D399' // green for Regular Workday
      });
    }
  
    return transformedData;
  };

  const chartData = attendanceData ? transformData(attendanceData) : [];

  // Custom Pie Chart label renderer
  const renderCustomizedLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent, index, name }) => {
    const RADIAN = Math.PI / 180;
    const radius = outerRadius * 1.1;
    const x = cx + radius * Math.cos(-midAngle * RADIAN);
    const y = cy + radius * Math.sin(-midAngle * RADIAN);

    return percent > 0.05 ? (
      <text 
        x={x} 
        y={y} 
        fill={chartData[index].color}
        textAnchor={x > cx ? 'start' : 'end'} 
        dominantBaseline="central"
        className="text-xs font-medium"
      >
        {`${(percent * 100).toFixed(0)}%`}
      </text>
    ) : null;
  };

  return (
    <div className="w-full h-full absolute top-0 left-0 bg-gray-50 flex flex-col">
      <div className="w-full h-full flex flex-col overflow-hidden">
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-600 to-indigo-700 p-4 text-white">
          <div className="flex flex-col md:flex-row md:items-center justify-between">
            <div className="flex items-center space-x-3">
              <Calendar className="h-5 w-5" />
              <div>
                <h1 className="text-xl font-bold">Attendance Dashboard</h1>
                <div className="flex items-center">
                  <User className="h-4 w-4 mr-1" />
                  <p className="text-xs opacity-90">
                    Employee ID: {userId || 'U08EMPMLH6H'}
                  </p>
                </div>
              </div>
            </div>
            
            <div className='pr-20'>
            <div className="flex items-center mt-2 md:mt-0 bg-white/10 rounded-lg p-1 ">
              <button 
                className="p-1 rounded-l-md hover:bg-white/20 transition-colors"
                onClick={goToPreviousMonth}
              >
                <ArrowLeft className="h-4 w-4" />
              </button>
              <div className="px-3 py-1 font-medium text-sm">
                {currentMonth}
              </div>
              <button 
                className="p-1 rounded-r-md hover:bg-white/20 transition-colors"
                onClick={goToNextMonth}
              >
                <ArrowRight className="h-4 w-4" />
              </button>
            </div>
            </div>
            <div>
            <button
              onClick={handleBackButton}
              className="py-2 px-4 bg-blue-300 text-white rounded-lg text-sm font-medium cursor-pointer transition-colors hover:bg-blue-600 flex items-center gap-2"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
              </svg>
              Home
            </button>
            </div>
          </div>
        </div>
        
        {/* Content */}
        <div className="flex-grow overflow-auto p-4">
          <div className="h-full">
            {loading ? (
              <div className="flex flex-col justify-center items-center h-full">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
                <p className="mt-4 text-gray-600">Loading attendance data...</p>
              </div>
            ) : error ? (
              <div className="flex flex-col justify-center items-center h-full text-red-500">
                <div className="bg-red-100 p-4 rounded-lg max-w-md text-center">
                  <p className="font-medium">Error loading data</p>
                  <p className="text-sm mt-1">{error}</p>
                  <button 
                    className="mt-4 px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition-colors text-sm"
                    onClick={() => window.location.reload()}
                  >
                    Retry
                  </button>
                </div>
              </div>
            ) : chartData.length === 0 ? (
              <div className="flex flex-col justify-center items-center h-full text-gray-500">
                <PieChartIcon className="h-16 w-16 text-gray-300" />
                <p className="mt-4">No attendance data available for {currentMonth}.</p>
                <button 
                  className="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors text-sm"
                  onClick={goToPreviousMonth}
                >
                  Check Previous Month
                </button>
              </div>
            ) : (
              <div className="h-full grid grid-cols-1 lg:grid-cols-3 gap-4">
                {/* Chart */}
                <div className="lg:col-span-2 bg-white rounded-lg shadow p-4 flex flex-col">
                    <h2 className="text-base font-semibold text-gray-800 mb-2">Attendance Distribution</h2>
                    <div className="flex-grow">
                        <ResponsiveContainer width="100%" height="100%" minHeight={300}> {/* Increased minHeight for larger chart */}
                        <PieChart>
                            <Pie
                            data={chartData}
                            cx="50%"
                            cy="50%"
                            labelLine={false} 
                            outerRadius={110}  
                            fill="#8884d8"
                            dataKey="value"
                            label={renderCustomizedLabel}
                            >
                            {chartData.map((entry, index) => (
                                <Cell 
                                key={`cell-${index}`} 
                                fill={entry.color}
                                stroke="#fff"
                                strokeWidth={2}
                                />
                            ))}
                            </Pie>
                            <Tooltip 
                            formatter={(value, name) => [`${value} days (${((value / totalDaysInfo.totalWorkingDays) * 100).toFixed(1)}%)`, name]}
                            contentStyle={{ borderRadius: '6px', border: 'none', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)' }}
                            />
                            <Legend 
                            layout="horizontal" 
                            verticalAlign="bottom"
                            align="center"
                            iconType="circle"
                            iconSize={8}
                            formatter={(value) => <span className="text-xs text-gray-700">{value}</span>}
                            />
                        </PieChart>
                        </ResponsiveContainer>
                    </div>
                </div>
                
                {/* Summary */}
                <div className="bg-white rounded-lg shadow p-4">
                  <h2 className="text-base font-semibold text-gray-800 mb-2">Monthly Summary</h2>
                  <div className="p-3 bg-blue-50 rounded-lg mb-3">
                    <p className="text-xs text-gray-500">Total Working Days</p>
                    <p className="text-2xl font-bold text-blue-600">{totalDaysInfo.totalWorkingDays}</p>
                  </div>
                  
                  <div className="space-y-2 max-h-60 overflow-y-auto pr-1">
                    {chartData.map((item, index) => (
                      <div key={index} className="flex items-center justify-between p-2 bg-gray-50 rounded hover:bg-gray-100 transition-colors">
                        <div className="flex items-center space-x-2">
                          <div 
                            className="w-3 h-3 rounded-full" 
                            style={{ backgroundColor: item.color }}
                          ></div>
                          <span className="text-xs font-medium text-gray-700">{item.name}</span>
                        </div>
                        <span className="text-xs font-bold">{item.value} days</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
        
        {/* Footer */}
        <div className="bg-white shadow p-3 border-t border-gray-200">
          <h4 className="font-medium text-xs text-gray-700 mb-2">Legend:</h4>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-2">
            {Object.entries(typeNameMap).map(([code, name], index) => (
              <div key={index} className="flex items-center bg-gray-50 px-2 py-1 rounded text-xs">
                <span className="font-bold mr-1 text-blue-600">{code}:</span>
                <span className="text-gray-700 truncate">{name}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default EmployeeAttendanceChart;