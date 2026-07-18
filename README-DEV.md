# Montero S.A.C. — Documentación para Desarrolladores

Sistema web de gestión de reservas de pasajes interprovinciales. Permite a los usuarios buscar viajes, seleccionar asientos, pagar en línea y recibir un código QR de verificación. Incluye un panel de administración para gestionar viajes, usuarios, reservas y pagos.

---

## Stack Tecnológico

| Tecnología | Versión | Propósito |
|---|---|---|
| Java | 17+ | Lenguaje base |
| Spring Boot | 3.x | Framework web |
| Spring Data JPA | — | Persistencia |
| Spring Security | — | Autenticación y autorización |
| MySQL | 8.x | Base de datos |
| Thymeleaf | — | Motor de templates server-side |
| Tailwind CSS | 3.x | Estilos (CDN) |
| ZXing | — | Generación de códigos QR |
| Maven | — | Build y dependencias |

---

## Estructura del Proyecto

```
src/main/java/com/montero/app/
├── MonteroApplication.java          # Entry point (@SpringBootApplication + @EnableScheduling)
├── config/
│   ├── SecurityConfig.java          # Seguridad: roles, login, logout, rutas públicas
│   ├── PasswordEncoderConfig.java   # BCrypt (strength 12)
│   └── DataInitializer.java         # Siembra admin inicial (CommandLineRunner)
├── controller/
│   ├── AdminController.java         # /admin/* — panel de administración
│   ├── AuthController.java          # Login, registro
│   ├── HomeController.java          # Página de inicio
│   ├── ViajeController.java         # /viajes/* — búsqueda de viajes
│   ├── ReservaController.java       # /reservas/* — flujo completo de reserva
│   ├── PagoController.java          # /pagos/* — procesamiento de pagos
│   ├── NotificacionController.java  # /notificaciones/* — listar, eliminar, badge
│   ├── PerfilUsuarioController.java # /perfil/* — perfil del usuario
│   ├── AjustesController.java       # /ajustes/* — contraseña, preferencias, sesiones
│   ├── TicketverificacionController.java  # /ticket/verificar/* — público
│   └── NavegacionModelAdvice.java   # @ControllerAdvice — badge notificaciones global
├── service/
│   ├── ReservaService.java          # Lógica de reservas (creación, cancelación, historial)
│   ├── ViajeService.java            # Búsqueda de viajes, disponibilidad
│   ├── PagoService.java             # Procesamiento de pagos (Yape, tarjeta, efectivo, QR)
│   ├── UsuarioService.java          # Registro, autenticación
│   ├── NotificacionService.java     # Notificaciones in-app, recordatorios programados
│   ├── PerfilUsuarioService.java    # Estadísticas de viaje del usuario
│   ├── SesionService.java           # Gestión de sesiones activas
│   └── CustomUserDetailsService.java # UserDetailsService para Spring Security
├── repository/
│   ├── ReservaRepository.java       # JOIN FETCH para evitar LazyInitializationException
│   ├── ViajeRepository.java
│   ├── PagoRepository.java
│   ├── UsuarioRepository.java
│   ├── NotificacionRepository.java
│   └── SesionRepository.java
├── model/
│   ├── Reserva.java                 # @ManyToOne → Viaje, Usuario (LAZY)
│   ├── Viaje.java                   # Origen, destino, fecha, precio, asientos
│   ├── Pago.java                    # @OneToOne → Reserva (LAZY), MetodoPago enum
│   ├── Usuario.java                 # Email, password, rol (USER/ADMIN)
│   ├── Notificacion.java            # destinatarioRol (USER/ADMIN), TipoNotificacion enum
│   └── SesionUsuario.java           # Sesiones activas por dispositivo
├── dto/
│   ├── ReservaRequestDTO.java       # Datos del formulario de reserva
│   ├── ReservaSessionDTO.java       # Progreso del wizard en sesión HTTP
│   ├── PagoYapeDTO.java             # Validación de pago Yape
│   ├── LoginRequestDTO.java         # Login form
│   ├── RegistroRequestDTO.java      # Registro form
│   ├── PerfilUsuarioDTO.java        # Datos de perfil
│   ├── PasajeroRequestDTO.java      # Datos del pasajero
│   ├── CambioPasswordDTO.java       # Cambio de contraseña
│   └── ProgresoReserva.java         # Enum: pasos del wizard
├── util/
│   └── QrCodeGenerator.java         # Genera QR en Base64 con ZXing
└── exception/
    └── GlobalExceptionHandler.java  # @ControllerAdvice para errores
```

```
src/main/resources/
├── templates/
│   ├── auth/                        # login, registro
│   ├── inicio/                      # Página de inicio, servicios
│   ├── reserva/                     # wizard: búsqueda, asientos, pasajero, resumen, confirmación, historial
│   ├── metodos_pago/                # yape, tarjeta, efectivo, qr
│   ├── perfil_usuario/              # perfil, editar
│   ├── notificaciones/              # notificaciones_dinamica.html
│   ├── ajustes_aplicacion/          # contraseña, preferencias, sesiones, seguridad
│   ├── admin/                       # dashboard, viajes, usuarios, reservas, pagos, notificaciones, etc.
│   ├── fragments/                   # navegacion.html (navbar + badge)
│   └── error/                       # error_generico.html
├── static/
│   ├── js/notificaciones.js         # Polling de notificaciones cada 30s
│   └── images/                      # montero_logo.png, etc.
└── application.properties
```

---

## Modelo de Datos

### Entidades y Relaciones

```
Reserva ──@ManyToOne──→ Viaje        (viaje, FetchType.LAZY)
Reserva ──@ManyToOne──→ Viaje        (viajeRetorno, FetchType.LAZY)
Reserva ──@ManyToOne──→ Usuario      (usuario, FetchType.LAZY)
Pago    ──@OneToOne───→ Reserva      (reserva, FetchType.LAZY)
Notificacion ─@ManyToOne──→ Usuario  (usuario, LAZY, nullable)
Notificacion ─@ManyToOne──→ Reserva  (reserva, LAZY)
```

### Enums Principales

| Enum | Archivo | Valores |
|---|---|---|
| `EstadoReserva` | `model/EstadoReserva.java` | `PENDIENTE`, `PAGADO`, `CANCELADO` |
| `MetodoPago` | `model/MetodoPago.java` | `YAPE`, `TARJETA`, `PAGO_EFECTIVO`, `PAGO_QR`, `TRANSFERENCIA` |
| `TipoNotificacion` | `model/Notificacion.java` | `CONFIRMACION_RESERVA`, `RECORDATORIO_VIAJE`, `CAMBIO_VIAJE`, `PROMOCION`, `INFORMACION`, `NUEVA_VENTA`, `CANCELACION`, `NUEVO_USUARIO`, `ALERTA` |

### `spring.jpa.open-in-view=false`

El proyecto deshabilita Open Session in View. Esto significa que **no se puede acceder a asociaciones lazy** (ej: `reserva.viaje.origen`) desde las templates Thymeleaf sin una sesión de Hibernate abierta.

**Cómo se maneja:**
- Los repositorios usan `JOIN FETCH` en consultas personalizadas (`ReservaRepository.findByIdWithViaje()`, `ReservaRepository.findAllWithViaje()`, `PagoRepository.findAllWithReserva()`)
- Los servicios usan `@Transactional(readOnly = true)` cuando es necesario
- Las asociaciones se inicializan dentro del ámbito transaccional

---

## Flujo de Reserva (Wizard Multi-paso)

El proceso de reserva se maneja a través de la sesión HTTP usando `ReservaSessionDTO` para preservar el progreso entre pasos:

1. **Búsqueda** → `ViajeController` busca viajes por origen/destino/fecha
2. **Asientos** → `ReservaController` muestra mapa de asientos con disponibilidad
3. **Pasajero** → Formulario con datos del pasajero (nombre, DNI, email)
4. **Resumen** → Vista previa antes de confirmar
5. **Confirmar** → `ReservaController.confirmarReserva()` crea la reserva en BD con estado `PENDIENTE` y redirige al pago
6. **Pago** → El usuario selecciona método de pago
7. **Confirmación** → Tras pagar, se muestra resumen con código QR

---

## Flujo de Pago

Todos los métodos de pago convergen en `PagoService.finalizarPago()`:

```java
private Pago finalizarPago(Reserva reserva, MetodoPago metodo, Pago pago) {
    pago.setMetodoPago(metodo);
    pago.setFechaPago(LocalDateTime.now());
    Pago pagoGuardado = pagoRepository.save(pago);
    reserva.setEstado(EstadoReserva.PAGADO);
    reservaRepository.save(reserva);
    notificacionService.crearNotificacionConfirmacionReserva(reserva);
    notificacionService.crearNotificacionNuevaVenta(reserva, metodo);
    return pagoGuardado;
}
```

**Métodos soportados:**
| Ruta | Método | DTO |
|---|---|---|
| `/pagos/yape/procesar/{reservaId}` | YAPE | `PagoYapeDTO` (teléfono + código 6 dígitos) |
| `/pagos/tarjeta/procesar/{reservaId}` | TARJETA | Parámetros individuales |
| `/pagos/pagoefectivo/procesar/{reservaId}` | PAGO_EFECTIVO | Correo electrónico |
| `/pagos/qr/procesar/{reservaId}` | PAGO_QR | Sin datos adicionales |

---

## Sistema de Notificaciones

### Arquitectura

- **Almacenamiento:** Tabla `notificaciones` en MySQL
- **Destinatarios:** Usuarios específicos (`usuario_id`) o por rol (`destinatario_rol = "ADMIN"`)
- **Polling:** `notificaciones.js` consulta `/notificaciones/api/no-leidas` cada 30s y actualiza el badge
- **Badge:** Se inyecta via `NavegacionModelAdvice` como atributo global `notificacionesNoLeidas`

### Tipos de Notificación

| Tipo | Disparador | Destinatario |
|---|---|---|
| `CONFIRMACION_RESERVA` | Pago exitoso | Usuario |
| `RECORDATORIO_VIAJE` | `@Scheduled` 8:00 AM (viaje al día siguiente) | Usuario |
| `CAMBIO_VIAJE` | Cambio en el viaje reservado | Usuario |
| `NUEVA_VENTA` | Pago exitoso | Admin |
| `CANCELACION` | Reserva cancelada | Admin |
| `NUEVO_USUARIO` | Nuevo registro | Admin |
| `ALERTA` | `@Scheduled` cada 2h (disponibilidad crítica ≤3 asientos) | Admin |

### Tareas Programadas

```java
@Scheduled(cron = "0 0 8 * * *")    // 8:00 AM — Recordatorio de viaje próximo
@Scheduled(cron = "0 0 */2 * * *")  // Cada 2h — Alerta de poca disponibilidad
```

---

## Seguridad

`SecurityConfig.java` define:

| Ruta | Acceso |
|---|---|
| `/login`, `/registro`, `/css/**`, `/js/**`, `/images/**`, `/ticket/**`, `/` | Público |
| `/admin/**` | `ROLE_ADMIN` |
| `/viajes/**`, `/reservas/**`, `/perfil/**` | `ROLE_USER` |
| Cualquier otra | Autenticado |

El login unificado redirige según el rol:
- **Admin** → `/admin/dashboard`
- **User** → `/viajes`

El admin también puede cerrar sesión via `GET /admin/logout` (configurado con `AntPathRequestMatcher`).

---

## Configuración y Despliegue

### Base de Datos

Configuración via variables de entorno (con defaults):

```properties
DB_HOST=localhost          # Puerto 3306
DB_NAME=montero_db
DB_USERNAME=root
DB_PASSWORD=soldepiura
SPRING_JPA_HIBERNATE_DDL_AUTO=update   # Hibernate crea/actualiza tablas
```

### Usuario Admin Inicial

Creado automáticamente por `DataInitializer` si no existe:
- Email: `admin@montero.com`
- Password: `Admin123*`

### Compilar y Ejecutar

```bash
mvn clean install
mvn spring-boot:run
# o
java -jar target/montero-app-*.jar
```
