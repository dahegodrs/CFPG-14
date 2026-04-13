import java.io.PrintWriter;
import java.util.Random;

/**
 * Clase utilitaria para generar archivos de prueba pseudoaleatorios.
 * Crea los archivos de entrada necesarios para el programa principal:
 * productos, vendedores y registros de ventas por vendedor.
 *
 * @author 840
 * @version 1.0
 */
public class GenerateInfoFiles {

    /** Lista de nombres para generación aleatoria de vendedores. */
    private static final String[] NOMBRES = {"Carlos", "Ana", "Luis", "Maria", "Juan"};

    /** Lista de apellidos para generación aleatoria de vendedores. */
    private static final String[] APELLIDOS = {"Gomez", "Perez", "Rodriguez", "Martinez"};

    /** Tipos de documento válidos. */
    private static final String[] TIPOS_DOC = {"CC", "CE", "TI"};

    /** Nombres de productos disponibles para la generación. */
    private static final String[] PRODUCTOS_NOMBRES = {"Laptop", "Mouse", "Teclado", "Monitor"};

    /** Precios correspondientes a cada producto en PRODUCTOS_NOMBRES. */
    private static final double[] PRODUCTOS_PRECIOS = {2500000.50, 80000.00, 150000.99, 950000.00};

    /**
     * Método principal. Genera los archivos de prueba: productos.csv,
     * vendedores.csv y un archivo de ventas por cada vendedor.
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando generación de archivos...");
            createProductsFile(PRODUCTOS_NOMBRES.length);
            createSalesManInfoFile(3);
            System.out.println("¡Archivos generados exitosamente!");
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    /**
     * Crea el archivo productos.csv con información pseudoaleatoria de productos.
     * Formato por línea: IDProducto;NombreProducto;PrecioPorUnidad
     *
     * @param productsCount Número de productos a generar.
     * @throws Exception Si ocurre un error al escribir el archivo.
     */
    public static void createProductsFile(int productsCount) throws Exception {
        try (PrintWriter writer = new PrintWriter("productos.csv", "UTF-8")) {
            for (int i = 0; i < productsCount; i++) {
                writer.println((i + 1) + ";" + PRODUCTOS_NOMBRES[i] + ";" + PRODUCTOS_PRECIOS[i]);
            }
        }
    }

    /**
     * Crea el archivo vendedores.csv con información de vendedores generada
     * pseudoaleatoriamente, y genera un archivo de ventas por cada vendedor.
     * Formato por línea: TipoDocumento;NumeroDocumento;Nombres;Apellidos
     *
     * @param salesmanCount Número de vendedores a generar.
     * @throws Exception Si ocurre un error al escribir los archivos.
     */
    public static void createSalesManInfoFile(int salesmanCount) throws Exception {
        Random rand = new Random();
        try (PrintWriter writer = new PrintWriter("vendedores.csv", "UTF-8")) {
            for (int i = 0; i < salesmanCount; i++) {
                long id = 100000000L + rand.nextInt(900000000);
                String nombre = NOMBRES[rand.nextInt(NOMBRES.length)];
                String apellido = APELLIDOS[rand.nextInt(APELLIDOS.length)];
                // FIX: se guarda el tipo de documento asignado al vendedor
                String tipoDoc = TIPOS_DOC[rand.nextInt(TIPOS_DOC.length)];

                writer.println(tipoDoc + ";" + id + ";" + nombre + ";" + apellido);

                // Se pasa el tipoDoc real del vendedor, no uno aleatorio
                createSalesMenFile(rand.nextInt(4) + 2, nombre, id, tipoDoc);
            }
        }
    }

    /**
     * Crea un archivo CSV de ventas para un vendedor específico.
     * La primera línea identifica al vendedor; las siguientes representan ventas.
     * Formato línea 1: TipoDocumento;NumeroDocumento
     * Formato ventas: IDProducto;CantidadVendida
     *
     * @param randomSalesCount Número de ventas aleatorias a generar.
     * @param name             Nombre del vendedor (usado para identificación).
     * @param id               Número de documento del vendedor.
     * @param tipoDoc          Tipo de documento real del vendedor.
     * @throws Exception Si ocurre un error al escribir el archivo.
     */
    public static void createSalesMenFile(int randomSalesCount, String name, long id, String tipoDoc) throws Exception {
        Random rand = new Random();
        String nombreArchivo = "vendedor_" + id + ".csv";
        try (PrintWriter writer = new PrintWriter(nombreArchivo, "UTF-8")) {
            // FIX: se usa el tipoDoc del vendedor, no uno aleatorio
            writer.println(tipoDoc + ";" + id);
            for (int i = 0; i < randomSalesCount; i++) {
                int idProducto = rand.nextInt(PRODUCTOS_NOMBRES.length) + 1;
                int cantidad = rand.nextInt(10) + 1;
                writer.println(idProducto + ";" + cantidad);
            }
        }
    }
}