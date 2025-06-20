package com.labredes.monitor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Gerencia a escrita de logs CSV para as camadas 2, 3 e 4
 */
public class CsvLogger {
  private static final Logger logger = LoggerFactory.getLogger(CsvLogger.class);
  private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private CSVPrinter layer2Printer;
  private CSVPrinter layer3Printer;
  private CSVPrinter layer4Printer;

  public void initializeLogs() throws IOException {
    // Inicializa arquivo da camada 2 (Enlace)
    layer2Printer = new CSVPrinter(
        new FileWriter("camada2.csv", true), // true para append
        CSVFormat.DEFAULT.withHeader(
            "Data e Hora",
            "Endereço MAC de Origem",
            "Endereço MAC de Destino",
            "Protocolo (EtherType)",
            "Tamanho Total do Quadro (bytes)"));

    // Inicializa arquivo da camada 3 (Rede)
    layer3Printer = new CSVPrinter(
        new FileWriter("camada3.csv", true),
        CSVFormat.DEFAULT.withHeader(
            "Data e Hora",
            "Nome do Protocolo",
            "Endereço IP de Origem",
            "Endereço IP de Destino",
            "Número Identificador do Protocolo",
            "Tamanho Total do Pacote (bytes)"));

    // Inicializa arquivo da camada 4 (Transporte)
    layer4Printer = new CSVPrinter(
        new FileWriter("camada4.csv", true),
        CSVFormat.DEFAULT.withHeader(
            "Data e Hora",
            "Nome do Protocolo",
            "Endereço IP de Origem",
            "Porta de Origem",
            "Endereço IP de Destino",
            "Porta de Destino",
            "Tamanho Total do Pacote (bytes)"));

    logger.info("Logs CSV inicializados: camada2.csv, camada3.csv, camada4.csv");
  }

  public void logPacket(PacketInfo packetInfo) {
    try {
      String timestamp = packetInfo.getTimestamp().format(TIMESTAMP_FORMATTER);

      // Log da camada 2 (sempre presente se houver Ethernet)
      if (packetInfo.getSourceMac() != null) {
        layer2Printer.printRecord(
            timestamp,
            packetInfo.getSourceMac(),
            packetInfo.getDestinationMac(),
            packetInfo.getEtherType(),
            packetInfo.getFrameSize());
        layer2Printer.flush();
      }

      // Log da camada 3 (se houver IP)
      if (packetInfo.getNetworkProtocol() != null) {
        layer3Printer.printRecord(
            timestamp,
            packetInfo.getNetworkProtocol(),
            packetInfo.getSourceIp(),
            packetInfo.getDestinationIp(),
            packetInfo.getProtocolNumber(),
            packetInfo.getPacketSize());
        layer3Printer.flush();
      }

      // Log da camada 4 (se houver protocolo de transporte)
      if (packetInfo.getTransportProtocol() != null) {
        layer4Printer.printRecord(
            timestamp,
            packetInfo.getTransportProtocol(),
            packetInfo.getSourceIp(),
            packetInfo.getSourcePort(),
            packetInfo.getDestinationIp(),
            packetInfo.getDestinationPort(),
            packetInfo.getPacketSize());
        layer4Printer.flush();
      }

    } catch (IOException e) {
      logger.error("Erro ao escrever log CSV: {}", e.getMessage(), e);
    }
  }

  public void closeLogs() {
    try {
      if (layer2Printer != null) {
        layer2Printer.close();
      }
      if (layer3Printer != null) {
        layer3Printer.close();
      }
      if (layer4Printer != null) {
        layer4Printer.close();
      }
      logger.info("Logs CSV fechados");
    } catch (IOException e) {
      logger.error("Erro ao fechar logs CSV: {}", e.getMessage(), e);
    }
  }
}