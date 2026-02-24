# redis: 
# https://www.docker.com/blog/how-to-use-the-redis-docker-official-image/
docker run --name redis -p 6379:6379 -d redis
# redis cli
docker exec -it redis redis-cli