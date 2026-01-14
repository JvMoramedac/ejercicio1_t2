

import java.io.File;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.sql.*;

public class MainActividad {
    // 1. Configuramos la conexión (tus datos)
    private static ConexionMySQL con = new ConexionMySQL("system", "Jv30102004", "xe");
    private static Scanner teclado = new Scanner(System.in);
    // El punto "." significa "busca dentro de la carpeta de este proyecto"
    private static String ruta = "./datos_equipos";
    public static void main(String[] args) {
        try {
            con.conectar(); // Clic virtual para conectar a Oracle
            int opcion;
            do {
                System.out.println("\n--- MENU FUTBOL ---");
                System.out.println("1. Crear tablas (lee la carpeta)");
                System.out.println("2. Rellenar datos (lee el XML)");
                System.out.println("3. Ver equipo (SELECT)");
                System.out.println("4. Borrar todo");
                System.out.println("5. Salir");
                opcion = Integer.parseInt(teclado.nextLine());

                if(opcion == 1) menuCrearTablas();
                if(opcion == 2) menuRellenar();
                if(opcion == 3) menuMostrar();
                if(opcion == 4) menuBorrar();

            } while (opcion != 5);
            con.desconectar();
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // EXPLICACIÓN PARA EL PROFESOR:
    // Este método mira la carpeta, ve que hay un "Betis.xml" y dice:
    // "Ah, pues voy a crear una tabla que se llame BETIS".
    private static void menuCrearTablas() throws SQLException {
        File carpeta = new File(ruta);
        File[] lista = carpeta.listFiles();

        // Este es el "paracaídas": si la carpeta está vacía o no existe, avisa y sale
        if (lista == null || lista.length == 0) {
            System.out.println("ERROR: No veo la carpeta '" + ruta + "' o está vacía.");
            System.out.println("Asegúrate de que la carpeta esté en: " + carpeta.getAbsolutePath());
            return;
        }

        for (File f : lista) {
            if (f.getName().endsWith(".xml")) {
                String tabla = f.getName().replace(".xml", "").toUpperCase();
                try {
                    con.ejecutarInsertDeleteUpdate("CREATE TABLE " + tabla + " (info Jugador)");
                    System.out.println("Tabla " + tabla + " creada correctamente.");
                } catch (SQLException e) {
                    System.out.println("La tabla " + tabla + " ya existía.");
                }
            }
        }
    }

    private static void menuRellenar() {
        System.out.println("Escribe el nombre del equipo (ej: Betis):");
        String nombre = teclado.nextLine();
        try {
            // 1. Abrimos el archivo XML
            File xml = new File(ruta + "/" + nombre + ".xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            Document doc = dbf.newDocumentBuilder().parse(xml);

            // 2. Buscamos a los jugadores dentro del XML
            NodeList nodos = doc.getElementsByTagName("jugador");
            for (int i = 0; i < nodos.getLength(); i++) {
                Element e = (Element) nodos.item(i);
                String n = e.getElementsByTagName("nombre").item(0).getTextContent();
                String d = e.getElementsByTagName("dorsal").item(0).getTextContent();
                String p = e.getElementsByTagName("demarcacion").item(0).getTextContent();
                String f = e.getElementsByTagName("nacimiento").item(0).getTextContent();

                // 3. Lo metemos en Oracle como un OBJETO
                String sql = "INSERT INTO " + nombre.toUpperCase() +
                        " VALUES (Jugador('" + n + "', " + d + ", '" + p + "', '" + f + "'))";
                con.ejecutarInsertDeleteUpdate(sql);
            }
            System.out.println("Datos guardados.");
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    private static void menuMostrar() throws SQLException {
        System.out.println("¿Qué equipo quieres ver?");
        String nombre = teclado.nextLine().toUpperCase();
        // Para leer objetos en Oracle usamos: alias.columna.atributo
        ResultSet rs = con.ejecutarSelect("SELECT t.info.nombre, t.info.dorsal FROM " + nombre + " t");
        while (rs.next()) {
            System.out.println(rs.getString(1) + " - Dorsal: " + rs.getInt(2));
        }
    }

    private static void menuBorrar() throws SQLException {
        // Simplemente recorremos y hacemos DROP TABLE
        File[] lista = new File(ruta).listFiles();
        for (File f : lista) {
            String tabla = f.getName().replace(".xml", "").toUpperCase();
            con.ejecutarInsertDeleteUpdate("DROP TABLE " + tabla);
        }
        System.out.println("Base de datos limpia.");
    }
}