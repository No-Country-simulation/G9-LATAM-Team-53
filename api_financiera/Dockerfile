# Usamos una imagen oficial de Python ligera
FROM python:3.12-slim

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos primero los requirements para aprovechar el caché de Docker
COPY requirements.txt .

# Instalamos las dependencias
RUN pip install --no-cache-dir -r requirements.txt

# Descargamos los recursos necesarios de NLTK dentro de la imagen del contenedor
RUN python -m nltk.downloader stopwords

# Copiamos el resto de los archivos del proyecto (el main.py y el .pkl)
COPY . .

# Exponemos el puerto que usa FastAPI
EXPOSE 8000

# Comando para arrancar la API cuando el contenedor inicie
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]