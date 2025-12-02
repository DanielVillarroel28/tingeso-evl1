import http from "../http-common";

// Obtiene los movimientos, opcionalmente con filtros
const getMovements = (params) => {
    // Axios convierte el objeto 'params' a query string, ej: ?toolId=1&startDate=...
    return http.get("/api/v1/kardex/", { params });
};

const kardexService = {
    getMovements,
};

export default kardexService;