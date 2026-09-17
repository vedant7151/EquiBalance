import { apiClient } from "./client";

export const register = (name, email, password) =>
  apiClient.post("/auth/register", { name, email, password }).then((res) => res.data);

export const login = (email, password) =>
  apiClient.post("/auth/login", { email, password }).then((res) => res.data);
