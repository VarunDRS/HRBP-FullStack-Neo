import { Navigate } from "react-router-dom";
import axios from "axios";
import { jwtDecode } from "jwt-decode";

const ProtectedRoute = ({ children, allowedRoles }) => {
  const token = localStorage.getItem("Authorization");
  const decodedToken = jwtDecode(token);
  const userId = decodedToken.userId;  
  const userRole = decodedToken.roles?.[0];

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // // Fetch user role based on token and userId
  // const getUserRole = async () => {
  //   try {
  //     const response = await axios.get(`http://localhost:8080/users/${userId}`, {
  //       headers: { Authorization: token },
  //     });
  //     return response.data.role; 
  //   } catch (error) {
  //     console.error("Error fetching user role:", error);
  //     return null;
  //   }
  // };


  // if (!userRole) {
  //   getUserRole().then((role) => {
  //     if (role) {
  //       localStorage.setItem("userRole", role);
  //       window.location.reload(); 
  //     }
  //   });
  //   return null; 
  // }

  if (!allowedRoles.includes(userRole)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

export default ProtectedRoute;
