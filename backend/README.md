# Module 8 – AI Code Review Assistant (Backend)

## Overview

This module implements the backend for the AI Code Review Assistant.

Implemented Features:

- AI Review REST endpoint
- Gemini API integration
- Prompt Builder
- Gemini Request/Response DTOs
- Response Parser
- Code Review Service
- Review Analysis Service
- PostgreSQL integration
- MongoDB review storage
- Structured review response
- AI review status workflow

Endpoint:

POST /api/reviews/analyze

Sample Request:

```json
{
  "taskId": "<task-id>",
  "requestedBy": 1,
  "reviewSource": "PASTED_CODE",
  "language": "Java",
  "sourceCode": "public class Test { ... }"
}
```

Current Limitation

The implementation is complete.

Local execution currently depends on a valid Google Gemini API key with available quota.

If the configured API key has exhausted its quota, Google returns:

HTTP 429 RESOURCE_EXHAUSTED

which results in review generation failing until quota is restored.

Required Local Configuration

- Java 17
- PostgreSQL
- MongoDB
- Gemini API Key
- application.properties configured correctly

Notes

The backend successfully:

- validates request
- loads Task
- loads User
- builds AI prompt
- calls Gemini API
- parses AI response
- stores review history in MongoDB

The remaining dependency is an active Gemini API quota.