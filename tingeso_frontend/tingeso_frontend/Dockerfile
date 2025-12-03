# Etapa 1: Build
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# Argumentos (Recibidos desde Jenkins)
ARG VITE_API_SERVER
ARG VITE_API_PORT
ARG VITE_KEYCLOAK_URL
ARG VITE_KEYCLOAK_REALM
ARG VITE_KEYCLOAK_CLIENT_ID

# Crear .env Y LUEGO CONSTRUIR (Esto faltaba)
RUN echo "VITE_API_SERVER=${VITE_API_SERVER}" > .env && \
    echo "VITE_API_PORT=${VITE_API_PORT}" >> .env && \
    echo "VITE_KEYCLOAK_URL=${VITE_KEYCLOAK_URL}" >> .env && \
    echo "VITE_KEYCLOAK_REALM=${VITE_KEYCLOAK_REALM}" >> .env && \
    echo "VITE_KEYCLOAK_CLIENT_ID=${VITE_KEYCLOAK_CLIENT_ID}" >> .env && \
    npm run build 
# ^^^ AQUÍ ESTABA EL ERROR: Faltaba el npm run build

# Etapa 2: Nginx
FROM nginx:stable-alpine
# Copiamos desde la carpeta dist generada en la etapa anterior
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]