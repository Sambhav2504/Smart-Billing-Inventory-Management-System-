import { useState } from "react";
import { useTranslation } from "react-i18next";

function Customers() {
  const { t } = useTranslation();
  const [form, setForm] = useState({ name: "", mobile: "", email: "" });

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log("Customer Data:", form);
    alert(`${t("customerSaved")}\n${t("name")}: ${form.name}\n${t("enterMobile")}: ${form.mobile}\n${t("enterEmail")}: ${form.email}`);
    setForm({ name: "", mobile: "", email: "" });
  };

  return (
    <div className="max-w-md mx-auto p-6 bg-white shadow-lg rounded-lg">
      <h2 className="text-2xl font-bold mb-4">{t("customerTitle")}</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <input type="text" name="name" value={form.name} onChange={handleChange} placeholder={t("enterName")} className="w-full border p-2 rounded" required />
        <input type="text" name="mobile" value={form.mobile} onChange={handleChange} placeholder={t("enterMobile")} className="w-full border p-2 rounded" required />
        <input type="email" name="email" value={form.email} onChange={handleChange} placeholder={t("enterEmail")} className="w-full border p-2 rounded" />
        <button type="submit" className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">{t("saveCustomer")}</button>
      </form>
    </div>
  );
}

export default Customers;

