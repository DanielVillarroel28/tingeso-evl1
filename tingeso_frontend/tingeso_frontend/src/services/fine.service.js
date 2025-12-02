import http from "../http-common";

// Obtener todas las multas
const getAll = () => {
    return http.get("/api/v1/fines/");
};

// Marcar una multa como pagada
const pay = (id) => {
    return http.put(`/api/v1/fines/${id}/pay`);
};

const getMyFines = () => {
    return http.get("/api/v1/fines/my-fines");
};

const fineService = {
    getAll,
    pay,
    getMyFines,
};

export default fineService;