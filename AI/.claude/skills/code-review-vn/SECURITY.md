# Security Checklist

## Input Validation

- [ ] Tất cả user inputs được validate
- [ ] Sử dụng parameterized queries cho database
- [ ] Escape output khi render HTML

## Authentication & Authorization

- [ ] Kiểm tra authentication ở mọi protected endpoints
- [ ] Verify authorization cho mỗi action
- [ ] Session management an toàn

## Data Protection

- [ ] Sensitive data được encrypt
- [ ] Không log sensitive information
- [ ] Secure headers được set đúng
