# Simple nginx container that copies pre-built Angular files
FROM nginx:alpine

# Copy the locally built Angular files
COPY dist/frontend/browser /usr/share/nginx/html/

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]