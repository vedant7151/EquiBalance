import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import * as api from "../api/portfolios";
import AllocationChart from "../components/AllocationChart.jsx";
import DriftChart from "../components/DriftChart.jsx";
import PerformanceChart from "../components/PerformanceChart.jsx";

export default function AnalyticsPage() {
  const { id } = useParams();
  const [analytics, setAnalytics] = useState(null);
  const [drift, setDrift] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    const load = async () => {
      try {
        const [analyticsData, driftData] = await Promise.all([api.getAnalytics(id), api.getDrift(id)]);
        setAnalytics(analyticsData);
        setDrift(driftData);
      } catch (err) {
        setError(err.response?.data?.message || "Failed to load analytics");
      }
    };
    load();
  }, [id]);

  if (error) return <p className="text-sm text-red-600">{error}</p>;
  if (!analytics || !drift) return <p className="text-slate-500">Loading...</p>;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-semibold">Analytics</h1>
        <Link to={`/portfolios/${id}`} className="text-sm text-emerald-700 hover:underline">
          Back to Portfolio
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-white border border-slate-200 rounded-lg p-4">
          <p className="text-sm text-slate-500">Total Value</p>
          <p className="text-2xl font-semibold">{Number(analytics.totalValue ?? 0).toFixed(2)}</p>
        </div>
        <div className="bg-white border border-slate-200 rounded-lg p-4">
          <p className="text-sm text-slate-500">Day Gain/Loss</p>
          <p className="text-2xl font-semibold">{Number(analytics.dayGainLoss ?? 0).toFixed(2)}</p>
          <p className="text-xs text-slate-400">Requires historical close data - not yet wired up</p>
        </div>
        <div className="bg-white border border-slate-200 rounded-lg p-4">
          <p className="text-sm text-slate-500">Overall Return</p>
          <p className="text-2xl font-semibold">{Number(analytics.overallReturnPct ?? 0).toFixed(2)}%</p>
          <p className="text-xs text-slate-400">Requires cost-basis tracking - not yet wired up</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <AllocationChart data={analytics.currentAllocation} title="Current Allocation" />
        <AllocationChart data={analytics.targetAllocation} title="Target Allocation" />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <DriftChart items={drift.items} />
        <PerformanceChart points={analytics.performanceHistory} />
      </div>

      <div className="bg-white border border-slate-200 rounded-lg p-5">
        <h3 className="text-sm font-medium mb-3 text-slate-700">Rebalancing History</h3>
        {analytics.rebalanceHistory.length === 0 ? (
          <p className="text-sm text-slate-500">No rebalancing events yet.</p>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-slate-500 border-b border-slate-200">
                <th className="py-2">Date</th>
                <th className="py-2">Total Drift Before</th>
                <th className="py-2">Trades Suggested</th>
                <th className="py-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {analytics.rebalanceHistory.map((r) => (
                <tr key={r.id} className="border-b border-slate-100">
                  <td className="py-2">{new Date(r.triggeredAt).toLocaleString()}</td>
                  <td className="py-2">{Number(r.totalDriftBefore).toFixed(2)}%</td>
                  <td className="py-2">{r.tradesCount}</td>
                  <td className="py-2">{r.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
