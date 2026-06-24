# Configuration Profiles

The Config Server uses its native backend and serves configuration packaged under
`infrastructure/config-server/src/main/resources/config/`.

Each client application has these four files:

- `application.yaml`: service-specific defaults such as port, database name and Kafka consumer group.
- `application-dev.yaml`: local development overrides.
- `application-prod.yaml`: production operational overrides.
- `application-test.yaml`: test overrides; Eureka registration and Kafka listeners are disabled globally.

Shared values are in `config/application*.yaml`; service-specific values are in
`config/<application-name>/application*.yaml`. Client modules keep only a small
bootstrap `application.yaml` containing the application name, active profile and
Config Server URL.

Use `SPRING_PROFILES_ACTIVE` to select a profile. It defaults to `dev` for local
development. Docker Compose can continue to override connection details through
its existing environment variables.

After starting Config Server, inspect the resolved configuration with:

```bash
curl http://localhost:18888/customer-service/dev
curl http://localhost:18888/api-gateway/prod
```
