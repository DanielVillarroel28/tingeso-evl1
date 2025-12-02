import http from "../http-common";

// Obtener la tarifa de multa por atraso
const getLateFee = () => {
    return http.get("/config/late-fee");
};

// Actualizar la tarifa de multa por atraso
const updateLateFee = (data) => {
    return http.put("/config/late-fee", data);
};

const getRepairFee = () => {
    return http.get("/config/repair-fee");
};

// Actualizar la tarifa de reparación
const updateRepairFee = (data) => {
    return http.put("/config/repair-fee", data);
};

const configurationService = {
    getLateFee,
    updateLateFee,
    getRepairFee,
    updateRepairFee,
};

export default configurationService;