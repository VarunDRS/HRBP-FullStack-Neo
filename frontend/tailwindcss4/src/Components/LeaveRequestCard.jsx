import React from "react";
import { Clock, User } from "lucide-react";

const STATUS_COLORS = {
  'Planned Leave': 'bg-yellow-100 text-yellow-800',
  'Approved': 'bg-green-100 text-green-800',
  'Work From Home': 'bg-blue-100 text-blue-800',
  'Sick Leave': 'bg-red-100 text-red-800',
  'Traveling to Gurugram': 'bg-purple-100 text-purple-800',
  'Awaiting Approval': 'bg-gray-100 text-gray-800',
  'Unplanned Leave': 'bg-red-100 text-red-800',
  'Holiday': 'bg-green-100 text-green-800',
  'Planned Leave(First Half)': 'bg-yellow-100 text-yellow-800',
  'Planned Leave(Second Half)': 'bg-yellow-100 text-yellow-800',
  'Elections': 'bg-blue-100 text-blue-800'
};

const LeaveRequestCard = ({ request }) => {
  const { employeeName, startDate, status, type } = request;

  // Get status color, default to gray if not found
  const statusColorClass = STATUS_COLORS[status] || STATUS_COLORS['Pending'];

  return (
    <div className="bg-white border rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow duration-300">
      <div className="flex justify-between items-center mb-3">
        <div className="flex items-center">
          <User size={16} className="mr-2 text-gray-500" />
          <span className="font-medium text-gray-700">{employeeName}</span>
        </div>
        <span 
          className={`px-2 py-1 rounded-full text-xs font-semibold ${statusColorClass}`}
        >
          {status}
        </span>
      </div>
      
      <div className="flex items-center text-gray-600">
        <Clock size={16} className="mr-2 text-gray-500" />
        <span className="text-sm">{startDate}</span>
      </div>
      
      <div className="mt-2 text-xs text-gray-500">
        Leave Type: {type}
      </div>
    </div>
  );
};

export default LeaveRequestCard;