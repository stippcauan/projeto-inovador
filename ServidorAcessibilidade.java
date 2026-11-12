import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServidorAcessibilidade {

    // Lista em memória para armazenar os locais cadastrados
    private static final List<LocalAcessivel> locais = new ArrayList<>();

    static class LocalAcessivel {
        String nome;
        String categoria;
        String recursos;
        String descricao;

        LocalAcessivel(String nome, String categoria, String recursos, String descricao) {
            this.nome = nome;
            this.categoria = categoria;
            this.recursos = recursos;
            this.descricao = descricao;
        }

        String toJson() {
            return String.format(
                "{\"nome\":\"%s\",\"categoria\":\"%s\",\"recursos\":\"%s\",\"descricao\":\"%s\"}",
                escapeJson(nome), escapeJson(categoria), escapeJson(recursos), escapeJson(descricao)
            );
        }
    }

    public static void main(String[] args) throws IOException {
        // Dados iniciais de exemplo
        locais.add(new LocalAcessivel(
            "Biblioteca Municipal Central",
            "Educação e Cultura",
            "Rampa de Acesso, Banheiro Adaptado, Piso Tátil",
            "Entrada principal nivelada, elevador funcional para todos os andares e acervo em Braille."
        ));
        locais.add(new LocalAcessivel(
            "Parque das Árvores",
            "Lazer",
            "Rampa de Acesso, Vaga PCD, Banheiro Adaptado",
            "Trilhas asfaltadas sem degraus e banheiros adaptados próximos à entrada sul."
        ));

        int porta = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(porta), 0);

        // Rota principal: entrega a interface gráfica (index.html)
        server.createContext("/", exchange -> {
            if (!exchange.getRequestURI().getPath().equals("/")) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            Path htmlPath = Path.of("index.html");
            if (!Files.exists(htmlPath)) {
                String erro = "Erro: arquivo index.html não encontrado na pasta do projeto.";
                byte[] bytes = erro.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(404, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
                return;
            }

            byte[] htmlBytes = Files.readAllBytes(htmlPath);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, htmlBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(htmlBytes);
            }
        });

        // Rota da API: lista (GET) e cadastra (POST) locais
        server.createContext("/api/locais", exchange -> {
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                StringBuilder json = new StringBuilder("[");
                synchronized (locais) {
                    for (int i = 0; i < locais.size(); i++) {
                        json.append(locais.get(i).toJson());
                        if (i < locais.size() - 1) json.append(",");
                    }
                }
                json.append("]");

                byte[] response = json.toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }

            } else if ("POST".equalsIgnoreCase(method)) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> dados = parseFormData(body);

                LocalAcessivel novo = new LocalAcessivel(
                    dados.getOrDefault("nome", "Local sem nome"),
                    dados.getOrDefault("categoria", "Geral"),
                    dados.getOrDefault("recursos", "Nenhum recurso especificado"),
                    dados.getOrDefault("descricao", "")
                );

                synchronized (locais) {
                    locais.add(0, novo);
                }

                byte[] response = "{\"status\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(201, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.setExecutor(null);
        server.start();

        String url = "http://localhost:" + porta;
        System.out.println("Servidor iniciado com sucesso em: " + url);
        System.out.println("Pressione Ctrl + C no terminal para encerrar.");

        // Abre a interface gráfica automaticamente no navegador padrão
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ignored) {
            }
        }
    }

    private static Map<String, String> parseFormData(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isEmpty()) return map;
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(key, value);
        }
        return map;
    }

    private static String escapeJson(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "");
    }
}