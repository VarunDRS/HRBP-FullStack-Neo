import React, { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { motion } from "framer-motion";
import * as THREE from "three";

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
  const canvasRef = useRef(null);

  // Set up Three.js space scene with car and rocket - copied from login page
  useEffect(() => {
    if (!canvasRef.current) return;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(
      75,
      window.innerWidth / window.innerHeight,
      0.1,
      1000
    );
    camera.position.z = 5;

    const renderer = new THREE.WebGLRenderer({
      canvas: canvasRef.current,
      alpha: true,
      antialias: true
    });
    renderer.setSize(window.innerWidth, window.innerHeight);
    renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

    // Create stars background
    const starGeometry = new THREE.BufferGeometry();
const starCount = 1500;
const starPositions = new Float32Array(starCount * 3);

for (let i = 0; i < starCount * 3; i += 3) {
  starPositions[i] = (Math.random() - 0.5) * 100;
  starPositions[i + 1] = (Math.random() - 0.5) * 100;
  starPositions[i + 2] = (Math.random() - 0.5) * 100;
}

starGeometry.setAttribute(
  'position',
  new THREE.BufferAttribute(starPositions, 3)
);

const starMaterial = new THREE.PointsMaterial({
  color: 0xffffff,
  size: 0.3,  // Increased size for better visibility
  transparent: true,
  opacity: 0.8,  // Increased opacity
  sizeAttenuation: true  // Make stars smaller when farther away
});

const stars = new THREE.Points(starGeometry, starMaterial);
scene.add(stars);

    // Add ambient light
    const ambientLight = new THREE.AmbientLight(0x404040);
    scene.add(ambientLight);

    // Add directional light
    const directionalLight = new THREE.DirectionalLight(0xffffff, 1);
    directionalLight.position.set(5, 5, 5);
    scene.add(directionalLight);

    // Create a simple car model
    const createCar = () => {
      const car = new THREE.Group();
      
      // Car body
      const bodyGeometry = new THREE.BoxGeometry(1, 0.4, 2);
      const bodyMaterial = new THREE.MeshPhongMaterial({ color: 0x3498db });
      const body = new THREE.Mesh(bodyGeometry, bodyMaterial);
      body.position.y = 0.2;
      car.add(body);
      
      // Car top
      const topGeometry = new THREE.BoxGeometry(0.8, 0.3, 1);
      const topMaterial = new THREE.MeshPhongMaterial({ color: 0x2980b9 });
      const top = new THREE.Mesh(topGeometry, topMaterial);
      top.position.y = 0.55;
      top.position.z = -0.2;
      car.add(top);
      
      // Car wheels
      const wheelGeometry = new THREE.CylinderGeometry(0.2, 0.2, 0.1, 16);
      const wheelMaterial = new THREE.MeshPhongMaterial({ color: 0x333333 });
      
      const wheelPositions = [
        { x: -0.5, y: 0, z: 0.7 },
        { x: 0.5, y: 0, z: 0.7 },
        { x: -0.5, y: 0, z: -0.7 },
        { x: 0.5, y: 0, z: -0.7 }
      ];
      
      wheelPositions.forEach(position => {
        const wheel = new THREE.Mesh(wheelGeometry, wheelMaterial);
        wheel.position.set(position.x, position.y, position.z);
        wheel.rotation.x = Math.PI / 2;
        car.add(wheel);
      });
      
      // Windows (transparent)
      const windowMaterial = new THREE.MeshPhongMaterial({ 
        color: 0x84d2f6, 
        transparent: true,
        opacity: 0.7
      });
      
      // Windshield
      const windshieldGeometry = new THREE.PlaneGeometry(0.7, 0.3);
      const windshield = new THREE.Mesh(windshieldGeometry, windowMaterial);
      windshield.position.set(0, 0.55, 0.3);
      windshield.rotation.x = Math.PI / 2 - 0.5;
      car.add(windshield);
      
      // Headlights
      const headlightGeometry = new THREE.CircleGeometry(0.1, 16);
      const headlightMaterial = new THREE.MeshPhongMaterial({ color: 0xffff00, emissive: 0xffff00 });
      
      const leftHeadlight = new THREE.Mesh(headlightGeometry, headlightMaterial);
      leftHeadlight.position.set(-0.3, 0.2, 1);
      car.add(leftHeadlight);
      
      const rightHeadlight = new THREE.Mesh(headlightGeometry, headlightMaterial);
      rightHeadlight.position.set(0.3, 0.2, 1);
      car.add(rightHeadlight);
      
      // Scale and position the car
      car.scale.set(0.3, 0.3, 0.3);
      car.position.set(-5 + Math.random() * 3, Math.random() * 3 - 1.5, -5);
      car.rotation.y = Math.PI / 2;
      
      return car;
    };
    
    // Create a simple rocket model
    const createRocket = () => {
      const rocket = new THREE.Group();
      
      // Rocket body
      const bodyGeometry = new THREE.CylinderGeometry(0.2, 0.2, 1.5, 16);
      const bodyMaterial = new THREE.MeshPhongMaterial({ color: 0xe74c3c });
      const body = new THREE.Mesh(bodyGeometry, bodyMaterial);
      rocket.add(body);
      
      // Rocket nose cone
      const noseGeometry = new THREE.ConeGeometry(0.2, 0.5, 16);
      const noseMaterial = new THREE.MeshPhongMaterial({ color: 0xc0392b });
      const nose = new THREE.Mesh(noseGeometry, noseMaterial);
      nose.position.y = 1;
      rocket.add(nose);
      
      // Rocket fins
      const finGeometry = new THREE.BoxGeometry(0.1, 0.5, 0.3);
      const finMaterial = new THREE.MeshPhongMaterial({ color: 0xc0392b });
      
      const finPositions = [
        { x: 0, y: -0.5, z: 0.3, ry: 0 },
        { x: 0.3, y: -0.5, z: 0, ry: Math.PI / 2 },
        { x: 0, y: -0.5, z: -0.3, ry: Math.PI },
        { x: -0.3, y: -0.5, z: 0, ry: -Math.PI / 2 }
      ];
      
      finPositions.forEach(position => {
        const fin = new THREE.Mesh(finGeometry, finMaterial);
        fin.position.set(position.x, position.y, position.z);
        fin.rotation.y = position.ry;
        rocket.add(fin);
      });
      
      // Rocket windows (portholes)
      const windowGeometry = new THREE.CircleGeometry(0.05, 16);
      const windowMaterial = new THREE.MeshPhongMaterial({ color: 0xecf0f1 });
      
      const window1 = new THREE.Mesh(windowGeometry, windowMaterial);
      window1.position.set(0, 0.3, 0.21);
      window1.rotation.y = Math.PI;
      rocket.add(window1);
      
      const window2 = new THREE.Mesh(windowGeometry, windowMaterial);
      window2.position.set(0, 0, 0.21);
      window2.rotation.y = Math.PI;
      rocket.add(window2);
      
      const window3 = new THREE.Mesh(windowGeometry, windowMaterial);
      window3.position.set(0, -0.3, 0.21);
      window3.rotation.y = Math.PI;
      rocket.add(window3);
      
      // Rocket exhaust fire
      const exhaustGeometry = new THREE.ConeGeometry(0.2, 0.7, 16);
      const exhaustMaterial = new THREE.MeshPhongMaterial({ 
        color: 0xf39c12, 
        emissive: 0xf39c12,
        transparent: true,
        opacity: 0.8
      });
      const exhaust = new THREE.Mesh(exhaustGeometry, exhaustMaterial);
      exhaust.position.y = -1.15;
      exhaust.rotation.x = Math.PI;
      rocket.add(exhaust);
      
      // Scale and position the rocket
      rocket.scale.set(0.3, 0.3, 0.3);
      rocket.position.set(5 + Math.random() * 3, Math.random() * 3 - 1.5, -5);
      rocket.rotation.z = -Math.PI / 15;
      
      return rocket;
    };
    
    const car = createCar();
    const rocket = createRocket();
    
    scene.add(car);
    scene.add(rocket);
    
    // Animation variables
    const clock = new THREE.Clock();
    
    const animate = () => {
      const elapsedTime = clock.getElapsedTime();
      
      // Rotate stars slowly
      stars.rotation.y = elapsedTime * 0.05;
      stars.rotation.x = elapsedTime * 0.025;
      
      // Animate car movement
      car.position.x += 0.02;
      if (car.position.x > 8) {
        car.position.x = -8;
        car.position.y = Math.random() * 3 - 1.5;
      }
      
      // Add slight floating motion to car
      car.position.y += Math.sin(elapsedTime * 2) * 0.003;
      
      // Animate rocket movement
      rocket.position.x -= 0.03;
      if (rocket.position.x < -8) {
        rocket.position.x = 8;
        rocket.position.y = Math.random() * 3 - 1.5;
      }
      
      // Add slight floating motion to rocket
      rocket.position.y += Math.sin(elapsedTime * 3) * 0.005;
      
      renderer.render(scene, camera);
      requestAnimationFrame(animate);
    };

    animate();

    const handleResize = () => {
      camera.aspect = window.innerWidth / window.innerHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(window.innerWidth, window.innerHeight);
    };

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      renderer.dispose();
      scene.clear();
    };
  }, []);

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

  const renderPasswordStrengthBar = () => {
    const colors = ['bg-red-500', 'bg-yellow-500', 'bg-green-500', 'bg-green-600', 'bg-green-700'];
    const width = (passwordStrength / 5) * 100;
    
    return (
      <div className="mt-2">
        <div className="flex justify-between mb-1 text-xs">
          <span>Password Strength</span>
          <span>{['Weak', 'Fair', 'Good', 'Strong', 'Excellent'][passwordStrength - 1] || 'None'}</span>
        </div>
        <div className="w-full h-2 bg-gray-200 rounded-full">
          <div 
            className={`h-full rounded-full ${colors[passwordStrength - 1] || ''}`}
            style={{ width: `${width}%` }}
          ></div>
        </div>
      </div>
    );
  };

  const renderPasswordValidation = () => {
    return (
      <div className="mt-3 text-sm">
        <div className="flex items-center mb-1">
          {passwordValidations.length ? <Check size={16} className="text-green-500 mr-2" /> : <X size={16} className="text-red-500 mr-2" />}
          <span>At least 8 characters</span>
        </div>
        <div className="flex items-center mb-1">
          {passwordValidations.uppercase ? <Check size={16} className="text-green-500 mr-2" /> : <X size={16} className="text-red-500 mr-2" />}
          <span>At least one uppercase letter</span>
        </div>
        <div className="flex items-center mb-1">
          {passwordValidations.lowercase ? <Check size={16} className="text-green-500 mr-2" /> : <X size={16} className="text-red-500 mr-2" />}
          <span>At least one lowercase letter</span>
        </div>
        <div className="flex items-center mb-1">
          {passwordValidations.number ? <Check size={16} className="text-green-500 mr-2" /> : <X size={16} className="text-red-500 mr-2" />}
          <span>At least one number</span>
        </div>
        <div className="flex items-center mb-1">
          {passwordValidations.specialChar ? <Check size={16} className="text-green-500 mr-2" /> : <X size={16} className="text-red-500 mr-2" />}
          <span>At least one special character</span>
        </div>
      </div>
    );
  };

  const nextStep = () => {
    setActiveStep(activeStep + 1);
  };

  const prevStep = () => {
    setActiveStep(activeStep - 1);
  };

  const renderStepContent = () => {
    switch (activeStep) {
      case 0:
        return (
          <div className="space-y-4">
            <div className="mb-4">
              <label htmlFor="userId" className="block text-sm font-medium text-gray-700 mb-1">
                User ID
              </label>
              <div className="relative">
                <span className="absolute left-3 top-3 text-gray-400">
                  <User size={18} />
                </span>
                <input
                  type="text"
                  id="userId"
                  name="userId"
                  value={formData.userId}
                  onChange={handleInputChange}
                  className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Enter user ID"
                  required
                />
              </div>
            </div>
            <div className="mb-4">
              <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-1">
                Username
              </label>
              <div className="relative">
                <span className="absolute left-3 top-3 text-gray-400">
                  <User size={18} />
                </span>
                <input
                  type="text"
                  id="username"
                  name="username"
                  value={formData.username}
                  onChange={handleInputChange}
                  className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Enter username"
                  required
                />
              </div>
            </div>
            <div className="mb-4">
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
                Email
              </label>
              <div className="relative">
                <span className="absolute left-3 top-3 text-gray-400">
                  <Mail size={18} />
                </span>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className={`w-full pl-10 pr-3 py-2 border ${emailError ? 'border-red-500' : 'border-gray-300'} rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500`}
                  placeholder="Enter email address"
                  required
                />
              </div>
              {emailError && <p className="text-red-500 text-xs mt-1">{emailError}</p>}
            </div>
          </div>
        );
      case 1:
        return (
          <div className="space-y-4">
            <div className="mb-4">
              <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
                Password
              </label>
              <div className="relative">
                <span className="absolute left-3 top-3 text-gray-400">
                  <Lock size={18} />
                </span>
                <input
                  type="password"
                  id="password"
                  name="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Enter password"
                  required
                />
              </div>
              {renderPasswordStrengthBar()}
              {renderPasswordValidation()}
            </div>
            <div className="mb-4">
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-1">
                Confirm Password
              </label>
              <div className="relative">
                <span className="absolute left-3 top-3 text-gray-400">
                  <Lock size={18} />
                </span>
                <input
                  type="password"
                  id="confirmPassword"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleInputChange}
                  className={`w-full pl-10 pr-3 py-2 border ${
                    formData.confirmPassword && formData.password !== formData.confirmPassword
                      ? 'border-red-500'
                      : 'border-gray-300'
                  } rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500`}
                  placeholder="Confirm password"
                  required
                />
              </div>
              {formData.confirmPassword && formData.password !== formData.confirmPassword && (
                <p className="text-red-500 text-xs mt-1">Passwords do not match</p>
              )}
            </div>
          </div>
        );
      case 2:
        return (
          <div className="space-y-4">
            <div className="mb-4">
              <label htmlFor="roles" className="block text-sm font-medium text-gray-700 mb-1">
                Roles
              </label>
              <div className="relative">
                <span className="absolute left-3 top-3 text-gray-400">
                  <Shield size={18} />
                </span>
                <select
                  id="roles"
                  name="roles"
                  multiple
                  value={formData.roles}
                  onChange={handleInputChange}
                  className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                >
                  <option value="USER">Employee</option>
                  <option value="MANAGER">Manager</option>
                  <option value="HR">HR</option>
                </select>
              </div>
              <p className="text-xs text-gray-500 mt-1">Hold Ctrl (or Cmd) to select multiple roles</p>
            </div>
            <div className="mb-4">
              <label htmlFor="managerId" className="block text-sm font-medium text-gray-700 mb-1">
                Manager ID
              </label>
              <div className="relative">
                <span className="absolute left-3 top-3 text-gray-400">
                  <User size={18} />
                </span>
                <input
                  type="text"
                  id="managerId"
                  name="managerId"
                  value={formData.managerId}
                  onChange={handleInputChange}
                  className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Enter manager ID"
                />
              </div>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen flex flex-col relative overflow-hidden">
      <canvas ref={canvasRef} className="absolute top-0 left-0 w-full h-full -z-10" />
      
      <div className="flex-grow flex items-center justify-center p-4">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="bg-white bg-opacity-90 p-8 rounded-lg shadow-xl w-full max-w-lg"
        >
          <div className="mb-6 text-center">
            <h1 className="text-2xl font-bold text-gray-800 flex items-center justify-center">
              <UserPlus className="mr-2" /> Create New User
            </h1>
            <p className="text-gray-600 mt-2">
              Enter user details to create a new account
            </p>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="flex justify-between mb-6">
              {Array.from({ length: 3 }).map((_, index) => (
                <div key={index} className="flex items-center">
                  <div
                    className={`rounded-full h-8 w-8 flex items-center justify-center border-2 ${
                      activeStep >= index ? 'bg-blue-500 border-blue-500 text-white' : 'border-gray-300 text-gray-500'
                    }`}
                  >
                    {index + 1}
                  </div>
                  {index < 2 && (
                    <div 
                      className={`h-1 w-10 ${activeStep > index ? 'bg-blue-500' : 'bg-gray-300'}`}
                    />
                  )}
                </div>
              ))}
            </div>

            {renderStepContent()}

            <div className="flex justify-between mt-6">
              {activeStep > 0 && (
                <button
                  type="button"
                  onClick={prevStep}
                  className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition duration-200"
                >
                  Back
                </button>
              )}
              
              {activeStep < 2 ? (
                <button
                  type="button"
                  onClick={nextStep}
                  className="ml-auto px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition duration-200 flex items-center"
                >
                  Next <ChevronRight size={16} className="ml-1" />
                </button>
              ) : (
                <button
                  type="submit"
                  className="ml-auto px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 transition duration-200 flex items-center"
                >
                  Create User <Check size={16} className="ml-1" />
                </button>
              )}
            </div>
          </form>
        </motion.div>
      </div>
    </div>
  );
};

export default CreateUserPage;