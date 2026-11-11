import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final List<String> pontosJson = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Ponto de exemplo inicial no Paraná
        pontosJson.add("{"
            + "\"estado\":\"PR\","
            + "\"cidade\":\"Curitiba\","
            + "\"estabelecimento\":\"Praça Tiradentes\","
            + "\"tipo\":\"Rampa de Acesso\","
            + "\"status\":\"total\","
            + "\"lat\":-25.4284,"
            + "\"lng\":-49.2733,"
            + "\"comentario\":\"Rampa em ótimo estado com piso antiderrapante e corrimão duplo.\""
            + "}");

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Rota principal: Serve o arquivo index.html
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("/".equals(exchange.getRequestURI().getPath())) {
                    File file = new File("index.html");
                    if (file.exists()) {
                        byte[] bytes = Files.readAllBytes(file.toPath());
                        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                        exchange.sendResponseHeaders(200, bytes.length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(bytes);
                        os.close();
                        return;
                    }
                }
                String notFound = "Erro: O arquivo index.html não foi encontrado.";
                exchange.sendResponseHeaders(404, notFound.length());
                OutputStream os = exchange.getResponseBody();
                os.write(notFound.getBytes());
                os.close();
            }
        });

        // Rota API JSON para listar e salvar os comentários e locais
        server.createContext("/api/pontos", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String response = "[" + String.join(",", pontosJson) + "]";
                    byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    pontosJson.add(body);

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(201, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                }
            }
        });

        server.setExecutor(null);
        System.out.println("Servidor Java rodando na porta 8080!");
        System.out.println("Acesse: http://localhost:8080");
        server.start();
    }
}