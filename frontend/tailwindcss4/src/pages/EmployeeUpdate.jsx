import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import axios from "axios";

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

  // Handle input changes
  const handleChange = (e) => {
    setEmployee({ ...employee, [e.target.name]: e.target.value });
  };

  // Handle manager update
  const handleManagerUpdate = async () => {
    try {
      // Log the userId and newManagerId for debugging
      console.log("Updating manager for userId:", id);
      console.log("New manager ID:", employee.managerId);

      if (!id || !employee.managerId) {
        throw new Error("User ID or Manager ID is missing.");
      }

      const response = await axios.put(
        `http://localhost:8080/hr/updateManager/${id}/${employee.managerId}`,
        null, // No request body needed
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      toast.success("Manager updated successfully!");
    } catch (error) {
      console.error("Error updating manager:", error);
      toast.error("Failed to update manager");
    }
  };

  // Handle role update
  const handleRoleUpdate = async () => {
    try {
      // Ensure the role is selected
      if (!employee.role || employee.role.trim() === "") {
        throw new Error("Role is required.");
      }

      // Prepare the request body
      const requestBody = {
        userId: id, // Use userId from useParams
        roles: [employee.role], // Wrap the role in an array
      };

      // Log the request body for debugging
      console.log("Updating roles with request body:", requestBody);

      // Make the PUT request
      const response = await axios.put(
        "http://localhost:8080/hr/updaterole",
        requestBody,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      // Log the response for debugging
      console.log("Role update response:", response.data);

      toast.success("Role updated successfully!");
    } catch (error) {
      console.error("Error updating roles:", error);
      toast.error("Failed to update roles");
    }
  };

  // Handle form submission
  const handleSubmit = async () => {
    setIsSubmitting(true);
    setError(null);

    try {
      // Update Manager if a new manager ID is provided
      if (employee.managerId && employee.managerId.trim() !== "") {
        await handleManagerUpdate();
      }

      // Update Role if a new role is selected
      if (employee.role && employee.role.trim() !== "") {
        await handleRoleUpdate();
      }

      // Navigate back to HR dashboard if at least one update is successful
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
      <div className="flex justify-center items-center min-h-screen bg-gray-50">
        <div className="animate-pulse flex flex-col items-center">
          <div className="h-12 w-12 border-t-4 border-b-4 border-blue-500 rounded-full animate-spin"></div>
          <p className="mt-4 text-gray-700 font-medium">Loading employee details...</p>
        </div>
      </div>
    );
  }

  // Error state
  if (error && !employee) {
    return (
      <div className="flex justify-center items-center min-h-screen bg-gray-50">
        <div className="bg-white p-8 rounded-lg shadow-md max-w-md w-full">
          <div className="flex items-center justify-center mb-4 text-red-500">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <p className="text-center text-gray-800 font-medium">{error}</p>
          <button
            onClick={() => navigate("/employees")}
            className="mt-6 w-full bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 transition duration-200"
          >
            Return to Employee List
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex justify-center items-center min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-lg w-full bg-white shadow-lg rounded-lg overflow-hidden">
        <div className="bg-blue-600 px-6 py-4">
          <h2 className="text-xl font-bold text-white">
            Edit Employee Details
          </h2>
        </div>

        <div className="p-6">
          <div className="grid gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Employee ID</label>
              <input
                type="text"
                name="userId"
                value={id}
                className="w-full p-2 border border-gray-300 rounded-md bg-gray-100 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                readOnly
              />
              <p className="mt-1 text-xs text-gray-500">ID cannot be modified</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Manager ID</label>
              <input
                type="text"
                name="managerId"
                value={employee.managerId}
                onChange={handleChange}
                className="w-full p-2 border border-gray-300 rounded-md text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Role</label>
              <select
                name="role"
                value={employee.role}
                onChange={handleChange}
                className="w-full p-2 border border-gray-300 rounded-md text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
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
              className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            >
              Cancel
            </button>
            <button
              onClick={handleSubmit}
              disabled={isSubmitting}
              className={`px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 ${
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
        </div>
      </div>
    </div>
  );
};

export default EmployeeDetails;