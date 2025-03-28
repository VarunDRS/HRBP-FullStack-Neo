import React from "react";

const MonthNavigation = ({
  navigateToMonth,
  getFormattedMonthYear,
  generateMonthOptions,
  jumpToMonth,
  fromMonth,
  setFromMonth,
  toMonth,
  setToMonth,
  handleGenerateReport,
  handleGenerateMultiMonthReport,
}) => {
  return (
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

      <select
        value={fromMonth}
        onChange={(e) => setFromMonth(e.target.value)}
        className="py-2 px-4 bg-white border border-blue-300 text-blue-700 rounded-lg text-sm font-medium cursor-pointer focus:ring-2 focus:ring-blue-300 focus:border-blue-500 focus:outline-none"
      >
        <option value="" disabled>
          From Month
        </option>
        {generateMonthOptions()}
      </select>

      <select
        value={toMonth}
        onChange={(e) => setToMonth(e.target.value)}
        className="py-2 px-4 bg-white border border-blue-300 text-blue-700 rounded-lg text-sm font-medium cursor-pointer focus:ring-2 focus:ring-blue-300 focus:border-blue-500 focus:outline-none"
      >
        <option value="" disabled>
          To Month
        </option>
        {generateMonthOptions()}
      </select>

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

      <button
        onClick={handleGenerateMultiMonthReport}
        className="py-2 px-4 bg-purple-500 text-white rounded-lg text-sm font-medium cursor-pointer transition-colors hover:bg-purple-600 flex items-center gap-2"
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
        Generate Multi-Month Report
      </button>
    </div>
  );
};

export default MonthNavigation;