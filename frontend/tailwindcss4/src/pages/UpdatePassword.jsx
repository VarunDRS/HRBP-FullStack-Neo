import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { useLocation } from "react-router-dom";
import { jwtDecode } from "jwt-decode";
import { toast } from "react-toastify";
import { motion } from "framer-motion";
import * as THREE from "three";
import { 
  User, 
  Lock, 
  Check, 
  X, 
  ChevronLeft,
  Shield,
  Key
} from 'lucide-react';

const UpdatePassword = () => {
  const [stage, setStage] = useState("verify"); // "verify" or "update"
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState(0);
  const [passwordValidations, setPasswordValidations] = useState({
    length: false,
    uppercase: false,
    lowercase: false,
    number: false,
    specialChar: false
  });
  const navigate = useNavigate();
  const location = useLocation();
  const canvasRef = useRef(null);

  useEffect(() => {
    localStorage.setItem("previousRoute", location.pathname);
  }, [location]);

  // Password verification animation states
  const [verifying, setVerifying] = useState(false);
  const [passwordShake, setPasswordShake] = useState(false);

  // Set up Three.js space scene
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
      size: 0.1,
      transparent: true
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

  // Calculate password strength
  useEffect(() => {
    if (!newPassword) {
      setPasswordStrength(0);
      setPasswordValidations({
        length: false,
        uppercase: false,
        lowercase: false,
        number: false,
        specialChar: false
      });
      return;
    }
    
    const validations = {
      length: newPassword.length >= 8,
      uppercase: /[A-Z]/.test(newPassword),
      lowercase: /[a-z]/.test(newPassword),
      number: /[0-9]/.test(newPassword),
      specialChar: /[!@#$%^&*(),.?":{}|<>]/.test(newPassword),
    };
    
    const strengthScore = Object.values(validations).filter((v) => v).length;
    setPasswordStrength(strengthScore);
    setPasswordValidations(validations);
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
      toast.error("Wrong password");
      setError("Current password is incorrect");
      setPasswordShake(true);
      setTimeout(() => setPasswordShake(false), 500);
    }
  };

  const handleCancelButton = () => {
    const token = localStorage.getItem("Authorization");
    const decodedToken = jwtDecode(token);
    const roles = decodedToken.roles;
    
    const previousRoute = localStorage.getItem("previousRoute") || "/";
    navigate(-1);
  };

  const handleUpdatePassword = async (e) => {
    e.preventDefault();
    
    // Validate inputs
    if (!newPassword || !confirmPassword) {
      setError("Please fill all fields");
      return;
    }
    
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    
    if (passwordStrength < 3) {
      setError("Password is not strong enough");
      return;
    }
    
    setLoading(true);
    setError("");
    
    try {
      const token = localStorage.getItem("Authorization");
      const decodedToken = jwtDecode(token);
      const userId = decodedToken.userId;
      
      const response = await axios.post(
        "http://localhost:8080/users/updatePassword",
        { 
          userId, 
          currentPassword, 
          newPassword 
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      
      if (response.data.success) {
        setLoading(false);
        setSuccess(true);
        toast.success("Password updated successfully");
        
        // Redirect after success message
        setTimeout(() => {
          navigate(-1);
        }, 2000);
      } else {
        throw new Error("Password update failed");
      }
    } catch (error) {
      console.error("Error updating password:", error);
      setLoading(false);
      setError(error.response?.data?.message || "Failed to update password");
      toast.error("Password update failed");
    }
  };
  
  return (
    <div className="relative min-h-screen w-full bg-gray-900 flex justify-center items-center overflow-hidden">
      {/* Background canvas */}
      <canvas ref={canvasRef} className="absolute top-0 left-0 w-full h-full z-0" />
      
      {/* Main content */}
      <div className="relative z-10 w-full max-w-md">
        <div className="bg-gray-800 bg-opacity-80 backdrop-blur-sm rounded-lg shadow-xl p-8 mx-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
          >
            <h2 className="text-2xl font-bold text-white mb-6 flex items-center">
              <Key className="mr-2" size={24} /> 
              {stage === "verify" ? "Verify Your Password" : "Update Password"}
            </h2>
            
            {stage === "verify" ? (
              <form onSubmit={handleVerifyPassword}>
                <div className="mb-6">
                  <label className="block text-gray-300 text-sm font-medium mb-2">
                    Current Password
                  </label>
                  <div className="relative">
                    <motion.div
                      animate={passwordShake ? { x: [-10, 10, -10, 10, 0] } : {}}
                      transition={{ duration: 0.4 }}
                    >
                      <input
                        type="password"
                        className={`w-full px-4 py-2 bg-gray-700 border ${
                          error ? "border-red-500" : "border-gray-600"
                        } rounded-lg text-white focus:outline-none focus:border-blue-500`}
                        value={currentPassword}
                        onChange={(e) => setCurrentPassword(e.target.value)}
                        disabled={loading}
                        placeholder="Enter your current password"
                      />
                    </motion.div>
                    <Lock className="absolute right-3 top-2.5 text-gray-400" size={18} />
                  </div>
                  {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
                </div>
                
                <div className="flex justify-between">
                  <button
                    type="button"
                    onClick={handleCancelButton}
                    className="px-4 py-2 bg-gray-700 text-gray-300 rounded-lg hover:bg-gray-600 transition flex items-center"
                    disabled={loading}
                  >
                    <ChevronLeft size={16} className="mr-1" />
                    Back
                  </button>
                  
                  <button
                    type="submit"
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition flex items-center"
                    disabled={loading}
                  >
                    {loading ? (
                      <div className="flex items-center">
                        <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Verifying...
                      </div>
                    ) : (
                      <>
                        Verify <Shield size={16} className="ml-1" />
                      </>
                    )}
                  </button>
                </div>
              </form>
            ) : success ? (
              <div className="text-center">
                <div className="flex justify-center mb-4">
                  <div className="h-16 w-16 bg-green-500 rounded-full flex items-center justify-center">
                    <Check size={32} className="text-white" />
                  </div>
                </div>
                <h3 className="text-xl font-medium text-white mb-2">Password Updated!</h3>
                <p className="text-gray-300 mb-6">Your password has been updated successfully.</p>
                <button
                  onClick={() => navigate("/profile")}
                  className="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
                >
                  Continue to Profile
                </button>
              </div>
            ) : (
              <form onSubmit={handleUpdatePassword}>
                <div className="mb-4">
                  <label className="block text-gray-300 text-sm font-medium mb-2">
                    New Password
                  </label>
                  <div className="relative">
                    <input
                      type="password"
                      className="w-full px-4 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white focus:outline-none focus:border-blue-500"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      disabled={loading}
                      placeholder="Create new password"
                    />
                    <Lock className="absolute right-3 top-2.5 text-gray-400" size={18} />
                  </div>
                </div>
                
                {/* Password strength indicator */}
                <div className="mb-4">
                  <div className="flex justify-between text-xs text-gray-400 mb-1">
                    <span>Password Strength</span>
                    <span>{passwordStrength === 0 ? "" : passwordStrength < 3 ? "Weak" : passwordStrength < 5 ? "Good" : "Strong"}</span>
                  </div>
                  <div className="h-2 w-full bg-gray-700 rounded-full overflow-hidden">
                    <div 
                      className={`h-full ${
                        passwordStrength < 3 
                          ? "bg-red-500" 
                          : passwordStrength < 5 
                          ? "bg-yellow-500" 
                          : "bg-green-500"
                      } transition-all duration-300`}
                      style={{ width: `${passwordStrength * 20}%` }}
                    ></div>
                  </div>
                </div>
                
                {/* Password validation list */}
                <div className="mb-4 bg-gray-700 p-3 rounded-lg">
                  <h4 className="text-sm font-medium text-gray-300 mb-2">Password must contain:</h4>
                  <ul className="space-y-1 text-sm">
                    <li className="flex items-center">
                      {passwordValidations.length ? (
                        <Check size={14} className="mr-2 text-green-500" />
                      ) : (
                        <X size={14} className="mr-2 text-gray-400" />
                      )}
                      <span className={passwordValidations.length ? "text-green-500" : "text-gray-400"}>
                        At least 8 characters
                      </span>
                    </li>
                    <li className="flex items-center">
                      {passwordValidations.uppercase ? (
                        <Check size={14} className="mr-2 text-green-500" />
                      ) : (
                        <X size={14} className="mr-2 text-gray-400" />
                      )}
                      <span className={passwordValidations.uppercase ? "text-green-500" : "text-gray-400"}>
                        Uppercase letter
                      </span>
                    </li>
                    <li className="flex items-center">
                      {passwordValidations.lowercase ? (
                        <Check size={14} className="mr-2 text-green-500" />
                      ) : (
                        <X size={14} className="mr-2 text-gray-400" />
                      )}
                      <span className={passwordValidations.lowercase ? "text-green-500" : "text-gray-400"}>
                        Lowercase letter
                      </span>
                    </li>
                    <li className="flex items-center">
                      {passwordValidations.number ? (
                        <Check size={14} className="mr-2 text-green-500" />
                      ) : (
                        <X size={14} className="mr-2 text-gray-400" />
                      )}
                      <span className={passwordValidations.number ? "text-green-500" : "text-gray-400"}>
                        Number
                      </span>
                    </li>
                    <li className="flex items-center">
                      {passwordValidations.specialChar ? (
                        <Check size={14} className="mr-2 text-green-500" />
                      ) : (
                        <X size={14} className="mr-2 text-gray-400" />
                      )}
                      <span className={passwordValidations.specialChar ? "text-green-500" : "text-gray-400"}>
                        Special character (!@#$%^&*(),.?":{}|&lt;&gt;)
                      </span>
                    </li>
                  </ul>
                </div>
                
                <div className="mb-6">
                  <label className="block text-gray-300 text-sm font-medium mb-2">
                    Confirm New Password
                  </label>
                  <div className="relative">
                    <input
                      type="password"
                      className={`w-full px-4 py-2 bg-gray-700 border ${
                        confirmPassword && confirmPassword !== newPassword ? "border-red-500" : "border-gray-600"
                      } rounded-lg text-white focus:outline-none focus:border-blue-500`}
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      disabled={loading}
                      placeholder="Confirm your new password"
                    />
                    <Lock className="absolute right-3 top-2.5 text-gray-400" size={18} />
                  </div>
                  {confirmPassword && confirmPassword !== newPassword && (
                    <p className="text-red-500 text-sm mt-1">Passwords do not match</p>
                  )}
                </div>
                
                {error && <p className="text-red-500 text-sm mb-4">{error}</p>}
                
                <div className="flex justify-between">
                  <button
                    type="button"
                    onClick={() => setStage("verify")}
                    className="px-4 py-2 bg-gray-700 text-gray-300 rounded-lg hover:bg-gray-600 transition flex items-center"
                    disabled={loading}
                  >
                    <ChevronLeft size={16} className="mr-1" />
                    Back
                  </button>
                  
                  <button
                    type="submit"
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition flex items-center"
                    disabled={loading || passwordStrength < 3 || !confirmPassword || confirmPassword !== newPassword}
                  >
                    {loading ? (
                      <div className="flex items-center">
                        <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Updating...
                      </div>
                    ) : (
                      <>
                        Update Password <Check size={16} className="ml-1" />
                      </>
                    )}
                  </button>
                </div>
              </form>
            )}
          </motion.div>
        </div>
      </div>
    </div>
  );
  };
  
  export default UpdatePassword;