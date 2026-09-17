import { useEffect, useState } from "react";
import * as api from "../api/portfolios";

/**
 * Stretch-goal feature (PRD 6.7). There is no dedicated backend entity/table for
 * watchlists yet - tickers are kept in localStorage and prices are pulled through
 * the existing /api/prices/{ticker} endpoint (same Redis-cached path portfolios use).
 * See README "Known Limitations" for the plan to move this server-side.
 */
export default function WatchlistPage() {
  const [tickers, setTickers] = useState(() => {
    const stored = localStorage.getItem("watchlist");
    return stored ? JSON.parse(stored) : [];
  });
  const [prices, setPrices] = useState({});
  const [newTicker, setNewTicker] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    localStorage.setItem("watchlist", JSON.stringify(tickers));
  }, [tickers]);

  const refreshPrices = async () => {
    const results = {};
    for (const ticker of tickers) {
      try {
        results[ticker] = await api.getPrice(ticker);
      } catch (err) {
        results[ticker] = null;
      }
    }
    setPrices(results);
  };

  useEffect(() => {
    if (tickers.length > 0) {
      refreshPrices();
    }
  }, [tickers]);

  const handleAdd = (e) => {
    e.preventDefault();
    const symbol = newTicker.trim().toUpperCase();
    if (!symbol) return;
    if (tickers.includes(symbol)) {
      setError("Already on your watchlist");
      return;
    }
    setError("");
    setTickers((prev) => [...prev, symbol]);
    setNewTicker("");
  };

  const handleRemove = (symbol) => {
    setTickers((prev) => prev.filter((t) => t !== symbol));
  };

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-6">Watchlist</h1>

      <form onSubmit={handleAdd} className="flex gap-2 mb-4">
        <input
          value={newTicker}
          onChange={(e) => setNewTicker(e.target.value)}
          placeholder="Add ticker, e.g. MSFT"
          className="border border-slate-300 rounded px-3 py-2 text-sm flex-1"
        />
        <button type="submit" className="bg-emerald-700 text-white rounded px-4 py-2 text-sm hover:bg-emerald-800">
          Add
        </button>
      </form>

      {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

      {tickers.length === 0 ? (
        <p className="text-slate-500 text-sm">Your watchlist is empty.</p>
      ) : (
        <div className="bg-white border border-slate-200 rounded-lg p-5">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-slate-500 border-b border-slate-200">
                <th className="py-2">Ticker</th>
                <th className="py-2">Price</th>
                <th className="py-2">Fetched At</th>
                <th className="py-2"></th>
              </tr>
            </thead>
            <tbody>
              {tickers.map((t) => {
                const p = prices[t];
                return (
                  <tr key={t} className="border-b border-slate-100">
                    <td className="py-2 font-medium">{t}</td>
                    <td className="py-2">
                      {p ? Number(p.price).toFixed(2) : "—"}
                      {p?.stale && (
                        <span className="ml-2 text-xs bg-slate-100 text-slate-500 px-1.5 py-0.5 rounded">
                          stale
                        </span>
                      )}
                    </td>
                    <td className="py-2">{p ? new Date(p.fetchedAt).toLocaleTimeString() : "—"}</td>
                    <td className="py-2 text-right">
                      <button onClick={() => handleRemove(t)} className="text-xs text-red-600 hover:underline">
                        Remove
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
