# Microservicio de Clasificación Financiera (Data Science) 🧠💰

Esta API REST, construida con **FastAPI** y **Scikit-Learn**, expone el modelo de Machine Learning entrenado por el equipo de Data Science para clasificar automáticamente transacciones bancarias.

## 🛠️ Requisitos Previos

Asegúrate de tener Python 3.10 o superior instalado.

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

## 📖 Documentación y Pruebas

FastAPI genera una interfaz gráfica interactiva (Swagger UI) para probar los endpoints.
Una vez levantado el servidor, ingresa en tu navegador a:
👉 http://127.0.0.1:8000/docs

---

## ⚡ Ejemplo de Uso (Para el equipo Backend)

### Endpoint Principal
POST /clasificar

### Estructura del JSON a enviar (Request)

    {
      "descripcion_clean": "farmacity medicamentos",
      "valor": 15000,
      "pais": "AR",
      "tipo_texto": "string",
      "dia_semana": 4,
      "mes": 3,
      "dia": 22
    }

### Respuesta esperada del modelo (Response 200 OK)

    {
      "categoria_predicha": "salud",
      "probabilidad": 0.7304,
      "estado": "exito"
    }

### Prueba rápida por Terminal (cURL)
Si quieres probar que el endpoint funciona desde tu terminal sin usar el navegador, ejecuta:

    curl -X 'POST' \
      'http://127.0.0.1:8000/clasificar' \
      -H 'accept: application/json' \
      -H 'Content-Type: application/json' \
      -d '{
      "descripcion_clean": "farmacity medicamentos",
      "valor": 15000,
      "pais": "AR",
      "tipo_texto": "string",
      "dia_semana": 4,
      "mes": 3,
      "dia": 22
    }'