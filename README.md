# Proyecto Spring Boot con MySQL (XAMPP)

Este proyecto contiene la configuración y archivos base para una aplicación Java con **Spring Boot 3** y conexión a base de datos **MySQL** a través de **XAMPP**.

---

## 📋 Requisitos Previos

1. **Java JDK 17 o superior** instalado y configurado en el `PATH`.
2. **Maven** instalado (o una IDE como VS Code, IntelliJ IDEA o Eclipse que lo gestione automáticamente).
3. **XAMPP** con el módulo de **MySQL** activo.

---

## ⚙️ Configuración en XAMPP

1. Abre el **XAMPP Control Panel**.
2. Inicia el servicio **MySQL** (haz clic en **Start**).
3. Por defecto, XAMPP configura:
   - **Host:** `localhost`
   - **Puerto:** `3306`
   - **Usuario:** `root`
   - **Contraseña:** *(vacía / sin contraseña)*
4. En `src/main/resources/application.properties`, la URL está configurada con:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/demo_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
   ```
   > 💡 Gracias al parámetro `createDatabaseIfNotExist=true`, MySQL creará la base de datos `demo_db` automáticamente la primera vez que se inicie la aplicación.

---

## 🚀 Cómo Ejecutar el Proyecto

### Opción 1: Desde la terminal (Maven)
Abre una terminal en esta carpeta y ejecuta:
```bash
mvn spring-boot:run
```

### Opción 2: Desde VS Code o IntelliJ IDEA
1. Abre la carpeta del proyecto en tu editor preferido.
2. Abre el archivo `src/main/java/com/example/demo/DemoApplication.java`.
3. Haz clic en **Run** o presiona `Shift + F10` / `F5`.

---

## 🧪 Endpoints de Prueba

Una vez que la aplicación esté corriendo, prueba los siguientes endpoints:

- **Comprobar Conexión con MySQL:**
  - Abre en tu navegador: [http://localhost:8080/api/conexion](http://localhost:8080/api/conexion)
  - Respuesta esperada:
    ```json
    {
      "estado": "EXITOSO",
      "mensaje": "Conexión establecida correctamente con MySQL (XAMPP)",
      "base_de_datos": "demo_db",
      "usuario_db": "root@localhost",
      "total_usuarios_registrados": 0
    }
    ```

- **Listar Usuarios:**
  - GET: [http://localhost:8080/api/usuarios](http://localhost:8080/api/usuarios)

- **Crear un Usuario (POST con JSON):**
  - POST a: `http://localhost:8080/api/usuarios`
  - Body (JSON):
    ```json
    {
      "nombre": "Juan Perez",
      "email": "juan@example.com"
    }
    ```
