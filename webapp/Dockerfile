# syntax=docker/dockerfile:1

# --- Build stage ---
FROM node:22-alpine AS builder

WORKDIR /app

# Copy package manifests first to leverage layer caching
COPY package*.json ./

# Install dependencies using a clean, reproducible lockfile install
RUN npm ci

# Copy application source and build the production bundle
COPY . .
RUN npm run build

# --- Runtime stage ---
FROM nginx:alpine

# Create a non-root user for nginx
RUN adduser -S -G nginx nginxuser 2>/dev/null || true

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built static assets to nginx document root
COPY --from=builder /app/dist /usr/share/nginx/html

# Adjust ownership so the non-root nginx user can read the files
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown nginx:nginx /etc/nginx/conf.d/default.conf && \
    touch /var/run/nginx.pid && \
    chown nginx:nginx /var/run/nginx.pid

USER nginx

EXPOSE 5173

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --server-response --quiet --output-document=/dev/null http://localhost:5173 2>&1 | grep -q 'HTTP/' || exit 1

CMD ["nginx", "-g", "daemon off;"]
