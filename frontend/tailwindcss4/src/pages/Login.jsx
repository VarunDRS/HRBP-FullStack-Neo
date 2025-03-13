import React, { useState, useEffect, useRef } from "react";
import { Link, useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import axios from "axios";
import { Eye, EyeOff } from "lucide-react";
import loginImage from "../assets/logo2.jpg";
import * as THREE from "three";
import { jwtDecode } from "jwt-decode";

const AstronautLogin = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const canvasRef = useRef(null);

  // Set up Three.js space scene with car and rocket
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
        throw new Error(response.data.message || "Login failed.");
      }
    } catch (error) {
      setError(error.response?.data?.message || "Login failed. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-black fixed inset-0 overflow-hidden">
      <canvas
        ref={canvasRef}
        className="absolute top-0 left-0 w-full h-full"
      />

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