# smart-invoice-and-payment-reminder-system-4219-4234

This repository contains the backend Spring Boot service (invoice_backend). The frontend React app lives in a sibling workspace.

Quick environment setup:

Backend (invoice_backend):
- CORS_ALLOWED_ORIGINS: comma-separated origins, e.g. http://localhost:3000
- JWT_SECRET: long random string (required in prod)
- JWT_EXP_MINUTES: token validity minutes (default 60)
- ADMIN_EMAIL, ADMIN_PASSWORD: optional admin bootstrap
- See invoice_backend/README.md for full list

Frontend (invoice_frontend):
- REACT_APP_API_BASE_URL: base URL for backend API, e.g. http://localhost:8080
- See the frontend README for usage details.
- In code, set axios/fetch base to process.env.REACT_APP_API_BASE_URL (no trailing slash).