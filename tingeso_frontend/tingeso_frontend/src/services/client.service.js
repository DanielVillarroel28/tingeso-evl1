import httpClient from "../http-common";

const getAll = () => {
    return httpClient.get('/api/v1/clients/');
}

const create = data => {
    return httpClient.post("/api/v1/clients/", data);
}

const get = id => {
    return httpClient.get(`/api/v1/clients/${id}`);
}

const update = (id, data) => {
    return httpClient.put(`/api/v1/clients/${id}`, data);
}

const remove = id => {
    return httpClient.delete(`/api/v1/clients/${id}`);
}

const getMyProfile = () => {
    return httpClient.get('/api/v1/clients/me');
}

// Actualiza los datos del perfil del usuario actual
const updateMyProfile = data => {
    return httpClient.put('/api/v1/clients/me', data);
}
export default { getAll, create, get, update, remove, getMyProfile, updateMyProfile };