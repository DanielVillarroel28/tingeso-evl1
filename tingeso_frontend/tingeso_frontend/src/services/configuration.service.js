import http from "../http-common";

const getLateFee = () => {
    return http.get("/config/late-fee");
};

const updateLateFee = (data) => {
    return http.put("/config/late-fee", data);
};

const getRepairFee = () => {
    return http.get("/config/repair-fee");
};


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