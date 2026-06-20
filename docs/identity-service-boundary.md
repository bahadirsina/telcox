# Identity Service Boundary

Keycloak is TelcoX's only identity provider and the only component that issues,
refreshes or revokes OAuth2/OIDC tokens. The API Gateway validates those tokens.

Identity Service is an application-owned adapter. It may store a Keycloak user's
`sub` value, TelcoX-specific profile data and audit records. It must not:

- accept passwords;
- issue, sign, refresh or validate application JWTs;
- persist token material or password hashes for new users.

The `identity_service_refresh_token` table is retained only for migration
compatibility. New session and token lifecycle work belongs to Keycloak; SEC-06
may keep gateway-side revocation metadata in Redis without storing tokens.
