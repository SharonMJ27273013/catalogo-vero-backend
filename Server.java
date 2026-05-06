import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Server {

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(System.getenv("PORT"));

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/productos", exchange -> {

            String json = """
            [
                {"nombre":"Paleta Vero","precio":10},
                {"nombre":"Chilito","precio":8},
                {"nombre":"Pulparindo","precio":12},
                {"nombre":"Gomitas","precio":9}
            ]
            """;

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

            exchange.sendResponseHeaders(200, json.getBytes().length);

            OutputStream os = exchange.getResponseBody();
            os.write(json.getBytes());
            os.close();
        });

        server.start();
        System.out.println("Servidor corriendo en puerto " + port);
    }
}
