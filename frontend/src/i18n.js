import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";

const resources = {
  en: {
    translation: {
      // General
      login: "Login",
      username: "Username",
      password: "Password",
      billing: "Billing",
      dashboard: "Dashboard",
      inventory: "Inventory",
      customers: "Customers",
      logout: "Logout",
      accessDenied: "Access Denied",
      welcome: "Welcome",
      cancel: "Cancel",
      save: "Save",
      delete: "Delete",
      add: "Add",

      // Login page
      loginTitle: "Login",
      loginHint: "Use admin/manager/cashier with password 1234 (demo)",

      // Billing page
      billingTitle: "Billing System",
      enterProductCode: "Enter Product Code",
      getItem: "Get Item",
      productId: "Product ID",
      name: "Name",
      qty: "Qty",
      price: "Price",
      total: "Total",
      grandTotal: "Grand Total",

      // Customers page
      customerTitle: "Customer Details",
      enterName: "Enter Name",
      enterMobile: "Enter Mobile Number",
      enterEmail: "Enter Email",
      saveCustomer: "Save Customer",
      customerSaved: "Customer Saved!",

      // Inventory page
      inventoryTitle: "Inventory",
      addProduct: "➕ Add Product",
      productIDPlaceholder: "Product ID",
      productNamePlaceholder: "Product Name",
      stockQuantity: "Stock Quantity",
      saveProduct: "Save Product",
      restockNeeded: "Restock Needed",
      lowStock: "Low Stock",
      inStock: "In Stock",
      deleteProduct: "Delete",
      confirmDelete: "Are you sure you want to delete this product?",

      // Dashboard
      salesForecast: "Sales Forecast",
      monthlyReminders: "Monthly Reminders",

      // i18n languages (not required but handy)
      lang_en: "EN",
      lang_hi: "HI",
      lang_te: "TE",
      lang_mr: "MR"
    }
  },
  hi: {
    translation: {
      login: "लॉगिन",
      username: "उपयोगकर्ता नाम",
      password: "पासवर्ड",
      billing: "बिलिंग",
      dashboard: "डैशबोर्ड",
      inventory: "सूची",
      customers: "ग्राहक",
      logout: "लॉगआउट",
      accessDenied: "पहुँच अस्वीकृत",
      welcome: "स्वागत है",
      cancel: "रद्द करें",
      save: "सहेजें",
      delete: "हटाएं",

      loginTitle: "लॉगिन",
      loginHint: "डेमो के लिए admin/manager/cashier और पासवर्ड 1234 इस्तेमाल करें",

      billingTitle: "बिलिंग सिस्टम",
      enterProductCode: "प्रोडक्ट कोड दर्ज करें",
      getItem: "आइटम लाएँ",
      productId: "प्रोडक्ट आईडी",
      name: "नाम",
      qty: "मात्रा",
      price: "कीमत",
      total: "कुल",
      grandTotal: "कुल योग",

      customerTitle: "ग्राहक विवरण",
      enterName: "नाम दर्ज करें",
      enterMobile: "मोबाइल नंबर दर्ज करें",
      enterEmail: "ईमेल दर्ज करें",
      saveCustomer: "ग्राहक सहेजें",
      customerSaved: "ग्राहक सहेजा गया!",

      inventoryTitle: "सूची",
      addProduct: "➕ उत्पाद जोड़ें",
      productIDPlaceholder: "प्रोडक्ट आईडी",
      productNamePlaceholder: "प्रोडक्ट का नाम",
      stockQuantity: "स्टॉक मात्रा",
      saveProduct: "उत्पाद सहेजें",
      restockNeeded: "पुनः स्टॉक की आवश्यकता",
      lowStock: "कम स्टॉक",
      inStock: "स्टॉक में",
      deleteProduct: "हटाएँ",
      confirmDelete: "क्या आप वाकई इस उत्पाद को हटाना चाहते हैं?",

      salesForecast: "बिक्री पूर्वानुमान",
      monthlyReminders: "मासिक अनुस्मारक",

      lang_en: "EN",
      lang_hi: "HI",
      lang_te: "TE",
      lang_mr: "MR"
    }
  },
  te: {
    translation: {
      login: "లాగిన్",
      username: "వాడుకరి పేరు",
      password: "పాస్వర్డ్",
      billing: "బిల్లింగ్",
      dashboard: "డ్యాష్‌బోర్డ్",
      inventory: "ఇన్వెంటరీ",
      customers: "వినియోగదారులు",
      logout: "లాగ్ అవుట్",
      accessDenied: "అనుమతి రాదు",
      welcome: "స్వాగతం",
      cancel: "రద్దు",
      save: "సేవ్",
      delete: "తొలగించు",

      loginTitle: "లాగిన్",
      loginHint: "డెమో కొరకు admin/manager/cashier  మరియు పాస్‌వర్డ్ 1234 వాడండి",

      billingTitle: "బిల్లింగ్ సిస్టమ్",
      enterProductCode: "ఉత్పత్తి కోడ్ నమోదు చేయండి",
      getItem: "ఐటంను పొందండి",
      productId: "ఉత్పత్తి ID",
      name: "పేరు",
      qty: " саны", // minor translation; keep simple
      price: "ధర",
      total: "మొత్తం",
      grandTotal: "మొత్తం ఫలితం",

      customerTitle: "కస్టమర్ వివరాలు",
      enterName: "పేరు నమోదు చేయండి",
      enterMobile: "మొబైల్ నంబర్ నమోదు చేయండి",
      enterEmail: "ఇమెయిల్ నమోదు చేయండి",
      saveCustomer: "కస్టమర్ సేవ్ చేయండి",
      customerSaved: "కస్టమర్ సేవ్ అయిపోయింది!",

      inventoryTitle: "ఇన్వెంటరీ",
      addProduct: "➕ ఉత్పత్తిని చేర్చు",
      productIDPlaceholder: "ఉత్పత్తి ID",
      productNamePlaceholder: "ఉత్పత్తి పేరు",
      stockQuantity: "స్టాక్ పరిమాణం",
      saveProduct: "ఉత్పత్తి సేవ్ చేయండి",
      restockNeeded: "రీస్టాక్ అవసరం",
      lowStock: "తక్కువ స్టాక్",
      inStock: "స్టాక్‌లో ఉంది",
      deleteProduct: "తొలగించు",
      confirmDelete: "మీరు నిజంగా ఈ ఉత్పత్తిని తొలగించాలని ఖచ్చితంగా అనుకుంటున్నారా?",

      salesForecast: "విక్రయాల అంచనా",
      monthlyReminders: "నెలవారీ రీమైన్డర్లు",

      lang_en: "EN",
      lang_hi: "HI",
      lang_te: "TE",
      lang_mr: "MR"
    }
  },
  mr: {
    translation: {
      login: "लॉगिन",
      username: "वापरकर्ता नाव",
      password: "पासवर्ड",
      billing: "बिलिंग",
      dashboard: "डॅशबोर्ड",
      inventory: "साठा",
      customers: "ग्राहक",
      logout: "लॉगआउट",
      accessDenied: "परवानगी नाही",
      welcome: "स्वागत आहे",
      cancel: "रद्द करा",
      save: "जतन",
      delete: "हटवा",

      loginTitle: "लॉगिन",
      loginHint: "डेमो साठी admin/manager/cashier आणि पासवर्ड 1234 वापरा",

      billingTitle: "बिलिंग सिस्टिम",
      enterProductCode: "उत्पादन कोड प्रविष्ट करा",
      getItem: "आयटम आणा",
      productId: "उत्पादन आयडी",
      name: "नाव",
      qty: "प्रमाण",
      price: "किंमत",
      total: "एकूण",
      grandTotal: "एकूण रक्कम",

      customerTitle: "ग्राहक तपशील",
      enterName: "नाव प्रविष्ट करा",
      enterMobile: "मोबाईल नंबर प्रविष्ट करा",
      enterEmail: "ईमेल प्रविष्ट करा",
      saveCustomer: "ग्राहक जतन करा",
      customerSaved: "ग्राहक जतन केला!",

      inventoryTitle: "साठा",
      addProduct: "➕ उत्पादन जोडा",
      productIDPlaceholder: "उत्पादन आयडी",
      productNamePlaceholder: "उत्पादनाचे नाव",
      stockQuantity: "साठा प्रमाण",
      saveProduct: "उत्पादन जतन करा",
      restockNeeded: "पुन्हा साठा आवश्यक",
      lowStock: "कमी साठा",
      inStock: "साठ्यात आहे",
      deleteProduct: "हटवा",
      confirmDelete: "या उत्पादनाला खरंच हटवायचे का?",

      salesForecast: "विक्री अंदाज",
      monthlyReminders: "मासिक स्मरणपत्रे",

      lang_en: "EN",
      lang_hi: "HI",
      lang_te: "TE",
      lang_mr: "MR"
    }
  }
};

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: "en",
    interpolation: { escapeValue: false }
  });

export default i18n;


