package com.labredes.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatisticsDisplay implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(StatisticsDisplay.class);
  private static final int DISPLAY_INTERVAL_MS = 5000; // 5 segundos

  private final AtomicBoolean running;
  private final ConcurrentHashMap<String, AtomicLong> protocolCounters;
  private final ConcurrentHashMap<String, AtomicLong> networkCounters;
  private final AtomicLong totalPackets;
  private final AtomicLong totalBytes;

  public StatisticsDisplay() {
    this.running = new AtomicBoolean(true);
    this.protocolCounters = new ConcurrentHashMap<>();
    this.networkCounters = new ConcurrentHashMap<>();
    this.totalPackets = new AtomicLong(0);
    this.totalBytes = new AtomicLong(0);
  }

  public void updateStatistics(PacketInfo packetInfo) {
    totalPackets.incrementAndGet();
    totalBytes.addAndGet(packetInfo.getFrameSize());

    // Contadores de protocolos de transporte
    if (packetInfo.getTransportProtocol() != null) {
      protocolCounters.computeIfAbsent(packetInfo.getTransportProtocol(), k -> new AtomicLong(0))
          .incrementAndGet();
    }

    // Contadores de protocolos de rede
    if (packetInfo.getNetworkProtocol() != null) {
      networkCounters.computeIfAbsent(packetInfo.getNetworkProtocol(), k -> new AtomicLong(0))
          .incrementAndGet();
    }
  }

  @Override
  public void run() {
    logger.info("Iniciando exibição de estatísticas...");

    while (running.get()) {
      try {
        Thread.sleep(DISPLAY_INTERVAL_MS);
        displayStatistics();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        logger.error("Erro ao exibir estatísticas: {}", e.getMessage(), e);
      }
    }

    logger.info("Exibição de estatísticas parada");
  }

  private void displayStatistics() {
    clearScreen();

    LocalDateTime now = LocalDateTime.now();
    String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    System.out.println("+==============================================================+");
    System.out.println("|                MONITOR DE TRAFEGO DE REDE                   |");
    System.out.println("|                    Interface: tun0                          |");
    System.out.println("|                    " + timestamp + "                    |");
    System.out.println("+==============================================================+");
    System.out.println("|                        ESTATISTICAS GERAIS                  |");
    System.out.println("+==============================================================+");
    System.out.printf("|  Total de Pacotes: %-45d |\n", totalPackets.get());
    System.out.printf("|  Total de Bytes: %-47d |\n", totalBytes.get());
    System.out.printf("|  Taxa Media: %-49.2f pacotes/s |\n",
        calculatePacketRate());
    System.out.println("+==============================================================+");

    // Protocolos de rede (Camada 3)
    System.out.println("|                    PROTOCOLOS DE REDE                       |");
    System.out.println("+==============================================================+");
    networkCounters.forEach((protocol, count) -> {
      System.out.printf("|  %-15s: %-40d |\n", protocol, count.get());
    });

    // Protocolos de transporte (Camada 4)
    System.out.println("+==============================================================+");
    System.out.println("|                  PROTOCOLOS DE TRANSPORTE                   |");
    System.out.println("+==============================================================+");
    protocolCounters.forEach((protocol, count) -> {
      System.out.printf("|  %-15s: %-40d |\n", protocol, count.get());
    });

    System.out.println("+==============================================================+");
    System.out.println("Pressione Ctrl+C para parar o monitor...");
  }

  private void clearScreen() {
    // Limpa a tela (funciona em terminais Unix/Linux)
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }

  private double calculatePacketRate() {
    // Implementação simples - pode ser melhorada com histórico
    return totalPackets.get() > 0 ? (double) totalPackets.get() / 10.0 : 0.0;
  }

  public void stop() {
    running.set(false);
  }
}