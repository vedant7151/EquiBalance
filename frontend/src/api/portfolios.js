import { apiClient } from "./client";

export const listPortfolios = () => apiClient.get("/portfolios").then((res) => res.data);

export const getPortfolio = (id) => apiClient.get(`/portfolios/${id}`).then((res) => res.data);

export const createPortfolio = (payload) => apiClient.post("/portfolios", payload).then((res) => res.data);

export const updatePortfolio = (id, payload) => apiClient.put(`/portfolios/${id}`, payload).then((res) => res.data);

export const deletePortfolio = (id) => apiClient.delete(`/portfolios/${id}`);

export const addHolding = (portfolioId, payload) =>
  apiClient.post(`/portfolios/${portfolioId}/holdings`, payload).then((res) => res.data);

export const updateHolding = (portfolioId, holdingId, payload) =>
  apiClient.patch(`/portfolios/${portfolioId}/holdings/${holdingId}`, payload).then((res) => res.data);

export const removeHolding = (portfolioId, holdingId) =>
  apiClient.delete(`/portfolios/${portfolioId}/holdings/${holdingId}`);

export const getDrift = (portfolioId) => apiClient.get(`/portfolios/${portfolioId}/drift`).then((res) => res.data);

export const triggerRebalance = (portfolioId) =>
  apiClient.post(`/portfolios/${portfolioId}/rebalance`).then((res) => res.data);

export const getTransactions = (portfolioId) =>
  apiClient.get(`/portfolios/${portfolioId}/transactions`).then((res) => res.data);

export const getAnalytics = (portfolioId) =>
  apiClient.get(`/portfolios/${portfolioId}/analytics`).then((res) => res.data);

export const getPrice = (ticker) => apiClient.get(`/prices/${ticker}`).then((res) => res.data);
