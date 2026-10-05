# CI/CD de Orders

El workflow `.github/workflows/orders.yml` usa GitHub Actions y un runner Linux
local con la etiqueta `orders-demo`. El repositorio debe ser privado.

## Flujo

1. Un pull request o push a `master`/`main` ejecuta todos los tests con Java 21
   y una base MySQL 8.4 efímera en GitHub, separada de los datos de la demo.
2. Si pasan, construye la imagen usando el Dockerfile existente.
3. Un push a la rama predeterminada, o una ejecución manual desde esa rama,
   transfiere la imagen identificada por el SHA del commit a esta PC.
4. El runner local despliega esa misma imagen con Compose y comprueba que
   `GET http://127.0.0.1:8080/pedidos` devuelve un array JSON y HTTP 200.
   Si falla, intenta restaurar la imagen anterior y marca el job como fallido.

Los pull requests nunca ejecutan el job de despliegue. Los despliegues se
serializan. El rollback es de la imagen, no de datos ni migraciones SQL.

## Requisitos locales

- Docker Engine, Compose v2, curl, Python 3 y Git.
- MySQL del proyecto `orders-demo` iniciado y healthy.
- `/home/jesibel/PedidoMService/.env` existente, legible por el usuario del runner.
- Runner registrado en el repositorio con etiqueta `orders-demo`, instalado
  como servicio y ejecutado por `jesibel`, con acceso al grupo `docker`.
- La PC encendida y conectada a Internet. Si está apagada, el job espera al runner.

La carpeta de trabajo del runner es independiente del proyecto que editas.
El checkout no toca tu `.env` ni sobrescribe cambios locales. El despliegue
conserva `orders-demo_mysql_data` y solo recrea `orders-service`.
No administra Cloudflare ni modifica la URL del túnel: el proceso del túnel
debe estar en ejecución para que la API sea accesible públicamente.

## Activación en GitHub

1. Inicia sesión mediante `gh auth login` (incluye permiso `workflow`).
2. Publica este proyecto en un repositorio privado.
3. En Settings → Actions → Runners → New self-hosted runner selecciona Linux
   x64. Sigue los comandos oficiales, usando la etiqueta adicional `orders-demo`.
4. Instala e inicia el servicio del runner con `sudo ./svc.sh install jesibel`
   y `sudo ./svc.sh start` desde su carpeta. El usuario necesita acceso a Docker.
5. Ejecuta `Orders CI/CD` en la pestaña Actions. Comprueba que ambos jobs pasan.

Las credenciales de MySQL de CI son desechables. Las credenciales reales se
leen del `.env` local, nunca se suben a GitHub ni se necesitan como secrets.
El runner tiene acceso a Docker en esta PC; concede escritura al repositorio
solo a personas autorizadas a desplegar aquí. Mantén el repositorio privado.

## Hacer cambios

```bash
git add <archivos-modificados>
git commit -m "Describe el cambio"
git push
```

Consulta Actions para ver los tests, logs y resultado del despliegue. Desde
otra máquina puedes clonar el repositorio con tu cuenta y seguir el mismo flujo.
Para revisar antes de desplegar, utiliza una rama y un pull request hacia
la rama predeterminada.

## Despliegue manual de una imagen local

```bash
bash scripts/deploy-local.sh orders-service:SHA_DEL_COMMIT
```

No se ejecuta `down`, no se borran volúmenes ni se reinicia `cloudflared`.

Referencias oficiales:
- https://docs.github.com/en/actions/how-tos/manage-runners/self-hosted-runners/add-runners
- https://docs.github.com/en/actions/how-tos/manage-runners/self-hosted-runners/use-in-a-workflow
