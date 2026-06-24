FROM node:22-alpine AS build
WORKDIR /workspace/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
COPY assets/ /workspace/assets/

ARG VITE_API_BASE_URL=/api/v1
ARG VITE_ENABLE_LIVE_API=false
ARG VITE_ENVIRONMENT=UAT
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL \
    VITE_ENABLE_LIVE_API=$VITE_ENABLE_LIVE_API \
    VITE_ENVIRONMENT=$VITE_ENVIRONMENT
RUN npm run build

FROM nginx:1.27-alpine
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/frontend/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=15s --timeout=5s --retries=5 CMD wget -qO- http://127.0.0.1/ >/dev/null || exit 1
