---
name: api-builder-java
description: Creates RESTful API endpoints following team conventions. Includes validation, error handling, and documentation. Trigger when user asks to create endpoint, add API, or build route.
tools: Read, Write, Edit, Bash
---

# API Builder Skill

Khi tạo API endpoint mới, hãy tuân theo quy trình sau:

## 1. Gather Requirements

Hỏi user về:

- HTTP method và path
- Request body schema (nếu có)
- Response format mong muốn
- Authentication requirements

## 2. Implementation

Tạo endpoint với structure:

- Input validation sử dụng Zod/Joi
- Error handling với appropriate status codes
- Response formatting nhất quán
  Tham khảo file CONVENTIONS.md để biết chi tiết về naming và structure.

## 3. Documentation

Tự động tạo OpenAPI/Swagger documentation cho endpoint mới.

## 4. Testing

Tạo test cases cơ bản covering:

- Happy path
- Validation errors
- Authentication failures
