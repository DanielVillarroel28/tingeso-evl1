# Etapa 1: Build
FROM node:18-alpine AS builder

WORKDIR /app

# Copiar archivos de dependencias
COPY package*.json ./

# Instalar dependencias
RUN npm install

# Copiar el resto del código
COPY . .

# Variables de entorno para el build
ARG VITE_API_SERVER
ARG VITE_API_PORT
ARG VITE_KEYCLOAK_URL
ARG VITE_KEYCLOAK_REALM
ARG VITE_KEYCLOAK_CLIENT_ID

# Crear archivo .env para el build
RUN echo "VITE_API_SERVER=${VITE_API_SERVER}" > .env && \
    echo "VITE_API_PORT=${VITE_API_PORT}" >> .env && \
    echo "VITE_KEYCLOAK_URL=${VITE_KEYCLOAK_URL}" >> .env && \
    echo "VITE_KEYCLOAK_REALM=${VITE_KEYCLOAK_REALM}" >> .env && \
    echo "VITE_KEYCLOAK_CLIENT_ID=${VITE_KEYCLOAK_CLIENT_ID}" >> .env

# Build de la aplicación
RUN npm run build

# Etapa 2: Nginx
FROM nginx:stable-alpine

# Copiar el build desde la etapa anterior
COPY --from=builder /app/dist /usr/share/nginx/html

# Copiar configuración personalizada de nginx (para SPA routing)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]