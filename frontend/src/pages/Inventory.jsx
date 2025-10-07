import { useState } from "react";
import { useTranslation } from "react-i18next";

function Inventory() {
  const { t } = useTranslation();

  const [products, setProducts] = useState([
    { id: "1001", name: "Amul Milk", stock: 5 },
    { id: "1002", name: "Bread", stock: 0 },
    { id: "1003", name: "Eggs (6 pack)", stock: 12 },
    { id: "1004", name: "Rice Bag", stock: 1 },
  ]);

  const [newProduct, setNewProduct] = useState({ id: "", name: "", stock: "" });
  const [showForm, setShowForm] = useState(false);

  const handleChange = (e) => setNewProduct({ ...newProduct, [e.target.name]: e.target.value });

  const handleAddProduct = (e) => {
    e.preventDefault();
    if (!newProduct.id || !newProduct.name || newProduct.stock === "") {
      alert(t("pleaseFillAll") || t("Please fill all fields")); // fallback
      return;
    }
    setProducts([...products, { id: newProduct.id, name: newProduct.name, stock: parseInt(newProduct.stock, 10) }]);
    setNewProduct({ id: "", name: "", stock: "" });
    setShowForm(false);
  };

  const handleDelete = (id) => {
    if (window.confirm(t("confirmDelete"))) {
      setProducts(products.filter((p) => p.id !== id));
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white shadow-lg rounded-lg">
      <h2 className="text-2xl font-bold mb-4">{t("inventoryTitle")}</h2>

      <button onClick={() => setShowForm(!showForm)} className="mb-4 bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">
        {showForm ? t("cancel") : t("addProduct")}
      </button>

      {showForm && (
        <form onSubmit={handleAddProduct} className="mb-6 p-4 border rounded bg-gray-50 space-y-3">
          <input type="text" name="id" placeholder={t("productIDPlaceholder")} value={newProduct.id} onChange={handleChange} className="w-full border p-2 rounded" />
          <input type="text" name="name" placeholder={t("productNamePlaceholder")} value={newProduct.name} onChange={handleChange} className="w-full border p-2 rounded" />
          <input type="number" name="stock" placeholder={t("stockQuantity")} value={newProduct.stock} onChange={handleChange} className="w-full border p-2 rounded" />
          <div className="flex space-x-2">
            <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">{t("saveProduct")}</button>
            <button type="button" onClick={() => setShowForm(false)} className="bg-gray-300 px-4 py-2 rounded">{t("cancel")}</button>
          </div>
        </form>
      )}

      <div className="overflow-x-auto">
        <table className="w-full border-collapse border">
          <thead>
            <tr className="bg-gray-200">
              <th className="border px-4 py-2">{t("productId")}</th>
              <th className="border px-4 py-2">{t("name")}</th>
              <th className="border px-4 py-2">{t("stockQuantity")}</th>
              <th className="border px-4 py-2">Status</th>
              <th className="border px-4 py-2"> </th>
            </tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id} className={p.stock === 0 ? "bg-red-100" : ""}>
                <td className="border px-4 py-2">{p.id}</td>
                <td className="border px-4 py-2">{p.name}</td>
                <td className="border px-4 py-2">{p.stock}</td>
                <td className="border px-4 py-2">
                  {p.stock === 0 ? (
                    <span className="text-red-600 font-bold">{t("restockNeeded")}</span>
                  ) : p.stock < 3 ? (
                    <span className="text-yellow-600 font-bold">{t("lowStock")}</span>
                  ) : (
                    <span className="text-green-600 font-bold">{t("inStock")}</span>
                  )}
                </td>
                <td className="border px-4 py-2">
                  <button onClick={() => handleDelete(p.id)} className="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600">
                    {t("deleteProduct")}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default Inventory;


