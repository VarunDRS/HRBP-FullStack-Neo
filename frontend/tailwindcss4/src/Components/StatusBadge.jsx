import React from "react";

const StatusBadge = ({ status }) => {
  const getStatusColorClass = (status) => {
    switch (status.toLowerCase()) {
      case "approved":
        return "bg-green-100 text-green-800";
      case "rejected":
        return "bg-red-100 text-red-800";
      default:
        return "bg-yellow-100 text-yellow-800";
    }
  };

  return (
    <span className={`px-3 py-1 rounded-full text-xs font-semibold ${getStatusColorClass(status)}`}>
      {status}
    </span>
  );
};

export default StatusBadge;