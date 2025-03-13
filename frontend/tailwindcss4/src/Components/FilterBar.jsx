import React from "react";
import { Filter, Calendar, Users, Search } from "lucide-react";
import DropdownMenu from "./DropdownMenu";

const FilterBar = ({
  employees,
  monthFilter,
  setMonthFilter,
  employeeFilter,
  setEmployeeFilter,
  searchQuery,
  setSearchQuery
}) => {
  // Months for dropdown
  const months = [
    { value: "1", label: "January" },
    { value: "2", label: "February" },
    { value: "3", label: "March" },
    { value: "4", label: "April" },
    { value: "5", label: "May" },
    { value: "6", label: "June" },
    { value: "7", label: "July" },
    { value: "8", label: "August" },
    { value: "9", label: "September" },
    { value: "10", label: "October" },
    { value: "11", label: "November" },
    { value: "12", label: "December" },
  ];

  // Employee options for dropdown
  const employeeOptions = employees.map(employee => ({
    value: employee.id.toString(),
    label: employee.name
  }));

  return (
    <div className="bg-white rounded-lg p-4 mb-6 flex flex-wrap gap-4 items-center w-full shadow-sm">
      <div className="text-gray-700 flex items-center">
        <Filter size={18} className="mr-2" />
        <span>Filters:</span>
      </div>

      {/* Month Filter */}
      <DropdownMenu
        icon={Calendar}
        options={months}
        value={monthFilter}
        onChange={setMonthFilter}
        placeholder="Filter by Month"
      />

      {/* Employee Filter */}
      <DropdownMenu
        icon={Users}
        options={employeeOptions}
        value={employeeFilter}
        onChange={setEmployeeFilter}
        placeholder="Filter by Employee"
      />

      {/* Search box */}
      <div className="flex-1 min-w-52">
        <div className="relative">
          <input
            type="text"
            placeholder="Search employees..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full py-2 pl-10 pr-4 bg-white text-gray-800 rounded-lg border border-gray-300 focus:border-blue-500 focus:outline-none"
          />
          <Search
            size={18}
            className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
          />
        </div>
      </div>
    </div>
  );
};

export default FilterBar;