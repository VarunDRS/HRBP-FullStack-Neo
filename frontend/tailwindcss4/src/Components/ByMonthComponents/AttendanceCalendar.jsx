import React from "react";
import AttendanceLegend from "./AttendanceLegend";

const AttendanceCalendar = ({
  reportData,
  getFormattedMonthYear,
  getAttendanceColor,
  getAttendanceTooltip,
  extractUsername,
}) => {
  if (!reportData) return null;

  const [year, monthNum] = getFormattedMonthYear().split("-");
  const month = parseInt(monthNum);
  const daysInMonth = new Date(year, month, 0).getDate();
  const weekdays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

  const dates = Array.from({ length: daysInMonth }, (_, i) => {
    const date = new Date(year, month - 1, i + 1);
    return date
      .toLocaleDateString("en-US", { month: "short", day: "2-digit" })
      .replace(" ", "-");
  });

  const dayOfWeeks = dates.map((_, i) => {
    const date = new Date(year, month - 1, i + 1);
    return date.getDay();
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
        <AttendanceLegend getAttendanceColor={getAttendanceColor} />
      </div>

      <div className="flex-1 overflow-auto">
        <table className="w-full text-sm border-collapse">
          <thead>
            <tr>
              <th className="sticky top-0 left-0 bg-blue-500 text-white z-30 text-left py-4 px-4 font-semibold border border-blue-700">
                User Name
              </th>
              <th className="sticky top-0 left-[180px] bg-blue-500 text-white z-30 text-center py-3 px-2 font-semibold border border-blue-700 min-w-[80px] max-w-[80px]">
                Total WFH
              </th>
              <th className="sticky top-0 left-[260px] bg-blue-500 text-white z-30 text-center py-3 px-2 font-semibold border border-blue-700 min-w-[80px] max-w-[80px]">
                Total Leaves
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
            {Object.entries(reportData).map(([userKey, attendance], rowIndex) => {
              const displayName = extractUsername(userKey);
              
              return (
                <tr
                  key={userKey}
                  className="hover:bg-blue-50 transition-colors duration-150"
                >
                  <td className="sticky left-0 bg-white z-10 text-left py-4 px-4 font-medium min-w-[180px] max-w-[250px] whitespace-nowrap overflow-hidden text-ellipsis border border-gray-300 shadow-sm">
                    {displayName}
                  </td>
                  <td className="sticky left-[180px] bg-white z-10 text-center py-4 px-2 font-semibold border border-gray-300 min-w-[80px] max-w-[80px]">
                    {attendance["Total WFH"] || 0}
                  </td>
                  <td className="sticky left-[260px] bg-white z-10 text-center py-4 px-2 font-semibold border border-gray-300 min-w-[80px] max-w-[80px]">
                    {attendance["Total Leaves"] || 0}
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
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AttendanceCalendar;