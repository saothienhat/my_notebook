# Performance Guidelines

## Database Queries

- Tránh N+1 queries — sử dụng eager loading khi cần
- Index cho các columns thường xuyên query
- Limit và paginate kết quả lớn

## Caching Strategy

- Cache expensive computations
- Sử dụng appropriate TTL
- Invalidate cache đúng cách khi data thay đổi

## Code Patterns

- Lazy loading cho heavy resources
- Avoid blocking operations trong main thread
- Sử dụng connection pooling
