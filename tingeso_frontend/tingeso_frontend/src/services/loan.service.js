import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get('/api/v1/loans/');
}

const create = data => {
    return httpClient.post("/api/v1/loans/", data);
}

const get = id => {
    return httpClient.get(`/api/v1/loans/${id}`);
}

const update = (id, data) => {
    return httpClient.put(`/api/v1/loans/${id}`, data);
}

const remove = id => {
    return httpClient.delete(`/api/v1/loans/${id}`);
}

const processReturn = (id, data) => {
    return httpClient.post(`/api/v1/loans/${id}/return`, data); 
};


const getMyLoans = () => {
    return httpClient.get("/api/v1/loans/my-loans");
};


export default { getAll, create, get, update, remove, processReturn, getMyLoans };