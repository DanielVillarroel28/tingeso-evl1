import http from "../http-common";


const getMovements = (params) => {

    return http.get("/api/v1/kardex/", { params });
};

const kardexService = {
    getMovements,
};

export default kardexService;