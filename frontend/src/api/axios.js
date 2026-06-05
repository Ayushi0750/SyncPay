




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

    console.log(" [REQUEST INTERCEPTOR]");
    console.log("  URL:", config.url);
    console.log("  Method:", config.method?.toUpperCase());
    console.log("  Token found:", token ? " YES" : " NO");
    console.log("  Full URL:", config.baseURL + config.url);

    if (token) {
      
      config.headers["Authorization"] = `Bearer ${token}`;
      console.log("   Authorization header attached");
      console.log("  Header value:", config.headers["Authorization"].substring(0, 50) + "...");
    } else {
      console.warn(
        "   NO TOKEN IN LOCALSTORAGE - request will fail with 403"
      );
    }

    
    console.log("  All Headers:", config.headers);

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
      statusText: response.statusText,
      url: response.config.url,
      dataLength: response.data ? Object.keys(response.data).length : 0,
    });
    return response;
  },
  (error) => {
    console.error(" [RESPONSE ERROR]", {
      status: error.response?.status,
      statusText: error.response?.statusText,
      url: error.config?.url,
      data: error.response?.data,
      message: error.message,
      headers: error.response?.headers,
    });

    // Handle 401/403 errors
    if (error.response?.status === 401 || error.response?.status === 403) {
      console.error(" AUTHENTICATION ERROR");
      console.error("   Status:", error.response.status);
      console.error("   Message:", error.response.data?.error);
      
      // Clear token and redirect to login
      localStorage.removeItem("token");
      window.location.href = "/login";
    }

    return Promise.reject(error);
  }
);

export default api;