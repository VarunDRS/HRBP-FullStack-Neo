import React from "react";
import { Calendar } from "lucide-react";
import LeaveRequestCard from "./LeaveRequestCard";

// Status code mapping
const STATUS_CODES = {
  'P': 'Planned Leave',
  'W': 'Work From Home',
  'S': 'Sick Leave',
  'T': 'Traveling to Gurugram',
  '?': 'Awaiting Approval',
  'U': 'Unplanned Leave',
  'A': 'Approved',
  'H': "Holiday",
  'P**': 'Planned Leave(First Half)',
  'P*': 'Planned Leave(Second Half)',
  'E': 'Elections'
};

const LeaveRequestsPanel = ({ leaveRequests }) => {
  // Transform the data structure to be compatible with LeaveRequestCard
  const transformLeaveRequests = (requests) => {
    const transformedRequests = [];

    // If requests is an object with employee names as keys
    if (requests && typeof requests === 'object' && !Array.isArray(requests)) {
      Object.entries(requests).forEach(([employeeName, monthlyRequests]) => {
        Object.entries(monthlyRequests).forEach(([date, status]) => {
          transformedRequests.push({
            id: `${employeeName}-${date}`, // Create a unique identifier
            employeeName,
            startDate: date,
            status: STATUS_CODES[status] || status,
            type: STATUS_CODES[status] || 'Leave'
          });
        });
      });
    } else if (Array.isArray(requests)) {
      // If already in the expected format, use as-is
      return requests;
    }

    return transformedRequests;
  };

  // Process leave requests
  const processedLeaveRequests = transformLeaveRequests(leaveRequests);

  return (
    <div className="bg-white rounded-lg p-4 overflow-hidden flex flex-col shadow-sm flex-1 max-h-full">
      <h2 className="text-xl font-semibold mb-4 text-gray-800 flex items-center">
        <Calendar size={20} className="mr-2 text-blue-600" />
        My Leave Requests
      </h2>

      <div className="flex-1 overflow-y-auto pr-2
            [&::-webkit-scrollbar]:w-2
            [&::-webkit-scrollbar-track]:rounded-full
            [&::-webkit-scrollbar-track]:bg-gray-100
            [&::-webkit-scrollbar-thumb]:rounded-full
            [&::-webkit-scrollbar-thumb]:bg-gray-300
            dark:[&::-webkit-scrollbar-track]:bg-neutral-500
            dark:[&::-webkit-scrollbar-thumb]:bg-neutral-400">
        <div
          className="grid grid-cols-1 xl:grid-cols-2 gap-4"
          style={{
            scrollbarColor: "#D1D5DB #F3F4F6",
            scrollbarWidth: "thin",
          }}
        >
          {processedLeaveRequests.length > 0 ? (
            processedLeaveRequests.map((request) => (
              <LeaveRequestCard key={request.id} request={request} />
            ))
          ) : (
            <div className="col-span-full flex flex-col items-center justify-center h-64 text-gray-400">
              <Calendar size={48} className="mb-2 opacity-50" />
              <p>No leave requests found</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default LeaveRequestsPanel;

