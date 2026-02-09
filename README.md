# Finance BFF Web

Backend-For-Frontend (BFF) para clientes web del sistema financiero DUOC. Este microservicio actua como capa intermedia entre el frontend web y el microservicio backend `finance-batch`, agregando y adaptando las respuestas para optimizar el consumo desde el navegador.

## Arquitectura

```
Cliente Web (Browser)
        |
        | HTTPS (puerto 8081)
        v
+-------------------+
|  finance-bff-web  |  <-- Este proyecto
|  (BFF - Puerto    |
|   8081 HTTPS)     |
+-------------------+
        |
        | HTTP (puerto 8080)
        v
+-------------------+
|  finance-batch    |
|  (Backend -       |
|   Puerto 8080)    |
+-------------------+
```

## Tecnologias

- **Java 21**
- **Spring Boot 4.0.2**
- **Spring Security** - Autenticacion y autorizacion
- **JWT (jjwt 0.11.5)** - Tokens de autenticacion con HS512
- **Lombok** - Reduccion de boilerplate en DTOs
- **java-dotenv** - Carga de variables de entorno desde archivo .env
- **Maven** - Gestion de dependencias y build

## Estructura del Proyecto

```
src/main/java/cl/duoc/finance_bff_web/
|
|-- FinanceBffWebApplication.java    # Clase principal, bean RestTemplate
|
|-- config/
|   |-- SecurityConfig.java          # Configuracion Spring Security, filtros, usuarios
|
|-- controller/
|   |-- AuthController.java          # Endpoint POST /auth/login (publico)
|   |-- FinanceWebController.java    # Endpoint GET /bff/web/v1/cuentas/{id} (protegido)
|
|-- model/
|   |-- CuentaDTO.java               # DTO de cuenta financiera
|   |-- EstadoFinancieroDTO.java     # DTO de transaccion/movimiento
|   |-- ResumenWebDTO.java           # DTO de respuesta combinada para el frontend
|
|-- security/
|   |-- JwtFilter.java               # Filtro que valida JWT en cada peticion
|   |-- JwtUtil.java                 # Utilidad para generar/validar tokens JWT
|
|-- service/
    |-- FinanceWebService.java       # Interfaz del servicio BFF
    |-- FinanceWebServiceImpl.java   # Implementacion: orquesta llamadas al backend
```

## Configuracion

### Archivo .env

Crear un archivo `.env` en la raiz del proyecto con la clave secreta para JWT:

```env
JWT_SECRET=<clave_en_base64>
```

La clave debe estar codificada en Base64 y tener al menos 64 bytes para el algoritmo HS512.

### application.properties

```properties
spring.application.name=finance-bff-web
server.port=8081
server.ssl.key-store=classpath:finance-keystore.p12
server.ssl.key-store-password=<password>
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=finance-local
server.ssl.enabled=true
```

El servidor se ejecuta en HTTPS (puerto 8081) usando un keystore PKCS12.

## Requisitos Previos

- **Java 21** instalado
- **Maven 3.9+** instalado
- **finance-batch** corriendo en `http://localhost:8080`
- Archivo `.env` con la variable `JWT_SECRET`
- Keystore `finance-keystore.p12` en `src/main/resources/`

## Ejecucion

```bash
# Compilar y ejecutar
mvn clean spring-boot:run

# Solo compilar
mvn clean package
```

La aplicacion iniciara en `https://localhost:8081`.

## Endpoints

### Autenticacion (Publico)

#### POST /auth/login

Autentica al usuario y retorna un token JWT.

**Request:**
```json
{
  "username": "usuario_web",
  "password": "1234"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Response (401):**
```
Error: Credenciales invalidas
```

### Consulta de Cuenta (Protegido - Requiere rol CLIENTE_WEB)

#### GET /bff/web/v1/cuentas/{id}

Obtiene el resumen financiero completo de una cuenta (datos + movimientos).

**Headers requeridos:**
```
Authorization: Bearer <token_jwt>
```

**Response (200):**
```json
{
  "mensaje": "Consulta Exitosa - Cliente Web (Datos Completos)",
  "fechaConsulta": "2026-02-09T17:00:00",
  "cuenta": {
    "id": 1,
    "cuentaId": 1001,
    "nombre": "Juan Perez",
    "saldo": 150000.0,
    "edad": 30,
    "tipo": "ahorro",
    "interesAplicado": 2500.0
  },
  "movimientos": [
    {
      "id": 1,
      "cuentaId": 1001,
      "fecha": "2026-02-01",
      "transaccion": "deposito",
      "monto": 50000.0,
      "descripcion": "Deposito mensual"
    }
  ]
}
```

## Ejemplo de Uso con curl

```bash
# 1. Obtener token JWT
curl -k -X POST https://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "usuario_web", "password": "1234"}'

# 2. Consultar cuenta con el token obtenido
curl -k -H "Authorization: Bearer <token>" \
  https://localhost:8081/bff/web/v1/cuentas/1
```

> Nota: La flag `-k` se usa para aceptar el certificado autofirmado en desarrollo.

## Seguridad

- **HTTPS** habilitado con certificado PKCS12 autofirmado
- **JWT con HS512** para autenticacion stateless
- **Spring Security** con filtro personalizado (JwtFilter)
- **CSRF deshabilitado** (no necesario con tokens JWT)
- **Sesiones stateless** (sin estado en el servidor)
- **Token relay** hacia el backend (propaga el JWT del cliente)
- Clave secreta externalizada en archivo `.env` (no hardcodeada)

## Usuarios de Prueba

| Username      | Password | Rol          | Acceso                   |
|---------------|----------|--------------|--------------------------|
| usuario_web   | 1234     | CLIENTE_WEB  | /bff/web/v1/**           |
