
import java.io.File;
import java.sql.*;
import java.util.Scanner;
import org.w3c.dom.*;
import javax.xml.parsers.*;

/**
 * Aplicación Integral para la Gestión de Liga de Fútbol.
 * Incluye gestión de conexión Oracle y lógica de negocio en un solo archivo.
 */
public class MainActividad {

    private static final String RUTA_XML = "src/Equipos";
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        // Inicializar la conexión (Usuario, Password, SID/Servicio)
        ConexionBD db = new ConexionBD("system", "Jv30102004", "xe");

        try {
            db.conectar();
            System.out.println("--- Conexión establecida con éxito ---");

        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return;
        }

        int opcion = 0;
        do {
            imprimirMenu();
            try {
                opcion = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                opcion = 0;
            }

            switch (opcion) {
                case 1: crearTablasDesdeDirectorio(db); break;
                case 2: rellenarDatosDesdeXML(db); break;
                case 3: mostrarEquipo(db); break;
                case 4: eliminarTablasDirectorio(db); break;
                case 5:
                    System.out.println("Cerrando sistema...");
                    try { db.desconectar(); } catch (SQLException e) { e.printStackTrace(); }
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (opcion != 5);

        sc.close();
    }

    private static void imprimirMenu() {
        System.out.println("\n--- MENU LIGA ---");
        System.out.println("1. Crear tablas (desde archivos en directorio)");
        System.out.println("2. Rellenar datos desde XML");
        System.out.println("3. Mostrar equipo");
        System.out.println("4. Eliminar todas las tablas");
        System.out.println("5. Salir");
        System.out.print("Opción: ");
    }

    // --- MÉTODOS DE LÓGICA DE NEGOCIO ---

    private static void crearTablasDesdeDirectorio(ConexionBD db) {
        File carpeta = new File(RUTA_XML);
        File[] archivos = carpeta.listFiles();
        if (archivos == null) return;

        for (File f : archivos) {
            if (f.getName().endsWith(".xml")) {
                String nombre = f.getName().replace(".xml", "");
                try {
                    db.crearTablaEquipo(nombre);
                    System.out.println("Tabla creada: " + nombre);
                } catch (SQLException e) {
                    System.err.println("Error en " + nombre + ": " + e.getMessage());
                }
            }
        }
    }

    private static void rellenarDatosDesdeXML(ConexionBD db) {
        System.out.print("Nombre del equipo: ");
        String equipo = sc.nextLine();
        File f = new File(RUTA_XML + "/" + equipo + ".xml");

        if (!f.exists()) {
            System.out.println("Archivo no encontrado.");
            return;
        }

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
            NodeList lista = doc.getElementsByTagName("jugador");

            for (int i = 0; i < lista.getLength(); i++) {
                Element e = (Element) lista.item(i);
                String sql = String.format(
                        "INSERT INTO %s (nombre, dorsal, demarcacion, nacimiento) VALUES ('%s', %s, '%s', '%s')",
                        equipo, getVal(e, "nombre"), getVal(e, "dorsal"), getVal(e, "demarcacion"), getVal(e, "nacimiento")
                );
                db.ejecutarInsertDeleteUpdate(sql);
            }
            System.out.println("Datos cargados en " + equipo);
        } catch (Exception ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

    private static void mostrarEquipo(ConexionBD db) {
        System.out.print("Equipo a consultar: ");
        String equipo = sc.nextLine();
        try (ResultSet rs = db.ejecutarSelect("SELECT * FROM " + equipo)) {
            while (rs.next()) {
                System.out.printf("[%d] %s - %s (%s)%n",
                        rs.getInt("dorsal"), rs.getString("nombre"), rs.getString("demarcacion"), rs.getString("nacimiento"));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void eliminarTablasDirectorio(ConexionBD db) {
        File[] archivos = new File(RUTA_XML).listFiles();
        if (archivos == null) return;
        for (File f : archivos) {
            if (f.getName().endsWith(".xml")) {
                try {
                    db.eliminarTablaEquipo(f.getName().replace(".xml", ""));
                } catch (SQLException e) {
                    System.err.println("Error al borrar: " + e.getMessage());
                }
            }
        }
        System.out.println("Proceso de borrado finalizado.");
    }

    private static String getVal(Element e, String tag) {
        return e.getElementsByTagName(tag).item(0).getTextContent();
    }
}

/**
 * Clase interna para gestionar la comunicación con Oracle.
 */
class ConexionBD {
    private String user, pass, bd, host = "localhost:1521";
    private Connection conn;

    public ConexionBD(String user, String pass, String bd) {
        this.user = user;
        this.pass = pass;
        this.bd = bd;
    }

    public void conectar() throws SQLException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@//" + host + "/" + bd, user, pass);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver Oracle no encontrado");
        }
    }

    public void desconectar() throws SQLException {
        if (conn != null && !conn.isClosed()) conn.close();
    }

    // --- NUEVOS MÉTODOS SOLICITADOS ---

    public void crearTablaEquipo(String nombreTabla) throws SQLException {
        String sql = "CREATE TABLE " + nombreTabla + " (" +
                "nombre VARCHAR2(100), " +
                "dorsal NUMBER(2), " +
                "demarcacion VARCHAR2(50), " +
                "nacimiento VARCHAR2(50))";
        ejecutarInsertDeleteUpdate(sql);
    }

    public void eliminarTablaEquipo(String nombreTabla) throws SQLException {
        String sql = "DROP TABLE " + nombreTabla;
        ejecutarInsertDeleteUpdate(sql);
    }

    // --- MÉTODOS DE EJECUCIÓN ---

    public ResultSet ejecutarSelect(String sql) throws SQLException {
        return conn.createStatement().executeQuery(sql);
    }

    public int ejecutarInsertDeleteUpdate(String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }
}