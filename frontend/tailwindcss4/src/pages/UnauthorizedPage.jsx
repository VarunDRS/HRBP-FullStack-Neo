import React, { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import * as THREE from "three";
import loginImage from "../assets/logo2.jpg";
import { Lock } from "lucide-react";

const UnauthorizedPage = () => {
  const canvasRef = useRef(null);
  const navigate = useNavigate();  // Import useNavigate

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

    // Animation variables
    const clock = new THREE.Clock();
    
    const animate = () => {
      const elapsedTime = clock.getElapsedTime();
      
      // Rotate stars slowly
      stars.rotation.y = elapsedTime * 0.05;
      stars.rotation.x = elapsedTime * 0.025;
      
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

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-black fixed inset-0 overflow-hidden">
      {/* Full-Screen Three.js Canvas */}
      <canvas
        ref={canvasRef}
        className="absolute top-0 left-0 w-full h-full"
      />

      {/* Main Content */}
      <div className="relative z-10 flex items-center justify-center w-full h-full p-4">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
          className="w-full max-w-md p-8 backdrop-blur-md bg-white/10 rounded-2xl shadow-2xl text-center"
        >
          <div className="flex flex-col items-center">
            <img className="h-16 w-auto mb-2" src={loginImage} alt="Company logo" />
            <div className="mb-4 text-red-400">
              <Lock size={64} strokeWidth={1.5} />
            </div>
            <h1 className="text-5xl font-bold text-red-400 mb-4">403</h1>
            <h2 className="text-3xl font-bold text-white mb-4">
              Access Denied
            </h2>
            <p className="text-blue-200 mb-6">
              Houston, you do not have clearance to access this restricted area of the mission.
            </p>
            <button 
              onClick={() => navigate(-1)}  // Go back to previous page
              className="w-full py-3 bg-red-500 !text-black rounded shadow hover:bg-red-700 transition-colors text-lg font-medium inline-block"
            >
              Return to Previous Page
            </button>
          </div>
        </motion.div>
      </div>
    </div>
  );
};

export default UnauthorizedPage;
