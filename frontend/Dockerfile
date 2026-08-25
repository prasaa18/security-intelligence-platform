# Multi-stage build with direct Angular CLI usage
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
    echo "=== Finding JS files ===" && \
    find /app/dist/frontend -name "*.js" -type f

FROM nginx:alpine

# Copy build output
COPY --from=build /app/dist/frontend /usr/share/nginx/html/

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]