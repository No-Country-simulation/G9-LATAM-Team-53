from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import pandas as pd
import numpy as np

# 1. Inicializar la aplicación FastAPI
app = FastAPI(
    title="Microservicio de Clasificación Financiera",
    description="API en Python para clasificar transacciones usando Regresión Logística",
    version="1.0"
)

# 2. Cargar el modelo entrenado al iniciar el servidor
try:
    modelo = joblib.load('modelo_finanzas_v1.pkl')
except Exception as e:
    modelo = None
    print(f"Error al cargar el modelo: {e}")

# 3. Definir la estructura del JSON que nos enviará Java (Spring Boot)
class TransaccionEntrada(BaseModel):
    descripcion_clean: str
    valor: float
    pais: str
    tipo_texto: str
    dia_semana: int
    mes: int
    dia: int

# 4. Lista de categorías para traducir el número que devuelve el modelo a texto
CATEGORIAS = [
    'alimentacion', 'educacion', 'ocio', 'otros', 'salud', 
    'servicios', 'streaming', 'transporte', 'vivienda'
]

# 5. Crear el Endpoint (La URL que escuchará las peticiones POST)
@app.post("/clasificar")
def clasificar_transaccion(datos: TransaccionEntrada):
    if not modelo:
        raise HTTPException(status_code=500, detail="El modelo no está disponible.")
    
    try:
        # Convertir el JSON recibido a un DataFrame de Pandas (como lo espera el modelo)
        df_entrada = pd.DataFrame([datos.dict()])
        
        # Obtener la predicción (devuelve un número del 0 al 8)
        prediccion_num = modelo.predict(df_entrada)[0]
        
        # Obtener las probabilidades y extraer la más alta
        probabilidades = modelo.predict_proba(df_entrada)[0]
        probabilidad_max = round(float(np.max(probabilidades)), 4)
        
        # Traducir el número a la etiqueta de texto
        categoria_final = CATEGORIAS[prediccion_num]
        
        # Devolver el JSON final de respuesta para el Backend
        return {
            "categoria_predicha": categoria_final,
            "probabilidad": probabilidad_max,
            "estado": "exito"
        }
        
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Error en el procesamiento: {str(e)}")