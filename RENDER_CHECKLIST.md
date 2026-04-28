# ✅ Checklist Pre-Despliegue Render

## Archivos Docker
- [x] `Dockerfile` (multi-stage build Java 21)
- [x] `.dockerignore` (optimización de capas)
- [x] `.env.example` (documentación de variables)

## Configuración Spring Boot
- [x] `application.yaml` con variables de entorno
- [x] `pom.xml` con Spring Boot 4.0.5
- [x] `.mvn/wrapper/` (Maven wrapper para consistency)

## Construcción Local
- [x] `mvnw clean package -DskipTests` ✅
- [x] JAR generado: `ArqHexagonal-0.0.1-SNAPSHOT.jar` (70MB)
- [x] Compilación sin errores

## Variables de Entorno en Render

Copiar y pegar estas en Render Dashboard:

```
DB_URL=jdbc:postgresql://your-db-host:5432/navaja_db
DB_USERNAME=your_db_user
DB_PASSWORD=your_secure_password
JWT_SECRET=your_production_jwt_secret_min_32_chars
JWT_EXPIRATION_MILLIS=86400000
FRONTEND_URL=https://your-frontend-domain.vercel.app
PORT=8080
```

## Pasos en Render

1. New Web Service → Connect GitHub
2. Runtime: Docker
3. Build Command: (dejar vacío)
4. Start Command: (dejar vacío)
5. Add Environment: Copiar variables de arriba
6. Deploy

## URLs POST-Despliegue para Probar

```bash
# Health Check
curl https://your-render-url.onrender.com/api/v1/public/health

# Generar QR
curl "https://your-render-url.onrender.com/api/v1/tools/qr?url=https://google.com" -o qr.png

# Login
curl -X POST https://your-render-url.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","contrasena":"password123"}'
```

## Dominio Custom (Opcional)

Render asigna automáticamente algo como:
```
https://navaja-backend-xxxx.onrender.com
```

Para dominio custom:
1. Render Dashboard → Settings → Custom Domains
2. Añade tu dominio
3. Apunta DNS A record a la IP de Render

## Monitoreo

- **Logs**: Render Dashboard → Logs (real-time streaming)
- **Métricas**: Render Dashboard → Metrics
- **Health**: Render Dashboard → Health Checks

---

**Ready to deploy!** 🚀

