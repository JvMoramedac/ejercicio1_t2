# ⚽ Gestión de Liga de Fútbol (Java & Oracle SQL)

Este proyecto es una herramienta de consola desarrollada en **Java** para la administración automatizada de una liga de fútbol. Permite integrar archivos de datos **XML** con una base de datos **Oracle**, facilitando la creación de estructuras, la carga masiva de jugadores y la consulta de plantillas.

---

## 🚀 Funcionalidades Principales

El sistema está diseñado para realizar el ciclo completo de gestión de datos:

1.  **Escaneo de Directorio**: Localiza archivos XML en la ruta local `src/Equipos/`.
2.  **Generación Automática**: Crea tablas en Oracle basadas en el nombre de los archivos encontrados (ej: `BARCELONA.xml` -> Tabla `BARCELONA`).
3.  **Carga de Datos (Parsing XML)**: Utiliza la API **DOM** para leer los nodos de cada jugador e insertarlos mediante SQL.
4.  **Consulta de Plantillas**: Recupera y formatea los datos almacenados en la base de datos para mostrarlos por consola.
5.  **Limpieza de Esquema**: Permite eliminar de forma rápida todas las tablas vinculadas a los archivos del proyecto.

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 11+
* **Base de Datos:** Oracle Database Express Edition (XE)
* **Conectividad:** JDBC (Driver `ojdbc8.jar` o superior)
* **Formato de Datos:** XML (Estructura de nodos `jugador`)

---

## 📂 Estructura del Código Unificado

Para simplificar la portabilidad, el código se ha consolidado en una estructura robusta dentro de un solo archivo:

* **`GestionLiga`**: Clase principal que gestiona el menú interactivo y la lógica de flujo.
* **`ConexionBD`**: Clase interna (Helper) que encapsula la configuración JDBC, apertura/cierre de sesiones y ejecución de sentencias SQL (`DDL` y `DML`).

---

## 📋 Requisitos y Configuración

### 1. Requisitos Previos
* Tener instalado **Oracle Database**.
* Configurar las credenciales en el código (por defecto: `system` / `2424`).
* Añadir el driver de Oracle al **Classpath** de tu proyecto.

### 2. Estructura de Archivos XML
Los archivos deben estar en `src/Equipos/` con el siguiente formato:
```xml
<equipo>
    <jugador>
        <nombre>Nombre del Jugador</nombre>
        <dorsal>10</dorsal>
        <demarcacion>Delantero</demarcacion>
        <nacimiento>1995</nacimiento>
    </jugador>
</equipo>
