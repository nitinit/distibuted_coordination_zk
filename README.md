Docker build for Zookeeper 

Go to director docker/3.5.8

```
docker build --tag zookeeper:1.0 .

docker run -p 2181:2181 -p 10080:8080 --name zookeeper zookeeper:1.0
```

For zookeeper navigator

```
docker run -d -e HTTP_PORT=9000 -p 9000:9000  --name zoonavigator   --restart unless-stopped   elkozmon/zoonavigator:latest
```

And use the connection string docker.for.mac.host.internal:2181 (varies by operating system, see this answer: https://stackoverflow.com/a/43541732/7983959
