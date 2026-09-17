export default function HoldingsTable({ holdings, onRemove }) {
  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="text-left text-slate-500 border-b border-slate-200">
          <th className="py-2">Ticker</th>
          <th className="py-2">Units</th>
          <th className="py-2">Price</th>
          <th className="py-2">Value</th>
          <th className="py-2">Target %</th>
          <th className="py-2">Current %</th>
          <th className="py-2">Drift</th>
          <th className="py-2"></th>
        </tr>
      </thead>
      <tbody>
        {holdings.map((h) => (
          <tr key={h.id} className="border-b border-slate-100">
            <td className="py-2 font-medium">
              {h.ticker}
              {h.stale && (
                <span className="ml-2 text-xs bg-slate-100 text-slate-500 px-1.5 py-0.5 rounded">
                  stale
                </span>
              )}
            </td>
            <td className="py-2">{h.units}</td>
            <td className="py-2">{h.currentPrice != null ? Number(h.currentPrice).toFixed(2) : "—"}</td>
            <td className="py-2">{Number(h.currentValue ?? 0).toFixed(2)}</td>
            <td className="py-2">{Number(h.targetAllocationPct).toFixed(2)}%</td>
            <td className="py-2">{Number(h.currentAllocationPct ?? 0).toFixed(2)}%</td>
            <td className={`py-2 ${Math.abs(h.driftPct ?? 0) > 5 ? "text-red-600" : "text-slate-600"}`}>
              {Number(h.driftPct ?? 0).toFixed(2)}%
            </td>
            <td className="py-2 text-right">
              <button
                onClick={() => onRemove(h.id)}
                className="text-xs text-red-600 hover:underline"
              >
                Remove
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
