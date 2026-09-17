import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="bg-white border-b border-slate-200 px-6 py-3 flex items-center justify-between">
      <Link to="/" className="text-lg font-semibold text-emerald-700">
        EquiBalance
      </Link>
      {user && (
        <div className="flex items-center gap-4 text-sm">
          <Link to="/" className="hover:text-emerald-700">
            Portfolios
          </Link>
          <Link to="/watchlist" className="hover:text-emerald-700">
            Watchlist
          </Link>
          <span className="text-slate-500">{user.name}</span>
          <button
            onClick={handleLogout}
            className="rounded bg-slate-100 px-3 py-1 hover:bg-slate-200"
          >
            Log out
          </button>
        </div>
      )}
    </nav>
  );
}
