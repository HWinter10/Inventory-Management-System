// Role enum file is used to keep role boundaries strict. We create this file so that 
// we can use Role as an enum in the User class
package com.hwinterton.inventory_api.model;

public enum Role {
    OWNER,
    MANAGER,
    STAFF
}
