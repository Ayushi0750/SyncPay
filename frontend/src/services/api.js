import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8081",
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true,
  timeout: 10000,
});


api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");

    console.log("🔍 [REQUEST INTERCEPTOR]");
    console.log("  URL:", config.url);
    console.log("  Method:", config.method?.toUpperCase());
    console.log("  Token found:", token ? " YES" : " NO");

    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
      console.log("   Authorization header attached");
    } else {
      console.warn("   NO TOKEN IN LOCALSTORAGE");
    }

    return config;
  },
  (error) => {
    console.error(" [REQUEST INTERCEPTOR ERROR]", error);
    return Promise.reject(error);
  }
);


api.interceptors.response.use(
  (response) => {
    console.log(" [RESPONSE SUCCESS]", {
      status: response.status,
      url: response.config.url,
    });
    return response;
  },
  (error) => {
    const status = error.response?.status;
    const url = error.config?.url;
    const errorData = error.response?.data;
    
    console.error(" [RESPONSE ERROR]", {
      status: status,
      url: url,
      data: errorData,
    });

    
    const isMockBankEndpoint = url?.includes("/mockbank/");
    const isWalletEndpoint = url?.includes("/wallet/");
    const isAuthEndpoint = url?.includes("/auth/");
    
    
    const shouldRedirect = (status === 401 || status === 403) && 
                           !isMockBankEndpoint &&
                           !isWalletEndpoint &&
                           !isAuthEndpoint;

    if (shouldRedirect) {
      console.error(" REAL AUTHENTICATION ERROR - Redirecting to login");
      localStorage.removeItem("token");
      window.location.href = "/login";
    } else {
     
      if (isMockBankEndpoint) {
        console.log(" [MOCK BANK ERROR] - Keeping user on page, NO redirect");
      } else if (isWalletEndpoint) {
        console.log(" [WALLET ERROR] - Keeping user on page, NO redirect");
      } else {
        console.log(" [BUSINESS ERROR] - Keeping user on page, NO redirect");
      }
    }

    return Promise.reject(error);
  }
);

export default api;