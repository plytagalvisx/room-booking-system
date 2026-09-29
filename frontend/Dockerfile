# ---------- Build stage ----------
FROM node:24-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Vite’s VITE_* values are substituted when "npm run build" runs,
# so for this Docker image we pass them as build arguments, not runtime variables.

ARG VITE_API_BASE_URL
ARG VITE_KEYCLOAK_URL
ARG VITE_KEYCLOAK_REALM
ARG VITE_KEYCLOAK_CLIENT_ID

ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
ENV VITE_KEYCLOAK_URL=${VITE_KEYCLOAK_URL}
ENV VITE_KEYCLOAK_REALM=${VITE_KEYCLOAK_REALM}
ENV VITE_KEYCLOAK_CLIENT_ID=${VITE_KEYCLOAK_CLIENT_ID}

RUN npm run build


# ---------- Runtime stage ----------
# OBS! Nginx is a reverse proxy and web server that can serve static content.
# Usually, the role of a reverse proxy is to protect the backend server from direct access by clients,
# and to provide additional features such as security, load balancing, caching, and SSL termination.
FROM nginx:stable-alpine-slim

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

# The official Nginx image is designed for serving static content like the files produced by Vite.