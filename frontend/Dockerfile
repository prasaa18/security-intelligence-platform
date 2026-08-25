# Multi-stage build with verbose output
FROM node:20 AS build

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

# Ensure Angular CLI is available
RUN npx ng version

# Build with verbose output
RUN npm run build -- --configuration=production --verbose

# Debug: Show what was created
RUN echo "=== Build output ===" && \
    ls -la /app/ && \
    echo "=== Dist directory ===" && \
    ls -la /app/dist/ && \
    echo "=== Dist/frontend directory ===" && \
    ls -la /app/dist/frontend/ && \
    echo "=== Finding index.html ===" && \
    find /app -name "index.html" -type f

FROM nginx:alpine

# Copy build output - try the expected location first
COPY --from=build /app/dist/frontend /usr/share/nginx/html/

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]