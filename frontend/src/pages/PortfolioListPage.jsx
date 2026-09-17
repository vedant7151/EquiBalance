import { useEffect, useState } from "react";
import * as api from "../api/portfolios";
import PortfolioCard from "../components/PortfolioCard.jsx";

export default function PortfolioListPage() {
  const [portfolios, setPortfolios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [threshold, setThreshold] = useState(5);

  const load = async () => {
    setLoading(true);
    try {
      const data = await api.listPortfolios();
      setPortfolios(data);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load portfolios");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await api.createPortfolio({
        name,
        description,
        baseCurrency: "USD",
        rebalanceThresholdPct: Number(threshold),
      });
      setName("");
      setDescription("");
      setThreshold(5);
      setShowForm(false);
      await load();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to create portfolio");
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-semibold">Your Portfolios</h1>
        <button
          onClick={() => setShowForm((s) => !s)}
          className="bg-emerald-700 text-white rounded px-4 py-2 text-sm hover:bg-emerald-800"
        >
          {showForm ? "Cancel" : "New Portfolio"}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleCreate} className="bg-white border border-slate-200 rounded-lg p-5 mb-6 space-y-3">
          <div>
            <label className="block text-sm mb-1">Name</label>
            <input
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full border border-slate-300 rounded px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-sm mb-1">Description</label>
            <input
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full border border-slate-300 rounded px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-sm mb-1">Rebalance threshold (%)</label>
            <input
              type="number"
              step="0.5"
              min="0.5"
              max="50"
              value={threshold}
              onChange={(e) => setThreshold(e.target.value)}
              className="w-full border border-slate-300 rounded px-3 py-2"
            />
          </div>
          <button type="submit" className="bg-emerald-700 text-white rounded px-4 py-2 text-sm hover:bg-emerald-800">
            Create
          </button>
        </form>
      )}

      {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

      {loading ? (
        <p className="text-slate-500">Loading...</p>
      ) : portfolios.length === 0 ? (
        <p className="text-slate-500">No portfolios yet. Create your first one above.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {portfolios.map((p) => (
            <PortfolioCard key={p.id} portfolio={p} />
          ))}
        </div>
      )}
    </div>
  );
}
