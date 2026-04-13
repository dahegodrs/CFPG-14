import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Representa un producto del catálogo con su ID, nombre, precio
 * y la cantidad total vendida entre todos los vendedores.
 *
 * @author 840
 * @version 1.0
 */
class Producto {

    /** Identificador único del producto. */
    String id;

    /** Nombre descriptivo del producto. */
    String nombre;

    /** Precio unitario del producto. */
    double precio;

    /** Acumulado de unidades vendidas de este producto. */
    int cantidadVendida = 0;

    /**
     * Constructor del producto.
     *
     * @param id     Identificador del producto.
     * @param nombre Nombre del producto.
     * @param precio Precio por unidad.
     */
    Producto(String id, String nombre, double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    /**
     * Retorna el identificador del producto.
     *
     * @return ID del producto como String.
     */
    public String getId() { return id; }
}

/**
 * Representa un vendedor con su información personal
 * y el total acumulado de sus ventas.
 *
 * @author 840
 * @version 1.0
 */
class Vendedor {

    /** Tipo de documento de identidad del vendedor. */
    String tipoDoc;

    /** Número de documento del vendedor. */
    String numDoc;

    /** Nombres del vendedor. */
    String nombres;

    /** Apellidos del vendedor. */
    String apellidos;

    /** Total de dinero recaudado por el vendedor. */
    double ventasTotales = 0.0;

    /**
     * Constructor del vendedor.
     *
     * @param tipoDoc  Tipo de documento.
     * @param numDoc   Número de documento.
     * @param nombres  Nombres del vendedor.
     * @param apellidos Apellidos del vendedor.
     */
    Vendedor(String tipoDoc, String numDoc, String nombres, String apellidos) {
        this.tipoDoc = tipoDoc;
        this.numDoc = numDoc;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }

    /**
     * Retorna el número de documento del vendedor.
     *
     * @return Número de documento como String.
     */
    public String getNumDoc() { return numDoc; }
}

/**
 * Clase principal del sistema de reportes de ventas.
 * Lee los archivos generados por GenerateInfoFiles y produce:
 * - reporte_vendedores.csv: vendedores ordenados por total de ventas (mayor a menor).
 * - reporte_productos.csv: productos ordenados por cantidad vendida (mayor a menor).
 *
 * @author 840
 * @version 1.0
 */
public class Main {

    /**
     * Método principal. Orquesta la carga de datos, procesamiento
     * de ventas y generación de reportes.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando procesamiento...");

            Map<String, Producto> mapaProductos = cargarDatos(
                "productos.csv",
                linea -> {
                    String[] datos = linea.split(";");
                    return new Producto(datos[0], datos[1], Double.parseDouble(datos[2]));
                },
                Producto::getId
            );

            Map<String, Vendedor> mapaVendedores = cargarDatos(
                "vendedores.csv",
                linea -> {
                    String[] datos = linea.split(";");
                    return new Vendedor(datos[0], datos[1], datos[2], datos[3]);
                },
                Vendedor::getNumDoc
            );

            Files.walk(Paths.get("."))
                .filter(path -> path.getFileName().toString().startsWith("vendedor_"))
                .forEach(path -> procesarArchivoVenta(path, mapaProductos, mapaVendedores));

            generarReportes(mapaVendedores, mapaProductos);
            System.out.println("¡Reportes generados exitosamente!");

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    /**
     * Carga un archivo CSV y lo convierte en un mapa indexado por clave.
     *
     * @param <T>         Tipo del objeto a construir por línea.
     * @param archivo     Ruta del archivo a leer.
     * @param constructor Función que convierte una línea en un objeto T.
     * @param getKey      Función que extrae la clave del objeto T.
     * @return Mapa con los objetos indexados por su clave.
     * @throws IOException Si el archivo no existe o no se puede leer.
     */
    private static <T> Map<String, T> cargarDatos(
            String archivo,
            Function<String, T> constructor,
            Function<T, String> getKey) throws IOException {

        return Files.lines(Paths.get(archivo))
            .filter(linea -> !linea.trim().isEmpty())
            .map(constructor)
            .collect(Collectors.toMap(getKey, item -> item));
    }

    /**
     * Procesa un archivo de ventas de un vendedor y acumula
     * las ventas totales por vendedor y por producto.
     *
     * @param archivo Ruta del archivo de ventas del vendedor.
     * @param prods   Mapa de productos disponibles.
     * @param vends   Mapa de vendedores registrados.
     */
    private static void procesarArchivoVenta(
            Path archivo,
            Map<String, Producto> prods,
            Map<String, Vendedor> vends) {
        try {
            List<String> lineas = Files.readAllLines(archivo);
            if (lineas.isEmpty()) return;

            String idVendedor = lineas.get(0).split(";")[1].trim();
            Vendedor vendedor = vends.get(idVendedor);
            if (vendedor == null) {
                System.err.println("ADVERTENCIA: vendedor no encontrado en " + archivo.getFileName());
                return;
            }

            for (int i = 1; i < lineas.size(); i++) {
                String linea = lineas.get(i).trim();

                // FIX: validación de líneas vacías o mal formadas
                if (linea.isEmpty()) continue;
                String[] datos = linea.split(";");
                if (datos.length < 2 || datos[0].trim().isEmpty() || datos[1].trim().isEmpty()) {
                    System.err.println("ADVERTENCIA: línea mal formada en " + archivo.getFileName() + " → " + linea);
                    continue;
                }

                Producto producto = prods.get(datos[0].trim());
                if (producto == null) {
                    System.err.println("ADVERTENCIA: producto ID '" + datos[0].trim() + "' no existe.");
                    continue;
                }

                int cantidad = Integer.parseInt(datos[1].trim());
                vendedor.ventasTotales += producto.precio * cantidad;
                producto.cantidadVendida += cantidad;
            }

        } catch (Exception e) {
            System.err.println("ADVERTENCIA al procesar archivo: " + archivo.getFileName());
        }
    }

    /**
     * Genera los dos archivos de reporte en formato CSV.
     * reporte_vendedores.csv: ordenado por ventas totales descendente.
     * reporte_productos.csv: ordenado por cantidad vendida descendente.
     *
     * @param mapaVendedores Mapa con todos los vendedores procesados.
     * @param mapaProductos  Mapa con todos los productos procesados.
     * @throws IOException Si ocurre un error al escribir los archivos.
     */
    private static void generarReportes(
            Map<String, Vendedor> mapaVendedores,
            Map<String, Producto> mapaProductos) throws IOException {

        List<Vendedor> vendedoresOrdenados = mapaVendedores.values().stream()
            .sorted(Comparator.comparingDouble((Vendedor v) -> v.ventasTotales).reversed())
            .collect(Collectors.toList());

        // FIX: UTF-8 en el reporte de vendedores
        try (PrintWriter writer = new PrintWriter("reporte_vendedores.csv", "UTF-8")) {
            for (Vendedor v : vendedoresOrdenados) {
                writer.println(v.nombres + " " + v.apellidos + ";" + String.format("%.2f", v.ventasTotales));
            }
        }

        List<Producto> productosOrdenados = mapaProductos.values().stream()
            .sorted(Comparator.comparingInt((Producto p) -> p.cantidadVendida).reversed())
            .collect(Collectors.toList());

        // FIX: UTF-8 + se agrega cantidadVendida al reporte de productos
        try (PrintWriter writer = new PrintWriter("reporte_productos.csv", "UTF-8")) {
            for (Producto p : productosOrdenados) {
                writer.println(p.nombre + ";" + String.format("%.2f", p.precio) + ";" + p.cantidadVendida);
            }
        }
    }
}