# Multi-stage build with correct Angular 17 build output location
FROM node:20 AS build

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

# Use Angular CLI directly for build
RUN npx ng build --configuration=production

# Debug: Show what was created
RUN echo "=== Build output ===" && \
    ls -la /app/ && \
    echo "=== Dist directory ===" && \
    ls -la /app/dist/ && \
    echo "=== Dist/frontend directory ===" && \
    ls -la /app/dist/frontend/ && \
    echo "=== Dist/frontend/browser directory ===" && \
    ls -la /app/dist/frontend/browser/ && \
    echo "=== Finding JS files ===" && \
    find /app/dist/frontend -name "*.js" -type f

FROM nginx:alpine

# Copy build output from browser directory (Angular 17 structure)
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html/

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]