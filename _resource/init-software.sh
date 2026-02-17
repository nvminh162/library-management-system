# Axon server
# local: D:/Workspace/course/project/library-management-system/_software/AxonServer-2025.2.5
java -jar .\axonserver.jar

# docker:
docker run -d --name axon-server -p 8024:8024 -p 8124:8124 docker.axoniq.io/axoniq/axonserver