import React from "react";

const AttendanceLegend = ({ getAttendanceColor }) => {
  return (
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
  );
};

export default AttendanceLegend;