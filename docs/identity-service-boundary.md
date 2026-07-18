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

Current adapter endpoints:

- `GET /api/v1/identity/profile/me` reads or provisions the application profile
  from gateway-relayed user context headers.
- `PATCH /api/v1/identity/profile/me` updates TelcoX-owned profile fields only.
- `GET /api/v1/identity/audit/me` lists audit records for the current local
  application profile.

These endpoints trust user context produced by the API Gateway after Keycloak JWT
validation. They do not authenticate credentials, issue tokens or make role
authorization decisions.
