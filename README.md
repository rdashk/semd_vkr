# Semd
Сервис для проверки корректности
структурированных электронных медицинских
документов.

Telegram-бот <b>@semd_med_bot</b>

## Запуск проекта
1. Добавление образа rabbitMQ
```
docker pull rabbitmq:3.11.0-management
docker volume create rabbitmq_data
docker run -d --hostname rabbitmq --name rabbitmq -p 5672:5672 -p 15672:15672 -v rabbitmq_data:/var/lib/rabbitmq --restart=unless-stopped rabbitmq:3.11.0-management
```
Подключиться к контейнеру с rabbitmq и создать пользователя, сделать его админом и установить права:
```
docker exec -it rabbitmq /bin/bash

rabbitmqctl add_user admin 1111
rabbitmqctl set_user_tags admin administrator
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"
```

2. Добавление образа MongoDB
```
docker run -p 27017:27017 -v mongodb-data:/data/db --name semd_mongodb -d mongo
```
3. Запуск клиентской части 

(client/src/main/java/ru/isu/Client.java)
4. Запуск серверной части 

(fileHandler/src/main/java/ru/isu/FileHandlerApplication.java)