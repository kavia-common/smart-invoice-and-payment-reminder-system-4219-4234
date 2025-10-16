# Invoice Backend

This backend is a Spring Boot service providing REST APIs for the Smart Invoice & Payment Reminder System.

## Environment Variables

Set these in your deployment platform or `.env` (do not commit secrets). Defaults shown in parentheses.

Authentication and Admin bootstrap:
- JWT_SECRET (required in prod)
- JWT_EXP_MINUTES (60)
- ADMIN_EMAIL (optional; seeds an initial admin user)
- ADMIN_PASSWORD (optional; seeds the admin password)

CORS:
- CORS_ALLOWED_ORIGINS (*)
  - Comma-separated list of allowed origins.
  - Example: http://localhost:3000,https://app.example.com

Database (prod profile):
- DB_URL, DB_USERNAME, DB_PASSWORD, DB_SCHEMA, DB_POOL_SIZE (10)

Storage:
- STORAGE_PROVIDER (local|s3|azure; default: local)
- STORAGE_LOCAL_PATH (attachments)
- S3_BUCKET, S3_REGION, S3_ACCESS_KEY, S3_SECRET_KEY (if STORAGE_PROVIDER=s3)
- AZURE_CONN (if STORAGE_PROVIDER=azure)

Reminders and links:
- REMINDER_CRON (e.g., 0 0/30 * * * *)
- SITE_URL (used for building invoice links; also used by frontend auth redirects)

Messaging providers:
- EMAIL_PROVIDER (noop|smtp|sendgrid)
- SMS_PROVIDER (noop|twilio|vonage)
- WHATSAPP_PROVIDER (noop|meta|twilio)

Email:
- EMAIL_FROM, SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD, SENDGRID_API_KEY

SMS:
- SMS_FROM, TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, VONAGE_API_KEY, VONAGE_API_SECRET

WhatsApp:
- WHATSAPP_FROM, META_WA_TOKEN, META_WA_PHONE_ID

Webhooks:
- WEBHOOKS_OUTGOING_ENABLED (false)
- WEBHOOK_OUTGOING_SIGNING_SECRET

## CORS Configuration

- Property: `app.cors.allowed-origins` mapped from `CORS_ALLOWED_ORIGINS` env.
- The application sends CORS headers allowing the configured origins and methods.
- Preflight (OPTIONS) is permitted by Spring Security.

Example for local dev with React on port 3000:
CORS_ALLOWED_ORIGINS=http://localhost:3000

## Profiles

- Default profile: dev (H2 in-memory)
- Production: set `spring.profiles.active=prod` and the DB_* environment variables.

## OpenAPI

- Swagger UI: /swagger-ui.html
- API docs: /api-docs
