# 🏦 Clasificación de Transacciones Bancarias con NLP

Proyecto de Machine Learning que clasifica automáticamente descripciones de transacciones bancarias (ej: *"COMPRA SUPERMERCADO LIDER"*) en 9 categorías de gasto: alimentación, transporte, vivienda, salud, educación, streaming, servicios, ocio y otros.

## 📋 ¿Qué hace?

A partir del texto libre de una transacción (como aparece en una cartola bancaria), el modelo predice a qué categoría de gasto corresponde. Esto permite automatizar el etiquetado de movimientos sin intervención manual.

## 🔍 Proceso

1. **Auditoría del dataset**: revisión de calidad, nulos, duplicados y balance de clases.
2. **Análisis exploratorio (EDA)**: estudio del vocabulario, longitud de descripciones y patrones por categoría.
3. **Preprocesamiento**: limpieza de texto (minúsculas, sin acentos, sin números ni caracteres especiales).
4. **Vectorización**: comparación entre `CountVectorizer` y `TF-IDF`, con distintas configuraciones de n-gramas.
5. **Entrenamiento**: evaluación de 9 modelos (Naive Bayes, Regresión Logística, Árboles, Random Forest, SVM, SGD, Gradient Boosting) mediante validación cruzada.
6. **Optimización**: ajuste de hiperparámetros con `GridSearchCV`.
7. **Evaluación e interpretabilidad**: métricas finales, matriz de confusión y análisis de las palabras más influyentes por categoría.

## 📊 Resultados

Sobre el conjunto de prueba, la combinación **CountVectorizer + LinearSVC** obtuvo el mejor desempeño (accuracy y F1 cercanos al 95-99%), superando a TF-IDF y a los modelos basados en árboles.

## ⚠️ Nota importante

Aunque **LinearSVC** fue el modelo con mejores métricas en este notebook, **no fue el modelo finalmente elegido para producción**. Al someterlo a pruebas con datos más ruidosos y variados (descripciones distintas a las del dataset de entrenamiento), su desempeño se degradó de forma importante. Esto sugiere que el buen resultado obtenido aquí está influenciado por la baja variabilidad del dataset sintético utilizado, y no necesariamente refleja cómo se comportaría con datos bancarios reales.

Por este motivo, el modelo aquí presentado se conserva como **referencia y punto de comparación**, pero la solución final del proyecto usa un enfoque distinto, más robusto frente a ruido y variaciones del lenguaje.

## 🛠️ Tecnologías

- Python, Pandas, NumPy
- Scikit-Learn (CountVectorizer, TF-IDF, LinearSVC, entre otros)
- Matplotlib

## 📁 Estructura

- `dataset_10k_usuarios.csv`: dataset de transacciones sintéticas.
- Notebook con las 12 fases del proyecto, desde la auditoría de datos hasta la preparación para producción (incluyendo una función `predict()` de ejemplo).

---
*Proyecto desarrollado con fines educativos, usando datos sintéticos.*
