# FinanceAI — Documentación técnica

> El **backend** es el núcleo de FinanceAI: aplica las reglas de negocio, protege la información, clasifica gastos y genera el análisis financiero. El frontend React consume esos resultados y los presenta al usuario.

## 1. Backend: arquitectura y responsabilidades

El backend está construido con Spring Boot, Spring Security, JPA y MySQL. Gestiona usuarios, categorías y transacciones; se conecta a un clasificador financiero externo y calcula el estado mensual de cada usuario.

Responsabilidades principales:

- Autenticar usuarios mediante JWT y revocar la sesión al cerrar sesión.
- Aislar los datos por usuario autenticado.
- Clasificar automáticamente cada nueva transacción.
- Calcular gastos, porcentajes, saldo disponible, perfil financiero y recomendaciones.
- Permitir la corrección manual de la categoría de una transacción, conservando la validación de propiedad.

### Seguridad

Solo `POST /auth/register` y `POST /auth/login` son públicos. El resto requiere:

```http
Authorization: Bearer <JWT>
```

El filtro de seguridad valida formato, vigencia y revocación del token, y obtiene el email del usuario autenticado. Todas las operaciones financieras usan ese email, por lo que un usuario no puede consultar ni modificar transacciones de otra persona. Un JWT inválido, vencido o revocado devuelve `401 Unauthorized`.

Las contraseñas se almacenan con BCrypt. En el logout el token se registra como inválido en el backend y el frontend elimina su copia local.

### Flujo de negocio

```text
Ingreso mensual + transacciones del usuario
                  │
Nueva transacción → clasificador externo → categoría
                  │
GET /analisis-financiero
                  │
Saldo disponible + gastos por categoría + perfil + recomendaciones
```

## 2. Endpoints y comportamiento

### Autenticación

| Método | Endpoint | Comportamiento |
| --- | --- | --- |
| `POST` | `/auth/register` | Valida datos, evita emails repetidos, cifra la contraseña, crea el usuario y retorna JWT (`201`). |
| `POST` | `/auth/login` | Comprueba email y contraseña BCrypt, genera un JWT nuevo (`200`) o responde `401`. |
| `POST` | `/auth/logout` | Revoca el token actual y responde sin contenido (`204`). |

Registro:

```json
{ "nombre": "Ana", "apellido": "García", "email": "ana@correo.com", "contrasena": "secreta1" }
```

La respuesta de registro y login contiene `token`, `userId`, `nombre`, `apellido` y `email`.

### Ingreso mensual y análisis

| Método | Endpoint | Comportamiento |
| --- | --- | --- |
| `GET` | `/analisis-financiero/ingreso-mensual` | Indica si el ingreso mensual está registrado. |
| `POST` | `/analisis-financiero/ingreso-mensual` | Registra el primer ingreso (`201`). |
| `PATCH` | `/analisis-financiero/ingreso-mensual` | Actualiza un ingreso ya existente (`200`). |
| `GET` | `/analisis-financiero` | Devuelve el panorama financiero mensual del usuario. |

Los endpoints de ingreso reciben:

```json
{ "ingresoMensual": 850000 }
```

El monto debe ser positivo. Sin ingreso válido, el análisis responde `400 Bad Request` porque no puede calcular porcentajes ni saldo.

Al consultar el análisis, el backend toma las transacciones activas desde el inicio hasta el fin del mes actual, en la zona `America/Argentina/Buenos_Aires`, y devuelve:

```json
{
  "nombreYApellido": "Ana García",
  "mesYFecha": "08/2026",
  "gastosPorCategoria": { "Alimentacion": 64000 },
  "porcentajePorCategoria": { "Alimentacion": 7.53 },
  "montoRestante": 786000,
  "perfilFinanciero": "SALUDABLE",
  "recomendaciones": ["..."]
}
```

El saldo se calcula como `ingreso mensual - total gastado`. Se generan alertas si una categoría supera su tope recomendado. El perfil es:

- `EN_RIESGO`: gasto total superior al 100% del ingreso o cuatro o más alertas.
- `EN_OBSERVACION`: gasto superior al 80% o dos o más alertas.
- `SALUDABLE`: los demás casos.

Los topes incluyen Vivienda (30%), Alimentación (12%), Transporte (10%), Servicios (8%) y Ocio (7%). Para una categoría no definida se aplica 5%.

### Transacciones

| Método | Endpoint | Comportamiento |
| --- | --- | --- |
| `POST` | `/transacciones` | Crea una transacción y la clasifica automáticamente (`201`). |
| `GET` | `/transacciones?pagina={n}&tamanio={n}` | Lista transacciones activas del usuario, ordenadas por fecha e ID descendentes. |
| `PATCH` | `/transacciones/{idTransaccion}/categoria` | Corrige la categoría de una transacción propia. |

Creación:

```json
{ "descripcion": "Compra en supermercado", "valor": 24500 }
```

La descripción es obligatoria y el valor debe ser positivo. El backend envía descripción, monto y fecha al clasificador. Luego busca la categoría predicha; si no existe, usa `otros`. Si el clasificador no está disponible, responde `503 Service Unavailable`.

El listado usa páginas desde `0`; `tamanio` debe estar entre `1` y `100`. La respuesta contiene `content`, `totalElements`, `totalPages`, `number`, `first` y `last`.

Corrección de categoría:

```json
{ "categoriaId": 4 }
```

El backend valida que la transacción esté activa y pertenezca al usuario autenticado, y que la categoría exista. Si alguna validación falla responde `400`. Tras una corrección, el frontend recarga historial y análisis porque pueden cambiar los porcentajes, perfil y recomendaciones.

### Categorías

| Método | Endpoint | Comportamiento |
| --- | --- | --- |
| `GET` | `/categorias` | Devuelve las categorías disponibles (`id`, `nombre`). |
| `POST` | `/categorias` | Crea una categoría no repetida; el nombre es obligatorio y tiene máximo 255 caracteres. |

## 3. Frontend: funciones y conexión

El frontend usa React + Vite. El cliente `src/services/api.js` centraliza las solicitudes, agrega el JWT, interpreta errores y conserva `financeai_token` y `financeai_user` en `localStorage`.

Funciones disponibles:

- Inicio, registro, login y cierre de sesión.
- Carga y actualización de ingreso mensual.
- Dashboard con dinero disponible, gastos, perfil y recomendaciones del backend.
- Registro de gastos con clasificación automática.
- Historial paginado.
- Modo **Ajustar**: muestra lápices para cambiar categorías; **Terminar ajuste** vuelve a la vista normal.

En desarrollo, el proxy de Vite redirige `/api/*` a `http://localhost:8080`:

```env
VITE_API_URL=/api
VITE_BACKEND_URL=http://localhost:8080
```

## 4. Ejecución y verificación

```bash
npm install
npm run dev
npm run lint
npm run build
```
