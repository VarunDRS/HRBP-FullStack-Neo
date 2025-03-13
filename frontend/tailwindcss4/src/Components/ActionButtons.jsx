import React from "react";
import { UserPlus, UserCog, Lock } from "lucide-react";

const ActionButtons = ({ onCreateUser, onUpdateUser, onUpdatePassword }) => {
  return (
    <div className="bg-white rounded-lg p-4 mb-6 flex flex-wrap justify-center gap-4 w-full shadow-sm">
      <button
        onClick={onCreateUser}
        className="flex-1 min-w-44 p-4 bg-blue-400 rounded-lg text-white flex flex-col items-center justify-center transition-all hover:bg-blue-600"
      >
        <UserPlus size={24} />   
        <span className="mt-2 font-medium">Create User</span>
      </button>

      <button
        onClick={onUpdatePassword}
        className="flex-1 min-w-44 p-4 bg-cyan-500 rounded-lg text-white flex flex-col items-center justify-center transition-all hover:bg-cyan-600"
      >
        <Lock size={24} />
        <span className="mt-2 font-medium">Update Password</span>
      </button>
    </div>
  );
};

export default ActionButtons;