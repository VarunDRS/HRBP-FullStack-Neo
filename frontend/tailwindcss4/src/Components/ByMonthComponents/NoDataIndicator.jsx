import React from "react";

const NoDataIndicator = () => {
  return (
    <div className="h-full flex items-center justify-center">
      <div className="text-center py-16 text-gray-500 flex flex-col items-center">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="h-16 w-16 text-gray-300 mb-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
          />
        </svg>
        <span className="text-lg font-medium">
          No attendance data found.
        </span>
        <p className="mt-2 text-gray-400">
          Try selecting a different month or check user ID.
        </p>
      </div>
    </div>
  );
};

export default NoDataIndicator;