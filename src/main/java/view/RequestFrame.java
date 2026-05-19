package view;

import controller.PromoterController;
import controller.RequestController;
import model.Promoter;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.math.RoundingMode;

public class RequestFrame extends JFrame {

    private final RequestController requestController;
    private final PromoterController promoterController;
    private final User loggedUser;

    private JTable table;
    private DefaultTableModel tableModel;

    private final Color ORANGE = new Color(255, 102, 0);
    private final Color BLACK = new Color(18, 18, 18);
    private final Color WHITE = Color.WHITE;
    private final Color LIGHT_GRAY = new Color(245, 245, 245);
    private final Color BORDER_GRAY = new Color(220, 220, 220);
    private final Color TEXT_GRAY = new Color(90, 90, 90);
    private final Color RED = new Color(190, 40, 40);

    private final Locale brLocale = new Locale("pt", "BR");
    private final NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance(brLocale);

    public RequestFrame(User loggedUser) {
        this.loggedUser = loggedUser;
        this.requestController = new RequestController();
        this.promoterController = new PromoterController();

        setTitle("Sistema At Promo - Solicitações");
        setSize(1250, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadPending();

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(null);
        header.setPreferredSize(new Dimension(1250, 95));
        header.setBackground(BLACK);

        JLabel title = new JLabel("Gerenciamento de Solicitações");
        title.setForeground(WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setBounds(30, 20, 450, 32);
        header.add(title);

        JLabel subtitle = new JLabel("Acompanhe solicitações do RH, aprovações financeiras e dados para pagamento.");
        subtitle.setForeground(new Color(210, 210, 210));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setBounds(32, 55, 650, 22);
        header.add(subtitle);

        JLabel userLabel = new JLabel(loggedUser.getName() + " • " + loggedUser.getJobTittle());
        userLabel.setForeground(ORANGE);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        userLabel.setBounds(835, 30, 360, 30);
        header.add(userLabel);

        return header;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(null);
        main.setBackground(LIGHT_GRAY);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        actionPanel.setBackground(WHITE);
        actionPanel.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        actionPanel.setBounds(25, 20, 1185, 65);

        JButton btnListAll = createSecondaryButton("Todas");
        JButton btnPending = createPrimaryButton("Pendentes");
        JButton btnPeriod = createSecondaryButton("Período");
        JButton btnCreate = createPrimaryButton("Criar");
        JButton btnApprove = createPrimaryButton("Aprovar");
        JButton btnReject = createDangerButton("Rejeitar");
        JButton btnDetails = createDarkButton("Detalhes");

        actionPanel.add(btnListAll);
        actionPanel.add(btnPending);
        actionPanel.add(btnPeriod);
        actionPanel.add(btnCreate);
        actionPanel.add(btnApprove);
        actionPanel.add(btnReject);
        actionPanel.add(btnDetails);

        main.add(actionPanel);

        JButton btnExportPending = createDarkButton("Exportar Pendentes");
        btnExportPending.setBounds(1025, 92, 185, 36);
        main.add(btnExportPending);

        JButton btnExportPixBatch = createPrimaryButton("Exportar Pix Lote");
        btnExportPixBatch.setBounds(815, 92, 190, 36);
        main.add(btnExportPixBatch);

        String[] columns = {
                "ID", "Promotor", "Tipo", "Valor", "Mensagem", "Status", "Data", "MensagemCompleta"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(BLACK);
        table.getTableHeader().setForeground(WHITE);
        table.setSelectionBackground(new Color(255, 225, 205));
        table.setSelectionForeground(BLACK);
        table.setGridColor(new Color(235, 235, 235));

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getColumnModel().getColumn(4).setPreferredWidth(270);

        table.getColumnModel().getColumn(7).setMinWidth(0);
        table.getColumnModel().getColumn(7).setMaxWidth(0);
        table.getColumnModel().getColumn(7).setWidth(0);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showRequestDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(25, 140, 1185, 355);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        main.add(scrollPane);

        btnListAll.addActionListener(e -> loadAll());
        btnPending.addActionListener(e -> loadPending());
        btnPeriod.addActionListener(e -> loadByPeriod());
        btnCreate.addActionListener(e -> openCreateDialog());
        btnApprove.addActionListener(e -> approveSelected());
        btnReject.addActionListener(e -> rejectSelected());
        btnDetails.addActionListener(e -> showRequestDetails());
        btnExportPending.addActionListener(e -> exportPendingRequests());
        btnExportPixBatch.addActionListener(e -> exportPixBatch());

        if (loggedUser.getJobTittle().equalsIgnoreCase("RH")) {
            btnApprove.setEnabled(false);
            btnReject.setEnabled(false);
        }

        if (loggedUser.getJobTittle().equalsIgnoreCase("FINANCEIRO")) {
            btnCreate.setEnabled(false);
        }

        return main;
    }

    private void loadAll() {
        fillTable(requestController.getAllWithPromoterName());
    }

    private void loadPending() {
        fillTable(requestController.getPendingWithPromoterName());
    }

    private void loadByPeriod() {
        try {
            JSpinner startSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner endSpinner = new JSpinner(new SpinnerDateModel());

            startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "dd/MM/yyyy"));
            endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "dd/MM/yyyy"));

            Object[] fields = {
                    "Data início:", startSpinner,
                    "Data fim:", endSpinner
            };

            int option = JOptionPane.showConfirmDialog(
                    this,
                    fields,
                    "Selecionar Período",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (option != JOptionPane.OK_OPTION) return;

            java.util.Date startDate = (java.util.Date) startSpinner.getValue();
            java.util.Date endDate = (java.util.Date) endSpinner.getValue();

            LocalDate start = startDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            LocalDate end = endDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (start.isAfter(end)) {
                showWarning("Data inicial não pode ser maior que a final.");
                return;
            }

            fillTable(
                    requestController.getByPeriodWithPromoterName(
                            start.atStartOfDay(),
                            end.atTime(23, 59, 59)
                    )
            );

        } catch (Exception e) {
            showError("Erro ao selecionar período.");
        }
    }

    private void openCreateDialog() {
        JTextField searchField = new JTextField();
        DefaultListModel<PromoterItem> listModel = new DefaultListModel<>();
        JList<PromoterItem> promoterList = new JList<>(listModel);

        promoterList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        promoterList.setSelectionBackground(new Color(255, 225, 205));
        promoterList.setSelectionForeground(BLACK);

        JScrollPane listScroll = new JScrollPane(promoterList);
        listScroll.setPreferredSize(new Dimension(330, 100));

        searchField.addCaretListener(e -> {
            String text = searchField.getText().trim();
            listModel.clear();

            if (text.length() < 2) return;

            List<Promoter> promoters = promoterController.searchByName(text);

            for (Promoter p : promoters) {
                listModel.addElement(new PromoterItem(p.getId(), p.getName()));
            }
        });

        JComboBox<String> typeBox = new JComboBox<>(new String[]{
                "Bonificação",
                "Ajuda de Custo",
                "Desconto",
                "ASO",
                "EPI"
        });

        JTextField amountField = new JTextField();

        JTextArea messageArea = new JTextArea(5, 25);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        Object[] fields = {
                "Buscar promotor pelo nome:", searchField,
                "Resultados:", listScroll,
                "Tipo:", typeBox,
                "Valor (ex: 1.234,56):", amountField,
                "Mensagem / PIX / Dados para pagamento:", new JScrollPane(messageArea)
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Criar Solicitação",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                PromoterItem selectedPromoter = promoterList.getSelectedValue();

                if (selectedPromoter == null) {
                    showWarning("Selecione um promotor na lista.");
                    return;
                }

                BigDecimal amount = parseMoney(amountField.getText());
                String selectedType = typeBox.getSelectedItem().toString();
                String type = convertTypeToDatabase(selectedType);
                String message = messageArea.getText().trim();

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    showWarning("O valor precisa ser maior que zero.");
                    return;
                }

                if (message.isBlank()) {
                    showWarning("Mensagem não pode ficar vazia.");
                    return;
                }

                requestController.createRequest(
                        loggedUser.getId(),
                        0,
                        selectedPromoter.getId(),
                        type,
                        amount,
                        message
                );

                showSuccess("Solicitação criada!");
                loadPending();

            } catch (Exception e) {
                showError(e.getMessage());
            }
        }
    }

    private void approveSelected() {
        int row = table.getSelectedRow();

        if (row == -1) {
            showWarning("Selecione uma solicitação.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deseja aprovar esta solicitação?",
                "Confirmar aprovação",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            requestController.approve(id, loggedUser.getId());
            showSuccess("Solicitação aprovada!");
            loadPending();
        }
    }

    private void rejectSelected() {
        int row = table.getSelectedRow();

        if (row == -1) {
            showWarning("Selecione uma solicitação.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        int id = (int) tableModel.getValueAt(modelRow, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deseja rejeitar esta solicitação?",
                "Confirmar rejeição",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            requestController.reject(id);
            showSuccess("Solicitação rejeitada!");
            loadPending();
        }
    }

    private void showRequestDetails() {
        int row = table.getSelectedRow();

        if (row == -1) {
            showWarning("Selecione uma solicitação.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);

        Object promoter = tableModel.getValueAt(modelRow, 1);
        Object type = tableModel.getValueAt(modelRow, 2);
        Object amount = tableModel.getValueAt(modelRow, 3);
        Object shortMessage = tableModel.getValueAt(modelRow, 4);
        Object status = tableModel.getValueAt(modelRow, 5);
        Object date = tableModel.getValueAt(modelRow, 6);
        Object fullMessage = tableModel.getValueAt(modelRow, 7);

        String messageToShow = fullMessage != null ? fullMessage.toString() : shortMessage.toString();

        String promoterPix = "Não informado";

        List<Promoter> promoters = promoterController.searchByName(promoter.toString());

        if (!promoters.isEmpty()) {
            Promoter promoterData = promoters.get(0);

            if (promoterData.getPix() != null && !promoterData.getPix().isBlank()) {
                promoterPix = promoterData.getPix();
            }
        }

        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Solicitação - " + type);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(BLACK);

        JLabel subtitle = new JLabel("Promotor: " + promoter + " • Status: " + status);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_GRAY);

        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(WHITE);
        header.add(title);
        header.add(subtitle);

        JPanel info = new JPanel(new GridLayout(0, 2, 12, 10));
        info.setBackground(WHITE);

        addDetail(info, "Promotor", promoter.toString());
        addDetail(info, "PIX", promoterPix);
        addDetail(info, "Tipo", type.toString());
        addDetail(info, "Valor", amount.toString());
        addDetail(info, "Status", status.toString());
        addDetail(info, "Data", date.toString());

        JTextArea messageArea = new JTextArea(messageToShow);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageArea.setBackground(WHITE);
        messageArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setPreferredSize(new Dimension(560, 160));
        messageScroll.setBorder(BorderFactory.createTitledBorder("Mensagem / Dados para pagamento"));

        panel.add(header, BorderLayout.NORTH);
        panel.add(info, BorderLayout.CENTER);
        panel.add(messageScroll, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(
                this,
                panel,
                "Detalhes da Solicitação",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private void addDetail(JPanel panel, String label, String value) {
        JPanel item = new JPanel(new BorderLayout(0, 3));
        item.setBackground(WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 12));
        title.setForeground(TEXT_GRAY);

        JLabel content = new JLabel(value);
        content.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        content.setForeground(BLACK);

        item.add(title, BorderLayout.NORTH);
        item.add(content, BorderLayout.CENTER);

        panel.add(item);
    }

    private void exportPendingRequests() {
        List<String> pendingRequests = requestController.getPendingWithPromoterName();

        if (pendingRequests.isEmpty()) {
            showWarning("Não existem solicitações pendentes para exportar.");
            return;
        }

        String path = util.FileSaveDialog.chooseXlsxPath(this, "solicitacoes_pendentes.xlsx");

        if (path == null) {
            return;
        }

        util.ExcelGenerator.generatePendingRequests(pendingRequests, path);

        showSuccess("Relatório de solicitações pendentes exportado com sucesso!");
    }

    private void exportPixBatch() {
        JSpinner paymentDateSpinner = new JSpinner(new SpinnerDateModel());
        paymentDateSpinner.setEditor(new JSpinner.DateEditor(paymentDateSpinner, "dd/MM/yyyy"));

        int dateOption = JOptionPane.showConfirmDialog(
                this,
                paymentDateSpinner,
                "Data do pagamento",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (dateOption != JOptionPane.OK_OPTION) {
            return;
        }

        java.util.Date selectedDate = (java.util.Date) paymentDateSpinner.getValue();

        LocalDate paymentDate = selectedDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        List<model.PromoterPaymentData> payments = requestController.getPendingPixBatch(paymentDate);

        if (payments.isEmpty()) {
            showWarning("Não há solicitações pendentes com PIX cadastrado.");
            return;
        }

        String path = util.FileSaveDialog.chooseXlsxPath(this, "pix_lote_solicitacoes.xlsx");

        if (path == null) {
            return;
        }

        util.ExcelGenerator.generatePixBatch(payments, path);

        showSuccess("Pix Lote exportado com sucesso!");
    }

    private void fillTable(List<String> lines) {
        tableModel.setRowCount(0);

        for (String line : lines) {
            addLineToTable(line);
        }
    }

    private void addLineToTable(String line) {
        try {
            String[] parts = line.split("\\|");

            int id = Integer.parseInt(parts[0].trim());
            String promoter = parts[1].replace("Promotor:", "").trim();
            String type = convertTypeToView(parts[2].trim());
            String amount = formatMoney(parseMoney(parts[3].trim()));
            String fullMessage = parts[4].trim();
            String shortMessage = shortenText(fullMessage, 45);
            String status = parts[5].trim();
            String date = formatDateTime(parts[6].trim());

            tableModel.addRow(new Object[]{
                    id,
                    promoter,
                    type,
                    amount,
                    shortMessage,
                    status,
                    date,
                    fullMessage
            });

        } catch (Exception e) {
            tableModel.addRow(new Object[]{
                    "-",
                    "-",
                    "-",
                    "-",
                    shortenText(line, 45),
                    "-",
                    "-",
                    line
            });
        }
    }

    private JButton createPrimaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(ORANGE);
        button.setForeground(WHITE);
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(WHITE);
        button.setForeground(BLACK);
        button.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        return button;
    }

    private JButton createDarkButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(BLACK);
        button.setForeground(WHITE);
        return button;
    }

    private JButton createDangerButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(RED);
        button.setForeground(WHITE);
        return button;
    }

    private JButton baseButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(125, 38));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private String convertTypeToDatabase(String type) {
        return switch (type) {
            case "Bonificação" -> "BONIFICACAO";
            case "Ajuda de Custo" -> "AJUDA_CUSTO";
            case "Desconto" -> "DESCONTO";
            case "ASO" -> "ASO";
            case "EPI" -> "EPI";
            default -> type.toUpperCase();
        };
    }

    private String convertTypeToView(String type) {
        return switch (type) {
            case "BONIFICACAO" -> "Bonificação";
            case "AJUDA_CUSTO" -> "Ajuda de Custo";
            case "DESCONTO" -> "Desconto";
            case "ASO" -> "ASO";
            case "EPI" -> "EPI";
            default -> type;
        };
    }

    private String shortenText(String text, int maxLength) {
        if (text == null) return "";

        if (text.length() <= maxLength) return text;

        return text.substring(0, maxLength) + "...";
    }

    private String formatDateTime(String dateTime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(dateTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return dt.format(formatter);
        } catch (Exception e) {
            return dateTime;
        }
    }

    private BigDecimal parseMoney(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe um valor.");
        }

        String normalized = value.trim()
                .replace("R$", "")
                .replace(" ", "")
                .replace("\u00A0", "");

        if (normalized.contains(",") && normalized.contains(".")) {
            if (normalized.lastIndexOf(",") > normalized.lastIndexOf(".")) {
                normalized = normalized.replace(".", "").replace(",", ".");
            } else {
                normalized = normalized.replace(",", "");
            }
        } else if (normalized.contains(",")) {
            normalized = normalized.replace(".", "").replace(",", ".");
        } else if (normalized.contains(".")) {
            int dotCount = normalized.length() - normalized.replace(".", "").length();
            int lastDot = normalized.lastIndexOf(".");
            int decimals = normalized.length() - lastDot - 1;

            if (dotCount > 1 || decimals == 3) {
                normalized = normalized.replace(".", "");
            }
        }

        BigDecimal amount = new BigDecimal(normalized).setScale(2, RoundingMode.HALF_UP);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor precisa ser maior que zero.");
        }

        return amount;
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return moneyFormatter.format(BigDecimal.ZERO);
        }

        return moneyFormatter.format(value.setScale(2, RoundingMode.HALF_UP));
    }

    private static class PromoterItem {
        private final int id;
        private final String name;

        public PromoterItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String toString() {
            return name;
        }
    }
}