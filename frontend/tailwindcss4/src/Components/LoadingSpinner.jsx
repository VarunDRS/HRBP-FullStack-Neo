import React from "react";

const LoadingSpinner = ({ size = "md", color = "blue" }) => {
  // Size classes
  const sizeClasses = {
    sm: "w-4 h-4",
    md: "w-8 h-8",
    lg: "w-12 h-12"
  };
  
  // Color classes
  const colorClasses = {
    blue: "border-blue-600",
    gray: "border-gray-600",
    white: "border-white"
  };
  
  // Get appropriate classes or use defaults
  const spinnerSize = sizeClasses[size] || sizeClasses.md;
  const spinnerColor = colorClasses[color] || colorClasses.blue;
  
  return (
    <div className="flex justify-center items-center">
      <div className={`${spinnerSize} border-4 border-t-transparent ${spinnerColor} rounded-full animate-spin`}></div>
      <span className="sr-only">Loading...</span>
    </div>
  );
};

export default LoadingSpinner;