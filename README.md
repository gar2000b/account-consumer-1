# account-consumer-1
Account Consumer 1

docker network create -d bridge account 
docker network ls  

docker build -t gar2000b/account-consumer-1 .  
docker run -it -d -p 9087:9087 --network="account-consumer-1" --name account-consumer-1 gar2000b/account-consumer-1  

All optional:

docker create -it gar2000b/account-consumer-1 bash  
docker ps -a  
docker start ####  
docker ps  
docker attach ####  
docker remove ####  
docker image rm gar2000b/account-consumer-1  
docker exec -it account-consumer-1 sh  
docker login  
docker push gar2000b/account-consumer-1  