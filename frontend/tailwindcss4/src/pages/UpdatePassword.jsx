import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useLocation } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import { toast } from "react-toastify";

const UpdatePassword = () => {
  const [stage, setStage] = useState("verify"); // "verify" or "update"
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState(0);
  const navigate = useNavigate();
  const location = useLocation();


  useEffect(() => {
    localStorage.setItem("previousRoute", location.pathname);
  }, [location]);

  // Password verification animation states
  const [verifying, setVerifying] = useState(false);
  const [passwordShake, setPasswordShake] = useState(false);

  // Calculate password strength
  useEffect(() => {
    if (!newPassword) {
      setPasswordStrength(0);
      return;
    }
    
    let strength = 0;
    // Length check
    if (newPassword.length >= 8) strength += 1;
    // Has uppercase
    if (/[A-Z]/.test(newPassword)) strength += 1;
    // Has lowercase
    if (/[a-z]/.test(newPassword)) strength += 1;
    // Has number
    if (/[0-9]/.test(newPassword)) strength += 1;
    // Has special character
    if (/[^A-Za-z0-9]/.test(newPassword)) strength += 1;
    
    setPasswordStrength(strength);
  }, [newPassword]);

  const handleVerifyPassword = async (e) => {
    e.preventDefault();
    if (!currentPassword) {
      setError("Please enter your current password");
      return;
    }

    setVerifying(true);
    setLoading(true);
    setError("");
    
    try {
      const token = localStorage.getItem("Authorization");
        const decodedToken = jwtDecode(token);
        const userId = decodedToken.userId;  
        const role = decodedToken.roles?.[0];
      
      const response = await axios.post(
        "http://localhost:8080/users/verify-password",
        { userId, password: currentPassword },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      if (response.data.success) {
        // Successful verification animation
        setTimeout(() => {
          setVerifying(false);
          setLoading(false);
          setStage("update");
        }, 1500);
      } else {
        throw new Error("Password verification failed");
      }
    } catch (error) {
      console.error("Error verifying password:", error);
      setVerifying(false);
      setLoading(false);
      toast.error("Wrong password")
      setError("Current password is incorrect");
      setPasswordShake(true);
      setTimeout(() => setPasswordShake(false), 500);
    }
  };

  const handleCancelButton = () => {
        const token = localStorage.getItem("Authorization");
        const decodedToken = jwtDecode(token);
        const userId = decodedToken.userId;  
        const roles = decodedToken.roles; 
    
        if (roles.includes("ROLE_HR")) {
          navigate("/hr");
        } else if (roles.includes("ROLE_EMPLOYEE")) {
            navigate("/employee");
        } else {
            navigate("/manager");
        }
    };

  const handleUpdatePassword = async (e) => {
    e.preventDefault();
    setError("");
    
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    
    if (newPassword.length < 8) {
      setError("Password must be at least 8 characters long");
      return;
    }
    
    setLoading(true);
    
    try {
      const token = localStorage.getItem("Authorization");
      const decodedToken = jwtDecode(token);
      const userId = decodedToken.userId;  
      const role = decodedToken.roles?.[0];
      
      const response = await axios.post(
        "http://localhost:8080/users/updatePassword",
        { userId, newPassword },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      if (response.data.success) {
        setLoading(false);
        setSuccess(true);

        toast.success("Password updated successfully");

        navigate(-1);
                
      } else {
        throw new Error("Failed to update password");
      }
    } catch (error) {
      console.error("Error updating password:", error);
      setLoading(false);
      toast.error("Failed to update password. Please try again.");
      setError("Failed to update password. Please try again.");
    }
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-gradient-to-br from-blue-900 via-indigo-800 to-purple-900">
      <div className="max-w-md w-full p-8 bg-white bg-opacity-10 backdrop-blur-lg rounded-2xl shadow-2xl border border-white border-opacity-20">
        <h1 className={`text-3xl font-bold text-center mb-8 text-black`}>
          {stage === "verify" ? "Verify Your Identity" : "Update Your Password"}  
        </h1>

        {/* Verification Stage */}
        {stage === "verify" && (
          <form onSubmit={handleVerifyPassword} className="space-y-6">
            <div className="relative">
              <label htmlFor="currentPassword" className="block text-black mb-2">
                Current Password :
              </label>
              <div className={`relative ${passwordShake ? "animate-shake" : ""}`}>
                <input
                  type="password"
                  id="currentPassword"
                  className="w-full py-3 px-4 bg-white bg-opacity-20 rounded-lg text-black placeholder-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-blue-500 border border-gray-400"
                  placeholder="Enter your current password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  disabled={verifying || loading}
                />
                {verifying && (
                  <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                    <div className="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-blue-400"></div>
                  </div>
                )}
              </div>
            </div>
            
            {/* {error && (
              <div className="text-red-300 text-sm font-semibold">{error}</div>
            )} */}
            
            <button
              type="submit"
              className={`w-full py-3 rounded-lg transition-all duration-300 font-bold ${
                loading
                  ? "bg-gray-600 cursor-not-allowed"
                  : "bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 transform hover:-translate-y-1"
              }`}
              disabled={loading}
            >
              {loading ? "Verifying..." : "Continue"}
            </button>
            
            <div className="text-center mt-4">
              <button
                type="button"
                className="text-blue-300 hover:text-blue-200 text-sm"
                onClick={handleCancelButton}
              >
                Cancel
              </button>
            </div>
          </form>
        )}
        
        {/* Update Password Stage */}
        {stage === "update" && (
          <form onSubmit={handleUpdatePassword} className="space-y-6">
            <div className="relative">
              <label htmlFor="newPassword" className="block text-black mb-2">
                New Password
              </label>
              <input
                type="password"
                id="newPassword"
                className="w-full py-3 px-4 bg-white bg-opacity-80 rounded-lg text-black placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-blue-500 border border-gray-400"
                placeholder="Enter your new password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                disabled={loading}
              />
              
              {/* Password strength meter */}
              <div className="mt-2">
                <div className="w-full h-2 bg-gray-700 rounded-full overflow-hidden">
                  <div 
                    className={`h-full transition-all duration-500 ${
                      passwordStrength === 0 ? "w-0" :
                      passwordStrength === 1 ? "w-1/5 bg-red-500" :
                      passwordStrength === 2 ? "w-2/5 bg-orange-500" :
                      passwordStrength === 3 ? "w-3/5 bg-yellow-500" :
                      passwordStrength === 4 ? "w-4/5 bg-blue-500" :
                      "w-full bg-green-500"
                    }`}
                  ></div>
                </div>
                <div className="text-xs text-gray-300 mt-1">
                  {passwordStrength === 0 ? "Enter a password" :
                   passwordStrength === 1 ? "Too weak" :
                   passwordStrength === 2 ? "Weak" :
                   passwordStrength === 3 ? "Medium" :
                   passwordStrength === 4 ? "Strong" :
                   "Very strong"}
                </div>
              </div>
            </div>
            
            <div className="relative">
              <label htmlFor="confirmPassword" className="block text-black mb-2">
                Confirm Password
              </label>
              <input
                type="password"
                id="confirmPassword"
                className={`w-full py-3 px-4 bg-white bg-opacity-80 rounded-lg text-black placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-blue-500 border border-gray-400 ${
                  confirmPassword && newPassword !== confirmPassword ? "border-2 border-red-500" : ""
                }`}
                placeholder="Confirm your new password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                disabled={loading}
              />
              
              {confirmPassword && newPassword !== confirmPassword && (
                <div className="text-red-300 text-sm mt-1">Passwords don't match</div>
              )}
            </div>
            
            
            
            <button
              type="submit"
              className={`w-full py-3 rounded-lg transition-all duration-300 font-bold ${
                loading || (confirmPassword && newPassword !== confirmPassword)
                  ? "bg-gray-600 cursor-not-allowed"
                  : "bg-gradient-to-r from-green-500 to-teal-500 hover:from-green-600 hover:to-teal-600 transform hover:-translate-y-1"
              }`}
              disabled={loading || (confirmPassword && newPassword !== confirmPassword)}
            >
              {loading ? "Updating..." : "Update Password"}
            </button>
            
            <div className="text-center mt-4">
              <button
                type="button"
                className="text-blue-300 hover:text-blue-200 text-sm"
                onClick={() => setStage("verify")}
              >
                Back
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default UpdatePassword;