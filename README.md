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
 
***Docker run***
> docker pull dedomena/inventory-service:latest && docker rm -f inventory-service && docker run -d -p 3001:8080 -v inventory_logs:/logs --env-file env.list --name inventory-service dedomena/inventory-service:latest

***Docker Log***
> docker logs -f inventory-service