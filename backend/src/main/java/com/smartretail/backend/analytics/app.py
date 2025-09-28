from flask import Flask, jsonify
from flask_cors import CORS
from flask_pymongo import PyMongo
import pandas as pd
from datetime import datetime
from dotenv import load_dotenv
import os

load_dotenv()
app = Flask(__name__)

# CORS Configuration - Allow Spring Boot requests
CORS(app, origins=["http://localhost:8080", "http://127.0.0.1:8080"],
     methods=["GET", "POST", "PUT", "DELETE"],
     allow_headers=["Content-Type", "Authorization"])

# MongoDB Config
app.config["MONGO_URI"] = os.getenv("MONGO_URI")
db_name = os.getenv("DB_NAME")

mongo = PyMongo(app)
db = mongo.cx[db_name]

# -----------------------
# Helper Functions
# -----------------------

def normalize_bills(raw_bills):
    """Normalize bills from Mongo into a consistent dataframe"""
    records = []
    for bill in raw_bills:
        # Use 'date' or 'createdAt'
        bill_date = bill.get("date") or bill.get("createdAt")
        if isinstance(bill_date, dict) and "$date" in bill_date:
            bill_date = bill_date["$date"]
        bill_date = pd.to_datetime(bill_date)

        # Use 'totalAmount' or 'total'
        total = bill.get("totalAmount") or bill.get("total", 0)

        items = bill.get("items", [])
        for item in items:
            qty = item.get("quantity") or item.get("qty", 0)
            price = item.get("price", 0)
            product_id = item.get("productId")
            name = item.get("name", None)

            records.append({
                "date": bill_date,
                "productId": product_id,
                "name": name,
                "qty": qty,
                "price": price,
                "revenue": price * qty,
                "total": total
            })

    return pd.DataFrame(records)

# -----------------------
# API Endpoints
# -----------------------

@app.route("/")
def home():
    return jsonify({"status": "Analytics API running 🚀", "cors": "Enabled for Spring Boot"})

@app.route("/analytics/daily")
def daily_sales():
    """Daily sales summary"""
    try:
        raw_bills = list(db.bills.find({}))
        df = normalize_bills(raw_bills)
        if df.empty:
            return jsonify({"message": "No billing data"}), 404

        summary = (
            df.groupby(df["date"].dt.date)["revenue"]
            .sum()
            .reset_index()
            .rename(columns={"date": "day", "revenue": "totalSales"})
        )
        summary["day"] = summary["day"].astype(str)

        return jsonify(summary.to_dict(orient="records"))
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/analytics/monthly")
def monthly_sales():
    """Monthly sales summary"""
    try:
        raw_bills = list(db.bills.find({}))
        df = normalize_bills(raw_bills)
        if df.empty:
            return jsonify({"message": "No billing data"}), 404

        summary = (
            df.groupby(df["date"].dt.to_period("M"))["revenue"]
            .sum()
            .reset_index()
        )
        summary["date"] = summary["date"].astype(str)
        summary = summary.rename(columns={"date": "month", "revenue": "totalSales"})

        return jsonify(summary.to_dict(orient="records"))
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/analytics/top-products")
def top_products():
    """Top selling products with category"""
    try:
        raw_bills = list(db.bills.find({}))
        products = list(db.products.find({}))

        df = normalize_bills(raw_bills)
        if df.empty:
            return jsonify({"message": "No billing data"}), 404

        products_df = pd.DataFrame(products)

        merged = df.merge(
            products_df[["productId", "name", "category"]],
            on="productId",
            how="left",
            suffixes=("_bill", "_prod")
        )

        merged["name"] = merged["name_bill"].combine_first(merged["name_prod"])
        merged["category"] = merged["category"].fillna("Unknown")

        summary = (
            merged.groupby(["productId", "name", "category"])
            .agg({"qty": "sum", "revenue": "sum"})
            .reset_index()
            .sort_values("revenue", ascending=False)
            .head(10)
        )

        return jsonify(summary.to_dict(orient="records"))
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/analytics/revenue-trend")
def revenue_trend():
    """Revenue trend by date"""
    try:
        raw_bills = list(db.bills.find({}))
        df = normalize_bills(raw_bills)
        if df.empty:
            return jsonify({"message": "No billing data"}), 404

        trend = (
            df.groupby(df["date"].dt.date)["revenue"]
            .sum()
            .reset_index()
            .rename(columns={"date": "day", "revenue": "totalRevenue"})
        )
        trend["day"] = trend["day"].astype(str)
        return jsonify(trend.to_dict(orient="records"))
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/analytics/weekly")
def weekly_sales():
    try:
        raw_bills = list(db.bills.find({}))
        df = normalize_bills(raw_bills)
        if df.empty:
            return jsonify({"message": "No billing data"}), 404

        summary = (
            df.groupby(df["date"].dt.to_period("W"))["revenue"]
            .sum()
            .reset_index()
        )
        summary["date"] = summary["date"].astype(str)
        summary = summary.rename(columns={"date": "week", "revenue": "totalSales"})

        return jsonify(summary.to_dict(orient="records"))
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/analytics/report")
def sales_report():
    try:
        raw_bills = list(db.bills.find({}))
        products = list(db.products.find({}))
        df = normalize_bills(raw_bills)
        if df.empty:
            return jsonify({"message": "No billing data"}), 404

        products_df = pd.DataFrame(products)

        # Daily
        daily = (
            df.groupby(df["date"].dt.date)["revenue"]
            .sum()
            .reset_index()
            .rename(columns={"date": "day", "revenue": "totalSales"})
        )
        daily["day"] = daily["day"].astype(str)

        # Weekly
        weekly = (
            df.groupby(df["date"].dt.to_period("W"))["revenue"]
            .sum()
            .reset_index()
        )
        weekly["date"] = weekly["date"].astype(str)
        weekly = weekly.rename(columns={"date": "week", "revenue": "totalSales"})

        # Monthly
        monthly = (
            df.groupby(df["date"].dt.to_period("M"))["revenue"]
            .sum()
            .reset_index()
        )
        monthly["date"] = monthly["date"].astype(str)
        monthly = monthly.rename(columns={"date": "month", "revenue": "totalSales"})

        # Top Products
        merged = df.merge(
            products_df[["productId", "name", "category"]],
            on="productId",
            how="left",
            suffixes=("_bill", "_prod")
        )
        merged["name"] = merged["name_bill"].combine_first(merged["name_prod"])
        merged["category"] = merged["category"].fillna("Unknown")

        top_products = (
            merged.groupby(["productId", "name", "category"])
            .agg({"qty": "sum", "revenue": "sum"})
            .reset_index()
            .sort_values("revenue", ascending=False)
            .head(10)
        )

        # Revenue Trend
        trend = (
            df.groupby(df["date"].dt.date)["revenue"]
            .sum()
            .reset_index()
            .rename(columns={"date": "day", "revenue": "totalRevenue"})
        )
        trend["day"] = trend["day"].astype(str)

        # Combine Report
        report = {
            "daily": daily.to_dict(orient="records"),
            "weekly": weekly.to_dict(orient="records"),
            "monthly": monthly.to_dict(orient="records"),
            "top_products": top_products.to_dict(orient="records"),
            "revenue_trend": trend.to_dict(orient="records"),
            "summary": {
                "total_revenue": float(df["revenue"].sum()),
                "total_orders": int(len(raw_bills)),
                "total_products_sold": int(df["qty"].sum())
            }
        }

        return jsonify(report)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/analytics/report/text")
def sales_report_text():
    try:
        raw_bills = list(db.bills.find({}))
        products = list(db.products.find({}))
        df = normalize_bills(raw_bills)

        if df.empty:
            return jsonify({"message": "No billing data"}), 404

        products_df = pd.DataFrame(products)

        # Summary
        total_revenue = float(df["revenue"].sum())
        total_orders = int(len(raw_bills))
        total_products_sold = int(df["qty"].sum())

        # Daily
        daily = (
            df.groupby(df["date"].dt.date)["revenue"]
            .sum()
            .reset_index()
            .rename(columns={"date": "day", "revenue": "totalSales"})
        )
        daily["day"] = daily["day"].astype(str)
        best_day = daily.loc[daily["totalSales"].idxmax()]

        # Weekly
        weekly = (
            df.groupby(df["date"].dt.to_period("W"))["revenue"]
            .sum()
            .reset_index()
        )
        weekly["date"] = weekly["date"].astype(str)
        best_week = weekly.loc[weekly["revenue"].idxmax()]

        # Monthly
        monthly = (
            df.groupby(df["date"].dt.to_period("M"))["revenue"]
            .sum()
            .reset_index()
        )
        monthly["date"] = monthly["date"].astype(str)
        best_month = monthly.loc[monthly["revenue"].idxmax()]

        # Top Products
        merged = df.merge(
            products_df[["productId", "name", "category"]],
            on="productId",
            how="left",
            suffixes=("_bill", "_prod")
        )
        merged["name"] = merged["name_bill"].combine_first(merged["name_prod"])
        merged["category"] = merged["category"].fillna("Unknown")

        top_products = (
            merged.groupby(["productId", "name", "category"])
            .agg({"qty": "sum", "revenue": "sum"})
            .reset_index()
            .sort_values("revenue", ascending=False)
            .head(3)
        )

        # Generate Plain English Report
        report_lines = []
        report_lines.append(f"📊 Sales Analysis Report")
        report_lines.append(f"Total revenue generated: ₹{total_revenue:,.2f}.")
        report_lines.append(f"Total number of orders: {total_orders}.")
        report_lines.append(f"Total products sold: {total_products_sold}.")

        report_lines.append(
            f"The best performing day was {best_day['day']} with sales of ₹{best_day['totalSales']:.2f}."
        )
        report_lines.append(
            f"The best performing week was {best_week['date']} with sales of ₹{best_week['revenue']:.2f}."
        )
        report_lines.append(
            f"The best performing month was {best_month['date']} with sales of ₹{best_month['revenue']:.2f}."
        )

        report_lines.append("Top selling products were:")
        for idx, row in top_products.iterrows():
            report_lines.append(
                f"- {row['name']} ({row['category']}) sold {row['qty']} units, revenue ₹{row['revenue']:.2f}"
            )

        return jsonify({"report": " ".join(report_lines)})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(port=5001, debug=True)