import { useState } from "react";
import { useTranslation } from "react-i18next";

function Billing() {
  const { t } = useTranslation();

  const [items, setItems] = useState([
    { productId: "1001", name: "Amul Milk", qty: 2, price: 58 },
    { productId: "1002", name: "Bread", qty: 1, price: 30 },
  ]);

  const grandTotal = items.reduce((total, item) => total + item.qty * item.price, 0);

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white shadow-lg rounded-lg">
      <h2 className="text-2xl font-bold mb-4">{t("billingTitle")}</h2>

      <div className="flex flex-col sm:flex-row sm:items-center sm:space-x-2 mb-6">
        <input
          type="text"
          placeholder={t("enterProductCode")}
          className="border p-2 flex-1 rounded mb-2 sm:mb-0"
        />
        <button className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
          {t("getItem")}
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full border-collapse border">
          <thead>
            <tr className="bg-gray-200">
              <th className="border px-4 py-2">{t("productId")}</th>
              <th className="border px-4 py-2">{t("name")}</th>
              <th className="border px-4 py-2">{t("qty")}</th>
              <th className="border px-4 py-2">{t("price")}</th>
              <th className="border px-4 py-2">{t("total")}</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item, idx) => (
              <tr key={idx}>
                <td className="border px-4 py-2">{item.productId}</td>
                <td className="border px-4 py-2">{item.name}</td>
                <td className="border px-4 py-2">{item.qty}</td>
                <td className="border px-4 py-2">₹{item.price}</td>
                <td className="border px-4 py-2">₹{item.qty * item.price}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="text-right mt-6 text-xl font-semibold">
        {t("grandTotal")}: ₹{grandTotal}
      </div>
    </div>
  );
}

export default Billing;
