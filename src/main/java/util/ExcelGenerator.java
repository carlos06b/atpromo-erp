package util;

import model.InvoiceView;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import model.Client;
import java.util.List;
import java.io.FileOutputStream;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelGenerator {

    public static void generateInvoices(List<InvoiceView> invoices, String path) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Faturamento");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // HEADER
            Row header = sheet.createRow(0);

            String[] columns = {
                    "Indústria", "Vínculo", "Valor",
                    "Previsto", "Faturado em", "Pago em", "Status"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // DADOS
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

            // AUTO SIZE
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            FileOutputStream fileOut = new FileOutputStream(path);
            workbook.write(fileOut);
            fileOut.close();

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

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

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

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(path)) {
                workbook.write(fileOut);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório de solicitações pendentes", e);
        }
    }

    public static void generateClients(List<Client> clients, String path) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Clientes");

            String[] columns = {"Indústria", "CNPJ", "Telefone", "Vínculo", "Status"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

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

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(path)) {
                workbook.write(fileOut);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar Excel de clientes", e);
        }
    }
}