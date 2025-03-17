import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import axios from "axios";
import { Eye, EyeOff } from "lucide-react";
import loginImage from "../assets/logo2.jpg";
import { jwtDecode } from "jwt-decode";
import SpaceBackground from "../Components/SpaceBackground"; // Import the background component

const AstronautLogin = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const response = await axios.post(
        "http://localhost:8080/users/login",
        { email, password },
        { headers: { "Content-Type": "application/json" } }
      );

      if (response.status === 200) {
        console.log("Login Successful:", response.data);
    
        // Store only the JWT token
        localStorage.setItem("Authorization", response.data.token);
    
        const decodedToken = jwtDecode(response.data.token);
        console.log("Decoded Token:", decodedToken);

        const userId = decodedToken.userId;  // Extract userId
        const roles = decodedToken.roles; // Extract roles array

        console.log("Extracted UserId:", userId);
        console.log("Extracted Roles:", roles);

        // Navigation based on role
        if (roles.includes("ROLE_HR")) {
            navigate("/hr");
        } else if (roles.includes("ROLE_EMPLOYEE")) {
            navigate("/employee");
        } else {
            navigate("/manager");
        }
      } else {
        throw new Error(response.data.message || "Login failed. Please enter correct details");
      }
    } catch (error) {
      setError(error.response?.data?.message || "Login failed. Please enter correct details.");
    } finally {
      setIsLoading(false);
    }
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-black fixed inset-0 overflow-hidden">
      {/* The background component is now imported and used here */}
      <SpaceBackground />

      <div className="relative z-10 flex items-center justify-center w-full h-full p-4">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          className="w-full max-w-md p-8 backdrop-blur-md bg-white/10 rounded-2xl shadow-2xl"
        >
          <div className="flex flex-col items-center">
            <img className="h-16 w-auto mb-2" src={loginImage} alt="Company logo" />
            <h2 className="mt-4 text-center text-3xl font-bold text-white">
              Autonaut Portal
            </h2>
            <p className="mt-2 text-center text-sm text-blue-200">
              Sign in to explore the universe of attendance
            </p>
          </div>

          <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
            <div>
              <input
                type="email"
                placeholder="Email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full p-3 rounded bg-gray-900 text-white placeholder-gray-400 border border-gray-700 focus:border-blue-500 focus:outline-none"
              />
            </div>
            
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full p-3 rounded bg-gray-900 text-white placeholder-gray-400 border border-gray-700 focus:border-blue-500 focus:outline-none"
              />
              <button 
                type="button" 
                onClick={togglePasswordVisibility}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400"
              >
                {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
              </button>
            </div>
            
            <motion.button
              type="submit"
              disabled={isLoading}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              className="w-full py-3 bg-blue-600 text-white rounded shadow hover:bg-blue-700 transition-colors text-lg font-medium"
            >
              {isLoading ? "Loading..." : "Sign In"}
            </motion.button>
          </form>

          {error && <p className="text-red-500 text-center mt-4">{error}</p>}
        </motion.div>
      </div>
    </div>
  );
};

export default AstronautLogin;