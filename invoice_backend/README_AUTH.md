# Authentication & RBAC

Environment variables (set these in .env or deployment platform):
- JWT_SECRET: long random string for HS256 signing (required in prod)
- JWT_EXP_MINUTES: token validity window in minutes (default 60)
- ADMIN_EMAIL: initial admin email to seed (optional)
- ADMIN_PASSWORD: initial admin password to seed (optional)
- CORS_ALLOWED_ORIGINS: allowed origins for CORS (comma-separated), default '*'

Auth endpoints:
- POST /api/auth/register {email, password, fullName}
- POST /api/auth/login {email, password}
- POST /api/auth/refresh {token}

Demo endpoints:
- GET /api/demo/public (no auth)
- GET /api/demo/user (ROLE_USER or ROLE_ADMIN)
- GET /api/demo/admin (ROLE_ADMIN)

OpenAPI docs:
- /swagger-ui.html (Authorize button: use 'Bearer <token>')
