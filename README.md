**Inventory Service**

Some docker command examples:

***Docker build***
> docker build -t sunright/inventory-service .

***Docker volume***
> docker volume create inventory_logs

***Docker run***
> docker run -p 80:8080 -v inventory_logs:/logs --env-file config/env.list a452a74f5771