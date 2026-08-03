# 1. Usar una imagen oficial de Python ligera
FROM python:3.12-slim

# 2. Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# 3. Copiar solo el archivo de requerimientos primero (para optimizar caché)
COPY requirements.txt .

# 4. Instalar las dependencias de Python
RUN pip install --no-cache-dir -r requirements.txt

# 5. Copiar el resto del código y el modelo (.pkl) a la carpeta /app
COPY . .

# 6. Exponer el puerto que usará FastAPI
EXPOSE 8000

# 7. Comando para iniciar el servidor cuando el contenedor se encienda
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]