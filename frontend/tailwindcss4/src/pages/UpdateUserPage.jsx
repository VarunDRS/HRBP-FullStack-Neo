import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from "jwt-decode";

const UpdateUserPage = () => {
  // const [employees, setEmployees] = useState([]);
  // const [selectedEmployee, setSelectedEmployee] = useState(null);
  // const [updateData, setUpdateData] = useState({
  //   roles: '',
  //   managerName: '',
  //   managerId: ''
  // });
  // const navigate = useNavigate();

  // // Fetch initial details
  // useEffect(() => {
  //   const fetchEmployees = async () => {
  //     try {
  //       const token = localStorage.getItem('Authorization');
  //       const response = await axios.get('http://localhost:8080/users/employees', {
  //         headers: { Authorization: `Bearer ${token}` }
  //       });
  //       setEmployees(response.data);
  //     } catch (error) {
  //       console.error('Error fetching employees:', error);
  //       alert('Failed to fetch employees');
  //     }
  //   };

  //   fetchEmployees();
  // }, []);
  

  // const handleEmployeeSelect = (employee) => {
  //   setSelectedEmployee(employee);
  //   setUpdateData({
  //     roles: employee.roles || '',
  //     managerName: employee.managerName || '',
  //     managerId: employee.managerId || ''
  //   });
  // };

  // const handleInputChange = (e) => {
  //   const { name, value } = e.target;
  //   setUpdateData(prevState => ({
  //     ...prevState,
  //     [name]: value
  //   }));
  // };
  
  // // Update user details
  // const handleSubmit = async (e) => {
  //   e.preventDefault();
  //   if (!selectedEmployee) {
  //     alert('Please select an employee to update');
  //     return;
  //   }

  //   try {
  //     const token = localStorage.getItem('Authorization');
  //     const response = await axios.put(`http://localhost:8080/users/update/${selectedEmployee.id}`, updateData, {
  //       headers: { Authorization: `Bearer ${token}`}
  //     });
      
  //     alert('User updated successfully!');
  //     navigate('/hr');
  //   } catch (error) {
  //     console.error('Error updating user:', error);
  //     alert('Failed to update user. Please try again.');
  //   }
  // };

  // return (
  //   <div className="min-h-screen w-full flex justify-center bg-blue-100 fixed inset-0 text-white">
  //     <div className="relative z-10 flex flex-col justify-center items-center w-full max-w-4xl p-8">
  //       <div className="bg-white/10 backdrop-blur-lg rounded-xl shadow-2xl w-full p-8">
  //         <h2 className="text-3xl font-bold mb-6 text-center text-blue-300">Update User</h2>
          
  //         {/* Employee Selection Grid */}
  //         <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 mb-6 max-h-64 overflow-y-auto">
  //           {employees.map((employee) => (
  //             <div 
  //               key={employee.id}
  //               onClick={() => handleEmployeeSelect(employee)}
  //               className={`p-4 rounded-md cursor-pointer transition duration-300 ease-in-out transform hover:scale-105 ${
  //                 selectedEmployee?.id === employee.id 
  //                 ? 'bg-blue-600' 
  //                 : 'bg-blue-900/50 hover:bg-blue-800/50'
  //               }`}
  //             >
  //               <p className="font-semibold">{employee.name}</p>
  //               <p className="text-sm text-blue-200">{employee.email}</p>
  //             </div>
  //           ))}
  //         </div>

  //         {/* Update Form */}
  //         {selectedEmployee && (
  //           <form onSubmit={handleSubmit} className="space-y-4">
  //             <div>
  //               <label className="block text-sm font-medium text-blue-200 mb-2">Selected Employee</label>
  //               <input
  //                 type="text"
  //                 value={`${selectedEmployee.name} (${selectedEmployee.email})`}
  //                 disabled
  //                 className="w-full px-3 py-2 bg-blue-900/50 border border-blue-700 rounded-md text-white opacity-70"
  //               />
  //             </div>
              
  //             <div>
  //               <label className="block text-sm font-medium text-blue-200 mb-2">Role</label>
  //               <select
  //                 name="roles"
  //                 value={updateData.roles}
  //                 onChange={handleInputChange}
  //                 required
  //                 className="w-full px-3 py-2 bg-blue-900/50 border border-blue-700 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
  //               >
  //                 <option value="">Select Role</option>
  //                 <option value="HR">HR</option>
  //                 <option value="MANAGER">Manager</option>
  //                 <option value="EMPLOYEE">Employee</option>
  //               </select>
  //             </div>
              
  //             <div>
  //               <label className="block text-sm font-medium text-blue-200 mb-2">Manager Name</label>
  //               <input
  //                 type="text"
  //                 name="managerName"
  //                 value={updateData.managerName}
  //                 onChange={handleInputChange}
  //                 className="w-full px-3 py-2 bg-blue-900/50 border border-blue-700 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
  //               />
  //             </div>
              
  //             <div>
  //               <label className="block text-sm font-medium text-blue-200 mb-2">Manager ID</label>
  //               <input
  //                 type="text"
  //                 name="managerId"
  //                 value={updateData.managerId}
  //                 onChange={handleInputChange}
  //                 className="w-full px-3 py-2 bg-blue-900/50 border border-blue-700 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
  //               />
  //             </div>
              
  //             <div className="pt-4">
  //               <button
  //                 type="submit"
  //                 className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-md transition duration-300 ease-in-out transform hover:scale-105 focus:outline-none focus:ring-2 focus:ring-blue-500"
  //               >
  //                 Update User
  //               </button>
  //             </div>
  //           </form>
  //         )}
  //       </div>
  //     </div>
  //   </div>
  // );
};

export default UpdateUserPage;