#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script: generar_dataset_completo_final.py

SCRIPT ÚNICO que genera el dataset final de entrenamiento, partiendo de:
  1. Dataset_argentina_pre.csv (comercios reales AR)
  2. Dataset_chile.csv (comercios reales CL)
  3. Diccionario de ítems genéricos (café, naranjas, pescado, etc.)

Pipeline completo:
  [1] Cargar comercios reales AR + CL
  [2] Generar usuarios sintéticos + transacciones (con comercios reales)
  [3] Split train/test por usuario (SIN leakage)
  [4] Augmentation + balanceo (SOLO train)
  [5] Agregar ítems genéricos (SOLO train)
  [6] Calcular perfil financiero + recomendaciones
  [7] Guardar train_final.csv, test_final.csv, dataset_completo.csv

Output: /mnt/user-data/outputs/dataset_final/
  ├── train_final.csv       (comercios + items genéricos, augmentado, balanceado)
  ├── test_final.csv        (solo comercios reales, limpio, sin leakage)
  └── dataset_completo.csv  (unión, para EDA)
"""

import os
import json
import random
from datetime import datetime, timedelta
import numpy as np
import pandas as pd
from sklearn.model_selection import GroupShuffleSplit
from sklearn.utils import resample

random.seed(42)
np.random.seed(42)

# ============================================================
# CONFIG
# ============================================================
CSV_AR = '/mnt/user-data/uploads/Dataset_argentina_pre.csv'
CSV_CL = '/mnt/user-data/uploads/Dataset_chile.csv'
OUTPUT_DIR = '/mnt/user-data/outputs/dataset_final'
os.makedirs(OUTPUT_DIR, exist_ok=True)

USERS_AR = 120
USERS_CL = 120
TX_PER_USER = 100
TEST_SIZE = 0.25
BALANCE_TARGET = 450
N_ITEMS_GENERICOS_POR_PALABRA = 8  # repeticiones con variantes de verbo/formato

print("🚀 GENERADOR DE DATASET COMPLETO (Comercios + Items Genéricos)")
print("=" * 80)

# ============================================================
# DICCIONARIO DE ÍTEMS GENÉRICOS
# ============================================================
items_genericos = {
    'alimentacion': [
        'café', 'naranjas', 'manzanas', 'plátanos', 'bananas', 'paltas', 'aguacate',
        'tomates', 'papas', 'cebollas', 'zanahorias', 'lechuga', 'espinaca',
        'limones', 'pepino', 'pimientos', 'ajo', 'apio', 'brócoli', 'zapallo',
        'pescado', 'salmón', 'atún', 'camarones', 'mariscos', 'pollo', 'carne',
        'carne molida', 'bistec', 'costillas', 'cerdo', 'jamón', 'chorizo',
        'longaniza', 'tocino', 'vacuno', 'pavo',
        'leche', 'queso', 'yogurt', 'mantequilla', 'huevos', 'crema',
        'pan', 'arroz', 'fideos', 'pasta', 'harina', 'azúcar', 'sal',
        'aceite', 'avena', 'cereal', 'legumbres', 'lentejas', 'porotos',
        'garbanzos', 'quinoa',
        'agua', 'jugo', 'té', 'gaseosa', 'vino', 'cerveza', 'yerba mate',
        'galletas', 'chocolate', 'dulces', 'snacks', 'conservas', 'mermelada',
        'miel', 'salsa', 'condimentos', 'especias', 'verduras', 'frutas',
        'víveres', 'abarrotes', 'despensa', 'mercadería',
    ],
    'salud': [
        'paracetamol', 'ibuprofeno', 'aspirina', 'antibiótico', 'vitaminas',
        'jarabe para la tos', 'pastillas', 'medicamento', 'remedio', 'analgésico',
        'antialérgico', 'protector solar', 'alcohol gel', 'gasas', 'curitas',
        'termómetro', 'mascarillas', 'suplemento', 'complejo b', 'omega 3',
        'colágeno', 'crema hidratante', 'shampoo medicado', 'pañales',
        'toallas higiénicas', 'preservativos', 'test embarazo', 'insulina',
        'jeringas', 'algodón', 'agua oxigenada', 'suero fisiológico',
    ],
    'ocio': [
        'entradas cine', 'entradas concierto', 'boletos teatro', 'palomitas',
        'juego de mesa', 'libro', 'revista', 'videojuego', 'juguete',
        'equipo deportivo', 'pelota', 'bicicleta', 'pesas', 'zapatillas deportivas',
        'traje de baño', 'cañas de pescar', 'carpa', 'mochila de camping',
    ],
    'vivienda': [
        'detergente', 'lavaloza', 'cloro', 'desinfectante', 'papel higiénico',
        'toallas de papel', 'bolsas de basura', 'ambientador', 'esponjas',
        'foco', 'ampolleta', 'pilas', 'baterías', 'cinta adhesiva', 'clavos',
        'tornillos', 'pintura', 'brocha', 'martillo', 'destornillador',
        'sábanas', 'toallas', 'cortina', 'almohada', 'velas',
    ],
    'educacion': [
        'cuaderno', 'lápices', 'lapicera', 'goma', 'sacapuntas', 'regla',
        'mochila escolar', 'útiles escolares', 'calculadora', 'diccionario',
        'libro de texto', 'fotocopias', 'impresiones', 'carpeta', 'block de dibujo',
        'témperas', 'plasticina', 'tijeras', 'pegamento',
    ],
    'transporte': [
        'bencina', 'nafta', 'gasolina', 'aceite de motor', 'neumático',
        'batería auto', 'peaje', 'estacionamiento', 'lavado de auto',
        'repuesto auto', 'casco moto',
    ],
    'otros': [
        'ropa', 'zapatos', 'calcetines', 'ropa interior', 'polera', 'pantalón',
        'chaqueta', 'accesorios', 'cartera', 'billetera', 'perfume', 'maquillaje',
        'corte de pelo', 'tinte de pelo', 'manicure', 'regalo', 'flores',
        'alimento para mascota', 'arena para gato', 'juguete para perro',
    ],
}

VERBOS_ITEMS = ['Compra de', 'Compré', 'Compra', 'Pago de', 'Gasto en', 'Adquisición de', '', '']

# ============================================================
# FUNCIONES UTILIDAD
# ============================================================

def normalizar_categoria(cat):
    cat = str(cat).lower().strip()
    mapping = {
        'alimentación': 'alimentacion', 'transporte': 'transporte', 'salud': 'salud',
        'vivienda': 'vivienda', 'educación': 'educacion', 'ocio': 'ocio',
        'streaming': 'streaming', 'servicios': 'servicios', 'otras categorías': 'otros',
    }
    return mapping.get(cat, 'otros')

def introducir_typo_simple(texto):
    palabras = texto.split()
    if not palabras or len(palabras) < 2:
        return texto
    i = random.randint(0, len(palabras) - 1)
    p = palabras[i]
    if len(p) < 2:
        return texto
    j = random.randint(0, len(p) - 2)
    p = p[:j] + p[j+1] + p[j] + p[j+2:]
    palabras[i] = p
    return ' '.join(palabras)

def aplicar_ruido_formato(texto):
    var = texto
    if random.random() < 0.35:
        var = var.upper()
    if random.random() < 0.15:
        var = var.replace(' ', '')
    if random.random() < 0.25:
        ciudades = ['BSAS', 'CABA', 'CORDOBA', 'ROSARIO', 'STGO', 'VALPO', 'CONCEPCION']
        var = var + ' ' + random.choice(ciudades)
    if random.random() < 0.2:
        var = var + ' ' + str(random.randint(1000, 9999))
    if random.random() < 0.08:
        var = introducir_typo_simple(var)
    return var

def augment_text(desc, n_max=2):
    variants = [desc]
    if random.random() < 0.50 and len(variants) < n_max + 1:
        variants.append(desc.upper())
    if random.random() < 0.20 and len(variants) < n_max + 1:
        variants.append(desc.replace(' ', ''))
    if random.random() < 0.2 and len(variants) < n_max + 1:
        variants.append(desc + ' ' + str(random.randint(100, 999)))
    if random.random() < 0.15 and len(variants) < n_max + 1:
        words = desc.split()
        if len(words) > 1:
            random.shuffle(words)
            variants.append(' '.join(words))
    return list(dict.fromkeys(variants))[:n_max + 1]

def calcular_perfil_financiero(ingreso, gasto_total, nivel_endeudamiento, freq_ahorro):
    ratio_gasto = gasto_total / ingreso if ingreso > 0 else 0
    score = 0
    if ratio_gasto > 0.9:
        score += 50
    elif ratio_gasto > 0.75:
        score += 35
    elif ratio_gasto > 0.6:
        score += 20
    else:
        score += 5
    if nivel_endeudamiento > 60:
        score += 30
    elif nivel_endeudamiento > 40:
        score += 20
    elif nivel_endeudamiento > 25:
        score += 10
    if freq_ahorro == 'Alta':
        score -= 20
    elif freq_ahorro == 'Media':
        score -= 10
    if score > 60:
        perfil, prob = 'en_riesgo', min(1.0, (score - 60) / 30)
    elif score > 35:
        perfil, prob = 'en_observacion', min(1.0, (score - 35) / 25)
    else:
        perfil, prob = 'saludable', min(1.0, 1.0 - (score / 35))
    return perfil, round(prob, 3)

def generar_recomendaciones(gasto_por_categoria, ratio_gasto, ingreso,
                             nivel_endeudamiento, freq_ahorro):
    recos = []
    if gasto_por_categoria.get('streaming', 0) > ingreso * 0.05:
        recos.append("Revisar suscripciones activas de streaming")
    if gasto_por_categoria.get('ocio', 0) > ingreso * 0.05:
        recos.append("Reducir gastos en entretenimiento y actividades de ocio")
    if gasto_por_categoria.get('transporte', 0) > ingreso * 0.15:
        recos.append("Optimizar gastos de transporte y combustible")
    if gasto_por_categoria.get('alimentacion', 0) > ingreso * 0.25:
        recos.append("Reducir gastos en alimentación (considerar compras mayoristas)")
    if freq_ahorro == 'Baja':
        recos.append("Establecer un plan de ahorro mensual")
    if nivel_endeudamiento > 50:
        recos.append("Priorizar reducción de deudas")
    if ratio_gasto > 0.80:
        recos.append("Reducir gastos generales para mejorar flujo de caja")
    if gasto_por_categoria.get('vivienda', 0) < ingreso * 0.15:
        recos.append("Verificar si todos los gastos de vivienda están registrados")
    if gasto_por_categoria.get('educacion', 0) > ingreso * 0.10:
        recos.append("La inversión en educación es alta, revisar ROI")
    if not recos:
        recos.append("Mantener el buen manejo financiero actual")
    return recos[:3]

# ============================================================
# [1/7] CARGAR COMERCIOS REALES
# ============================================================
print("\n[1/7] Cargando comercios reales AR + CL...")
ar = pd.read_csv(CSV_AR, encoding='latin-1')
cl = pd.read_csv(CSV_CL, encoding='latin-1')
print(f"  ✓ Argentina: {len(ar)} comercios")
print(f"  ✓ Chile: {len(cl)} comercios")

def estructurar_por_categoria(df, moneda):
    comercios = {}
    for _, row in df.iterrows():
        cat = normalizar_categoria(row.get('categoria', ''))
        nombre = str(row.get('comercio', '')).strip()
        if cat not in comercios:
            comercios[cat] = []
        min_col = f"rango_min_{moneda.lower()}"
        max_col = f"rango_max_{moneda.lower()}"
        rango_min = row.get(min_col, 1)
        rango_max = row.get(max_col, int(rango_min) + 100)
        comercios[cat].append({
            'nombre': nombre,
            'patron': row.get('patron_transaccional', '').strip() if pd.notna(row.get('patron_transaccional', '')) else '',
            'min': int(rango_min) if pd.notna(rango_min) else 1,
            'max': int(rango_max) if pd.notna(rango_max) else int(rango_min) + 100
        })
    return comercios

comercios_ar = estructurar_por_categoria(ar, 'ARS')
comercios_cl = estructurar_por_categoria(cl, 'CLP')

# ============================================================
# [2/7] CREAR USUARIOS
# ============================================================
print("\n[2/7] Creando usuarios sintéticos...")
usuarios = []
uid = 1
for _ in range(USERS_AR):
    usuarios.append({
        'user_id': uid, 'pais': 'AR', 'moneda': 'ARS',
        'ingreso_mensual': random.randint(80000, 300000),
        'nivel_endeudamiento': random.randint(5, 80),
        'frecuencia_ahorro': random.choice(['Baja', 'Media', 'Alta'])
    })
    uid += 1
for _ in range(USERS_CL):
    usuarios.append({
        'user_id': uid, 'pais': 'CL', 'moneda': 'CLP',
        'ingreso_mensual': random.randint(500000, 2000000),
        'nivel_endeudamiento': random.randint(5, 80),
        'frecuencia_ahorro': random.choice(['Baja', 'Media', 'Alta'])
    })
    uid += 1
print(f"  ✓ {len(usuarios)} usuarios ({USERS_AR} AR + {USERS_CL} CL)")

# ============================================================
# [3/7] GENERAR TRANSACCIONES (con comercios reales)
# ============================================================
print("\n[3/7] Generando transacciones con comercios reales...")

pesos = {
    'alimentacion': 0.18, 'transporte': 0.13, 'salud': 0.10, 'vivienda': 0.14,
    'educacion': 0.08, 'ocio': 0.08, 'streaming': 0.11, 'servicios': 0.15, 'otros': 0.03,
}

verbos_comercio = {
    'alimentacion': ['Compra en', 'Pago en', 'Gasto en', 'Almuerzo en', 'Mercado', 'Supermercado'],
    'transporte': ['Carga combustible', 'Viaje en', 'Pasaje', 'Nafta', 'Taxi', 'Uber', 'Peaje'],
    'salud': ['Compra en', 'Consulta en', 'Farmacia', 'Médico', 'Laboratorio', 'Dentista'],
    'vivienda': ['Pago', 'Cuota hipoteca', 'Alquiler', 'Expensas', 'Seguro hogar', 'Mantenimiento'],
    'educacion': ['Inscripción en', 'Cuota', 'Matrícula', 'Clases en', 'Curso de', 'Arancel'],
    'ocio': ['Entrada a', 'Ticket', 'Cine', 'Teatro', 'Gym', 'Hobby', 'Deporte'],
    'streaming': ['Suscripción a', 'Pago de', 'Renovación', 'Plan mensual', 'Cargo automático'],
    'servicios': ['Pago', 'Factura', 'Servicio', 'Luz', 'Agua', 'Gas', 'Internet'],
    'otros': ['Pago', 'Compra de', 'Gasto de', 'Transacción'],
}

filas = []
fecha_base = datetime(2026, 1, 1)

for u in usuarios:
    comercios = comercios_ar if u['pais'] == 'AR' else comercios_cl
    gasto_por_cat = {}
    user_transacciones = []

    for _ in range(TX_PER_USER):
        cat = random.choices(list(pesos.keys()), weights=list(pesos.values()), k=1)[0]
        if cat not in comercios or not comercios[cat]:
            cat = 'otros'
        comercio = random.choice(comercios[cat])

        vmin = comercio['min']
        vmax = min(comercio['max'], int(u['ingreso_mensual'] * 0.30))
        if vmax < vmin:
            vmax = vmin + 1

        if random.random() < 0.12:
            all_min = min([c['min'] for lst in comercios.values() for c in lst])
            all_max = max([c['max'] for lst in comercios.values() for c in lst])
            valor = random.randint(all_min, min(all_max, int(u['ingreso_mensual'] * 0.30)))
        else:
            valor = random.randint(vmin, vmax)

        gasto_por_cat[cat] = gasto_por_cat.get(cat, 0) + valor

        if comercio['patron'] and random.random() < 0.5:
            descripcion_base = comercio['patron'].lower()
            tipo_texto = 'cartola'
        else:
            verbo = random.choice(verbos_comercio.get(cat, ['Pago']))
            descripcion_base = f"{verbo} {comercio['nombre']}".lower()
            tipo_texto = 'natural'

        descripcion = aplicar_ruido_formato(descripcion_base)
        fecha = fecha_base + timedelta(days=random.randint(1, 365))

        fila = {
            'user_id': u['user_id'], 'pais': u['pais'], 'moneda': u['moneda'],
            'ingreso_mensual': u['ingreso_mensual'],
            'nivel_endeudamiento': u['nivel_endeudamiento'],
            'frecuencia_ahorro': u['frecuencia_ahorro'],
            'descripcion': descripcion, 'valor': valor, 'categoria': cat,
            'fecha': fecha.strftime('%Y-%m-%d'), 'tipo_texto': tipo_texto,
            'comercio': comercio['nombre'],
        }
        filas.append(fila)
        user_transacciones.append(fila)

    gasto_total = sum(gasto_por_cat.values())
    perfil, prob = calcular_perfil_financiero(
        u['ingreso_mensual'], gasto_total, u['nivel_endeudamiento'], u['frecuencia_ahorro']
    )
    recos = generar_recomendaciones(
        gasto_por_cat, gasto_total / u['ingreso_mensual'],
        u['ingreso_mensual'], u['nivel_endeudamiento'], u['frecuencia_ahorro']
    )
    for fila in user_transacciones:
        fila['perfil_financiero'] = perfil
        fila['probabilidad_perfil'] = prob
        fila['recomendaciones'] = json.dumps(recos, ensure_ascii=False)
        fila['gasto_total_mes'] = gasto_total
        fila['ratio_gasto_ingreso'] = round(gasto_total / u['ingreso_mensual'], 3)

df_base = pd.DataFrame(filas)
print(f"  ✓ {len(df_base):,} transacciones de comercios reales generadas")

# ============================================================
# [4/7] SPLIT POR USUARIO (SIN LEAKAGE)
# ============================================================
print("\n[4/7] Split train/test por usuario...")

gss = GroupShuffleSplit(test_size=TEST_SIZE, n_splits=1, random_state=42)
train_idx, test_idx = next(gss.split(df_base, groups=df_base['user_id'].values))
train_df = df_base.loc[train_idx].reset_index(drop=True)
test_df = df_base.loc[test_idx].reset_index(drop=True)

train_desc = set(train_df['descripcion'].astype(str).unique())
before = len(test_df)
test_df = test_df[~test_df['descripcion'].astype(str).isin(train_desc)].reset_index(drop=True)
purged = before - len(test_df)

overlap_users = set(train_df['user_id']) & set(test_df['user_id'])
print(f"  ✓ Train: {len(train_df):,} filas | Test: {len(test_df):,} filas")
print(f"  ✓ Overlap usuarios: {len(overlap_users)} (debe ser 0)")
print(f"  ✓ Descripciones purgadas de test: {purged}")

# ============================================================
# [5/7] AUGMENTATION + BALANCEO (SOLO TRAIN, comercios reales)
# ============================================================
print("\n[5/7] Augmentation y balanceo de comercios reales (solo train)...")

augmented = []
for _, row in train_df.iterrows():
    augmented.append(row.to_dict())
    variants = augment_text(row['descripcion'], n_max=1)
    for v in variants[1:]:
        new = row.to_dict()
        new['descripcion'] = v
        augmented.append(new)

train_aug = pd.DataFrame(augmented).reset_index(drop=True)

target = max(train_aug['categoria'].value_counts().max(), BALANCE_TARGET)
dfs_bal = []
for cat, g in train_aug.groupby('categoria'):
    if len(g) < target:
        up = resample(g, replace=True, n_samples=target, random_state=42)
        dfs_bal.append(up)
    else:
        dfs_bal.append(g.sample(n=target, random_state=42))

train_bal = pd.concat(dfs_bal).sample(frac=1, random_state=42).reset_index(drop=True)
print(f"  ✓ Train comercios: {len(train_df):,} → augmentado {len(train_aug):,} → balanceado {len(train_bal):,}")

# ============================================================
# [6/7] AGREGAR ÍTEMS GENÉRICOS (SOLO TRAIN)
# ============================================================
print("\n[6/7] Agregando ítems genéricos (café, naranjas, pescado, etc.)...")

usuario_ref = train_bal.iloc[0].to_dict()
nuevas_filas_items = []

for categoria, items in items_genericos.items():
    for item in items:
        for i in range(N_ITEMS_GENERICOS_POR_PALABRA):
            verbo = VERBOS_ITEMS[i % len(VERBOS_ITEMS)]
            desc = f"{verbo} {item}".lower() if verbo else item.lower()
            if random.random() < 0.3:
                desc = desc.upper()
            fila = usuario_ref.copy()
            fila['descripcion'] = desc
            fila['categoria'] = categoria
            fila['comercio'] = 'ITEM_GENERICO'
            fila['tipo_texto'] = 'item_generico'
            fila['valor'] = random.randint(500, 15000)
            nuevas_filas_items.append(fila)

df_items = pd.DataFrame(nuevas_filas_items)
train_final = pd.concat([train_bal, df_items], ignore_index=True)
train_final = train_final.sample(frac=1, random_state=42).reset_index(drop=True)

print(f"  ✓ Ítems genéricos agregados: {len(df_items):,}")
print(f"  ✓ Train final: {len(train_bal):,} → {len(train_final):,} filas")

# ============================================================
# [7/7] VALIDACIONES Y GUARDADO
# ============================================================
print("\n[7/7] Validaciones finales y guardado...")

overlap_final_desc = set(train_final['descripcion']) & set(test_df['descripcion'])
overlap_final_users = set(train_final['user_id']) & set(test_df['user_id'])
print(f"  ✓ Overlap textual final train/test: {len(overlap_final_desc)}")
print(f"  ✓ Overlap usuarios final train/test: {len(overlap_final_users)} (debe ser 0)")

print("\n  📊 Distribución TRAIN FINAL (comercios + items genéricos):")
for cat in sorted(train_final['categoria'].unique()):
    count = (train_final['categoria'] == cat).sum()
    pct = 100 * count / len(train_final)
    print(f"     {cat:20s}: {count:5d} ({pct:5.1f}%)")

print("\n  📊 Distribución TEST (solo comercios reales, sin cambios):")
for cat in sorted(test_df['categoria'].unique()):
    count = (test_df['categoria'] == cat).sum()
    pct = 100 * count / len(test_df)
    print(f"     {cat:20s}: {count:5d} ({pct:5.1f}%)")

train_path = os.path.join(OUTPUT_DIR, 'train_final.csv')
test_path = os.path.join(OUTPUT_DIR, 'test_final.csv')
combined_path = os.path.join(OUTPUT_DIR, 'dataset_completo.csv')

train_final.to_csv(train_path, index=False, encoding='utf-8-sig')
test_df.to_csv(test_path, index=False, encoding='utf-8-sig')
df_final = pd.concat([train_final, test_df], ignore_index=True)
df_final.to_csv(combined_path, index=False, encoding='utf-8-sig')

print(f"\n✅ DATASETS GUARDADOS EN: {OUTPUT_DIR}")
print(f"   ├─ train_final.csv ({len(train_final):,} filas — comercios reales + items genéricos)")
print(f"   ├─ test_final.csv ({len(test_df):,} filas — solo comercios reales)")
print(f"   └─ dataset_completo.csv ({len(df_final):,} filas)")

print("\n" + "=" * 80)
print("🎯 LISTO PARA ENTRENAR MODELOS")
print("=" * 80)
