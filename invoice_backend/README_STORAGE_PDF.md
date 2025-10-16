# Storage & PDF

Environment keys (set via container/platform; not all required unless selected):
- STORAGE_PROVIDER: local | s3 | azure (default local)
- STORAGE_LOCAL_PATH: base folder for local provider (default attachments)
- S3_BUCKET, S3_REGION, S3_ACCESS_KEY, S3_SECRET_KEY: only used if STORAGE_PROVIDER=s3
- AZURE_CONN: only used if STORAGE_PROVIDER=azure

Dev profile:
- Uses LocalStorageProvider with default path 'attachments' at project root.

Endpoints:
- POST /api/files/upload (multipart/form-data): fields partnerId [required], invoiceId [optional], file [required]
- GET /api/files/{id}/download: download by attachment id
- GET /api/invoices/{id}/pdf: generate and stream PDF for invoice

Note: S3/Azure providers are placeholders in this MVP and will throw if selected. Keep STORAGE_PROVIDER=local for development.

PDF:
- Uses OpenHTMLtoPDF with "Ocean Professional" themed HTML/CSS.
