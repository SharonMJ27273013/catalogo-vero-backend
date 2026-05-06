import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Server {

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(System.getenv("PORT"));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        // ================= PRODUCTOS =================
        server.createContext("/productos", exchange -> {

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                manejarOptions(exchange);
                return;
            }

            String json = """
            [
                {"nombre":"Paleta Vero","precio":10},
                {"nombre":"Chilito","precio":8},
                {"nombre":"Pulparindo","precio":12},
                {"nombre":"Gomitas","precio":9}
            ]
            """;

            responder(exchange, json);
        });


        // ================= VER CARRITO =================
        server.createContext("/carrito", exchange -> {

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                manejarOptions(exchange);
                return;
            }

            responder(exchange, listaAJson(carrito));
        });


        // ================= AGREGAR =================
        server.createContext("/agregar", exchange -> {

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                manejarOptions(exchange);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());

            String nombre = extraer(body, "nombre");
            int precio = Integer.parseInt(extraer(body, "precio"));

            Optional<Map<String, Object>> existente =
                    carrito.stream()
                            .filter(p -> p.get("nombre").equals(nombre))
                            .findFirst();

            if (existente.isPresent()) {
                int cant = (int) existente.get().get("cantidad");
                existente.get().put("cantidad", cant + 1);
            } else {
                Map<String, Object> nuevo = new HashMap<>();
                nuevo.put("nombre", nombre);
                nuevo.put("precio", precio);
                nuevo.put("cantidad", 1);
                carrito.add(nuevo);
            }

            responder(exchange, "ok");
        });


        // ================= SUMAR =================
        server.createContext("/sumar", exchange -> {

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                manejarOptions(exchange);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            String nombre = extraer(body, "nombre");

            carrito.forEach(p -> {
                if (p.get("nombre").equals(nombre)) {
                    int cant = (int) p.get("cantidad");
                    p.put("cantidad", cant + 1);
                }
            });

            responder(exchange, "ok");
        });


        // ================= RESTAR =================
        server.createContext("/restar", exchange -> {

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                manejarOptions(exchange);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            String nombre = extraer(body, "nombre");

            carrito.removeIf(p -> {
                if (p.get("nombre").equals(nombre)) {
                    int cant = (int) p.get("cantidad") - 1;

                    if (cant <= 0) return true;

                    p.put("cantidad", cant);
                }
                return false;
            });

            responder(exchange, "ok");
        });


        server.start();
        System.out.println("Servidor corriendo");
    }


    // ================= UTILIDADES =================

    static void responder(HttpExchange ex, String respuesta) throws IOException {

        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        ex.sendResponseHeaders(200, respuesta.getBytes().length);

        OutputStream os = ex.getResponseBody();
        os.write(respuesta.getBytes());
        os.close();
    }

    static void manejarOptions(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        ex.sendResponseHeaders(204, -1);
    }


    // Convertir lista a JSON
    static String listaAJson(List<Map<String, Object>> lista) {

        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < lista.size(); i++) {
            Map<String, Object> p = lista.get(i);

            sb.append("{\"nombre\":\"").append(p.get("nombre"))
                    .append("\",\"precio\":").append(p.get("precio"))
                    .append(",\"cantidad\":").append(p.get("cantidad"))
                    .append("}");

            if (i < lista.size() - 1) sb.append(",");
        }

        sb.append("]");

        return sb.toString();
    }


    // Extraer datos simples del JSON
    static String extraer(String json, String campo) {

        try {
            String valor = json.split("\"" + campo + "\":")[1];
            valor = valor.split(",|}")[0];
            return valor.replace("\"", "").trim();
        } catch (Exception e) {
            return "";
        }
    }
}
