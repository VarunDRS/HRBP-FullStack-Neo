import React, { useEffect, useState, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import axios from "axios";
import { motion } from "framer-motion";
import SpaceBackground from "../Components/SpaceBackground"; // Import the SpaceBackground component

const EmployeeDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [employee, setEmployee] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const token = localStorage.getItem("Authorization");

  // Fetch employee details on component mount
  useEffect(() => {
    if (!id) {
      setError("Employee ID is missing.");
      setIsLoading(false);
      return;
    }

    fetch(`http://localhost:8080/hr/update/${id}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to fetch employee details.");
        }
        return response.json();
      })
      .then((data) => {
        setEmployee({
          userId: data.userId || "",
          username: data.username || "",
          managerId: data.managerId || "",
          managerName: data.managerName || "",
          role: data.roles?.length > 0 ? data.roles[0] : "",
        });
        setIsLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching employee details:", error);
        setError("Failed to load employee details.");
        setIsLoading(false);
      });
  }, [id, token]);

  const handleChange = (e) => {
    setEmployee({ ...employee, [e.target.name]: e.target.value });
  };

  // Handle manager update
  const handleManagerUpdate = async () => {
    try {
      if (!id || !employee.managerId) {
        throw new Error("User ID or Manager ID is missing.");
      }

      const response = await axios.put(
        `http://localhost:8080/hr/updateManager/${id}/${employee.managerId}`,
        null,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      toast.success("Manager updated successfully!");
    } catch (error) {
          console.log("Error response:", error.response);
        
          if (error.response && error.response.data) {
            const { statuscode, message, data } = error.response.data;
        
            if (statuscode === 400) {
              toast.error(message);  // Show correct error message
            } else {
              toast.error("Unexpected error occurred: " + message);
            }
          } else if (error.message) {
            toast.error("Error: " + error.message);
          } else {
            toast.error("An unexpected error occurred. Please try again.");
          }
        }   
  };

  // Handle role update
  const handleRoleUpdate = async () => {
    try {
      if (!employee.role || employee.role.trim() === "") {
        throw new Error("Role is required.");
      }

      const requestBody = {
        userId: id,
        roles: [employee.role],
      };

      const response = await axios.put(
        "http://localhost:8080/hr/updaterole",
        requestBody,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      toast.success("Role updated successfully!");
    } catch (error) {
          console.log("Error response:", error.response);
        
          if (error.response && error.response.data) {
            const { statuscode, message, data } = error.response.data;
        
            if (statuscode === 400) {
              toast.error(message);  // Show correct error message
            } else {
              toast.error("Unexpected error occurred: " + message);
            }
          } else if (error.message) {
            toast.error("Error: " + error.message);
          } else {
            toast.error("An unexpected error occurred. Please try again.");
          }
        }   
  };

  // Handle form submission
  const handleSubmit = async () => {
    setIsSubmitting(true);
    setError(null);

    try {
      if (employee.managerId && employee.managerId.trim() !== "") {
        await handleManagerUpdate();
      }

      if (employee.role && employee.role.trim() !== "") {
        await handleRoleUpdate();
      }

      navigate("/hr");
    } catch (error) {
      console.error("Error updating employee details:", error);
      toast.error("Failed to update employee details");
    } finally {
      setIsSubmitting(false);
    }
  };

  // Loading state
  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-900">
        <div className="animate-pulse flex flex-col items-center">
          <div className="h-12 w-12 border-t-4 border-b-4 border-blue-500 rounded-full animate-spin"></div>
          <p className="mt-4 text-gray-300 font-medium">Loading employee details...</p>
        </div>
      </div>
    );
  }

  // Error state
  if (error && !employee) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-900">
        <div className="bg-gray-800 bg-opacity-80 backdrop-blur-sm rounded-lg shadow-xl p-8 mx-4">
          <div className="flex items-center justify-center mb-4 text-red-500">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <p className="text-center text-gray-300 font-medium">{error}</p>
          <button
            onClick={() => navigate("/employees")}
            className="mt-6 w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition duration-200"
          >
            Return to Employee List
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="relative min-h-screen w-full bg-gray-900 flex justify-center items-center overflow-hidden">
      {/* SpaceBackground component */}
      <SpaceBackground />

      {/* Main content */}
      <div className="relative z-10 w-full max-w-md">
        <div className="bg-gray-800 bg-opacity-80 backdrop-blur-sm rounded-lg shadow-xl p-8 mx-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
          >
            <h2 className="text-2xl font-bold text-white mb-6">Edit Employee Details</h2>

            <div className="grid gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Employee ID</label>
                <input
                  type="text"
                  name="userId"
                  value={id}
                  className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-blue-500"
                  readOnly
                />
                <p className="mt-1 text-xs text-gray-400">ID cannot be modified</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Manager ID</label>
                <input
                  type="text"
                  name="managerId"
                  value={employee.managerId}
                  onChange={handleChange}
                  className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">Role</label>
                <select
                  name="role"
                  value={employee.role}
                  onChange={handleChange}
                  className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-blue-500"
                >
                  <option value="">Select a role</option>
                  <option value="ROLE_HR">ROLE_HR</option>
                  <option value="ROLE_MANAGER">ROLE_MANAGER</option>
                  <option value="ROLE_EMPLOYEE">ROLE_EMPLOYEE</option>
                </select>
              </div>
            </div>

            <div className="mt-6 flex items-center justify-end space-x-3">
              <button
                onClick={() => navigate("/hr")}
                className="px-4 py-2 bg-gray-700 text-gray-300 rounded-lg hover:bg-gray-600 transition"
              >
                Cancel
              </button>
              <button
                onClick={handleSubmit}
                disabled={isSubmitting}
                className={`px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition ${
                  isSubmitting ? "opacity-70 cursor-not-allowed" : ""
                }`}
              >
                {isSubmitting ? (
                  <span className="flex items-center">
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Saving...
                  </span>
                ) : (
                  "Save Changes"
                )}
              </button>
            </div>
          </motion.div>
        </div>
      </div>
    </div>
  );
};

export default EmployeeDetails;