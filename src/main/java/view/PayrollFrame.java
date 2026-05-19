package view;

import controller.PayrollController;
import model.PayrollLine;
import model.PromoterPaymentData;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PayrollFrame extends JFrame {

    private final PayrollController payrollController = new PayrollController();

    private final Locale BR_LOCALE = new Locale("pt", "BR");
    private final NumberFormat MONEY_FORMAT = NumberFormat.getCurrencyInstance(BR_LOCALE);

    private final Color ORANGE = new Color(255, 102, 0);
    private final Color BLACK = new Color(18, 18, 18);
    private final Color WHITE = Color.WHITE;
    private final Color LIGHT_GRAY = new Color(245, 245, 245);
    private final Color BORDER_GRAY = new Color(220, 220, 220);
    private final Color TEXT_GRAY = new Color(90, 90, 90);

    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private JComboBox<String> typeBox;

    private JTable payrollTable;
    private DefaultTableModel tableModel;

    private JLabel lblTotalBase;
    private JLabel lblTotalDiscounts;
    private JLabel lblTotalNet;
    private JLabel lblPromoters;

    private List<PayrollLine> currentLines = new ArrayList<>();
    private LocalDate currentStartDate;
    private LocalDate currentEndDate;
    private String currentPromoterType = "TODOS";

    public PayrollFrame() {
        setTitle("Sistema At Promo - Folha de Pagamento");
        setSize(1120, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        generatePayroll();

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(null);
        header.setPreferredSize(new Dimension(1120, 95));
        header.setBackground(BLACK);

        JLabel title = new JLabel("Folha de Pagamento");
        title.setForeground(WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setBounds(30, 18, 430, 36);
        header.add(title);

        JLabel subtitle = new JLabel("Conferência profissional de salário base, descontos e líquido a pagar por promotor.");
        subtitle.setForeground(new Color(210, 210, 210));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setBounds(32, 56, 760, 22);
        header.add(subtitle);

        JPanel line = new JPanel();
        line.setBackground(ORANGE);
        line.setBounds(30, 84, 1040, 4);
        header.add(line);

        return header;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(null);
        main.setBackground(LIGHT_GRAY);

        createFilterPanel(main);
        createDashboard(main);
        createTable(main);
        createActionButtons(main);

        return main;
    }

    private void createFilterPanel(JPanel panel) {
        JPanel filterPanel = new JPanel(null);
        filterPanel.setBackground(WHITE);
        filterPanel.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        filterPanel.setBounds(30, 25, 1040, 105);
        panel.add(filterPanel);

        JLabel filterTitle = createSectionTitle("Filtros da folha");
        filterTitle.setBounds(20, 12, 220, 24);
        filterPanel.add(filterTitle);

        JLabel startLabel = createFieldLabel("Data início");
        startLabel.setBounds(20, 45, 100, 20);
        filterPanel.add(startLabel);

        startSpinner = new JSpinner(new SpinnerDateModel());
        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "dd/MM/yyyy"));
        startSpinner.setValue(toDate(LocalDate.now().withDayOfMonth(1)));
        startSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        startSpinner.setBounds(20, 67, 135, 32);
        filterPanel.add(startSpinner);

        JLabel endLabel = createFieldLabel("Data fim");
        endLabel.setBounds(175, 45, 100, 20);
        filterPanel.add(endLabel);

        endSpinner = new JSpinner(new SpinnerDateModel());
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "dd/MM/yyyy"));
        endSpinner.setValue(toDate(LocalDate.now()));
        endSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        endSpinner.setBounds(175, 67, 135, 32);
        filterPanel.add(endSpinner);

        JLabel typeLabel = createFieldLabel("Tipo de promotor");
        typeLabel.setBounds(330, 45, 140, 20);
        filterPanel.add(typeLabel);

        typeBox = new JComboBox<>(new String[]{"TODOS", "CLT", "MEI", "FERISTA"});
        typeBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeBox.setBounds(330, 67, 145, 32);
        filterPanel.add(typeBox);

        JButton btnGenerate = createPrimaryButton("Gerar Folha");
        btnGenerate.setBounds(510, 64, 135, 36);
        btnGenerate.addActionListener(e -> generatePayroll());
        filterPanel.add(btnGenerate);

        JButton btnClear = createDarkButton("Limpar");
        btnClear.setBounds(655, 64, 95, 36);
        btnClear.addActionListener(e -> clearPayroll());
        filterPanel.add(btnClear);

        JButton btnExportReport = createDarkButton("Exportar Relatório");
        btnExportReport.setBounds(765, 64, 160, 36);
        btnExportReport.addActionListener(e -> exportPayrollReport());
        filterPanel.add(btnExportReport);
    }

    private void createDashboard(JPanel panel) {
        JPanel dashboard = new JPanel(null);
        dashboard.setBackground(WHITE);
        dashboard.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        dashboard.setBounds(30, 145, 1040, 82);
        panel.add(dashboard);

        lblTotalBase = createDashboardLabel("Base: R$ 0,00", 25);
        lblTotalDiscounts = createDashboardLabel("Descontos: R$ 0,00", 285);
        lblTotalNet = createDashboardLabel("Líquido: R$ 0,00", 545);
        lblPromoters = createDashboardLabel("Promotores: 0", 805);

        dashboard.add(lblTotalBase);
        dashboard.add(lblTotalDiscounts);
        dashboard.add(lblTotalNet);
        dashboard.add(lblPromoters);
    }

    private JLabel createDashboardLabel(String text, int x) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(BLACK);
        label.setBounds(x, 25, 235, 30);
        return label;
    }

    private void createTable(JPanel panel) {
        tableModel = new DefaultTableModel(
                new Object[]{
                        "ID", "Promotor", "Tipo", "Salário/Base",
                        "Descontos", "Líquido", "Status", "Observação"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        payrollTable = new JTable(tableModel);
        payrollTable.setRowHeight(31);
        payrollTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        payrollTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        payrollTable.setGridColor(new Color(235, 235, 235));
        payrollTable.setShowVerticalLines(false);

        payrollTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column
            ) {
                Component component = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );

                if (component instanceof JLabel label) {
                    label.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

                    if (column == 3 || column == 4 || column == 5) {
                        label.setHorizontalAlignment(SwingConstants.RIGHT);
                    } else {
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                }

                String status = table.getValueAt(row, 6).toString();

                if (isSelected) {
                    component.setBackground(new Color(210, 230, 255));
                    component.setForeground(Color.BLACK);
                    return component;
                }

                if ("ATENÇÃO".equalsIgnoreCase(status)) {
                    component.setBackground(new Color(255, 235, 230));
                } else {
                    component.setBackground(Color.WHITE);
                }

                component.setForeground(Color.BLACK);
                return component;
            }
        });

        JTableHeader header = payrollTable.getTableHeader();
        header.setBackground(BLACK);
        header.setForeground(WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(payrollTable);
        scrollPane.setBounds(30, 245, 1040, 360);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        scrollPane.getViewport().setBackground(WHITE);
        panel.add(scrollPane);

        payrollTable.getColumnModel().getColumn(0).setMinWidth(0);
        payrollTable.getColumnModel().getColumn(0).setMaxWidth(0);
        payrollTable.getColumnModel().getColumn(0).setWidth(0);

        payrollTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        payrollTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        payrollTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        payrollTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        payrollTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        payrollTable.getColumnModel().getColumn(6).setPreferredWidth(100);
        payrollTable.getColumnModel().getColumn(7).setPreferredWidth(270);
    }

    private void createActionButtons(JPanel panel) {
        JButton btnRefresh = createPrimaryButton("Atualizar");
        btnRefresh.setBounds(650, 625, 125, 38);
        btnRefresh.addActionListener(e -> generatePayroll());
        panel.add(btnRefresh);

        JButton btnPixMei = createPrimaryButton("Gerar PIX MEI");
        btnPixMei.setBounds(790, 625, 140, 38);
        btnPixMei.addActionListener(e -> exportMeiPixBatch());
        panel.add(btnPixMei);

        JButton btnClose = createDarkButton("Fechar");
        btnClose.setBounds(945, 625, 125, 38);
        btnClose.addActionListener(e -> dispose());
        panel.add(btnClose);
    }

    private void generatePayroll() {
        try {
            LocalDate start = getStartDate();
            LocalDate end = getEndDate();

            if (start.isAfter(end)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Data inicial não pode ser maior que a final.",
                        "Período inválido",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String type = typeBox.getSelectedItem().toString();

            currentLines = payrollController.generatePayrollLines(start, end, type);
            currentStartDate = start;
            currentEndDate = end;
            currentPromoterType = type;

            fillTable(currentLines);
            updateDashboard(currentLines);

            if (currentLines.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Nenhum promotor encontrado para esse filtro.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillTable(List<PayrollLine> lines) {
        tableModel.setRowCount(0);

        for (PayrollLine line : lines) {
            tableModel.addRow(new Object[]{
                    line.getPromoterId(),
                    line.getPromoterName(),
                    line.getPromoterType(),
                    formatMoney(line.getBaseSalary()),
                    formatMoney(line.getDiscounts()),
                    formatMoney(line.getNetAmount()),
                    line.getStatus(),
                    line.getObservation()
            });
        }
    }

    private void updateDashboard(List<PayrollLine> lines) {
        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        int attentionCount = 0;

        for (PayrollLine line : lines) {
            totalBase = totalBase.add(nullToZero(line.getBaseSalary()));
            totalDiscounts = totalDiscounts.add(nullToZero(line.getDiscounts()));
            totalNet = totalNet.add(nullToZero(line.getNetAmount()));

            if ("ATENÇÃO".equalsIgnoreCase(line.getStatus())) {
                attentionCount++;
            }
        }

        lblTotalBase.setText("Base: " + formatMoney(totalBase));
        lblTotalDiscounts.setText("Descontos: " + formatMoney(totalDiscounts));
        lblTotalNet.setText("Líquido: " + formatMoney(totalNet));

        if (attentionCount > 0) {
            lblPromoters.setText("Promotores: " + lines.size() + " | Atenção: " + attentionCount);
            lblPromoters.setForeground(new Color(180, 40, 40));
        } else {
            lblPromoters.setText("Promotores: " + lines.size());
            lblPromoters.setForeground(BLACK);
        }
    }

    private void clearPayroll() {
        currentLines = new ArrayList<>();
        currentStartDate = null;
        currentEndDate = null;
        currentPromoterType = "TODOS";
        tableModel.setRowCount(0);
        updateDashboard(currentLines);
    }

    private void exportPayrollReport() {
        try {
            if (currentLines == null || currentLines.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Gere a folha antes de exportar o relatório.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String path = util.FileSaveDialog.chooseXlsxPath(this, "folha_pagamento.xlsx");

            if (path == null) {
                return;
            }

            util.ExcelGenerator.generatePayrollReport(
                    currentLines,
                    currentStartDate,
                    currentEndDate,
                    currentPromoterType,
                    path
            );

            JOptionPane.showMessageDialog(this, "Relatório da folha exportado com sucesso.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportMeiPixBatch() {
        try {
            JSpinner paymentDateSpinner = new JSpinner(new SpinnerDateModel());
            paymentDateSpinner.setEditor(new JSpinner.DateEditor(paymentDateSpinner, "dd/MM/yyyy"));
            paymentDateSpinner.setValue(toDate(LocalDate.now()));

            int result = JOptionPane.showConfirmDialog(
                    this,
                    paymentDateSpinner,
                    "Data de pagamento do PIX lote MEI",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            LocalDate paymentDate = ((Date) paymentDateSpinner.getValue())
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            List<PromoterPaymentData> payments = payrollController.getMeiPixBatch(paymentDate);

            if (payments.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Nenhum MEI ativo com chave PIX encontrado para gerar o PIX lote.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            String path = util.FileSaveDialog.chooseXlsxPath(this, "pix_lote_mei.xlsx");

            if (path == null) {
                return;
            }

            util.ExcelGenerator.generatePixBatch(payments, path);

            JOptionPane.showMessageDialog(this, "PIX lote MEI gerado com sucesso.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate getStartDate() {
        Date date = (Date) startSpinner.getValue();

        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private LocalDate getEndDate() {
        Date date = (Date) endSpinner.getValue();

        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "R$ 0,00";
        }

        return MONEY_FORMAT.format(value.setScale(2, RoundingMode.HALF_UP))
                .replace('\u00A0', ' ');
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(BLACK);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        return label;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_GRAY);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return label;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(ORANGE);
        button.setForeground(WHITE);
        button.setBorderPainted(false);
        return button;
    }

    private JButton createDarkButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(BLACK);
        button.setForeground(WHITE);
        button.setBorderPainted(false);
        return button;
    }

    private JButton baseButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}