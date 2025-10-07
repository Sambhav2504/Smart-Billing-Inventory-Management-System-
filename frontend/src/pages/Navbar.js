import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import i18n from "../i18n";

export default function Navbar({ role, setRole }) {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const { t } = useTranslation();

  const handleLogout = () => {
    localStorage.clear();
    setRole(null);
    navigate("/login");
    // reload not necessary if state is updated
  };

  const changeLanguage = (lang) => {
    i18n.changeLanguage(lang);
  };

  return (
    <nav className="bg-blue-600 text-white p-4">
      <div className="max-w-6xl mx-auto flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <Link to="/" className="text-lg font-bold">
            SmartBilling
          </Link>
        </div>

        <div className="sm:hidden">
          <button
            onClick={() => setOpen(!open)}
            aria-expanded={open}
            aria-label="Toggle menu"
            className="p-2 rounded-md focus:outline-none focus:ring-2 focus:ring-white"
          >
            {open ? (
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            ) : (
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            )}
          </button>
        </div>

        <div className="hidden sm:flex sm:items-center sm:space-x-4">
          <Link to="/login" className="hover:underline">
            {t("login")}
          </Link>

          {role === "owner" && (
            <Link to="/dashboard" className="hover:underline">
              {t("dashboard")}
            </Link>
          )}

          {(role === "owner" || role === "manager") && (
            <Link to="/inventory" className="hover:underline">
              {t("inventory")}
            </Link>
          )}

          {(role === "owner" || role === "manager") && (
            <Link to="/customers" className="hover:underline">
              {t("customers")}
            </Link>
          )}

          {(role === "owner" || role === "manager" || role === "cashier") && (
            <Link to="/billing" className="hover:underline">
              {t("billing")}
            </Link>
          )}

          {role && <span className="ml-4 font-semibold">👋 {t("welcome")}, {role.toUpperCase()}</span>}

          {role && (
            <button
              onClick={handleLogout}
              className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded ml-2"
            >
              {t("logout")}
            </button>
          )}

          <div className="flex items-center space-x-1 ml-2">
            <button onClick={() => changeLanguage("en")} className="px-2 py-1 bg-white text-black rounded">{t("lang_en")}</button>
            <button onClick={() => changeLanguage("hi")} className="px-2 py-1 bg-white text-black rounded">{t("lang_hi")}</button>
            <button onClick={() => changeLanguage("te")} className="px-2 py-1 bg-white text-black rounded">{t("lang_te")}</button>
            <button onClick={() => changeLanguage("mr")} className="px-2 py-1 bg-white text-black rounded">{t("lang_mr")}</button>
          </div>
        </div>
      </div>

      {/* Mobile menu */}
      <div className={`${open ? "block" : "hidden"} sm:hidden mt-2 max-w-6xl mx-auto`}>
        <div className="flex flex-col space-y-2 bg-blue-600/90 p-2 rounded">
          <Link to="/login" className="block px-2 py-2 rounded hover:bg-blue-500">{t("login")}</Link>
          {role === "owner" && <Link to="/dashboard" className="block px-2 py-2 rounded hover:bg-blue-500">{t("dashboard")}</Link>}
          {(role === "owner" || role === "manager") && <Link to="/inventory" className="block px-2 py-2 rounded hover:bg-blue-500">{t("inventory")}</Link>}
          {(role === "owner" || role === "manager") && <Link to="/customers" className="block px-2 py-2 rounded hover:bg-blue-500">{t("customers")}</Link>}
          {(role === "owner" || role === "manager" || role === "cashier") && <Link to="/billing" className="block px-2 py-2 rounded hover:bg-blue-500">{t("billing")}</Link>}

          {role && (
            <div className="flex items-center justify-between px-2 py-2">
              <span className="font-semibold">👋 {t("welcome")}, {role.toUpperCase()}</span>
              <button onClick={handleLogout} className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded">{t("logout")}</button>
            </div>
          )}

          <div className="flex space-x-1 px-2">
            <button onClick={() => changeLanguage("en")} className="px-2 py-1 bg-white text-black rounded">{t("lang_en")}</button>
            <button onClick={() => changeLanguage("hi")} className="px-2 py-1 bg-white text-black rounded">{t("lang_hi")}</button>
            <button onClick={() => changeLanguage("te")} className="px-2 py-1 bg-white text-black rounded">{t("lang_te")}</button>
            <button onClick={() => changeLanguage("mr")} className="px-2 py-1 bg-white text-black rounded">{t("lang_mr")}</button>
          </div>
        </div>
      </div>
    </nav>
  );
}
