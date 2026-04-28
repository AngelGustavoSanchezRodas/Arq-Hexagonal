# 🚀 Despliegue en Render

## Paso 1: Preparar el repositorio

Asegúrate de que estos archivos estén en la raíz del proyecto:
- ✅ `Dockerfile` (multi-stage build)
- ✅ `.dockerignore` (optimización de layer)
- ✅ `pom.xml` (Maven)
- ✅ `.mvn/` (Maven wrapper)

## Paso 2: Conectar repositorio en Render

1. Ve a https://render.com/dashboard
2. Crea un nuevo **Web Service**
3. Conecta tu repositorio GitHub
4. Selecciona el branch principal (main/master)

## Paso 3: Configuración en Render

### Build Settings
- **Runtime**: Docker
- **Build Command**: (dejar vacío, Render usará el Dockerfile)
- **Start Command**: (dejar vacío, Render usará ENTRYPOINT del Dockerfile)

### Environment Variables
Añade las siguientes variables de entorno en **Environment** > **Add Environment Variables**:

```
DB_URL = jdbc:postgresql://your-db-host:5432/navaja_db
DB_USERNAME = your_db_user
DB_PASSWORD = your_secure_password
JWT_SECRET = your_production_jwt_secret_at_least_32_chars
JWT_EXPIRATION_MILLIS = 86400000
FRONTEND_URL = https://your-frontend-domain.vercel.app
PORT = 8080
```

### Instancia
- **Instance Type**: Free tier (o pagar más si necesitas rendimiento)
- **Memory**: 512 MB (mínimo recomendado)

## Paso 4: Deploy automático

Una vez guardado, Render recogerá automáticamente:
1. La rama que seleccionaste
2. Ejecutará el Dockerfile (build multi-stage)
3. Desplegará la imagen en la URL asignada

## ✅ Verificar despliegue

Una vez complete, tendrás una URL pública como:
```
https://navaja-backend-xxxx.onrender.com
```

Prueba la API:
```bash
curl https://navaja-backend-xxxx.onrender.com/api/v1/tools/qr?url=https://google.com
```

## 📝 Notas

- **Tiempo de compilación**: ~5-10 minutos en primer build (se cachean dependencias)
- **Auto-redeployment**: Se dispara automáticamente con cada push a la rama
- **Logs**: Disponibles en Render Dashboard > Logs
- **Performance**: En free tier, puede quedar dormido tras 15 min de inactividad

## 🔒 Seguridad

- Nunca commitees `.env` a git
- Usa variables de entorno de Render para datos sensibles
- JWT_SECRET debe ser una cadena larga y aleatoria en producción

