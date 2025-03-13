import React, { useState, useRef } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { 
  User, 
  Mail, 
  Lock, 
  Check, 
  X, 
  ChevronRight, 
  UserPlus, 
  Shield
} from 'lucide-react';
import { toast } from 'react-toastify';

const CreateUserPage = () => {
  const [formData, setFormData] = useState({
    userId: '',
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    roles: [],
    managerName: '',
    managerId: ''
  });
  const [passwordStrength, setPasswordStrength] = useState(0);
  const [passwordValidations, setPasswordValidations] = useState({
    length: false,
    uppercase: false,
    lowercase: false,
    number: false,
    specialChar: false
  });
  const [activeStep, setActiveStep] = useState(0);
  const [emailError, setEmailError] = useState('');
  const navigate = useNavigate();
  const fileInputRef = useRef(null);

  const handleInputChange = (e) => {
    const { name, value, type, selectedOptions } = e.target;
    if (name === 'email') {
      const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (!emailPattern.test(value)) {
        setEmailError('Invalid email format');
      } else {
        setEmailError('');
      }
    }
    if (type === 'select-multiple') {
      const values = Array.from(selectedOptions, (option) => option.value);
      setFormData((prevState) => ({
        ...prevState,
        [name]: values,
      }));
    } else {
      if (name === 'password') {
        const validations = {
          length: value.length >= 8,
          uppercase: /[A-Z]/.test(value),
          lowercase: /[a-z]/.test(value),
          number: /[0-9]/.test(value),
          specialChar: /[!@#$%^&*(),.?":{}|<>]/.test(value),
        };
        const strengthScore = Object.values(validations).filter((v) => v).length;
        setPasswordStrength(strengthScore);
        setPasswordValidations(validations);
      }
      setFormData((prevState) => ({
        ...prevState,
        [name]: value,
      }));
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate password match
    if (formData.password !== formData.confirmPassword) {
      alert('Passwords do not match');
      return;
    }

    try {
      const token = localStorage.getItem('Authorization');
      
      // Remove confirmPassword before sending to backend
      const submitData = { ...formData };
      delete submitData.confirmPassword;

      const response = await axios.post('http://localhost:8080/hr/createUser', submitData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      toast.success('User created successfully!');
      navigate('/hr');

    } catch (error) {

      if (error.response && error.response.data) {
        const { statuscode, message, data } = error.response.data;

        if (statuscode === 400 && data) {
          let errorMessages = Object.values(data).join('\n');
          toast.error(`${errorMessages}`);
        } else {
          toast.error(`${message}`);
        }
      } else {
        toast.error('An unexpected error occurred. Please try again.');
      }
    }
  };

  const renderPasswordStrengthBar = () => {
    const colors = ['bg-red-500', 'bg-yellow-500', 'bg-green-500', 'bg-green-700', 'bg-green-900'];
    return (
      <div className="flex space-x-1 mt-1">
        {[0, 1, 2, 3, 4].map((index) => (
          <div 
            key={index} 
            className={`h-1 flex-1 rounded-full ${
              index < passwordStrength ? colors[index] : 'bg-gray-300'
            }`}
          />
        ))}
      </div>
    );
  };

  const steps = [
    {
      title: 'Basic Information',
      fields: ['userId', 'username', 'email']
    },
    {
      title: 'Security',
      fields: ['password', 'confirmPassword']
    },
    {
      title: 'Role & Management',
      fields: ['roles', 'managerName', 'managerId']
    }
  ];

  const isStepValid = (step) => {
    return steps[step].fields.every(field => 
      formData[field] && (field !== 'password' || passwordStrength > 4)
    );
  };

  const nextStep = () => {
    if (isStepValid(activeStep)) {
      setActiveStep(Math.min(activeStep + 1, steps.length - 1));
    } else {
      alert('Please fill all required fields correctly');
    }
  };

  const prevStep = () => {
    setActiveStep(Math.max(activeStep - 1, 0));
  };

  return (
    <div className="min-h-screen w-full flex justify-center bg-gradient-to-br from-blue-100 to-indigo-200 fixed inset-0">
      <div className="relative z-10 flex flex-col justify-center items-center w-full max-w-2xl p-8">
        <div className="bg-white/20 backdrop-blur-2xl rounded-2xl shadow-2xl w-full p-10 border border-white/30">
          {/* Stepper */}
          <div className="flex justify-between mb-8">
            {steps.map((step, index) => (
              <div 
                key={index} 
                className={`flex-1 text-center py-2 mx-2 rounded-full transition-all duration-300 ${
                  activeStep === index 
                    ? 'bg-blue-600 text-white' 
                    : activeStep > index 
                      ? 'bg-green-500 text-white' 
                      : 'bg-gray-200 text-gray-500'
                }`}
              >
                {step.title}
              </div>
            ))}
          </div>

          {/* Form Content */}
          <form onSubmit={handleSubmit} className="space-y-6">
            {activeStep === 0 && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="flex items-center text-sm font-medium text-blue-900 mb-2">
                    <User className="mr-2 text-blue-600" size={18} />
                    User ID
                  </label>
                  <input
                    type="text"
                    name="userId"
                    value={formData.userId}
                    onChange={handleInputChange}
                    required
                    className="w-full px-4 py-3 bg-white/50 border border-blue-300 rounded-xl text-blue-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="flex items-center text-sm font-medium text-blue-900 mb-2">
                    <UserPlus className="mr-2 text-blue-600" size={18} />
                    Username
                  </label>
                  <input
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleInputChange}
                    required
                    className="w-full px-4 py-3 bg-white/50 border border-blue-300 rounded-xl text-blue-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div className="md:col-span-2">
                  <label className="flex items-center text-sm font-medium text-blue-900 mb-2">
                    <Mail className="mr-2 text-blue-600" size={18} />
                    Email
                  </label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    required
                    className="w-full px-4 py-3 bg-white/50 border border-blue-300 rounded-xl text-blue-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>
            )}

            {activeStep === 1 && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="flex items-center text-sm font-medium text-blue-900 mb-2">
                    <Lock className="mr-2 text-blue-600" size={18} />
                    Password
                  </label>
                  <input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleInputChange}
                    required
                    className="w-full px-4 py-3 bg-white/50 border border-blue-300 rounded-xl text-blue-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                  {renderPasswordStrengthBar()}
                  <div className="mt-2 grid grid-cols-2 gap-2 text-xs">
                    <span className={`flex items-center ${passwordValidations.length ? 'text-green-600' : 'text-gray-400'}`}>
                      {passwordValidations.length ? <Check size={12} className="mr-1" /> : <X size={12} className="mr-1" />}
                      8+ Characters
                    </span>
                    <span className={`flex items-center ${passwordValidations.uppercase ? 'text-green-600' : 'text-gray-400'}`}>
                      {passwordValidations.uppercase ? <Check size={12} className="mr-1" /> : <X size={12} className="mr-1" />}
                      Uppercase Letter
                    </span>
                    <span className={`flex items-center ${passwordValidations.lowercase ? 'text-green-600' : 'text-gray-400'}`}>
                      {passwordValidations.lowercase ? <Check size={12} className="mr-1" /> : <X size={12} className="mr-1" />}
                      Lowercase Letter
                    </span>
                    <span className={`flex items-center ${passwordValidations.number ? 'text-green-600' : 'text-gray-400'}`}>
                      {passwordValidations.number ? <Check size={12} className="mr-1" /> : <X size={12} className="mr-1" />}
                      Number
                    </span>
                    <span className={`flex items-center ${passwordValidations.specialChar ? 'text-green-600' : 'text-gray-400'}`}>
                      {passwordValidations.specialChar ? <Check size={12} className="mr-1" /> : <X size={12} className="mr-1" />}
                      Special Character
                    </span>
                  </div>
                </div>
                <div>
                  <label className="flex items-center text-sm font-medium text-blue-900 mb-2">
                    <Lock className="mr-2 text-blue-600" size={18} />
                    Confirm Password
                  </label>
                  <input
                    type="password"
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleInputChange}
                    required
                    className="w-full px-4 py-3 bg-white/50 border border-blue-300 rounded-xl text-blue-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>
            )}

            {activeStep === 2 && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="flex items-center text-sm font-medium text-blue-900 mb-2">
                    <Shield className="mr-2 text-blue-600" size={18} />
                    Role
                  </label>
                  <select
                    name="roles"
                    value={formData.roles}
                    onChange={handleInputChange}
                    required
                    multiple
                    className="w-full px-4 py-3 bg-white/50 border border-blue-300 rounded-xl text-blue-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="HR">HR</option>
                    <option value="MANAGER">Manager</option>
                    <option value="EMPLOYEE">Employee</option>
                  </select>

                </div>
                <div>
                  <label className="flex items-center text-sm font-medium text-blue-900 mb-2">
                    <UserPlus className="mr-2 text-blue-600" size={18} />
                    Manager Name
                  </label>
                  <input
                    type="text"
                    name="managerName"
                    value={formData.managerName}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 bg-white/50 border border-blue-300 rounded-xl text-blue-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="flex items-center text-sm font-medium text-blue-900 mb-2">
                    <UserPlus className="mr-2 text-blue-600" size={18} />
                    Manager ID
                  </label>
                  <input
                    type="text"
                    name="managerId"
                    value={formData.managerId}
                    onChange={handleInputChange}
                    className="w-full px-4 py-3 bg-white/50 border border-blue-300 rounded-xl text-blue-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              </div>
            )}

            {/* Navigation Buttons */}
            <div className="flex justify-between mt-8">
              {activeStep > 0 && (
                <button
                  type="button"
                  onClick={prevStep}
                  className="flex items-center bg-gray-200 text-gray-800 px-6 py-3 rounded-xl hover:bg-gray-300 transition-colors"
                >
                  Previous
                </button>
              )}
              
              {activeStep < steps.length - 1 && (
                <button
                  type="button"
                  onClick={nextStep}
                  className="ml-auto flex items-center bg-blue-600 text-white px-6 py-3 rounded-xl hover:bg-blue-700 transition-colors"
                >
                  Next
                  <ChevronRight className="ml-2" size={18} />
                </button>
              )}
              
              {activeStep === steps.length - 1 && (
                <button
                  type="submit"
                  className="ml-auto flex items-center bg-green-600 text-white px-6 py-3 rounded-xl hover:bg-green-700 transition-colors"
                >
                  Create User
                  <UserPlus className="ml-2" size={18} />
                </button>
              )}
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateUserPage;