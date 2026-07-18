# Montero S.A.C. — Manual de Usuario

Plataforma web para la reserva de pasajes interprovinciales. Los pasajeros pueden buscar viajes, seleccionar asientos, pagar en línea y recibir un código QR para abordar. El administrador gestiona toda la operación desde un panel central.

---

## Índice

1. [Registro e Inicio de Sesión](#1-registro-e-inicio-de-sesión)
2. [Buscar Viajes](#2-buscar-viajes)
3. [Reservar Pasaje](#3-reservar-pasaje)
4. [Métodos de Pago](#4-métodos-de-pago)
5. [Confirmación y Código QR](#5-confirmación-y-código-qr)
6. [Historial de Viajes](#6-historial-de-viajes)
7. [Perfil de Usuario](#7-perfil-de-usuario)
8. [Panel de Administración](#8-panel-de-administración)
9. [Notificaciones](#9-notificaciones)

---

## 1. Registro e Inicio de Sesión

### Crear una cuenta

1. Ve a `/registro`
2. Completa: nombre completo, correo electrónico, teléfono (9 dígitos) y contraseña (mín. 8 caracteres)
3. Presiona **Registrarse**
4. Recibirás un mensaje de confirmación y serás redirigido al login

### Iniciar sesión

1. Ingresa tu correo electrónico y contraseña
2. Presiona **INICIAR SESIÓN**
3. Si eres usuario normal, serás redirigido a la página de viajes
4. Si eres administrador, serás redirigido al panel de administración

### Credenciales del Administrador (predefinidas)

| Campo | Valor |
|---|---|
| Correo | `admin@montero.com` |
| Contraseña | `Admin123*` |

---

## 2. Buscar Viajes

1. En la página principal, selecciona:
   - **Origen** (ej: Piura)
   - **Destino** (ej: Paita)
   - **Fecha de ida**
   - **Fecha de vuelta** (opcional, para viaje redondo)
2. Presiona **Buscar**
3. Se mostrarán los viajes disponibles con:
   - Horario de salida y llegada
   - Precio por asiento
   - Asientos disponibles
4. Presiona **Seleccionar** en el viaje deseado

---

## 3. Reservar Pasaje

### Paso 1: Seleccionar asientos

- Se muestra un mapa de asientos del bus
- Los asientos ocupados aparecen en gris/rojo
- Los asientos disponibles en blanco/verde
- Selecciona uno o varios asientos tocándolos
- Presiona **Continuar**

### Paso 2: Datos del pasajero

- Completa: nombres, apellidos, DNI (8 dígitos), correo electrónico y teléfono
- Si compras múltiples asientos, puedes registrar un pasajero por asiento
- Presiona **Continuar**

### Paso 3: Resumen de reserva

- Revisa: origen, destino, fecha, hora, asientos seleccionados, precio total
- Si todo está correcto, presiona **Confirmar Reserva**
- El sistema crea la reserva en estado `PENDIENTE` y te redirige al pago

---

## 4. Métodos de Pago

### Yape

1. Ingresa tu **número de teléfono** (9 dígitos)
2. Ingresa el **código de aprobación** de 6 dígitos generado por la app Yape
3. Presiona **Pagar con Yape**

### Tarjeta de crédito/débito

1. Ingresa: número de tarjeta, fecha de expiración, CVV y nombre del titular
2. Presiona **Pagar con Tarjeta**

### Pago en efectivo

1. Ingresa tu **correo electrónico** para recibir el comprobante
2. Presiona **Pagar en Efectivo**
3. Se genera un código CIP para pagar en agente o banco

### Pago con QR

1. Presiona **Pagar con QR**
2. Se procesa automáticamente la reserva

---

## 5. Confirmación y Código QR

Después del pago exitoso:

1. Se muestra la pantalla de **confirmación** con:
   - Origen y destino del viaje
   - Fecha y hora de salida
   - Número de asientos
   - Precio total pagado
   - Método de pago usado
2. Se genera un **código QR** único con formato `MNT-{id}`
3. Puedes **descargar o compartir** el código QR
4. El QR puede ser escaneado por el personal de la empresa para verificar el ticket

**Nota:** La verificación pública del ticket está disponible en `/ticket/verificar/MNT-{id}` sin necesidad de iniciar sesión.

---

## 6. Historial de Viajes

1. Ve a `/reservas/historial`
2. Se muestran todas tus reservas ordenadas por fecha (más recientes primero)
3. Puedes:
   - **Filtrar** por estado (todos, pendientes, pagados, cancelados)
   - **Buscar** por destino
   - **Ordenar** por fecha ascendente/descendente
4. Cada tarjeta muestra:
   - Origen y destino
   - Fecha y hora de salida
   - Número de asientos
   - Estado (PENDIENTE, PAGADO, CANCELADO)
   - Precio total
5. Las reservas `PENDIENTE` tienen un botón para **ir al pago**
6. Las reservas `PENDIENTE` pueden **cancelarse**
7. Las reservas `PAGADO` tienen un botón para **repetir el viaje**

---

## 7. Perfil de Usuario

1. Ve a `/perfil`
2. Puedes ver:
   - Tu foto de perfil
   - Nombre, DNI, teléfono
   - Estadísticas: viajes totales, destino favorito
3. Presiona **Editar perfil** para actualizar:
   - Nombre, DNI, teléfono
   - Preferencia de asiento (ventana, pasillo, etc.)
   - Preferencia de servicio
   - Foto de perfil
4. En `/ajustes` puedes:
   - Cambiar contraseña
   - Ver sesiones activas por dispositivo
   - Cerrar sesión remota

---

## 8. Panel de Administración

Accede con credenciales de administrador. El menú lateral contiene:

### Dashboard (`/admin/dashboard`)

Resumen general del sistema con tarjetas de:
- Viajes programados hoy
- Usuarios registrados
- Reservas activas
- Pagos registrados

Accesos directos a todas las secciones de gestión.

### Viajes y Horarios (`/admin/viajes`)

- Lista paginada de todos los viajes (ordenados del más reciente al más antiguo)
- Cada viaje muestra: origen, destino, fecha, hora, precio, asientos totales, asientos vendidos
- Botón **Editar** para modificar un viaje existente
- Botón **Nuevo Viaje** para crear uno nuevo
- Al crear/editar: origen, destino, fecha, hora, precio, asientos totales

### Usuarios (`/admin/usuarios`)

- Lista paginada de usuarios registrados (más recientes primero)
- Muestra: ID, nombre, email, teléfono, DNI, fecha de registro, activo
- Botón **Editar** para modificar datos del usuario
- Botón **Nuevo Usuario** para crear manualmente

### Reservas (`/admin/reservas`)

- Lista paginada de todas las reservas
- Muestra: ID, pasajero, viaje (origen → destino), asientos, estado, fecha de creación, total
- Botón **Editar** para modificar estado
- Botón **Nueva Reserva** para crear manualmente

### Pagos (`/admin/pagos`)

- Resumen: **Recaudado hoy** (suma de pagos del día actual) y **Pendientes** (reservas sin pagar)
- Lista paginada de todos los pagos
- Muestra: ID, reserva, método de pago, fecha, monto

### Notificaciones (`/admin/notificaciones`)

- Lista de notificaciones del sistema agrupadas por fecha (Hoy, Ayer, fecha)
- Tipos de notificación:
  - **Nueva venta** — cuando un usuario paga una reserva
  - **Reserva cancelada** — cuando se cancela una reserva
  - **Nuevo usuario registrado** — cuando alguien crea una cuenta
  - **Disponibilidad crítica** — cuando un viaje tiene 3 o menos asientos disponibles
- Botón **Marcar todo como leído**
- Eliminación individual con el ícono ✕

---

## 9. Notificaciones

- Las notificaciones aparecen en el icono de campana (esquina superior derecha)
- El badge rojo muestra la cantidad de notificaciones no leídas
- El badge se actualiza automáticamente cada 30 segundos
- Haz clic en la campana para ir a la página de notificaciones
- Desde allí puedes:
  - Ver las notificaciones agrupadas por fecha
  - Marcar todo como leído
  - Eliminar notificaciones individuales deslizando (en móvil) o presionando ✕

### Notificaciones para usuarios

| Notificación | ¿Cuándo ocurre? |
|---|---|
| Reserva confirmada | Después de pagar exitosamente |
| Recordatorio de viaje | Un día antes del viaje (8:00 AM) |
| Cambio en tu viaje | Cuando el administrador modifica el viaje |

### Notificaciones para administradores

| Notificación | ¿Cuándo ocurre? |
|---|---|
| Nueva venta | Cuando un usuario completa un pago |
| Reserva cancelada | Cuando se cancela una reserva |
| Nuevo usuario registrado | Cuando alguien crea una cuenta |
| Disponibilidad crítica | Cuando un viaje tiene ≤3 asientos libres |
