# API Notes
## Endpoints :
endpoints are just (URL + HTTP method)

### Authentication
POST /api/auth/login -> authenticates user credentials and starts authenticated access
    Permissions: Public
POST /api/auth/change-password -> allows authenticated user to change their own password, including first-login password change when mustChangePassword = true
    Permissions: Authenticated user / Authenticated user + mustChangePassword = true

### Users
GET /api/users -> returns all users
    Permissions: Owner only
GET /api/users/{id} -> returns user by id
    Permissions: Owner only
POST /api/users -> creates a user
    Permissions: Owner only
PATCH /api/users/{id}/reactivate -> reactivates inactive user
    Permissions: Owner only
PATCH /api/users/{id}/reset-password
    Permissions: Owner only
PATCH /api/users/{id} -> updates a user
    Permissions: Owner only
DELETE /api/users/{id} -> "deletes" (inactivates) a user
    Permissions: Owner only

### Categories
GET /api/categories -> returns all categories
    Permissions: Owner, Manager, Staff
GET /api/categories/{id} -> returns category by id
    Permissions: Owner, Manager, Staff
POST /api/categories -> creates a category
    Permissions: Owner, Manager
PATCH /api/categories/{id} -> updates category fields
    Permissions: Owner, Manager

### Subcategories
GET /api/subcategories -> returns all subcategories
    Permissions: Owner, Manager, Staff
GET /api/subcategories/{id}
    Permissions: Owner, Manager, Staff
GET /api/categories/{id}/subcategories
    Permissions: Owner, Manager, Staff
POST /api/subcategories
    Permissions: Owner, Manager
PATCH /api/subcategories/{id}
    Permissions: Owner, Manager

### Products
GET /api/products -> returns all base products
    Permissions: Owner, Manager, Staff
GET /api/products/{id} -> returns product by id
    Permissions: Owner, Manager, Staff
GET /api/products/{id}/variants -> returns all variants belonging to a specific product
    Permissions: Owner, Manager, Staff
POST /api/products -> creates product
    Permissions: Owner, Manager
PATCH /api/products/{id} -> updates product fields
    Permissions: Owner, Manager

### Product Variants
GET /api/variants -> returns all product variants
    Permissions: Owner, Manager, Staff
GET /api/variants/{id} -> returns variant by id
    Permissions: Owner, Manager, Staff
POST /api/variants -> creates product variant
    Permissions: Owner, Manager
PATCH /api/variants/{id} -> updates variant fields
    Permissions: Owner, Manager
PATCH /api/variants/{id}/threshold - updated low stock threshold 
    Permissions: Owner, Manager

### Inventory Adjustment
POST /api/inventory-adjustments -> creates inventory adjustment event
    Permissions: Owner, Manager
GET /api/inventory-adjustments -> returns inventory adjustment history
    Permissions: Owner, Manager

### Low Stock / Reorder
GET /api/inventory/low-stock -> returns variants at or below threshold
    Permissions: Owner, Manager

### Audit Logs
GET /api/audit-logs -> returns paginated audit log entries
Query Params:
    page (default: 0)
    size (default: 20)
    sort (default: createdAt,desc)
Permissions: Owner only