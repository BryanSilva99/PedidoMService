# Orders Service (PedidoMService)

CI/CD con GitHub Actions y despliegue en esta PC: [guía de configuración](docs/CI-CD.md).

Demo del bounded context Pedidos. Java 21, Spring Boot 4.1.1, Maven y MySQL.
El dominio y los endpoints existentes se conservan. Inventory se despliega por separado.

## Docker

Requisito en la PC: Docker en ejecución y Docker Compose v2 o posterior, con
contenedores Linux. No necesitas instalar Java, Maven ni MySQL en el host.
Desde esta carpeta:

```bash
cp .env.example .env
# Edita .env y cambia ambas contraseñas de ejemplo antes de iniciar.
docker compose config --quiet
docker compose up -d --build
docker compose ps
docker compose logs -f orders-service mysql
```

Variables de `.env`: `MYSQL_DATABASE` (pedidos), `MYSQL_USER` (pedidos_user),
`MYSQL_PASSWORD` y `MYSQL_ROOT_PASSWORD`. `.env` está ignorado por Git y no entra
en la imagen. No publiques tus credenciales. Compose requiere contraseñas no vacías.

Compose transforma esas variables en `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` para
Spring. La URL JDBC usa `mysql:3306`, el nombre del servicio en la red interna.
`localhost` dentro de Orders sería el propio contenedor de Orders.
La conexión interna del demo usa `useSSL=false&allowPublicKeyRetrieval=true`;
esto no configura ni reemplaza el HTTPS público que se preparará posteriormente.

La imagen se construye con Maven/JDK 21 y ejecuta solamente el JAR
`PedidoMService-0.0.1-SNAPSHOT.jar` con JRE 21. El build omite tests porque la prueba
de integración necesita MySQL en ejecución. Esto no equivale a validar integración.

- Orders escucha en `0.0.0.0:8080`, publicado en el puerto 8080 de la PC.
- MySQL 8.4 no publica el puerto 3306 al host.
- El healthcheck ejecuta `SELECT 1` con el usuario y la base configurados; Orders
  espera a que MySQL esté healthy. No se añadió Actuator ni un endpoint de salud.
- El volumen `orders-demo_mysql_data` guarda `/var/lib/mysql`.
- Spring crea/actualiza las tablas mediante la configuración existente de Hibernate.

### Comprobar HTTP y persistencia

Espera a ver MySQL healthy y el mensaje de arranque de Spring en los logs.
Los siguientes comandos usan curl como cliente de demostración:

```bash
curl -i -X POST http://localhost:8080/pedidos
# Copia el UUID de la respuesta 201:
PEDIDO_ID='UUID_RECIBIDO'
curl -i "http://localhost:8080/pedidos/$PEDIDO_ID"
curl -i -X POST "http://localhost:8080/pedidos/$PEDIDO_ID/items" \
  -H 'Content-Type: application/json' \
  -d '{"productoId":1,"cantidad":2,"precioUnitario":50.00}'
curl -i -X POST "http://localhost:8080/pedidos/$PEDIDO_ID/confirmar"
# Debe devolver CONFIRMADO, un ítem y total 100.

docker compose up -d --force-recreate orders-service
# Espera nuevamente al arranque de Spring y consulta el mismo UUID:
curl -i "http://localhost:8080/pedidos/$PEDIDO_ID"

# Para verificar además el volumen recreando ambos contenedores:
docker compose down
docker compose up -d
# Tras el arranque, consulta otra vez el mismo UUID.
curl -i "http://localhost:8080/pedidos/$PEDIDO_ID"
```

Los tres GET deben conservar el pedido; los posteriores a confirmar deben conservar
estado, ítems y total. No utilices `down -v`: eliminaría el volumen.

### Operación y portabilidad

```bash
docker compose logs --tail=100 orders-service mysql
docker compose stop       # Detiene, conserva contenedores y volumen.
docker compose start      # Arranca los contenedores detenidos.
docker compose down       # Retira contenedores/red, conserva volumen.
```

Las variables de inicialización de MySQL se aplican cuando el volumen está vacío.
Cambiar `.env` después no cambia automáticamente usuarios/contraseñas de una base
existente. Conserva las credenciales de ese volumen o cámbialas dentro de MySQL.

Copia este proyecto completo (sin target, IDE ni .env personal) a PC1, prepara allí
`.env` y ejecuta `docker compose up -d --build`. El volumen es local a esa PC;
copiar el código no copia los datos existentes. Desde otra PC usa
`http://IP_DE_PC1:8080/pedidos/...`, con conectividad y puerto permitido en su firewall.
AWS API Gateway utilizará posteriormente un backend HTTPS que llegue a este
servicio HTTP; esta fase no publica HTTPS ni configura AWS o túneles.

### Estado de validación

El paquete JAR se construyó fuera de Docker. La ejecución de imágenes, endpoints
Docker y persistencia del volumen quedan pendientes: el entorno de implementación
no dispone de Docker Engine. Los comandos anteriores son el procedimiento de
verificación, no resultados de pruebas Docker ya realizadas.
