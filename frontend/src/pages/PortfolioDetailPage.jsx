import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import * as api from "../api/portfolios";
import HoldingsTable from "../components/HoldingsTable.jsx";
import RebalanceModal from "../components/RebalanceModal.jsx";
import TransactionLog from "../components/TransactionLog.jsx";

export default function PortfolioDetailPage() {
  const { id } = useParams();
  const [portfolio, setPortfolio] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [error, setError] = useState("");
  const [showHoldingForm, setShowHoldingForm] = useState(false);
  const [ticker, setTicker] = useState("");
  const [units, setUnits] = useState("");
  const [targetPct, setTargetPct] = useState("");
  const [rebalanceResult, setRebalanceResult] = useState(null);
  const [tab, setTab] = useState("holdings");

  const load = async () => {
    try {
      const data = await api.getPortfolio(id);
      setPortfolio(data);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load portfolio");
    }
  };

  const loadTransactions = async () => {
    try {
      const data = await api.getTransactions(id);
      setTransactions(data);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load transactions");
    }
  };

  useEffect(() => {
    load();
  }, [id]);

  useEffect(() => {
    if (tab === "transactions") {
      loadTransactions();
    }
  }, [tab, id]);

  const handleAddHolding = async (e) => {
    e.preventDefault();
    try {
      await api.addHolding(id, {
        ticker: ticker.toUpperCase(),
        units: Number(units),
        targetAllocationPct: Number(targetPct),
      });
      setTicker("");
      setUnits("");
      setTargetPct("");
      setShowHoldingForm(false);
      await load();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to add holding");
    }
  };

  const handleRemoveHolding = async (holdingId) => {
    try {
      await api.removeHolding(id, holdingId);
      await load();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to remove holding");
    }
  };

  const handleRebalance = async () => {
    try {
      const result = await api.triggerRebalance(id);
      setRebalanceResult(result);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to generate rebalance");
    }
  };

  if (!portfolio) {
    return <p className="text-slate-500">{error || "Loading..."}</p>;
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-2">
        <div>
          <h1 className="text-2xl font-semibold">{portfolio.name}</h1>
          <p className="text-slate-500 text-sm">{portfolio.description}</p>
        </div>
        <div className="flex gap-2">
          <Link
            to={`/portfolios/${id}/analytics`}
            className="bg-slate-100 rounded px-4 py-2 text-sm hover:bg-slate-200"
          >
            Analytics
          </Link>
          <button
            onClick={handleRebalance}
            className="bg-emerald-700 text-white rounded px-4 py-2 text-sm hover:bg-emerald-800"
          >
            Rebalance
          </button>
        </div>
      </div>

      {portfolio.needsRebalancing && (
        <div className="bg-amber-50 border border-amber-200 text-amber-800 text-sm rounded px-4 py-2 mb-4">
          This portfolio has drifted beyond its {Number(portfolio.rebalanceThresholdPct).toFixed(1)}% threshold.
        </div>
      )}

      {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

      <div className="flex gap-4 border-b border-slate-200 mb-4 text-sm">
        <button
          onClick={() => setTab("holdings")}
          className={`py-2 ${tab === "holdings" ? "border-b-2 border-emerald-700 font-medium" : "text-slate-500"}`}
        >
          Holdings
        </button>
        <button
          onClick={() => setTab("transactions")}
          className={`py-2 ${tab === "transactions" ? "border-b-2 border-emerald-700 font-medium" : "text-slate-500"}`}
        >
          Transaction Log
        </button>
      </div>

      {tab === "holdings" && (
        <div className="bg-white border border-slate-200 rounded-lg p-5">
          <div className="flex items-center justify-between mb-3">
            <h2 className="font-medium">
              Total Value: {portfolio.baseCurrency} {Number(portfolio.totalValue ?? 0).toFixed(2)}
            </h2>
            <button
              onClick={() => setShowHoldingForm((s) => !s)}
              className="text-sm text-emerald-700 hover:underline"
            >
              {showHoldingForm ? "Cancel" : "+ Add Holding"}
            </button>
          </div>

          {showHoldingForm && (
            <form onSubmit={handleAddHolding} className="grid grid-cols-3 gap-3 mb-4">
              <input
                required
                placeholder="Ticker (e.g. AAPL)"
                value={ticker}
                onChange={(e) => setTicker(e.target.value)}
                className="border border-slate-300 rounded px-3 py-2 text-sm"
              />
              <input
                required
                type="number"
                step="0.000001"
                placeholder="Units"
                value={units}
                onChange={(e) => setUnits(e.target.value)}
                className="border border-slate-300 rounded px-3 py-2 text-sm"
              />
              <input
                required
                type="number"
                step="0.01"
                placeholder="Target %"
                value={targetPct}
                onChange={(e) => setTargetPct(e.target.value)}
                className="border border-slate-300 rounded px-3 py-2 text-sm"
              />
              <button
                type="submit"
                className="col-span-3 bg-emerald-700 text-white rounded py-2 text-sm hover:bg-emerald-800"
              >
                Add Holding
              </button>
            </form>
          )}

          <HoldingsTable holdings={portfolio.holdings} onRemove={handleRemoveHolding} />
        </div>
      )}

      {tab === "transactions" && (
        <div className="bg-white border border-slate-200 rounded-lg p-5">
          <TransactionLog transactions={transactions} />
        </div>
      )}

      <RebalanceModal result={rebalanceResult} onClose={() => setRebalanceResult(null)} />
    </div>
  );
}
