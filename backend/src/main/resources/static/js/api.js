const API_BASE_URL = (window.location.protocol.startsWith("http")) ? window.location.origin + "/api" : "http://localhost:8080/api";

const api = {
    getToken: () => localStorage.getItem("token"),
    getRole: () => localStorage.getItem("role"),
    getUsername: () => localStorage.getItem("username"),
    getCustomerId: () => localStorage.getItem("customerId"),

    setAuth: (data) => {
        localStorage.setItem("token", data.token);
        localStorage.setItem("role", data.role);
        localStorage.setItem("username", data.username);
        localStorage.setItem("customerId", data.customerId);
    },

    logout: () => {
        localStorage.clear();
        window.location.href = "login.html";
    },

    request: async (endpoint, options = {}) => {
        const token = api.getToken();
        const headers = {
            "Content-Type": "application/json",
            ...(token ? { "Authorization": `Bearer ${token}` } : {}),
            ...options.headers
        };

        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, { ...options, headers });
            if (response.status === 401 || response.status === 403) {
                api.logout();
                return;
            }
            return await response.json();
        } catch (error) {
            console.error("API Request Error:", error);
            throw error;
        }
    }
};

function checkAuth(requiredRole = null) {
    const token = api.getToken();
    const role = api.getRole();
    if (!token) {
        window.location.href = "login.html";
        return;
    }
    if (requiredRole && role !== requiredRole) {
        alert("Access Denied: You do not have permission to view this page.");
        window.location.href = role === "ROLE_ADMIN" ? "admin-dashboard.html" : "customer-dashboard.html";
    }
}
