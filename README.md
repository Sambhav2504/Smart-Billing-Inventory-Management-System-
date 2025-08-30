# 🛒 Smart Billing & Inventory Management System  

A **Java Full Stack Capstone Project** designed for small retailers to manage billing, inventory, and sales analytics without needing expensive hardware like barcode scanners.  
This system supports **4-digit product IDs**, **real-time inventory updates**, **sales forecasting**, **multi-language support**, and **UPI/QR payments** to make shop management simple and affordable.  

---

## 🚀 Features  
- **Retailer Authentication & Role-Based Access**  
- **Billing Without Barcode** – Add items to cart using 4-digit product ID  
- **Real-Time Inventory Management** – Auto stock updates, low-stock alerts  
- **Sales Forecasting & Analytics Dashboard** – Visual reports, top-selling items, profit analysis  
- **Customer Data Management** – Purchase history, reminders, loyalty features  
- **Automated Notifications** – Email/SMS purchase reminders  
- **UPI/QR Payments Integration** – Easy digital payments  
- **Multi-Language Support** – English, Hindi, Marathi, Telugu  
- **Bulk Product Upload** – via CSV/Excel  
- **Offline Mode with Auto-Sync** – Works even without internet  

---

## 🛠️ Tech Stack  

### Frontend  
- React.js  
- Tailwind CSS  
- Axios (API calls)  
- Recharts (Analytics Dashboard)  
- i18next (Multi-language)  

### Backend  
- Spring Boot (REST APIs)  
- Spring Security (JWT Authentication)  
- MongoDB + Spring Data MongoDB  
- JavaMail / Twilio (Notifications)  
- Razorpay/UPI API (Payments)  

### DevOps & Tools  
- GitHub (Version Control)  
- Postman (API Testing)  
- MongoDB Atlas (Cloud DB)  
- Vercel / Netlify (Frontend Deployment)  
- Heroku / Render (Backend Deployment)  

---

## 📂 Repository Structure  

```bash
smart-billing-inventory/
│
├── backend/                  # Spring Boot project
│   ├── src/main/java/...     # Controllers, Services, Repositories, Models
│   ├── src/main/resources/   # application.properties
│   └── pom.xml               # Maven dependencies
│
├── frontend/                 # React project
│   ├── src/components/       # Reusable components
│   ├── src/pages/            # Pages (Dashboard, Billing, Reports, etc.)
│   ├── src/services/         # API integration (Axios)
│   └── package.json
│
├── docs/                     # Documentation (SRS, ERD, API Contract, etc.)
│
├── .gitignore
├── README.md                 # Project overview
└── LICENSE
