import { createContext, useContext, useState } from "react";
import * as authApi from "../api/auth";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : null;
  });

  const persistSession = (authResponse) => {
    localStorage.setItem("accessToken", authResponse.accessToken);
    localStorage.setItem("refreshToken", authResponse.refreshToken);
    const currentUser = {
      id: authResponse.userId,
      name: authResponse.name,
      email: authResponse.email,
    };
    localStorage.setItem("user", JSON.stringify(currentUser));
    setUser(currentUser);
  };

  const login = async (email, password) => {
    const response = await authApi.login(email, password);
    persistSession(response);
  };

  const register = async (name, email, password) => {
    const response = await authApi.register(name, email, password);
    persistSession(response);
  };

  const logout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
