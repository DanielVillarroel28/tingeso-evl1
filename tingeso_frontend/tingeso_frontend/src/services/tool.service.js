import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get('/api/v1/tools/');
}

const create = data => {
    return httpClient.post("/api/v1/tools/", data);
}

const get = id => {
    return httpClient.get(`/api/v1/tools/${id}`);
}

const update = (id, data) => {
    return httpClient.put(`/api/v1/tools/${id}`, data);
}

const remove = id => {
    return httpClient.delete(`/api/v1/tools/${id}`);
}


export default { getAll, create, get, update, remove };