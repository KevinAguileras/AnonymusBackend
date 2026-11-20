# Anonymus Backend

Spring Boot con Java 21, MySQL, API REST `/api/v1`, Swagger, JWT y roles
ADMINISTRADOR, VENDEDOR y CLIENTE. El frontend consulta la API; la base no se expone
al navegador.

## Ejecutar

Para MySQL local configurar `DB_URL`, `DB_USER`, `DB_PASSWORD` y `JWT_SECRET`.
El puerto predeterminado es 8080.

```bash
mvn spring-boot:run
```

Para AWS usar [la guía de infraestructura](infra/README.md). Con IntelliJ se utiliza
el perfil `aws`, puerto 8081 y el túnel RDS por 13306. No subir secretos ni archivos PEM.

Swagger: `http://localhost:8081/swagger-ui/index.html` en esa configuración.

## API

- `/auth/registro`, `/auth/login`: registro y emisión de JWT.
- `/cuenta`: GET de identidad/rol actual y PUT del nombre del usuario autenticado.
- `/productos`, `/categorias`: consulta pública y escritura solo de administrador.
- `/boletas`: compra de cliente/administrador, consulta propia del cliente o general
  para administrador/vendedor.
- `/admin/usuarios`: consulta y modificación de rol solo de administrador.

La primera cuenta administrativa debe promoverla el responsable de la base; el
registro público siempre crea clientes. El frontend documenta los pasos de prueba.
La compra es una simulación académica, sin integración de pagos.

## Pruebas y cobertura

```bash
mvn test
mvn package
```

También se ejecutan desde IntelliJ: recargar Maven y usar Run sobre `src/test/java`
o sobre una clase de pruebas. Maven `test` genera `target/site/jacoco/index.html`.

JUnit y Mockito prueban registro, hash de contraseña, duplicados, login, JWT y compra
(total calculado con precio del servidor, descuento de stock, productos inexistentes,
stock insuficiente y usuario no autorizado). Las pruebas MockMvc cargan la seguridad
real con repositorios simulados para verificar 401, 403, escritura de administrador,
identidad de cuenta y validación del registro. No requieren AWS ni MySQL activo.

Estas pruebas no demuestran por sí solas persistencia, rollback real de la transacción
ni seguridad ante compras concurrentes: son comprobaciones adicionales de integración.
La prueba manual con AWS consiste en crear, consultar, reiniciar y volver a consultar.
No hay renovación automática ni revocación de JWT implementada.

[Pruebas Spring Boot](https://docs.spring.io/spring-boot/3.5/reference/testing/spring-boot-applications.html).
