**Inventory Service**

Build jar command:
> ./gradlew clean build

Some docker command:

***Docker build***
> docker build -t dedomena/inventory-service .

***Docker volume***
> docker volume create inventory_logs

***Docker push***
> docker push dedomena/inventory-service:latest
> 
***Docker run***
> docker run -p 80:8080 -v inventory_logs:/logs --env-file config/env.list a452a74f5771