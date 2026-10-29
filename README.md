# TecnoShop — Backend (etapa 1)

Base de datos y capa de persistencia para la evaluación parcial 3 de DSY1104.

## Requisitos
- Java 21
- Maven 3.9+
- MySQL local iniciado

## Configuración
Crear una base de datos MySQL llamada `tecnoshop` o permitir que la URL JDBC la cree con un usuario autorizado.
Definir `DB_URL`, `DB_USER`, `DB_PASSWORD` mediante variables de entorno (sin subir contraseñas a GitHub).

## Ejecutar
```bash
mvn spring-boot:run
```

## Componentes incluidos
- 5 modelos JPA: Usuario, Categoria, Producto, Boleta y DetalleBoleta.
- 5 interfaces `JpaRepository`.
- Configuración inicial Spring Boot y MySQL.
- Enum `Rol` para ADMINISTRADOR, VENDEDOR y CLIENTE.

## Importante
Esta es SOLO la etapa inicial de persistencia. Todavía no incluye JWT, endpoints CRUD, Swagger, controladores ni integración React.
Spring Security está declarado como dependencia, pero aún no está configurado para autenticación por JWT. No considerar el proyecto terminado ni apto para producción.

### Próximos pasos
1. Conexión a MySQL y prueba de persistencia.
2. Servicios, DTO, controladores REST, manejo de errores y Swagger.
3. Spring Security, autenticación por JWT y roles.
4. Integración frontend y documentos de entrega.
