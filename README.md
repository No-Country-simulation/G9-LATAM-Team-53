# G9-LATAM-Team-53

# Clasificación de Transacciones Bancarias con NLP

## Descripción del Proyecto

Este proyecto implementa un modelo de Machine Learning que clasifica automáticamente descripciones de transacciones bancarias en **9 categorías** de gasto:  
`alimentacion`, `transporte`, `vivienda`, `salud`, `educacion`, `streaming`, `servicios`, `ocio` y `otros`.

El modelo está entrenado sobre un dataset de transacciones sintéticas que combina registros de Argentina y Chile, con descripciones en formato natural y de cartola bancaria.

---

## Contenido del Notebook (`Modelo_ML.ipynb`)

1. **Fase 1: Preparación y fusión de datasets**  
   - Carga de múltiples fuentes de datos (limpio, ruidoso, completo, con ítems y refuerzo).  
   - Reasignación de `user_id` para evitar solapamiento.  
   - Fusión en un único DataFrame de **114,690 registros**.

2. **Fase 2: Análisis Exploratorio (EDA)**  
   - Estadísticas descriptivas, valores nulos, duplicados.  
   - Distribución de categorías (balanceadas).  
   - Visualizaciones de montos, evolución temporal y correlaciones.

3. **Fase 3: Ingeniería de atributos**  
   - Limpieza de texto (minúsculas, eliminación de acentos, números y caracteres especiales).  
   - Eliminación de *stopwords* del español y de términos financieros irrelevantes (ruido financiero).  
   - Extracción de características de fecha (día de la semana, mes, día).

4. **Fase 4: Prevención de fugas de datos**  
   - Separación estricta por `user_id` (80% entrenamiento, 20% prueba) para evitar que el modelo "aprenda" patrones de un mismo usuario entre conjuntos.

5. **Fase 5: Pipeline y preprocesamiento**  
   - Uso de `ColumnTransformer` para tratar diferentes tipos de columnas:  
     - Texto: `CountVectorizer` con n-gramas (1,2).  
     - Numéricas: `StandardScaler`.  
     - Categóricas: `OneHotEncoder`.

6. **Fase 6: Entrenamiento y evaluación de 3 modelos**  
   - **Logistic Regression** (con `class_weight='balanced'`).  
   - **LinearSVC** (con `class_weight='balanced'`).  
   - **XGBoost** (con `sample_weight` balanceado).  
   - Comparación de accuracy, reporte de clasificación y matrices de confusión.

7. **Fase 7: Exportación del modelo final**  
   - Selección del mejor modelo (Logistic Regression).  
   - Guardado del pipeline completo en un archivo `.pkl` (`modelo_finanzas_v4.pkl`).  
   - Ejemplos de uso con nuevas transacciones.

---

## Tecnologías utilizadas

- **Python** 3.x  
- **Pandas**, **NumPy**  
- **Scikit-learn**: `CountVectorizer`, `StandardScaler`, `OneHotEncoder`, `LogisticRegression`, `LinearSVC`, `Pipeline`, `ColumnTransformer`.  
- **XGBoost**  
- **NLTK** (stopwords)  
- **Joblib** (serialización del modelo)  
- **Matplotlib** y **Seaborn** para visualizaciones

---

## Resultados de los modelos

| Modelo               | Accuracy (test) |
|----------------------|----------------|
| Logistic Regression  | **96.92 %**    |
| LinearSVC            | 97.02 %        |
| XGBoost              | 92.22 %        |

*El modelo final elegido para producción fue **Logistic Regression** por su equilibrio entre rendimiento y simplicidad, y por su mejor comportamiento ante datos ruidosos (no mostrado en el notebook).*

---

## Modelo final exportado

Como resultado del entrenamiento, se genera el archivo **`modelo_finanzas_v4.pkl`**, que contiene el pipeline completo de preprocesamiento y el clasificador entrenado. Este archivo está listo para ser utilizado en entornos de producción para realizar predicciones en tiempo real.

---

## Autor

**Equipo G9-LATAM-Team-53**


Proyecto desarrollado con fines educativos, utilizando datos sintéticos para simular un entorno real de clasificación de transacciones bancarias.

Para cualquier consulta o contribución, por favor contactar a través de los canales oficiales del equipo.