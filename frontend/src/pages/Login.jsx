import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";

function Login({ setRole }) {
  const [form, setForm] = useState({ username: "", password: "" });
  const navigate = useNavigate();
  const { t } = useTranslation();

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = (e) => {
    e.preventDefault();

    // Demo login mapping: admin -> owner, manager -> manager, cashier -> cashier
    if (form.username === "admin" && form.password === "1234") {
      localStorage.setItem("role", "owner");
      setRole("owner");
      navigate("/dashboard");
    } else if (form.username === "manager" && form.password === "1234") {
      localStorage.setItem("role", "manager");
      setRole("manager");
      navigate("/inventory");
    } else if (form.username === "cashier" && form.password === "1234") {
      localStorage.setItem("role", "cashier");
      setRole("cashier");
      navigate("/billing");
    } else {
      alert(t("accessDenied") + " ❌");
    }
  };

  return (
    <div className="flex items-center justify-center h-screen bg-gray-100">
      <div className="bg-white shadow-lg rounded-lg p-8 w-96">
        <h2 className="text-2xl font-bold text-center mb-4">{t("loginTitle")}</h2>
        <p className="text-sm text-gray-600 mb-4">{t("loginHint")}</p>
        <form onSubmit={handleSubmit} className="space-y-3">
          <input
            type="text"
            name="username"
            value={form.username}
            onChange={handleChange}
            placeholder={t("username")}
            className="w-full border p-2 rounded"
            required
          />
          <input
            type="password"
            name="password"
            value={form.password}
            onChange={handleChange}
            placeholder={t("password")}
            className="w-full border p-2 rounded"
            required
          />
          <button type="submit" className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700">
            {t("login")}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login;




