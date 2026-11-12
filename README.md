# Mapeador Colaborativo de Acessibilidade (Java)

Aplicação desktop desenvolvida integralmente em **Java (Swing / Graphics2D)** para mapeamento colaborativo de acessibilidade urbana, permitindo registrar rampas, pisos táteis, banheiros adaptados e alertas de obstáculos.

## Funcionalidades
- Interface gráfica nativa em Java com mapa urbano interativo.
- Clique no mapa para capturar coordenadas e cadastrar novos pontos.
- Persistência local automática em arquivo (`pontos_acessibilidade.csv`).

## Como executar
1. Certifique-se de ter o **Java JDK 11 ou superior** instalado.
2. Compile o projeto:
   ```bash
   javac MapeadorAcessibilidade.java