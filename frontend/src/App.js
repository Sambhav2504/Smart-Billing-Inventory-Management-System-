import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { useState, useEffect } from "react";
import Billing from "./pages/Billing";
import Customers from "./pages/Customers";
import Inventory from "./pages/Inventory";
import Dashboard from "./pages/Dashboard";
import Login from "./pages/Login";
import Navbar from "./pages/Navbar";
import "./i18n";
import { useTranslation } from "react-i18next";

function App() {
  const [role, setRole] = useState(localStorage.getItem("role"));
  const { t } = useTranslation();

  // keep in sync if localStorage changed externally
  useEffect(() => {
    const saved = localStorage.getItem("role");
    if (saved && saved !== role) setRole(saved);
  }, [role]);

  return (
    <Router>
      <div className="flex flex-col h-screen">
        {/* Navbar only after login */}
        {role && <Navbar role={role} setRole={setRole} />}

        <div className="flex-1 p-6">
          <Routes>
            {/* login (default) */}
            <Route path="/" element={<Login setRole={setRole} />} />
            <Route path="/login" element={<Login setRole={setRole} />} />

            {/* protected routes */}
            <Route
              path="/dashboard"
              element={role === "owner" ? <Dashboard /> : <h2>{t("accessDenied")} ❌</h2>}
            />
            <Route
              path="/inventory"
              element={role === "owner" || role === "manager" ? <Inventory /> : <h2>{t("accessDenied")} ❌</h2>}
            />
            <Route
              path="/customers"
              element={role === "owner" || role === "manager" ? <Customers /> : <h2>{t("accessDenied")} ❌</h2>}
            />
            <Route
              path="/billing"
              element={role === "owner" || role === "manager" || role === "cashier" ? <Billing /> : <h2>{t("accessDenied")} ❌</h2>}
            />
          </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;



