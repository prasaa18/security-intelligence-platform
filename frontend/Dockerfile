FROM node:20 AS build

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

# Try different build approaches
RUN npm run build -- --configuration=production || npm run build || echo "Build failed, checking available scripts..."

# Debug: show what's in dist
RUN ls -la dist/ || echo "No dist directory"
RUN ls -la dist/frontend/ || echo "No dist/frontend directory"
RUN ls -la dist/ || echo "Trying to find build output"

FROM nginx:alpine

# Try multiple possible build output locations
COPY --from=build /app/dist/frontend /usr/share/nginx/html/ || \
COPY --from=build /app/dist /usr/share/nginx/html/ || \
echo "No build output found"

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]