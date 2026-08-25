# Microservicio de Clasificación Financiera (Data Science) 🧠💰

Esta API REST, construida con FastAPI y Scikit-Learn, expone el modelo de Machine Learning entrenado por el equipo de Data Science para clasificar automáticamente transacciones bancarias, integrándose perfectamente con la base de datos del backend.

## 🛠️ Requisitos Previos

Asegúrate de tener Python 3.10 o o Docker instalado en tu equipo.

## 🚀 Instalación y Ejecución

**Para usuarios de Linux / Ubuntu / macOS:**

    cd api_financiera
    python3 -m venv apifinanc
    source apifinanc/bin/activate
    pip install -r requirements.txt
    uvicorn main:app --reload

**Para usuarios de Windows (CMD o PowerShell):**

    cd api_financiera
    python -m venv apifinanc
    apifinanc\Scripts\activate
    pip install -r requirements.txt
    uvicorn main:app --reload

---

## 🐳 Ejecución mediante Docker

Para levantar el servicio de forma totalmente aislada sin conflictos de dependencias del sistema operativo:

1. **Construir la imagen de Docker:**
   ```bash
   docker build -t api-financiera .
   ```
2. **Ejecutar el contenedor:**
   ```bash
   docker run -d -p 8000:8000 --name contenedor-finanzas api-financiera
   ```

---

## 📖 Documentación y Pruebas

FastAPI genera una interfaz gráfica interactiva (Swagger UI) para probar los endpoints.
Una vez levantado el servidor (local o en Docker), ingresa en tu navegador a:
👉 http://127.0.0.1:8000/docs

---

## ⚡ Ejemplo de Uso (Para el equipo Backend)

### Endpoint Principal
POST /predecir

### Estructura del JSON a enviar (Request)

El backend envía los datos crudos directamente desde la tabla de transacciones (sin necesidad de calcular fechas ni limpiar textos por separado):

    {
      "id_transaccion": 1045,
      "descripcion": "transferencia mercado pago netflix",
      "valor": 6900.0,
      "fecha": "2026-08-04"
    }

### Respuesta esperada del modelo (Response 200 OK)

La API limpia el texto, procesa la fecha de forma interna, ejecuta la predicción del modelo y devuelve el resultado listo para hacer el INSERT en la tabla de predicciones:

    {
      "id_transaccion": 1045,
      "categoria_predicha": "streaming",
      "confianza": 0.9620
    }

### Prueba rápida por Terminal (cURL)
Si quieres probar que el endpoint funciona desde tu terminal sin usar el navegador, ejecuta:

    curl -X 'POST' \
      'http://localhost:8000/predecir' \
      -H 'accept: application/json' \
      -H 'Content-Type: application/json' \
      -d '{
      "id_transaccion": 1045,
      "descripcion": "transferencia mercado pago netflix",
      "valor": 6900.0,
      "fecha": "2026-08-04"
    }'

## ☁️ Despliegue en Oracle Cloud (OCI)

El microservicio está desplegado en una instancia de cómputo de Oracle Cloud (Santiago, Chile) con las siguientes características:

- **Shape:** VM.Standard.E2.1.Micro (Siempre Gratis, AMD)
- **Sistema Operativo:** Ubuntu 24.04 LTS
- **Puerto expuesto:** 8000
- **Contenedor Docker** con reinicio automático (`--restart always`)

### Resumen de pasos realizados

1. **Creación de la instancia**  
   - Se creó una VM con Ubuntu 24.04 y una clave SSH para acceso seguro.

2. **Configuración de red y seguridad**  
   - Se asignó una IP pública efímera.
   - Se abrió el puerto 8000 en el firewall del sistema operativo (`iptables`) y en las listas de seguridad de Oracle (Ingress Rule para TCP/8000).

3. **Instalación de Docker**  
   ```bash
   sudo apt update
   sudo apt install docker.io -y
   sudo systemctl enable --now docker
   sudo usermod -aG docker $USER```

4. **Transferencia de archivos**

    Desde la máquina local se copiaron los archivos esenciales (Dockerfile, main.py, modelo_finanzas_v4.pkl, requirements.txt) mediante scp:

    ```bash
    scp -i ~/Descargas/archivokey main.py modelo_finanzas_v4.pkl maquina@146.181.44.7:/ ```

5. **Construcción y ejecución del contenedor**

   ```bash 
    docker build -t api-financiera .
    docker run -d -p 8000:8000 --restart always --name contenedor-finanzas api-financiera ```

6. **Verificación**

    El contenedor se ejecuta correctamente y la API responde en la IP pública:

    **👉 http://146.181.44.7:8000/docs**


## 👥 Autores

 **Equipo G9-LATAM-Team-53**