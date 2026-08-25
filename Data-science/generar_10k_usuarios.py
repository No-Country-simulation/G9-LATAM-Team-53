import pandas as pd
import random
from datetime import datetime, timedelta

random.seed(42)

ar = pd.read_csv('/mnt/user-data/uploads/Dataset_argentina_pre.csv', encoding='latin-1')
cl = pd.read_csv('/mnt/user-data/uploads/Dataset_chile.csv', encoding='latin-1')

def normalizar_categoria(cat):
    cat = cat.lower().strip()
    if cat == 'alimentación': return 'alimentacion'
    if cat == 'transporte': return 'transporte'
    if cat == 'salud': return 'salud'
    if cat == 'vivienda': return 'vivienda'
    if cat == 'educación': return 'educacion'
    if cat == 'ocio': return 'ocio'
    if cat == 'servicios': return 'servicios'
    return 'otros'

def estructurar_por_categoria_ar(df):
    comercios = {}
    streaming_names = ['Netflix', 'Spotify', 'Disney Plus', 'HBO Max', 'Amazon Prime Video', 
                       'Twitch', 'YouTube Premium', 'Tidal', 'Apple Music', 'Disney+', 'Max', 'Paramount+']
    
    for _, row in df.iterrows():
        cat = normalizar_categoria(row['categoria'])
        nombre = row['comercio'].strip()
        
        if cat == 'ocio' and any(stream in nombre for stream in streaming_names):
            cat = 'streaming'
        
        if cat not in comercios:
            comercios[cat] = []
        
        comercios[cat].append({
            'nombre': nombre,
            'patron': row['patron_transaccional'],
            'min': row['rango_min_ars'],
            'max': row['rango_max_ars'],
        })
    
    return comercios

def estructurar_por_categoria_cl(df):
    comercios = {}
    streaming_names = ['Netflix', 'Spotify', 'Disney Plus', 'HBO Max', 'Amazon Prime Video', 
                       'Twitch', 'YouTube Premium', 'Tidal', 'Apple Music', 'Disney+', 'Max', 'Paramount+']
    
    for _, row in df.iterrows():
        cat = normalizar_categoria(row['categoria'])
        nombre = row['comercio'].strip()
        
        if cat == 'ocio' and any(stream in nombre for stream in streaming_names):
            cat = 'streaming'
        
        if cat not in comercios:
            comercios[cat] = []
        
        comercios[cat].append({
            'nombre': nombre,
            'patron': row['patron_transaccional'],
            'min': row['rango_min_clp'],
            'max': row['rango_max_clp'],
        })
    
    return comercios

comercios_ar = estructurar_por_categoria_ar(ar)
comercios_cl = estructurar_por_categoria_cl(cl)

print("Comercios AR por categoría:", {k: len(v) for k, v in comercios_ar.items()})
print("Comercios CL por categoría:", {k: len(v) for k, v in comercios_cl.items()})

usuarios = []
ID_USER = 1

for i in range(40):
    ingreso = random.randint(80000, 300000)
    usuarios.append({
        'user_id': ID_USER,
        'pais': 'AR',
        'ingreso_mensual': ingreso,
        'nivel_endeudamiento': random.randint(5, 80),
        'frecuencia_ahorro': random.choice(['Baja', 'Media', 'Alta']),
        'moneda': 'ARS',
    })
    ID_USER += 1

for i in range(40):
    ingreso = random.randint(500000, 2000000)
    usuarios.append({
        'user_id': ID_USER,
        'pais': 'CL',
        'ingreso_mensual': ingreso,
        'nivel_endeudamiento': random.randint(5, 80),
        'frecuencia_ahorro': random.choice(['Baja', 'Media', 'Alta']),
        'moneda': 'CLP',
    })
    ID_USER += 1

print(f"\n{len(usuarios)} usuarios generados\n")

verbos = {
    'alimentacion': ['Compra en', 'Pago en', 'Gasto en', 'Almuerzo en'],
    'transporte': ['Carga bencina en', 'Viaje en', 'Pago en', 'Pasaje en'],
    'salud': ['Compra en', 'Pago en', 'Consulta en'],
    'vivienda': ['Pago', 'Compra en', 'Contratacion de'],
    'educacion': ['Pago', 'Inscripcion en', 'Cuota'],
    'ocio': ['Suscripcion a', 'Entrada a', 'Compra en'],
    'streaming': ['Suscripcion a', 'Pago'],
    'servicios': ['Pago', 'Factura de'],
    'otros': ['Pago', 'Compra de', 'Gasto de'],
}

filas = []
fecha_base = datetime(2026, 1, 1)

for usuario in usuarios:
    user_id = usuario['user_id']
    pais = usuario['pais']
    ingreso = usuario['ingreso_mensual']
    comercios = comercios_ar if pais == 'AR' else comercios_cl
    
    for _ in range(125):
        pesos = {'alimentacion': 0.20, 'transporte': 0.15, 'salud': 0.10, 'vivienda': 0.15,
                 'educacion': 0.05, 'ocio': 0.08, 'streaming': 0.07, 'servicios': 0.15, 'otros': 0.05}
        categoria = random.choices(list(pesos.keys()), weights=list(pesos.values()), k=1)[0]
        
        if categoria not in comercios or not comercios[categoria]:
            categoria = 'otros'
        
        comercio_info = random.choice(comercios[categoria])
        
        # Rango coherente: mínimo el rango del comercio, máximo 30% del ingreso
        valor_min = comercio_info['min']
        valor_max = min(comercio_info['max'], int(ingreso * 0.30))
        
        # Seguridad: si aún hay conflicto, usa el rango del comercio puro
        if valor_max < valor_min:
            valor_max = comercio_info['max']
        
        valor = random.randint(int(valor_min), int(valor_max))
        
        if random.random() < 0.5:
            tipo_texto = 'cartola'
            descripcion = comercio_info['patron'].lower()
        else:
            tipo_texto = 'natural'
            verbo = random.choice(verbos.get(categoria, ['Pago']))
            descripcion = f"{verbo} {comercio_info['nombre']}".lower()
        
        dias_offset = random.randint(1, 28)
        fecha = fecha_base + timedelta(days=dias_offset)
        
        filas.append({
            'user_id': user_id,
            'pais': pais,
            'moneda': usuario['moneda'],
            'ingreso_mensual': ingreso,
            'nivel_endeudamiento': usuario['nivel_endeudamiento'],
            'frecuencia_ahorro': usuario['frecuencia_ahorro'],
            'descripcion': descripcion,
            'valor': valor,
            'categoria': categoria,
            'fecha': fecha.strftime('%Y-%m-%d'),
            'tipo_texto': tipo_texto,
        })

df_final = pd.DataFrame(filas)

print(f"=== VALIDACIONES ===")
print(f"Total filas: {len(df_final)}")
print(f"Usuarios unicos: {df_final['user_id'].nunique()}")
print(f"Paises: {list(df_final['pais'].unique())}")
print(f"Categorias ({len(df_final['categoria'].unique())}): {sorted(df_final['categoria'].unique())}")
print(f"\nTipo texto:")
for tipo, count in df_final['tipo_texto'].value_counts().items():
    print(f"  {tipo}: {count} ({count/len(df_final)*100:.1f}%)")

print(f"\nDescripciones unicas: {df_final['descripcion'].nunique()}")

print(f"\n=== BALANCE POR CATEGORIA ===")
for cat, count in df_final['categoria'].value_counts().items():
    print(f"  {cat}: {count} ({count/len(df_final)*100:.1f}%)")

gasto_por_usuario = df_final.groupby('user_id')['valor'].sum().reset_index()
gasto_por_usuario['ingreso_mensual'] = gasto_por_usuario['user_id'].apply(
    lambda x: df_final[df_final['user_id']==x]['ingreso_mensual'].iloc[0]
)
gasto_por_usuario['ratio'] = gasto_por_usuario['valor'] / gasto_por_usuario['ingreso_mensual']

print(f"\n=== COHERENCIA (gasto total vs ingreso mensual) ===")
print(f"Min: {gasto_por_usuario['ratio'].min():.1%}")
print(f"Max: {gasto_por_usuario['ratio'].max():.1%}")
print(f"Promedio: {gasto_por_usuario['ratio'].mean():.1%}")

output_path = '/mnt/user-data/outputs/dataset_10k_usuarios.csv'
df_final.to_csv(output_path, index=False, encoding='utf-8-sig')  # utf-8-sig = agrega BOM para que Excel lo abra bien
print(f"\n✅ Guardado: {output_path}\n")

print(f"=== MUESTRA (15 filas al azar) ===")
print(df_final.sample(15).to_string(index=False))

