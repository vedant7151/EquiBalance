export default function RebalanceModal({ result, onClose }) {
  if (!result) return null;

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 w-full max-w-md">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold">Suggested Trades</h2>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-700">
            ✕
          </button>
        </div>
        <p className="text-sm text-slate-500 mb-4">
          Total drift before rebalancing: {Number(result.totalDriftBefore ?? 0).toFixed(2)}%
        </p>
        {result.trades?.length === 0 ? (
          <p className="text-sm text-slate-500">Portfolio is already within its target allocation.</p>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-slate-500 border-b">
                <th className="py-1">Ticker</th>
                <th className="py-1">Action</th>
                <th className="py-1">Units</th>
                <th className="py-1">Est. Cost</th>
              </tr>
            </thead>
            <tbody>
              {result.trades?.map((t, idx) => (
                <tr key={idx} className="border-b border-slate-100">
                  <td className="py-1 font-medium">{t.ticker}</td>
                  <td className={`py-1 ${t.action === "BUY" ? "text-emerald-700" : "text-red-600"}`}>
                    {t.action}
                  </td>
                  <td className="py-1">{Number(t.units).toFixed(4)}</td>
                  <td className="py-1">{Number(t.estimatedCost).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        <button
          onClick={onClose}
          className="mt-5 w-full bg-emerald-700 text-white rounded py-2 text-sm hover:bg-emerald-800"
        >
          Close
        </button>
      </div>
    </div>
  );
}
