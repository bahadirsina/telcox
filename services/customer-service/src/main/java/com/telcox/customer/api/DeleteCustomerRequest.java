package com.telcox.customer.api;

import java.util.UUID;

/**
 * CUST-03: Soft-delete istegi. actorUserId audit log icin kim sildigini tutar.
 */
public record DeleteCustomerRequest(UUID actorUserId, String reason) {
}
