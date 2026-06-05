import api from "./api";

let config = {
  networkFail: false,
  forceFail: false,
  delay: 0,
};

export const setSimulationConfig = (newConfig) => {
  config = newConfig;
  console.log(" Simulation config updated:", config);
};

export const getSimulationConfig = () => config;

let interceptorId = null;

export const setupNetworkSimulation = () => {
  // Remove existing interceptor if any
  if (interceptorId !== null) {
    api.interceptors.request.eject(interceptorId);
  }
  
  // Add new interceptor
  interceptorId = api.interceptors.request.use(
    (requestConfig) => {
      const currentConfig = getSimulationConfig();
      
      //  NETWORK FAIL SIMULATION - Block request completely
      if (currentConfig.networkFail) {
        console.log(" [NETWORK SIMULATION] Blocking request to:", requestConfig.url);
        // Create a simulated network error
        const error = new Error("Network Error: Simulated network failure");
        error.code = "ERR_NETWORK";
        error.config = requestConfig;
        return Promise.reject(error);
      }
      
      //  DELAY SIMULATION - Add artificial delay
      if (currentConfig.delay > 0) {
        console.log(`"[DELAY SIMULATION] Adding ${currentConfig.delay}ms delay to:`, requestConfig.url);
        return new Promise((resolve) => {
          setTimeout(() => {
            resolve(requestConfig);
          }, currentConfig.delay);
        });
      }
      
      return requestConfig;
    },
    (error) => {
      return Promise.reject(error);
    }
  );
};

// Auto-setup when file loads
setupNetworkSimulation();