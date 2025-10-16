#!/bin/bash
cd /home/kavia/workspace/code-generation/smart-invoice-and-payment-reminder-system-4219-4234/invoice_backend
./gradlew checkstyleMain
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

