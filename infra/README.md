# Anonymus en AWS Academy Learner Lab

Esta primera etapa automatiza la infraestructura y prepara la instalación del backend.
No publica el frontend ni conecta sus servicios de localStorage a la API. Eso sigue pendiente.
No se han creado recursos AWS desde este proyecto. Las restricciones usadas son las
compartidas por el estudiante; la cuenta puede imponer permisos adicionales.

## Recursos y alcance

- EC2 Amazon Linux 2023, `t3.micro`, 8 GB EBS gp3, con `LabInstanceProfile` existente.
- RDS MySQL, `db.t3.micro`, 20 GB gp2, Single-AZ y monitoreo mejorado desactivado.
- VPC propia, una subred pública para EC2 y dos privadas para el grupo de subredes RDS.
  Las dos subredes no significan que se creen dos bases de datos.
- RDS privado: solo EC2 puede acceder al puerto 3306. Conexión Java con verificación TLS.
- SSH permitido únicamente desde la IPv4 pública del estudiante (`/32`).
- La API escucha en localhost de EC2 y se usa mediante túnel SSH. No es un sitio público.
- Sin balanceador, NAT Gateway, Elastic IP, Auto Scaling ni creación de roles IAM.

CloudFormation crea los recursos. El script instala el JAR y un servicio systemd que lo
inicia cuando arranca EC2. Todavía no hay despliegues automáticos desde GitHub.
Para esta demostración se usa el usuario administrador de la base y `ddl-auto=update`;
un despliegue de producción requeriría usuario SQL de permisos mínimos y migraciones.

## 1. Crear la infraestructura desde la consola

1. Iniciar Learner Lab y abrir AWS en **Norte de Virginia / us-east-1**.
2. En el buscador de AWS abrir **CloudFormation**.
3. Elegir **Crear pila > Con recursos nuevos (estándar)**.
4. Elegir una plantilla existente, **Cargar un archivo de plantilla**, y seleccionar
   `infra/learner-lab.yaml` de este repositorio.
5. Pulsar Siguiente y poner nombre `anonymus-lab`.
6. Completar los parámetros:
   - `StudentCidr`: IPv4 pública de tu conexión seguida de `/32`; no la IP privada del PC.
     Puedes consultarla en tu terminal con `curl -4 https://checkip.amazonaws.com`.
   - `DatabasePassword`: generar en un gestor de contraseñas una clave aleatoria de
     20 a 64 letras y números. Guardarla: se usa otra vez al instalar. No enviarla al chat.
   - `KeyName`: `vockey`.
   - `InstanceProfile`: `LabInstanceProfile`.
   - `LatestAmi`: dejar el valor predeterminado.
7. Mantener las opciones restantes predeterminadas. No crear un rol IAM nuevo.
8. Revisar y enviar para crear. **Este paso comienza a consumir presupuesto.**
9. Esperar `CREATE_COMPLETE`; RDS puede tardar varios minutos. Si aparece un error,
   revisar **Eventos** y compartir el motivo sin credenciales antes de volver a intentar.
10. Guardar las salidas `ServerId`, `ServerPublicIp`, `DatabaseId`, `DatabaseEndpoint`.

`CREATE_COMPLETE` confirma los recursos, no que la aplicación esté instalada.
El script posterior espera a que termine la preparación inicial de EC2.

## 2. Compilar y subir el backend desde tu computador

Requisitos locales: Java 21, Maven, SSH y SCP. En la carpeta del backend:

```bash
mvn package
```

Descargar la clave PEM del laboratorio desde **AWS Details**, si está disponible.
Debe corresponder a `vockey`. Guardarla fuera del repositorio. Si el laboratorio no
ofrece descargarla, crear un par EC2 propio antes de crear la pila y seleccionarlo
en `KeyName`; guardar su clave privada localmente.

Los siguientes comandos son para Bash en Linux. Reemplazar las rutas y valores:

```bash
KEY_FILE="$HOME/Descargas/labsuser.pem"
SERVER_IP='REEMPLAZAR_CON_IP_PUBLICA_EC2'
chmod 600 "$KEY_FILE"
scp -i "$KEY_FILE" target/tecno-shop-backend-0.0.1-SNAPSHOT.jar \
  infra/install-backend.sh "ec2-user@$SERVER_IP:/home/ec2-user/"
ssh -i "$KEY_FILE" "ec2-user@$SERVER_IP"
```

En la terminal de EC2, reemplazar el endpoint por la salida de CloudFormation:

```bash
sudo bash /home/ec2-user/install-backend.sh \
  REEMPLAZAR_CON_ENDPOINT_RDS \
  /home/ec2-user/tecno-shop-backend-0.0.1-SNAPSHOT.jar
```

El script pide la clave MySQL sin mostrarla, genera un secreto JWT y guarda las
variables en `/etc/anonymus/backend.env`, legible solo por root. No coloca claves
en UserData, el repositorio o los argumentos del comando. Descarga los certificados
RDS oficiales para verificar TLS. Finalmente comprueba `GET /api/v1/categorias`.

## 3. Probar desde el computador

Salir de SSH con `exit`. Abrir el túnel en una terminal local y mantenerla abierta:

```bash
ssh -i "$KEY_FILE" -o ExitOnForwardFailure=yes -N \
  -L 127.0.0.1:8080:127.0.0.1:8080 "ec2-user@$SERVER_IP"
```

Detener antes cualquier backend local que use el puerto 8080. Abrir:

- Swagger: http://localhost:8080/swagger-ui/index.html
- Categorías: http://localhost:8080/api/v1/categorias

Una lista vacía `[]` es válida en una base nueva. Verificar luego con Swagger el
registro y login, y los CRUD según permisos. No se importan datos locales automáticamente.
Para demostrar persistencia, crear datos de prueba autorizados, reiniciar el servicio
y comprobar que siguen disponibles. Esa prueba requiere la infraestructura desplegada.

## 4. Detener, reanudar y controlar presupuesto

Los US$50 son un límite disponible, no una garantía de duración. Revisar en la consola
las estimaciones de EC2 y RDS y el presupuesto de Academy. Incluso detenidos, el
almacenamiento y las instantáneas pueden seguir generando cargos.

Al terminar cada jornada:

1. Detener RDS desde **RDS > Bases de datos > Acciones > Detener temporalmente**.
2. Detener EC2 desde **EC2 > Instancias > Estado de instancia > Detener instancia**.
3. Verificar ambos estados y después terminar la sesión del laboratorio.

Según las instrucciones del laboratorio, RDS puede seguir activo al terminar la sesión
y vuelve a arrancar tras siete días detenido. El laboratorio puede arrancar EC2 al abrir
una nueva sesión, incluso si lo habías detenido manualmente.

Al reanudar, iniciar RDS y EC2 si hace falta. Consultar la IP pública actual **en EC2**:
puede cambiar y la salida guardada de CloudFormation puede quedar desactualizada.
Actualizar `SERVER_IP`. Si cambia la IP de tu conexión, actualizar la pila cambiando
solo `StudentCidr` y conservando los demás parámetros, incluida la contraseña.
Reabrir el túnel. El servicio reintentará iniciar mientras RDS esté arrancando.

Para retirar definitivamente el entorno, respaldar los datos que se necesiten y eliminar
la pila. La plantilla conserva una instantánea final de RDS para evitar perderlos sin
respaldo; esa instantánea puede seguir cobrando. Revisar y eliminar manualmente las
instantáneas cuando ya no se necesiten, y revisar también el archivo de plantilla S3
que la consola de CloudFormation cargó. No borrar respaldos que quieras conservar.

## Problemas frecuentes

- **AccessDenied al crear**: revisar Eventos; las reglas del laboratorio pueden restringir
  un servicio adicional. No intentar crear usuarios o políticas para saltar esas reglas.
- **SSH no conecta**: revisar EC2 encendido, IP pública actual, `StudentCidr`, clave y usuario `ec2-user`.
- **Clave MySQL incorrecta**: volver a ejecutar el instalador con la clave correcta.
- **API no arranca**: `sudo journalctl -u anonymus -n 100 --no-pager`.
- **Preparación EC2 fallida**: `sudo tail -n 80 /var/log/cloud-init-output.log`.
- **La tienda sigue guardando datos en el navegador**: falta la integración del frontend;
  instalar el backend no modifica automáticamente sus servicios.

## Referencias

Validación local: `mvn package` completado con Java 21; el JAR incluye el perfil `aws`.
No hay pruebas automatizadas en el repositorio actual. Se comprobó la sintaxis Bash del
instalador y la plantilla con `cfn-lint` para us-east-1, sin errores. El validador emite
W1011 porque recomienda referencias a un gestor de secretos en lugar de un parámetro
de contraseña. Aquí se usa `NoEcho` para no depender de permisos adicionales de Secrets
Manager en el laboratorio. `NoEcho` no equivale a gestionar rotación de secretos.
El instalador y la conexión RDS aún no se han ejecutado en AWS.

- [Crear una pila CloudFormation](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cfn-console-create-stack.html)
- [Propiedades RDS en CloudFormation](https://docs.aws.amazon.com/AWSCloudFormation/latest/TemplateReference/aws-resource-rds-dbinstance.html)
- [Certificados y TLS para RDS](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/UsingWithRDS.SSL.html)
- Restricciones EC2 y RDS de Learner Lab compartidas por el estudiante en este chat.
