# Data Models

## User
### Fields:
- id - Long - primary key - auto generated
- username - string - unique
- passwordHash - string
- role - enum
- mustChangePassword - boolean - default true for temporary accounts
- active - boolean - default true

### Validation Rules:
- username is required
- passwordHash is required
- role is required

### Notes:
Represents a system user. Passwords are never stored in plain text.

--------------------------------------------------------

## Role (enum)
### Fields:
- OWNER
- MANAGER
- STAFF

### Notes:
Defines access level for each user.

--------------------------------------------------------

## Category
### Fields:
- id - integer - primary key - auto generated
- name - string - unique
- description - string - optional

### Validation Rules:
- name is required

### Notes:
Represents the top-level grouping of products, such as Merch, Beverages, or Supplements.

--------------------------------------------------------

## Subcategory
### Fields:
- id - integer - primary key - auto generated
- name - string
- description - string - optional
- categoryId - integer - foreign key

### Validation Rules:
- name is required
- categoryId is required

### Notes:
Represents a grouping under a category, such as Hoodies, T-Shirt or Energy Drinks, Protein Drinks.

--------------------------------------------------------

## Product
### Fields:
- id - integer - primary key - auto generated
- name - string
- description - string - optional
- subcategoryId - integer - foreign key
- active - boolean - default true

### Validation Rules:
- name is required
- subcategoryId is required

### Notes:
Represents the base product. Product does not store in inventory directly.

--------------------------------------------------------

## ProductVariant
### Fields:
- id - integer - primary key - auto generated
- productId - integer - foreign key
- quantityOnHand - integer
- lowStockThreshold - integer
- active - boolean - default true

### Validation Rules:
- productId is required
- quantityOnHand cannot be negative
- lowStockThreshold cannot be negative

### Notes:
Represents the specific inventory-tracked version of a product. A ProductVariant is defined by a combination of attribute values (such as size, color, or flavor) linked to a base product. Inventory is stored at the variant level.

--------------------------------------------------------

## AttributeType
### Fields:
- id - integer - primary key - auto generated
- name - string - unique
- description - string - optional

### Validation Rules:
- name is required

### Notes:
Represents the type of variant detail, such as Size, Color, Flavor, or Gender.

--------------------------------------------------------

## AttributeValue
### Fields:
- id - integer - primary key - auto generated
- attributeTypeId - integer - foreign key
- value - string

### Validation Rules:
- attributeTypeId is required
- value is required

### Notes:
Represents a specific value for an attribute type, such as Medium, Pink, or Chocolate.

--------------------------------------------------------

## VariantAttribute
### Fields:
- id - integer - primary key - auto generated
- variantId - integer - foreign key
- attributeValueId - integer - foreign key

### Validation Rules:
- variantId is required
- attributeValueId is required

### Notes:
Links a product variant to its attribute values.

--------------------------------------------------------

## InventoryAdjustment
### Fields:
- id - integer - primary key - auto generated
- variantId - integer - foreign key
- changeAmount - integer
- reason - string
- createdAt - datetime
- performedByUserId - integer - foreign key

### Validation Rules:
- variantId is required
- changeAmount is required
- reason is required

### Notes:
Represents a manual inventory change for a specific product variant.

--------------------------------------------------------

## AuditLog
### Fields:
- id - integer - primary key - auto generated
- actorUserId - integer - foreign key - optional
- action - string
- details - string - optional
- createdAt - datetime

### Validation Rules:
- action is required

### Notes:
Stores important system events for accountability and traceability.

