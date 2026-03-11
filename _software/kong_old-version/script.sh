# docs latest:
# https://developer.konghq.com/gateway/install/docker/
# =====================================================================================================

# create net work
docker network create kong-net
# “You can name this network anything you want. We use kong-net as an example throughout this guide.”
# =====================================================================================================

# create postgres container
docker run -d --name kong-database \
  --network=kong-net \
  -p 5432:5432 \
  -e "POSTGRES_USER=kong" \
  -e "POSTGRES_DB=kong" \
  -e "POSTGRES_PASSWORD=kongpass" \
  postgres:13
#
docker run -d --name kong-database --network=kong-net -p 5432:5432 -e "POSTGRES_USER=kong" -e "POSTGRES_DB=kong" -e "POSTGRES_PASSWORD=kongpass" postgres:13
# POSTGRES_USER and POSTGRES_DB: Set these values to kong. This is the default value that Kong Gateway expects.
# POSTGRES_PASSWORD: Set the database password to any string.
# In this example, the Postgres container named kong-database can communicate with any containers on the kong-net network.
# =====================================================================================================

# prepare kong database
docker run --rm --network=kong-net \
  -e "KONG_DATABASE=postgres" \
  -e "KONG_PG_HOST=kong-database" \
  -e "KONG_PG_PASSWORD=kongpass" \
  -e "KONG_PASSWORD=test" \
  kong/kong-gateway:3.5.0.1 kong migrations bootstrap
#
docker run --rm --network=kong-net -e "KONG_DATABASE=postgres" -e "KONG_PG_HOST=kong-database" -e "KONG_PG_PASSWORD=kongpass" -e "KONG_PASSWORD=test" kong/kong-gateway:3.5.0.1 kong migrations bootstrap
# Where:
# KONG_DATABASE: Specifies the type of database that Kong Gateway is using.
# KONG_PG_HOST: The name of the PostgreSQL Docker container that is communicating over the kong-net network, from the previous step.
# KONG_PG_PASSWORD: The password that you set when bringing up the Postgres container in the previous step.
# KONG_PASSWORD (Enterprise only): The default password for the admin super user for Kong Gateway.
# [IMAGE-NAME:TAG] kong migrations bootstrap: In order, this is the Kong Gateway container image and tag, followed by the command to Kong to prepare the Postgres database.
# =====================================================================================================

# Run the following command to start a container with Kong Gateway:
docker run -d --name kong-gateway \
  --network=kong-net \
  -e "KONG_DATABASE=postgres" \
  -e "KONG_PG_HOST=kong-database" \
  -e "KONG_PG_USER=kong" \
  -e "KONG_PG_PASSWORD=kongpass" \
  -e "KONG_PROXY_ACCESS_LOG=/dev/stdout" \
  -e "KONG_PROXY_ERROR_LOG=/dev/stderr" \
  -e "KONG_ADMIN_ACCESS_LOG=/dev/stdout" \
  -e "KONG_ADMIN_ERROR_LOG=/dev/stderr" \
  -e "KONG_ADMIN_LISTEN=0.0.0.0:8001" \
  -e "KONG_ADMIN_GUI_URL=http://localhost:8002" \
  -e KONG_LICENSE_DATA \
  -p 8000:8000 \
  -p 8443:8443 \
  -p 8001:8001 \
  -p 8444:8444 \
  -p 8002:8002 \
  -p 8445:8445 \
  -p 8003:8003 \
  -p 8004:8004 \
  kong/kong-gateway:3.5.0.1
#
docker run -d --name kong-gateway --network=kong-net -e "KONG_DATABASE=postgres" -e "KONG_PG_HOST=kong-database" -e "KONG_PG_USER=kong" -e "KONG_PG_PASSWORD=kongpass" -e "KONG_PROXY_ACCESS_LOG=/dev/stdout" -e "KONG_PROXY_ERROR_LOG=/dev/stderr" -e "KONG_ADMIN_ACCESS_LOG=/dev/stdout" -e "KONG_ADMIN_ERROR_LOG=/dev/stderr" -e "KONG_ADMIN_LISTEN=0.0.0.0:8001" -e "KONG_ADMIN_GUI_URL=http://localhost:8002" -e KONG_LICENSE_DATA -p 8000:8000 -p 8443:8443 -p 8001:8001 -p 8444:8444 -p 8002:8002 -p 8445:8445 -p 8003:8003 -p 8004:8004 kong/kong-gateway:3.5.0.1
# Where:
# --name and --network: The name of the container to create, and the Docker network it communicates on.
# KONG_DATABASE: Specifies the type of database that Kong Gateway is using.
# KONG_PG_HOST: The name of the PostgreSQL Docker container that is communicating over the kong-net network.
# KONG_PG_USER and KONG_PG_PASSWORD: The PostgreSQL username and password. Kong Gateway needs this login information to store configuration data in the KONG_PG_HOST database.
# All _LOG parameters: Set filepaths for the logs to output to, or use the values in the example to print messages and errors to stdout and stderr.
# KONG_ADMIN_LISTEN: The port that the Kong Admin API listens on for requests.
# KONG_ADMIN_GUI_URL: The URL for accessing Kong Manager, preceded by a protocol (for example, http://).
# KONG_LICENSE_DATA (Enterprise only): If you have a license file and have saved it as an environment variable, this parameter pulls the license from your environment.
# =====================================================================================================

# Verify your installation:
# Access the /services endpoint using the Admin API:
# curl -i -X GET --url http://localhost:8001/services
# You should receive a 200 status code.
# =====================================================================================================

# Verify that Kong Manager is running by accessing it using the URL specified in KONG_ADMIN_GUI_URL:
# http://localhost:8002
# =====================================================================================================
