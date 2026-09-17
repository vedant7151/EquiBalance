import { Link } from "react-router-dom";

export default function PortfolioCard({ portfolio }) {
  return (
    <Link
      to={`/portfolios/${portfolio.id}`}
      className="block bg-white border border-slate-200 rounded-lg p-5 hover:shadow-md transition"
    >
      <div className="flex items-center justify-between">
        <h3 className="font-semibold text-lg">{portfolio.name}</h3>
        {portfolio.needsRebalancing && (
          <span className="text-xs bg-amber-100 text-amber-800 px-2 py-1 rounded-full">
            Needs Rebalancing
          </span>
        )}
      </div>
      <p className="text-slate-500 text-sm mt-1">{portfolio.description}</p>
      <div className="mt-4 flex justify-between text-sm">
        <span className="text-slate-500">
          {portfolio.holdings?.length ?? 0} holdings
        </span>
        <span className="font-medium">
          {portfolio.baseCurrency} {Number(portfolio.totalValue ?? 0).toLocaleString(undefined, { maximumFractionDigits: 2 })}
        </span>
      </div>
    </Link>
  );
}
