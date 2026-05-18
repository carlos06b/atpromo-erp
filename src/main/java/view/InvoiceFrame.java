package view;

import controller.ClientController;
import controller.InvoiceController;
import model.Client;
import model.InvoiceView;

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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class InvoiceFrame extends JFrame {

    private final InvoiceController invoiceController = new InvoiceController();
    private final ClientController clientController = new ClientController();

    private final DateTimeFormatter BR_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Locale BR_LOCALE = new Locale("pt", "BR");
    private final NumberFormat MONEY_FORMAT = NumberFormat.getCurrencyInstance(BR_LOCALE);

    private final Color ORANGE = new Color(255, 102, 0);
    private final Color BLACK = new Color(18, 18, 18);
    private final Color WHITE = Color.WHITE;
    private final Color LIGHT_GRAY = new Color(245, 245, 245);

    private JTextField txtStartDate;
    private JTextField txtEndDate;

    private JComboBox<String> cbStatusFilter;
    private JComboBox<String> cbCompanyFilter;

    private JTable table;
    private DefaultTableModel tableModel;

    private JLabel lblPending;
    private JLabel lblIssued;
    private JLabel lblPaid;
    private JLabel lblCanceled;

    public InvoiceFrame() {
        MONEY_FORMAT.setMinimumFractionDigits(2);
        MONEY_FORMAT.setMaximumFractionDigits(2);

        setTitle("Sistema At Promo - Faturamento");
        setSize(1120, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(null);
        mainPanel.setBackground(LIGHT_GRAY);
        add(mainPanel);

        createHeader(mainPanel);
        createFilterPanel(mainPanel);
        createDashboard(mainPanel);
        createTable(mainPanel);
        createActionButtons(mainPanel);

        loadInvoices();

        setVisible(true);
    }

    private void createHeader(JPanel panel) {
        JPanel header = new JPanel(null);
        header.setBackground(BLACK);
        header.setBounds(0, 0, 1120, 95);
        panel.add(header);

        JLabel title = new JLabel("Faturamento");
        title.setForeground(WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setBounds(30, 18, 400, 35);
        header.add(title);

        JLabel subtitle = new JLabel("Controle de cobranças pendentes, faturadas, recebidas e canceladas");
        subtitle.setForeground(new Color(210, 210, 210));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setBounds(32, 55, 700, 25);
        header.add(subtitle);

        JPanel orangeLine = new JPanel();
        orangeLine.setBackground(ORANGE);
        orangeLine.setBounds(30, 85, 1040, 4);
        header.add(orangeLine);
    }

    private void createFilterPanel(JPanel panel) {
        JPanel filterPanel = new JPanel(null);
        filterPanel.setBackground(WHITE);
        filterPanel.setBounds(30, 120, 1040, 95);
        filterPanel.setBorder(BorderFactory.createLineBorder(new Color(225, 225, 225)));
        panel.add(filterPanel);

        JLabel lblStart = new JLabel("Data inicial");
        lblStart.setBounds(20, 12, 120, 20);
        filterPanel.add(lblStart);

        txtStartDate = new JTextField(LocalDate.now().withDayOfMonth(1).format(BR_FORMAT));
        txtStartDate.setBounds(20, 38, 120, 32);
        filterPanel.add(txtStartDate);

        JLabel lblEnd = new JLabel("Data final");
        lblEnd.setBounds(155, 12, 120, 20);
        filterPanel.add(lblEnd);

        txtEndDate = new JTextField(LocalDate.now().format(BR_FORMAT));
        txtEndDate.setBounds(155, 38, 120, 32);
        filterPanel.add(txtEndDate);

        JLabel lblStatus = new JLabel("Status");
        lblStatus.setBounds(295, 12, 120, 20);
        filterPanel.add(lblStatus);

        cbStatusFilter = new JComboBox<>(new String[]{"Todos", "PENDENTE", "FATURADO", "PAGO", "CANCELADO"});
        cbStatusFilter.setBounds(295, 38, 140, 32);
        filterPanel.add(cbStatusFilter);

        JLabel lblCompany = new JLabel("Vínculo");
        lblCompany.setBounds(455, 12, 120, 20);
        filterPanel.add(lblCompany);

        cbCompanyFilter = new JComboBox<>(new String[]{"Todos", "AT", "TEJO"});
        cbCompanyFilter.setBounds(455, 38, 120, 32);
        filterPanel.add(cbCompanyFilter);

        JButton btnFilter = createDarkButton("Filtrar");
        btnFilter.setBounds(605, 38, 100, 32);
        btnFilter.addActionListener(e -> applyFilters());
        filterPanel.add(btnFilter);

        JButton btnRefresh = createSecondaryButton("Atualizar");
        btnRefresh.setBounds(715, 38, 110, 32);
        btnRefresh.addActionListener(e -> resetFilters());
        filterPanel.add(btnRefresh);

        JButton btnNew = createPrimaryButton("Novo faturamento");
        btnNew.setBounds(840, 38, 170, 32);
        btnNew.addActionListener(e -> openRegisterDialog());
        filterPanel.add(btnNew);
    }

    private void createDashboard(JPanel panel) {
        JPanel dashboard = new JPanel(null);
        dashboard.setBackground(WHITE);
        dashboard.setBounds(30, 230, 1040, 78);
        dashboard.setBorder(BorderFactory.createLineBorder(new Color(225, 225, 225)));
        panel.add(dashboard);

        lblPending = createDashboardLabel("Pendente: R$ 0,00", 25);
        lblIssued = createDashboardLabel("Faturado: R$ 0,00", 285);
        lblPaid = createDashboardLabel("Recebido: R$ 0,00", 545);
        lblCanceled = createDashboardLabel("Cancelado: 0 registros", 805);

        dashboard.add(lblPending);
        dashboard.add(lblIssued);
        dashboard.add(lblPaid);
        dashboard.add(lblCanceled);
    }

    private JLabel createDashboardLabel(String text, int x) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(BLACK);
        label.setBounds(x, 25, 230, 30);
        return label;
    }

    private void createTable(JPanel panel) {
        tableModel = new DefaultTableModel(
                new Object[]{
                        "ID", "Indústria", "Vínculo", "Valor", "Descrição",
                        "Previsto", "Faturado em", "Recebido em", "Status"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(230, 230, 230));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

                String status = table.getValueAt(row, 8).toString();

                if (isSelected) {
                    component.setBackground(new Color(210, 230, 255));
                    component.setForeground(Color.BLACK);
                    return component;
                }

                switch (status) {
                    case "PENDENTE" -> component.setBackground(new Color(255, 248, 220));
                    case "FATURADO" -> component.setBackground(new Color(255, 235, 205));
                    case "PAGO" -> component.setBackground(new Color(220, 245, 220));
                    case "CANCELADO" -> component.setBackground(new Color(245, 220, 220));
                    default -> component.setBackground(Color.WHITE);
                }

                component.setForeground(Color.BLACK);
                return component;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setBackground(BLACK);
        header.setForeground(WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(30, 325, 1040, 310);
        panel.add(scrollPane);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getColumnModel().getColumn(1).setPreferredWidth(240);
        table.getColumnModel().getColumn(2).setPreferredWidth(70);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(210);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);
    }

    private void createActionButtons(JPanel panel) {
        JButton btnCancel = createDangerButton("Cancelar");
        btnCancel.setBounds(200, 655, 150, 38);
        btnCancel.addActionListener(e -> cancelInvoice());
        panel.add(btnCancel);

        JButton btnIssue = createPrimaryButton("Faturar");
        btnIssue.setBounds(360, 655, 150, 38);
        btnIssue.addActionListener(e -> markAsIssued());
        panel.add(btnIssue);

        JButton btnPaid = createPrimaryButton("Receber");
        btnPaid.setBounds(520, 655, 150, 38);
        btnPaid.addActionListener(e -> markAsPaid());
        panel.add(btnPaid);

        JButton btnExcel = createPrimaryButton("Exportar Excel");
        btnExcel.setBounds(680, 655, 180, 38);
        btnExcel.addActionListener(e -> exportExcel());
        panel.add(btnExcel);

        JButton btnClose = createDarkButton("Fechar");
        btnClose.setBounds(880, 655, 150, 38);
        btnClose.addActionListener(e -> dispose());
        panel.add(btnClose);
    }

    private void openRegisterDialog() {
        List<Client> clients = clientController.listActiveClients();

        if (clients.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma indústria ativa cadastrada.");
            return;
        }

        JComboBox<ClientComboItem> cbClient = new JComboBox<>();

        for (Client client : clients) {
            cbClient.addItem(new ClientComboItem(client.getId(), client.getName(), client.getCompanyLink()));
        }

        JTextField txtAmount = new JTextField();
        JTextField txtDescription = new JTextField();
        JTextField txtDueDate = new JTextField(LocalDate.now().format(BR_FORMAT));

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Indústria:"));
        panel.add(cbClient);
        panel.add(new JLabel("Valor (ex: 1.234,56):"));
        panel.add(txtAmount);
        panel.add(new JLabel("Descrição:"));
        panel.add(txtDescription);
        panel.add(new JLabel("Data prevista (dd/MM/aaaa):"));
        panel.add(txtDueDate);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Novo Faturamento",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                ClientComboItem selectedClient = (ClientComboItem) cbClient.getSelectedItem();

                if (selectedClient == null) {
                    throw new RuntimeException("Selecione uma indústria.");
                }

                BigDecimal amount = parseMoney(txtAmount.getText());
                LocalDate dueDate = parseBrazilianDate(txtDueDate.getText());

                invoiceController.createPendingInvoice(
                        selectedClient.getId(),
                        amount,
                        txtDescription.getText(),
                        dueDate
                );

                JOptionPane.showMessageDialog(this, "Faturamento pendente criado com sucesso.");
                loadInvoices();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadInvoices() {
        try {
            LocalDate start = parseBrazilianDate(txtStartDate.getText());
            LocalDate end = parseBrazilianDate(txtEndDate.getText());

            fillTable(invoiceController.listByPeriod(start, end));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar faturamentos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilters() {
        try {
            LocalDate start = parseBrazilianDate(txtStartDate.getText());
            LocalDate end = parseBrazilianDate(txtEndDate.getText());

            String status = cbStatusFilter.getSelectedItem().toString();
            String company = cbCompanyFilter.getSelectedItem().toString();

            fillTable(invoiceController.listByFilters(start, end, status, company));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao filtrar faturamentos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFilters() {
        txtStartDate.setText(LocalDate.now().withDayOfMonth(1).format(BR_FORMAT));
        txtEndDate.setText(LocalDate.now().format(BR_FORMAT));
        cbStatusFilter.setSelectedIndex(0);
        cbCompanyFilter.setSelectedIndex(0);
        loadInvoices();
    }

    private void markAsIssued() {
        int id = getSelectedInvoiceId();

        if (id == -1) {
            return;
        }

        LocalDate issueDate = askDate(
                "Data de faturamento",
                "Informe a data de faturamento:",
                LocalDate.now()
        );

        if (issueDate == null) {
            return;
        }

        try {
            invoiceController.markAsIssued(id, issueDate);
            JOptionPane.showMessageDialog(this, "Faturamento marcado como faturado.");
            loadInvoices();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markAsPaid() {
        int id = getSelectedInvoiceId();

        if (id == -1) {
            return;
        }

        LocalDate paymentDate = askDate(
                "Data de recebimento",
                "Informe a data de recebimento:",
                LocalDate.now()
        );

        if (paymentDate == null) {
            return;
        }

        try {
            invoiceController.markAsPaid(id, paymentDate);
            JOptionPane.showMessageDialog(this, "Faturamento marcado como recebido.");
            loadInvoices();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelInvoice() {
        int id = getSelectedInvoiceId();

        if (id == -1) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deseja cancelar este faturamento?",
                "Confirmação",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                invoiceController.cancelInvoice(id);
                JOptionPane.showMessageDialog(this, "Faturamento cancelado.");
                loadInvoices();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int getSelectedInvoiceId() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um faturamento na tabela.");
            return -1;
        }

        return (int) tableModel.getValueAt(row, 0);
    }

    private void fillTable(List<InvoiceView> invoices) {
        tableModel.setRowCount(0);

        for (InvoiceView invoice : invoices) {
            tableModel.addRow(new Object[]{
                    invoice.getId(),
                    invoice.getClientName(),
                    invoice.getCompanyLink(),
                    formatMoney(invoice.getAmount()),
                    invoice.getDescription(),
                    formatDate(invoice.getDueDate()),
                    formatDate(invoice.getIssueDate()),
                    formatDate(invoice.getPaymentDate()),
                    invoice.getStatus()
            });
        }

        updateDashboard(invoices);
    }

    private void updateDashboard(List<InvoiceView> invoices) {
        BigDecimal pending = BigDecimal.ZERO;
        BigDecimal issued = BigDecimal.ZERO;
        BigDecimal paid = BigDecimal.ZERO;
        int canceledCount = 0;

        for (InvoiceView invoice : invoices) {
            if (invoice.getAmount() == null || invoice.getStatus() == null) {
                continue;
            }

            switch (invoice.getStatus()) {
                case "PENDENTE" -> pending = pending.add(invoice.getAmount());
                case "FATURADO" -> issued = issued.add(invoice.getAmount());
                case "PAGO" -> paid = paid.add(invoice.getAmount());
                case "CANCELADO" -> canceledCount++;
            }
        }

        lblPending.setText("Pendente: " + formatMoney(pending));
        lblIssued.setText("Faturado: " + formatMoney(issued));
        lblPaid.setText("Recebido: " + formatMoney(paid));
        lblCanceled.setText("Cancelado: " + canceledCount + " registros");
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return MONEY_FORMAT.format(BigDecimal.ZERO);
        }

        return MONEY_FORMAT.format(value.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal parseMoney(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Valor inválido. Use o formato 1.234,56.");
        }

        String cleaned = value.trim()
                .replace("R$", "")
                .replace(" ", "")
                .replace("\u00A0", "");

        if (cleaned.contains(",")) {
            cleaned = cleaned.replace(".", "").replace(",", ".");
        } else if (cleaned.contains(".")) {
            int dotCount = cleaned.length() - cleaned.replace(".", "").length();
            int lastDot = cleaned.lastIndexOf(".");
            int decimals = cleaned.length() - lastDot - 1;

            if (dotCount > 1 || decimals != 2) {
                cleaned = cleaned.replace(".", "");
            }
        }

        try {
            BigDecimal amount = new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("O valor do faturamento deve ser maior que zero.");
            }

            return amount;

        } catch (NumberFormatException e) {
            throw new RuntimeException("Valor inválido. Use o formato 1.234,56.");
        }
    }

    private LocalDate askDate(String title, String labelText, LocalDate initialDate) {
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy"));
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateSpinner.setValue(java.util.Date.from(
                initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        ));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel(labelText), BorderLayout.NORTH);
        panel.add(dateSpinner, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();

        return selectedDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "-";
        }

        return date.format(BR_FORMAT);
    }

    private LocalDate parseBrazilianDate(String date) {
        try {
            return LocalDate.parse(date.trim(), BR_FORMAT);
        } catch (Exception e) {
            throw new RuntimeException("Data inválida. Use o formato dd/MM/aaaa.");
        }
    }

    private void exportExcel() {
        try {
            LocalDate start = parseBrazilianDate(txtStartDate.getText());
            LocalDate end = parseBrazilianDate(txtEndDate.getText());

            String status = cbStatusFilter.getSelectedItem().toString();
            String company = cbCompanyFilter.getSelectedItem().toString();

            List<InvoiceView> invoices = invoiceController.listByFilters(start, end, status, company);

            if (invoices.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhum dado para exportar.");
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("faturamento.xlsx"));

            int option = chooser.showSaveDialog(this);

            if (option == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();

                if (!path.toLowerCase().endsWith(".xlsx")) {
                    path += ".xlsx";
                }

                util.ExcelGenerator.generateInvoices(invoices, path);

                JOptionPane.showMessageDialog(this, "Excel gerado com sucesso!");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ORANGE);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createDarkButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(BLACK);
        button.setForeground(WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(180, 40, 40));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(WHITE);
        button.setForeground(BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static class ClientComboItem {
        private final int id;
        private final String name;
        private final String companyLink;

        public ClientComboItem(int id, String name, String companyLink) {
            this.id = id;
            this.name = name;
            this.companyLink = companyLink;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return name + " - " + companyLink;
        }
    }
}