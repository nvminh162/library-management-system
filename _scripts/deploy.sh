cd /app/libraryx-system/

BRANCH=$(git rev-parse --abbrev-ref HEAD)

echo "Deploying with branch: $BRANCH"

git checkout $BRANCH
git fetch -a
git pull

sudo docker network create microservices-network 2>/dev/null || true
sudo -E docker compose -f docker-compose.yml down
sudo -E docker compose -f docker-compose.yml pull discover-server
sudo -E docker compose -f docker-compose.yml up -d discover-server
# clean up unused images and containers
sudo docker system prune -af
