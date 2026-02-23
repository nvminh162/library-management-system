# KeyCloak integration

## Prerequisites

### Install KeyCloak on Docker

`docker pull quay.io/keycloak/keycloak:latest`

Run keyCloak on port 8180

`docker run -d --name keycloak-latest -p 8180:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev`

### Endpoint
https://www.keycloak.org/securing-apps/oidc-layers