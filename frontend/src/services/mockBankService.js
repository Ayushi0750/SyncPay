import api from "./api";
import { getSimulationConfig } from "./simulationService";

export const creditMockBank = async (amount, forceFailFromUI = false) => {
  try {
    const simulation = getSimulationConfig();

    const res = await api.post("/mockbank/credit", {
      amount,
      referenceId: "UI-" + Date.now(),
      forceFail: forceFailFromUI || simulation.forceFail,
    });

    return { success: true, data: res.data };
  } catch (err) {
    
    if (err.code === "ERR_NETWORK" || err.message?.includes("Network Error")) {
      return {
        success: false,
        error: "🌐 NETWORK SIMULATION: Connection failed. Please check your network.",
      };
    }
    
    if (err.code === "ECONNABORTED" || err.message?.includes("timeout")) {
      return {
        success: false,
        error: "⏱️ NETWORK SIMULATION: Request timed out. Please try again.",
      };
    }
    return {
      success: false,
      error: err.response?.data?.error || err.response?.data || "Credit failed",
    };
  }
};


export const debitMockBank = async (amount, forceFailFromUI = false) => {
  try {
    const simulation = getSimulationConfig();

    const res = await api.post("/mockbank/debit", {
      amount,
      referenceId: "UI-" + Date.now(),
      forceFail: forceFailFromUI || simulation.forceFail,
    });

    return { success: true, data: res.data };
  } catch (err) {
    
    if (err.code === "ERR_NETWORK" || err.message?.includes("Network Error")) {
      return {
        success: false,
        error: "🌐 NETWORK SIMULATION: Connection failed. Please check your network.",
      };
    }
    
    if (err.code === "ECONNABORTED" || err.message?.includes("timeout")) {
      return {
        success: false,
        error: "⏱️ NETWORK SIMULATION: Request timed out. Please try again.",
      };
    }
    return {
      success: false,
      error: err.response?.data?.error || err.response?.data || "Debit failed",
    };
  }
};