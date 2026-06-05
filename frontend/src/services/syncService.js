import api from "./api";

export const syncTransaction = async (tx) => {
    try {
        const response = await api.post("/queue/store", {
            ...tx,
            idempotencyKey: tx.transactionId
        });

        return {
            status: response.status,
            data: response.data,
            success: response.status >= 200 && response.status < 300
        };

    } catch (error) {
        return {
            status: error?.response?.status || 500,
            data: error?.response?.data || null,
            success: false,
            error: true
        };
    }
};

export const fetchWalletBalance = async () => {
    try {
        const res = await api.get("/wallet/balance");

        return {
            success: true,
            data: res.data
        };
    } catch (err) {
        return {
            success: false,
            data: null
        };
    }
};


export const fetchTransactionHistory = async () => {
    try {
        const res = await api.get("/transaction/history");

        return {
            success: true,
            data: res.data || []
        };
    } catch (err) {
        return {
            success: false,
            data: []
        };
    }
};