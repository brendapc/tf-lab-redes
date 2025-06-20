package com.labredes.monitor;

import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Monitor de tráfego de rede em tempo real para interface tun0
 * Captura pacotes usando raw sockets e gera logs CSV das camadas 2, 3 e 4
 */
public class NetworkMonitor {
  private static final Logger logger = LoggerFactory.getLogger(NetworkMonitor.class);
  private static final String INTERFACE_NAME = "tun0";
  private static final int SNAPLEN = 65536;
  private static final int READ_TIMEOUT = 10;

  private final PacketAnalyzer packetAnalyzer;
  private final StatisticsDisplay statisticsDisplay;
  private final CsvLogger csvLogger;
  private final AtomicBoolean running;
  private final ExecutorService executorService;

  public NetworkMonitor() {
    this.packetAnalyzer = new PacketAnalyzer();
    this.statisticsDisplay = new StatisticsDisplay();
    this.csvLogger = new CsvLogger();
    this.running = new AtomicBoolean(false);
    this.executorService = Executors.newFixedThreadPool(2);
  }

  public void start() {
    if (running.get()) {
      logger.warn("Monitor já está em execução");
      return;
    }

    running.set(true);
    logger.info("Iniciando monitor de tráfego na interface: {}", INTERFACE_NAME);

    try {
      // Inicializa os logs CSV
      csvLogger.initializeLogs();

      // Inicia a thread de exibição de estatísticas
      executorService.submit(statisticsDisplay);

      // Inicia a captura de pacotes
      startPacketCapture();

    } catch (Exception e) {
      logger.error("Erro ao iniciar monitor: {}", e.getMessage(), e);
      stop();
    }
  }

  public void stop() {
    if (!running.get()) {
      return;
    }

    running.set(false);
    logger.info("Parando monitor de tráfego...");

    executorService.shutdown();
    csvLogger.closeLogs();

    logger.info("Monitor parado com sucesso");
  }

  private void startPacketCapture() throws PcapNativeException, NotOpenException {
    PcapNetworkInterface nif = getNetworkInterface();
    if (nif == null) {
      throw new RuntimeException("Interface " + INTERFACE_NAME + " não encontrada");
    }

    PcapHandle handle = nif.openLive(SNAPLEN, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);

    logger.info("Captura iniciada na interface: {}", nif.getName());

    try {
      while (running.get()) {
        Packet packet = handle.getNextPacket();
        if (packet != null) {
          processPacket(packet);
        }
      }
    } finally {
      handle.close();
    }
  }

  private PcapNetworkInterface getNetworkInterface() throws PcapNativeException {
    PcapNetworkInterface nif = Pcaps.findAllDevs().stream()
        .filter(device -> device.getName().equals(INTERFACE_NAME))
        .findFirst()
        .orElse(null);

    if (nif == null) {
      logger.error("Interface {} não encontrada. Interfaces disponíveis:", INTERFACE_NAME);
      Pcaps.findAllDevs().forEach(device -> logger.info("  - {}", device.getName()));
    }

    return nif;
  }

  private void processPacket(Packet packet) {
    try {
      LocalDateTime timestamp = LocalDateTime.now();

      // Analisa o pacote
      PacketInfo packetInfo = packetAnalyzer.analyzePacket(packet, timestamp);

      // Atualiza estatísticas
      statisticsDisplay.updateStatistics(packetInfo);

      // Registra nos logs CSV
      csvLogger.logPacket(packetInfo);

    } catch (Exception e) {
      logger.error("Erro ao processar pacote: {}", e.getMessage(), e);
    }
  }

  public static void main(String[] args) {
    NetworkMonitor monitor = new NetworkMonitor();

    // Adiciona shutdown hook para parar graciosamente
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("Recebido sinal de shutdown, parando monitor...");
      monitor.stop();
    }));

    try {
      monitor.start();
    } catch (Exception e) {
      logger.error("Erro fatal: {}", e.getMessage(), e);
      System.exit(1);
    }
  }
}