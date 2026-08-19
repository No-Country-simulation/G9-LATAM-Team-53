from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import pandas as pd
import numpy as np
import joblib
import re
import unicodedata
import nltk
from nltk.corpus import stopwords
from datetime import datetime

# ==========================================
# 1. CONFIGURACIÓN INICIAL Y CARGA DE MODELO
# ==========================================
app = FastAPI(
    title="API Clasificador Financiero - Equipo 53",
    description="Microservicio de IA que recibe transacciones y devuelve su categoría predicha."
)

# Cargamos el archivo .pkl
try:
    modelo = joblib.load('modelo_finanzas_v2.pkl')
except Exception as e:
    modelo = None
    print(f"Error al cargar el modelo: {e}")

# ==========================================
# 2. LÓGICA DE PREPROCESAMIENTO INTERNO
# ==========================================
nltk.download('stopwords', quiet=True)
stopwords_spanish = set(stopwords.words('spanish'))

ruido_financiero = {
    'transferencia', 'pago', 'compra', 'gasto', 'cajero', 'atm', 'pos', 'trx', 'suc', 'sucursal',
    'mercado', 'uala', 'naranja', 'modo', 'macro', 'brubank', 'dni', 'prex',
    'webpay', 'transbank', 'mach', 'tenpo', 'cuentarut', 'fpay', 'servipag', 'chek',
    'visa', 'mastercard', 'cabal', 'banelco', 'link', 'master'
}
stopwords_totales = stopwords_spanish.union(ruido_financiero)

def limpiar_texto(text):
    if not isinstance(text, str):
        return ""
    text = text.lower()
    text = unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode("utf-8")
    text = re.sub(r"\d+", "", text)
    texto_letras = re.sub(r"[^a-z\s]", "", text) 
    
    palabras = [word for word in texto_letras.split() if word not in stopwords_totales]
    texto_final = " ".join(palabras)
    
    # Salvavidas
    if texto_final.strip() == "" and texto_letras.strip() != "":
        return texto_letras.strip()
    return texto_final

# ==========================================
# 3. ESQUEMAS DE DATOS (JSON In / JSON Out)
# ==========================================
class TransaccionInput(BaseModel):
    id_transaccion: int | None = None
    descripcion: str
    valor: float
    fecha: str  # Formato esperado: "YYYY-MM-DD"

class PrediccionOutput(BaseModel):
    id_transaccion: int | None = None
    categoria_predicha: str
    confianza: float

# Diccionario para traducir la salida del modelo (0-8) a texto
CATEGORIAS = [
    'alimentacion', 'educacion', 'ocio', 'otros', 'salud', 
    'servicios', 'streaming', 'transporte', 'vivienda'
]

# ==========================================
# 4. ENDPOINT DE PREDICCIÓN
# ==========================================
@app.post("/predecir", response_model=PrediccionOutput)
def predecir_categoria(transaccion: TransaccionInput):
    if not modelo:
        raise HTTPException(status_code=500, detail="El modelo no está disponible.")
        
    try:
        # A. Procesar la Fecha (Desarmarla para el modelo)
        fecha_obj = datetime.strptime(transaccion.fecha, "%Y-%m-%d")
        dia = fecha_obj.day
        mes = fecha_obj.month
        dia_semana = fecha_obj.weekday()
        
        # B. Limpiar la Descripción
        desc_limpia = limpiar_texto(transaccion.descripcion)
        
        # C. Ensamblar el DataFrame con las columnas fijas requeridas por la Fase 4
        datos_modelo = pd.DataFrame([{
            'descripcion_clean': desc_limpia,
            'valor': transaccion.valor,
            'pais': 'AR',
            'tipo_texto': 'natural',
            'dia_semana': dia_semana,
            'mes': mes,
            'dia': dia
        }])
        
        # D. Ejecutar Predicción
        prediccion_num = modelo.predict(datos_modelo)[0]
        
        # E. Traducir el número a texto y extraer la confianza
        categoria_final = CATEGORIAS[prediccion_num]
        
        probabilidades = modelo.predict_proba(datos_modelo)[0]
        confianza = float(np.max(probabilidades))
        
        # F. Devolver respuesta exacta para el backend
        return PrediccionOutput(
            id_transaccion=transaccion.id_transaccion,
            categoria_predicha=categoria_final,
            confianza=round(confianza, 4)
        )
        
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))
