export default function TransactionLog({ transactions }) {
  if (transactions.length === 0) {
    return <p className="text-sm text-slate-500">No transactions yet.</p>;
  }

  return (
    <table className="w-full text-sm">
      <thead>
        <tr className="text-left text-slate-500 border-b border-slate-200">
          <th className="py-2">Date</th>
          <th className="py-2">Ticker</th>
          <th className="py-2">Type</th>
          <th className="py-2">Units</th>
          <th className="py-2">Price</th>
        </tr>
      </thead>
      <tbody>
        {transactions.map((t) => (
          <tr key={t.id} className="border-b border-slate-100">
            <td className="py-2">{new Date(t.executedAt).toLocaleString()}</td>
            <td className="py-2 font-medium">{t.ticker}</td>
            <td className="py-2">{t.type}</td>
            <td className="py-2">{t.units}</td>
            <td className="py-2">{Number(t.priceAtTime).toFixed(2)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
