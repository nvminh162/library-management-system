# ==========================================
# https://docs.docker.com/reference/dockerfile/
# ==========================================

# ==========================================
# Test build image:
# ==========================================
# D:\Course\library-management-system\discover-server
docker build -t discover-server .

# ==========================================
# Test chạy container:
# ==========================================
# D:\Course\library-management-system\discover-server (any where)
docker run -d -p 8761:8761 --name discover-server discover-server

# OTHER
docker compose build --no-cache notification-service