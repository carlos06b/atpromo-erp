package util;

import model.Client;
import model.InvoiceView;
import model.PromoterPaymentData;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelGenerator {

    public static void generateInvoices(List<InvoiceView> invoices, String path) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Faturamento");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            Row header = sheet.createRow(0);

            String[] columns = {
                    "Indústria", "Vínculo", "Valor",
                    "Previsto", "Faturado em", "Pago em", "Status"
            };

            CellStyle headerStyle = createHeaderStyle(workbook);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;

            for (InvoiceView inv : invoices) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(inv.getClientName());
                row.createCell(1).setCellValue(inv.getCompanyLink());
                row.createCell(2).setCellValue(inv.getAmount().doubleValue());

                row.createCell(3).setCellValue(
                        inv.getDueDate() != null ? inv.getDueDate().format(formatter) : "-"
                );

                row.createCell(4).setCellValue(
                        inv.getIssueDate() != null ? inv.getIssueDate().format(formatter) : "-"
                );

                row.createCell(5).setCellValue(
                        inv.getPaymentDate() != null ? inv.getPaymentDate().format(formatter) : "-"
                );

                row.createCell(6).setCellValue(inv.getStatus());
            }

            autoSizeColumns(sheet, columns.length);

            try (FileOutputStream fileOut = new FileOutputStream(path)) {
                workbook.write(fileOut);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar Excel", e);
        }
    }

    public static void generateTextReport(String content, String path) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Relatório");

            String[] lines = content.split("\\R");

            int rowNum = 0;

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            for (String line : lines) {
                Row row = sheet.createRow(rowNum++);
                Cell cell = row.createCell(0);
                cell.setCellValue(line);

                if (line.contains("RELATÓRIO") || line.contains("FOLHA") || line.contains("RESUMO")) {
                    cell.setCellStyle(titleStyle);
                }
            }

            sheet.autoSizeColumn(0);

            try (FileOutputStream fileOut = new FileOutputStream(path)) {
                workbook.write(fileOut);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar Excel", e);
        }
    }

    public static void generatePendingRequests(List<String> requests, String path) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Solicitações Pendentes");

            String[] columns = {
                    "ID", "Promotor", "Tipo", "Valor", "Mensagem", "Status", "Data"
            };

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row header = sheet.createRow(0);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;

            for (String request : requests) {
                String[] parts = request.split("\\|");

                Row row = sheet.createRow(rowNum++);

                for (int i = 0; i < parts.length && i < columns.length; i++) {
                    row.createCell(i).setCellValue(parts[i].trim());
                }
            }

            autoSizeColumns(sheet, columns.length);

            try (FileOutputStream fileOut = new FileOutputStream(path)) {
                workbook.write(fileOut);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório de solicitações pendentes", e);
        }
    }

    public static void generatePixBatch(List<PromoterPaymentData> payments, String path) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("PIX Lote");

            String[] columns = {
                    "Tipo de Inscrição",
                    "Inscrição",
                    "Nome",
                    "Tipo da Chave",
                    "Chave PIX",
                    "Valor",
                    "Data do Pagamento"
            };

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row header = sheet.createRow(0);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            int rowNum = 1;

            for (PromoterPaymentData payment : payments) {
                Row row = sheet.createRow(rowNum++);

                String document = payment.getDocument();
                String name = payment.getName();
                String pix = payment.getPix();
                String pixType = payment.getPixType();

                row.createCell(0).setCellValue(mapRegistrationType(document));
                row.createCell(1).setCellValue(formatDocument(document));
                row.createCell(2).setCellValue(name != null ? name.toUpperCase() : "");
                row.createCell(3).setCellValue(mapPixType(pixType));
                row.createCell(4).setCellValue(formatPix(pix, pixType));
                row.createCell(5).setCellValue(
                        payment.getAmount() != null ? formatMoney(payment.getAmount()) : ""
                );
                row.createCell(6).setCellValue(payment.getPaymentDate().format(formatter));
            }

            autoSizeColumns(sheet, columns.length);

            try (FileOutputStream fileOut = new FileOutputStream(path)) {
                workbook.write(fileOut);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PIX lote", e);
        }
    }

    public static void generateClients(List<Client> clients, String path) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Clientes");

            String[] columns = {"Indústria", "CNPJ", "Telefone", "Vínculo", "Status"};

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row header = sheet.createRow(0);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;

            for (Client client : clients) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(client.getName());
                row.createCell(1).setCellValue(client.getCnpj());
                row.createCell(2).setCellValue(client.getPhone());
                row.createCell(3).setCellValue(client.getCompanyLink());
                row.createCell(4).setCellValue(client.isActive() ? "Ativo" : "Inativo");
            }

            autoSizeColumns(sheet, columns.length);

            try (FileOutputStream fileOut = new FileOutputStream(path)) {
                workbook.write(fileOut);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar Excel de clientes", e);
        }
    }

    private static String mapRegistrationType(String document) {
        String clean = onlyNumbers(document);

        if (clean.isBlank()) {
            return "";
        }

        if (clean.length() == 14) {
            return "2"; // CNPJ
        }

        return "1"; // CPF
    }

    private static String mapPixType(String pixType) {
        if (pixType == null || pixType.isBlank()) {
            return "4";
        }

        return switch (pixType.trim().toUpperCase()) {
            case "TELEFONE" -> "1";
            case "EMAIL" -> "2";
            case "CPF", "CNPJ" -> "3";
            case "ALEATORIA", "ALEATÓRIA" -> "4";
            default -> "4";
        };
    }

    private static String formatDocument(String document) {
        if (document == null) {
            return "";
        }

        String clean = onlyNumbers(document);

        if (clean.length() == 11) {
            return clean.substring(0, 3) + "." +
                    clean.substring(3, 6) + "." +
                    clean.substring(6, 9) + "-" +
                    clean.substring(9);
        }

        if (clean.length() == 14) {
            return clean.substring(0, 2) + "." +
                    clean.substring(2, 5) + "." +
                    clean.substring(5, 8) + "/" +
                    clean.substring(8, 12) + "-" +
                    clean.substring(12);
        }

        return document;
    }

    private static String formatPix(String pix, String pixType) {
        if (pix == null || pix.isBlank()) {
            return "";
        }

        if (pixType == null || pixType.isBlank()) {
            return pix;
        }

        String type = pixType.trim().toUpperCase();
        String clean = onlyNumbers(pix);

        return switch (type) {
            case "TELEFONE" -> formatPhonePix(clean, pix);
            case "CPF", "CNPJ" -> formatDocument(pix);
            case "EMAIL", "ALEATORIA", "ALEATÓRIA" -> pix;
            default -> pix;
        };
    }

    private static String formatPhonePix(String clean, String original) {
        if (clean.length() == 13 && clean.startsWith("55")) {
            return "+" + clean.substring(0, 2) +
                    " (" + clean.substring(2, 4) + ")" +
                    clean.substring(4, 9) +
                    "-" +
                    clean.substring(9);
        }

        if (clean.length() == 11) {
            return "+55 (" + clean.substring(0, 2) + ")" +
                    clean.substring(2, 7) +
                    "-" +
                    clean.substring(7);
        }

        return original;
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) {
            return "R$ 0,00";
        }

        return "R$ " + value.setScale(2, java.math.RoundingMode.HALF_UP)
                .toString()
                .replace(".", ",");
    }

    private static String onlyNumbers(String value) {
        if (value == null) {
            return "";
        }

        return value.replaceAll("[^0-9]", "");
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        return headerStyle;
    }

    private static void autoSizeColumns(Sheet sheet, int totalColumns) {
        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}