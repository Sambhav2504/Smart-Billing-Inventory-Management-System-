import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { useTranslation } from "react-i18next";

function Dashboard() {
  const { t } = useTranslation();

  const salesData = [
    { month: "Jan", sales: 1200 },
    { month: "Feb", sales: 2100 },
    { month: "Mar", sales: 800 },
    { month: "Apr", sales: 1600 },
    { month: "May", sales: 2400 },
  ];

  const reminders = [
    { name: "Ravi", mobile: "9876543210", due: "₹500" },
    { name: "Anita", mobile: "9123456780", due: "₹1200" },
    { name: "Kiran", mobile: "9988776655", due: "₹300" },
  ];

  return (
    <div className="p-6">
      <h2 className="text-2xl font-bold mb-6">{t("dashboard")}</h2>

      <div className="bg-white shadow-lg p-6 rounded-lg mb-8">
        <h3 className="text-xl font-semibold mb-4">{t("salesForecast")}</h3>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={salesData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="month" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Line type="monotone" dataKey="sales" stroke="#2563eb" strokeWidth={3} />
          </LineChart>
        </ResponsiveContainer>
      </div>

      <div className="bg-white shadow-lg p-6 rounded-lg">
        <h3 className="text-xl font-semibold mb-4">{t("monthlyReminders")}</h3>
        <table className="w-full border-collapse border">
          <thead>
            <tr className="bg-gray-200">
              <th className="border px-4 py-2">{t("name")}</th>
              <th className="border px-4 py-2">{t("enterMobile")}</th>
              <th className="border px-4 py-2">Due</th>
            </tr>
          </thead>
          <tbody>
            {reminders.map((c, idx) => (
              <tr key={idx}>
                <td className="border px-4 py-2">{c.name}</td>
                <td className="border px-4 py-2">{c.mobile}</td>
                <td className="border px-4 py-2">{c.due}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default Dashboard;
