package view;

import dao.FixedExpenseDAO;
import dao.FixedExpenseHistoryDAO;
import dao.VariableExpenseDAO;
import model.FixedExpense;
import model.FixedExpenseHistory;
import model.VariableExpense;
import org.jdesktop.swingx.JXDatePicker;
import java.util.Date;
import java.time.ZoneId;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExpenseFrame extends JFrame {

    private final FixedExpenseDAO fixedExpenseDAO = new FixedExpenseDAO();
    private final FixedExpenseHistoryDAO fixedExpenseHistoryDAO = new FixedExpenseHistoryDAO();
    private final VariableExpenseDAO variableExpenseDAO = new VariableExpenseDAO();

    private final DateTimeFormatter brFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Locale brLocale = new Locale("pt", "BR");
    private final NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance(brLocale);

    private final Color ORANGE = new Color(255, 102, 0);
    private final Color BLACK = new Color(18, 18, 18);
    private final Color WHITE = Color.WHITE;
    private final Color LIGHT_GRAY = new Color(245, 245, 245);
    private final Color BORDER_GRAY = new Color(220, 220, 220);
    private final Color TEXT_GRAY = new Color(90, 90, 90);
    private final Color RED = new Color(190, 40, 40);

    private JTable fixedExpenseTable;
    private JTable fixedExpenseHistoryTable;
    private JTable variableExpenseTable;

    private JTextField txtFixedName;
    private JTextField txtFixedAmount;
    private JTextField txtFixedDueDate;

    private JTextField txtHistoryStartDate;
    private JTextField txtHistoryEndDate;

    private JTextField txtVariableName;
    private JTextField txtVariableAmount;
    private JTextField txtVariableDate;
    private JTextField txtVariableDescription;
    private JTextField txtVariableStartDate;
    private JTextField txtVariableEndDate;

    public ExpenseFrame() {
        setTitle("Sistema At Promo - Controle de Despesas");
        setSize(1320, 920);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(null);
        header.setPreferredSize(new Dimension(1120, 95));
        header.setBackground(BLACK);

        JLabel title = new JLabel("Controle de Despesas");
        title.setForeground(WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setBounds(30, 20, 400, 34);
        header.add(title);

        JLabel subtitle = new JLabel("Cadastre, acompanhe e controle despesas fixas, mensais e variáveis da empresa.");
        subtitle.setForeground(new Color(210, 210, 210));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setBounds(32, 56, 760, 22);
        header.add(subtitle);

        JPanel line = new JPanel();
        line.setBackground(ORANGE);
        line.setBounds(30, 82, 185, 4);
        header.add(line);

        return header;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(LIGHT_GRAY);
        main.setBorder(BorderFactory.createEmptyBorder(24, 28, 28, 28));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(WHITE);
        tabs.setForeground(BLACK);

        tabs.addTab("Despesas Fixas", createFixedExpensePanel());
        tabs.addTab("Fixas do Mês", createFixedExpenseHistoryPanel());
        tabs.addTab("Despesas Variáveis", createVariableExpensePanel());

        main.add(tabs, BorderLayout.CENTER);

        return main;
    }

    private JPanel createFixedExpensePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel formPanel = createCardPanel();
        formPanel.setLayout(null);
        formPanel.setPreferredSize(new Dimension(1000, 125));

        JLabel title = createSectionTitle("Cadastrar despesa fixa");
        title.setBounds(18, 12, 300, 25);
        formPanel.add(title);

        JLabel nameLabel = createFieldLabel("Nome");
        nameLabel.setBounds(18, 48, 160, 22);
        formPanel.add(nameLabel);

        txtFixedName = createTextField();
        txtFixedName.setBounds(18, 72, 230, 34);
        formPanel.add(txtFixedName);

        JLabel amountLabel = createFieldLabel("Valor");
        amountLabel.setBounds(265, 48, 120, 22);
        formPanel.add(amountLabel);

        txtFixedAmount = createTextField();
        txtFixedAmount.setBounds(265, 72, 125, 34);
        formPanel.add(txtFixedAmount);

        JLabel dueDateLabel = createFieldLabel("Vencimento");
        dueDateLabel.setBounds(405, 48, 180, 22);
        formPanel.add(dueDateLabel);

        txtFixedDueDate = createTextField();
        txtFixedDueDate.setBounds(405, 72, 140, 34);
        txtFixedDueDate.setToolTipText("Use o formato dd/MM/yyyy");
        formPanel.add(txtFixedDueDate);

        JButton btnSave = createPrimaryButton("Cadastrar");
        btnSave.setBounds(565, 70, 115, 38);
        formPanel.add(btnSave);

        JButton btnEdit = createDarkButton("Editar");
        btnEdit.setBounds(690, 70, 95, 38);
        formPanel.add(btnEdit);

        JButton btnList = createDarkButton("Atualizar");
        btnList.setBounds(795, 70, 110, 38);
        formPanel.add(btnList);

        JButton btnMarkPaid = createPrimaryButton("Marcar Paga");
        btnMarkPaid.setBounds(915, 70, 125, 38);
        formPanel.add(btnMarkPaid);

        fixedExpenseTable = createStyledTable();
        JScrollPane scrollPane = createTableScroll(fixedExpenseTable);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setBackground(LIGHT_GRAY);

        JButton btnInactive = createDangerButton("Inativar Selecionada");
        btnInactive.setPreferredSize(new Dimension(180, 38));
        bottomPanel.add(btnInactive);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> saveFixedExpense());
        btnEdit.addActionListener(e -> editFixedExpense());
        btnList.addActionListener(e -> loadFixedExpenses());
        btnInactive.addActionListener(e -> deleteFixedExpense());
        btnMarkPaid.addActionListener(e -> markFixedExpenseAsPaid());

        loadFixedExpenses();

        return panel;
    }

    private JPanel createFixedExpenseHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel filterPanel = createCardPanel();
        filterPanel.setLayout(null);
        filterPanel.setPreferredSize(new Dimension(1000, 125));

        JLabel title = createSectionTitle("Despesas fixas lançadas no mês");
        title.setBounds(18, 12, 360, 25);
        filterPanel.add(title);

        JLabel startLabel = createFieldLabel("Data inicial");
        startLabel.setBounds(18, 48, 140, 22);
        filterPanel.add(startLabel);

        txtHistoryStartDate = createTextField();
        txtHistoryStartDate.setBounds(18, 72, 145, 34);
        filterPanel.add(txtHistoryStartDate);

        JLabel endLabel = createFieldLabel("Data final");
        endLabel.setBounds(185, 48, 140, 22);
        filterPanel.add(endLabel);

        txtHistoryEndDate = createTextField();
        txtHistoryEndDate.setBounds(185, 72, 145, 34);
        filterPanel.add(txtHistoryEndDate);

        JButton btnSearch = createDarkButton("Buscar");
        btnSearch.setBounds(350, 70, 95, 38);
        filterPanel.add(btnSearch);

        JButton btnGenerateMonth = createPrimaryButton("Gerar Mês");
        btnGenerateMonth.setBounds(455, 70, 115, 38);
        filterPanel.add(btnGenerateMonth);

        JButton btnMarkPaid = createPrimaryButton("Marcar Paga");
        btnMarkPaid.setBounds(580, 70, 125, 38);
        filterPanel.add(btnMarkPaid);

        JButton btnEditAmount = createDarkButton("Editar");
        btnEditAmount.setBounds(715, 70, 125, 38);
        filterPanel.add(btnEditAmount);

        JButton btnReopen = createDarkButton("Reabrir");
        btnReopen.setBounds(850, 70, 95, 38);
        filterPanel.add(btnReopen);

        JButton btnCancel = createDangerButton("Cancelar");
        btnCancel.setBounds(955, 70, 115, 38);
        filterPanel.add(btnCancel);

        fixedExpenseHistoryTable = createStyledTable();
        JScrollPane scrollPane = createTableScroll(fixedExpenseHistoryTable);

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        btnGenerateMonth.addActionListener(e -> generateMonthlyFixedExpenses());
        btnSearch.addActionListener(e -> loadFixedExpenseHistoryByPeriod());
        btnMarkPaid.addActionListener(e -> markFixedExpenseHistoryAsPaid());
        btnEditAmount.addActionListener(e -> editFixedExpenseHistoryAmount());
        btnReopen.addActionListener(e -> reopenFixedExpenseHistory());
        btnCancel.addActionListener(e -> cancelFixedExpenseHistory());

        fillCurrentMonthFields();
        loadFixedExpenseHistoryByPeriod();

        return panel;
    }

    private JPanel createVariableExpensePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel topContainer = new JPanel(new BorderLayout(0, 12));
        topContainer.setBackground(LIGHT_GRAY);

        JPanel formPanel = createCardPanel();
        formPanel.setLayout(null);
        formPanel.setPreferredSize(new Dimension(1000, 125));

        JLabel title = createSectionTitle("Cadastrar despesa variável");
        title.setBounds(18, 12, 340, 25);
        formPanel.add(title);

        JLabel nameLabel = createFieldLabel("Nome");
        nameLabel.setBounds(18, 48, 160, 22);
        formPanel.add(nameLabel);

        txtVariableName = createTextField();
        txtVariableName.setBounds(18, 72, 230, 34);
        formPanel.add(txtVariableName);

        JLabel amountLabel = createFieldLabel("Valor");
        amountLabel.setBounds(270, 48, 120, 22);
        formPanel.add(amountLabel);

        txtVariableAmount = createTextField();
        txtVariableAmount.setBounds(270, 72, 130, 34);
        formPanel.add(txtVariableAmount);

        JLabel dateLabel = createFieldLabel("Data");
        dateLabel.setBounds(420, 48, 120, 22);
        formPanel.add(dateLabel);

        txtVariableDate = createTextField();
        txtVariableDate.setBounds(420, 72, 145, 34);
        formPanel.add(txtVariableDate);

        JLabel descriptionLabel = createFieldLabel("Descrição");
        descriptionLabel.setBounds(585, 48, 160, 22);
        formPanel.add(descriptionLabel);

        txtVariableDescription = createTextField();
        txtVariableDescription.setBounds(585, 72, 260, 34);
        formPanel.add(txtVariableDescription);

        JButton btnSave = createPrimaryButton("Cadastrar");
        btnSave.setBounds(870, 70, 135, 38);
        formPanel.add(btnSave);

        JPanel filterPanel = createCardPanel();
        filterPanel.setLayout(null);
        filterPanel.setPreferredSize(new Dimension(1000, 85));

        JLabel filterTitle = createSectionTitle("Listar despesas variáveis por período");
        filterTitle.setBounds(18, 10, 330, 25);
        filterPanel.add(filterTitle);

        JLabel startLabel = createFieldLabel("Inicial");
        startLabel.setBounds(18, 42, 80, 22);
        filterPanel.add(startLabel);

        txtVariableStartDate = createTextField();
        txtVariableStartDate.setBounds(75, 38, 130, 34);
        filterPanel.add(txtVariableStartDate);

        JLabel endLabel = createFieldLabel("Final");
        endLabel.setBounds(225, 42, 80, 22);
        filterPanel.add(endLabel);

        txtVariableEndDate = createTextField();
        txtVariableEndDate.setBounds(275, 38, 130, 34);
        filterPanel.add(txtVariableEndDate);

        JButton btnSearch = createDarkButton("Buscar");
        btnSearch.setBounds(430, 36, 110, 38);
        filterPanel.add(btnSearch);

        JButton btnMarkPaid = createPrimaryButton("Marcar como Paga");
        btnMarkPaid.setBounds(555, 36, 170, 38);
        filterPanel.add(btnMarkPaid);

        JButton btnDelete = createDangerButton("Excluir Selecionada");
        btnDelete.setBounds(740, 36, 175, 38);
        filterPanel.add(btnDelete);

        topContainer.add(formPanel, BorderLayout.NORTH);
        topContainer.add(filterPanel, BorderLayout.SOUTH);

        variableExpenseTable = createStyledTable();
        JScrollPane scrollPane = createTableScroll(variableExpenseTable);

        panel.add(topContainer, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        btnSave.addActionListener(e -> saveVariableExpense());
        btnSearch.addActionListener(e -> loadVariableExpensesByPeriod());
        btnMarkPaid.addActionListener(e -> markVariableExpenseAsPaid());
        btnDelete.addActionListener(e -> deleteVariableExpense());

        fillVariableCurrentMonthFields();
        loadVariableExpensesByPeriod();

        return panel;
    }

    private void saveFixedExpense() {
        try {
            String name = txtFixedName.getText().trim();
            BigDecimal amount = parseMoney(txtFixedAmount.getText());
            LocalDate dueDate = parseDate(txtFixedDueDate.getText());

            if (name.isEmpty()) {
                showError("Informe o nome da despesa fixa.");
                return;
            }

            FixedExpense expense = new FixedExpense();
            expense.setName(name);
            expense.setAmount(amount);
            expense.setDueDate(dueDate);
            expense.setStatus(false);
            expense.setPaymentDate(null);
            expense.setActive(true);

            fixedExpenseDAO.save(expense);

            clearFixedFields();
            loadFixedExpenses();

            showSuccess("Despesa fixa cadastrada com sucesso!");

        } catch (Exception ex) {
            showError("Erro ao cadastrar despesa fixa: " + ex.getMessage());
        }
    }

    private void editFixedExpense() {
        int id = getSelectedId(fixedExpenseTable);

        if (id == -1) {
            showError("Selecione uma despesa fixa para editar.");
            return;
        }

        int row = fixedExpenseTable.getSelectedRow();
        int modelRow = fixedExpenseTable.convertRowIndexToModel(row);

        String currentName = fixedExpenseTable.getModel().getValueAt(modelRow, 1).toString();
        String currentAmount = moneyForInput(
                fixedExpenseTable.getModel().getValueAt(modelRow, 2)
        );
        String currentDueDate = fixedExpenseTable.getModel().getValueAt(modelRow, 3).toString();

        JTextField nameField = createTextField(currentName);
        JTextField amountField = createTextField(currentAmount);
        JTextField dueDateField = createTextField(currentDueDate);

        Object[] fields = {
                "Nome:", nameField,
                "Valor:", amountField,
                "Vencimento (dd/MM/yyyy):", dueDateField
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Editar Despesa Fixa",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();

                if (name.isEmpty()) {
                    showError("Informe o nome da despesa fixa.");
                    return;
                }

                FixedExpense expense = new FixedExpense();
                expense.setId(id);
                expense.setName(name);
                expense.setAmount(parseMoney(amountField.getText()));
                expense.setDueDate(parseDate(dueDateField.getText()));

                fixedExpenseDAO.update(expense);
                loadFixedExpenses();

                showSuccess("Despesa fixa atualizada com sucesso!");

            } catch (Exception ex) {
                showError("Erro ao editar despesa fixa: " + ex.getMessage());
            }
        }
    }

    private void loadFixedExpenses() {
        List<FixedExpense> expenses = fixedExpenseDAO.findAll();

        DefaultTableModel model = createTableModel();
        model.addColumn("ID");
        model.addColumn("Nome");
        model.addColumn("Valor");
        model.addColumn("Vencimento");
        model.addColumn("Status");
        model.addColumn("Pagamento");

        for (FixedExpense e : expenses) {
            model.addRow(new Object[]{
                    e.getId(),
                    e.getName(),
                    formatMoney(e.getAmount()),
                    formatDate(e.getDueDate()),
                    e.isStatus() ? "PAGO" : "PENDENTE",
                    formatDate(e.getPaymentDate())
            });
        }

        fixedExpenseTable.setModel(model);
        hideIdColumn(fixedExpenseTable);
    }

    private void deleteFixedExpense() {
        int id = getSelectedId(fixedExpenseTable);

        if (id == -1) {
            showError("Selecione uma despesa fixa para inativar.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja inativar essa despesa fixa?\n\n" +
                        "Ela não será apagada do histórico, apenas deixará de aparecer e gerar nos próximos meses.",
                "Confirmar inativação",
                JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            fixedExpenseDAO.inactivate(id);
            loadFixedExpenses();
            showSuccess("Despesa fixa inativada com sucesso!");
        }
    }

    private void markFixedExpenseAsPaid() {

        int id = getSelectedId(fixedExpenseTable);

        if (id == -1) {
            showError("Selecione uma despesa fixa.");
            return;
        }

        JXDatePicker datePicker = new JXDatePicker();
        datePicker.setDate(new java.util.Date());

        int option = JOptionPane.showConfirmDialog(
                this,
                datePicker,
                "Informe a data do pagamento",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION || datePicker.getDate() == null) {
            return;
        }

        try {

            LocalDate paymentDate = datePicker.getDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            fixedExpenseDAO.markAsPaid(id, paymentDate);

            loadFixedExpenses();

            showSuccess("Despesa fixa marcada como paga!");

        } catch (Exception e) {

            showError("Erro ao marcar despesa como paga: " + e.getMessage());
        }
    }

    private void generateMonthlyFixedExpenses() {
        LocalDate today = LocalDate.now();

        int option = JOptionPane.showConfirmDialog(
                this,
                "Gerar despesas fixas para " + today.getMonthValue() + "/" + today.getYear() + "?",
                "Gerar despesas do mês",
                JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            fixedExpenseHistoryDAO.generateMonthlyExpenses(today.getMonthValue(), today.getYear());
            fillCurrentMonthFields();
            loadFixedExpenseHistoryByPeriod();
            showSuccess("Despesas fixas do mês geradas com sucesso!");
        }
    }

    private void loadFixedExpenseHistoryByPeriod() {
        try {
            LocalDate start = parseDate(txtHistoryStartDate.getText());
            LocalDate end = parseDate(txtHistoryEndDate.getText());

            if (start.isAfter(end)) {
                showError("A data inicial não pode ser maior que a data final.");
                return;
            }

            List<FixedExpenseHistory> expenses = fixedExpenseHistoryDAO.findByPeriod(start, end);

            DefaultTableModel model = createTableModel();
            model.addColumn("ID");
            model.addColumn("ID Fixa");
            model.addColumn("Nome");
            model.addColumn("Valor");
            model.addColumn("Vencimento");
            model.addColumn("Status");
            model.addColumn("Pagamento");

            for (FixedExpenseHistory e : expenses) {
                model.addRow(new Object[]{
                        e.getId(),
                        e.getFixedExpenseId(),
                        e.getName(),
                        formatMoney(e.getAmount()),
                        formatDate(e.getDueDate()),
                        e.getStatus(),
                        formatDate(e.getPaymentDate())
                });
            }

            fixedExpenseHistoryTable.setModel(model);
            hideIdColumn(fixedExpenseHistoryTable);
            hideFirstVisibleColumn(fixedExpenseHistoryTable);

        } catch (Exception ex) {
            showError("Erro ao buscar despesas fixas do mês: " + ex.getMessage());
        }
    }

    private void markFixedExpenseHistoryAsPaid() {
        int id = getSelectedId(fixedExpenseHistoryTable);

        if (id == -1) {
            showError("Selecione uma despesa fixa mensal.");
            return;
        }

        JXDatePicker datePicker = new JXDatePicker();
        datePicker.setDate(new java.util.Date());

        int option = JOptionPane.showConfirmDialog(
                this,
                datePicker,
                "Informe a data do pagamento",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION || datePicker.getDate() == null) {
            return;
        }

        LocalDate paymentDate = datePicker.getDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        fixedExpenseHistoryDAO.markAsPaid(id, paymentDate);

        loadFixedExpenseHistoryByPeriod();

        showSuccess("Despesa fixa mensal marcada como paga!");
    }

    private void editFixedExpenseHistoryAmount() {
        int id = getSelectedId(fixedExpenseHistoryTable);

        if (id == -1) {
            showError("Selecione uma despesa fixa mensal.");
            return;
        }

        int row = fixedExpenseHistoryTable.getSelectedRow();
        int modelRow = fixedExpenseHistoryTable.convertRowIndexToModel(row);

        String currentAmount = fixedExpenseHistoryTable.getModel()
                .getValueAt(modelRow, 3)
                .toString()
                .replace("R$ ", "")
                .trim();

        String currentDueDate = fixedExpenseHistoryTable.getModel()
                .getValueAt(modelRow, 4)
                .toString();

        String currentStatus = fixedExpenseHistoryTable.getModel()
                .getValueAt(modelRow, 5)
                .toString();

        if (!"PENDENTE".equalsIgnoreCase(currentStatus)) {
            showError("Somente despesas mensais pendentes podem ser editadas.");
            return;
        }

        JTextField amountField = createTextField(currentAmount);
        JTextField dueDateField = createTextField(currentDueDate);

        Object[] fields = {
                "Valor:", amountField,
                "Vencimento (dd/MM/yyyy):", dueDateField
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Editar Despesa Fixa Mensal",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                BigDecimal newAmount = parseMoney(amountField.getText());
                LocalDate newDueDate = parseDate(dueDateField.getText());

                fixedExpenseHistoryDAO.updateAmountAndDueDate(id, newAmount, newDueDate);

                loadFixedExpenseHistoryByPeriod();

                showSuccess("Despesa fixa mensal atualizada com sucesso!");

            } catch (Exception e) {
                showError(e.getMessage());
            }
        }
    }

    private void reopenFixedExpenseHistory() {
        int id = getSelectedId(fixedExpenseHistoryTable);

        if (id == -1) {
            showError("Selecione uma despesa fixa mensal.");
            return;
        }

        int row = fixedExpenseHistoryTable.getSelectedRow();
        int modelRow = fixedExpenseHistoryTable.convertRowIndexToModel(row);

        String currentStatus = fixedExpenseHistoryTable.getModel()
                .getValueAt(modelRow, 5)
                .toString();

        if (!"PAGO".equalsIgnoreCase(currentStatus)) {
            showError("Somente despesas mensais pagas podem ser reabertas.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                "Deseja reabrir esta despesa fixa mensal?\n\n" +
                        "Ela voltará para PENDENTE e a data de pagamento será removida.",
                "Confirmar reabertura",
                JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            try {
                fixedExpenseHistoryDAO.reopen(id);
                loadFixedExpenseHistoryByPeriod();
                showSuccess("Despesa fixa mensal reaberta com sucesso!");
            } catch (Exception e) {
                showError(e.getMessage());
            }
        }
    }

    private void cancelFixedExpenseHistory() {
        int id = getSelectedId(fixedExpenseHistoryTable);

        if (id == -1) {
            showError("Selecione uma despesa fixa mensal.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                "Deseja cancelar esta despesa fixa mensal?",
                "Confirmar cancelamento",
                JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            fixedExpenseHistoryDAO.cancel(id);
            loadFixedExpenseHistoryByPeriod();
            showSuccess("Despesa fixa mensal cancelada com sucesso!");
        }
    }

    private void saveVariableExpense() {
        try {
            String name = txtVariableName.getText().trim();
            BigDecimal amount = parseMoney(txtVariableAmount.getText());
            LocalDate date = parseDate(txtVariableDate.getText());
            String description = txtVariableDescription.getText().trim();

            if (name.isEmpty()) {
                showError("Informe o nome da despesa variável.");
                return;
            }

            if (description.isEmpty()) {
                description = "Sem descrição";
            }

            VariableExpense expense = new VariableExpense();
            expense.setName(name);
            expense.setAmount(amount);
            expense.setDate(date);
            expense.setStatus(false);
            expense.setPaymentDate(null);
            expense.setDescription(description);

            variableExpenseDAO.save(expense);

            clearVariableFields();
            loadVariableExpensesByPeriod();

            showSuccess("Despesa variável cadastrada com sucesso!");

        } catch (Exception ex) {
            showError("Erro ao cadastrar despesa variável: " + ex.getMessage());
        }
    }

    private void loadVariableExpensesByPeriod() {
        try {
            LocalDate start = parseDate(txtVariableStartDate.getText());
            LocalDate end = parseDate(txtVariableEndDate.getText());

            if (start.isAfter(end)) {
                showError("A data inicial não pode ser maior que a data final.");
                return;
            }

            List<VariableExpense> expenses = variableExpenseDAO.findByPeriod(start, end);

            DefaultTableModel model = createTableModel();
            model.addColumn("ID");
            model.addColumn("Nome");
            model.addColumn("Valor");
            model.addColumn("Data");
            model.addColumn("Status");
            model.addColumn("Pagamento");
            model.addColumn("Descrição");

            for (VariableExpense e : expenses) {
                model.addRow(new Object[]{
                        e.getId(),
                        e.getName(),
                        formatMoney(e.getAmount()),
                        formatDate(e.getDate()),
                        e.isStatus() ? "PAGO" : "PENDENTE",
                        formatDate(e.getPaymentDate()),
                        e.getDescription()
                });
            }

            variableExpenseTable.setModel(model);
            hideIdColumn(variableExpenseTable);

        } catch (Exception ex) {
            showError("Erro ao buscar despesas variáveis: " + ex.getMessage());
        }
    }

    private void markVariableExpenseAsPaid() {
        int id = getSelectedId(variableExpenseTable);

        if (id == -1) {
            showError("Selecione uma despesa variável.");
            return;
        }

        variableExpenseDAO.markAsPaid(id, LocalDate.now());
        loadVariableExpensesByPeriod();
        showSuccess("Despesa variável marcada como paga!");
    }

    private void deleteVariableExpense() {
        int id = getSelectedId(variableExpenseTable);

        if (id == -1) {
            showError("Selecione uma despesa variável para excluir.");
            return;
        }

        int option = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja excluir essa despesa variável?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            variableExpenseDAO.delete(id);
            loadVariableExpensesByPeriod();
            showSuccess("Despesa variável excluída com sucesso!");
        }
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return panel;
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

    private JTextField createTextField() {
        return createTextField("");
    }

    private JTextField createTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }

    private JTable createStyledTable() {
        JTable table = new JTable();
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(BLACK);
        table.getTableHeader().setForeground(WHITE);
        table.setSelectionBackground(new Color(255, 225, 205));
        table.setSelectionForeground(BLACK);
        table.setGridColor(new Color(235, 235, 235));
        table.setShowVerticalLines(false);
        return table;
    }

    private JScrollPane createTableScroll(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        scrollPane.getViewport().setBackground(WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        return scrollPane;
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
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

    private JButton createDangerButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(RED);
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

    private void hideIdColumn(JTable table) {
        if (table.getColumnModel().getColumnCount() > 0) {
            table.removeColumn(table.getColumnModel().getColumn(0));
        }
    }

    private void hideFirstVisibleColumn(JTable table) {
        if (table.getColumnModel().getColumnCount() > 0) {
            table.removeColumn(table.getColumnModel().getColumn(0));
        }
    }

    private int getSelectedId(JTable table) {
        int viewRow = table.getSelectedRow();

        if (viewRow == -1) {
            return -1;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        return Integer.parseInt(table.getModel().getValueAt(modelRow, 0).toString());
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

    private String moneyForInput(Object value) {
        if (value == null) {
            return "";
        }

        return value.toString()
                .replace("R$", "")
                .replace("\u00A0", " ")
                .trim();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe uma data.");
        }

        return LocalDate.parse(value.trim(), brFormatter);
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "-";
        }

        return date.format(brFormatter);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return moneyFormatter.format(BigDecimal.ZERO);
        }

        return moneyFormatter.format(value.setScale(2, RoundingMode.HALF_UP));
    }

    private void fillCurrentMonthFields() {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());

        txtHistoryStartDate.setText(formatDate(firstDay));
        txtHistoryEndDate.setText(formatDate(lastDay));
    }

    private void fillVariableCurrentMonthFields() {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());

        txtVariableDate.setText(formatDate(today));
        txtVariableStartDate.setText(formatDate(firstDay));
        txtVariableEndDate.setText(formatDate(lastDay));
    }

    private void clearFixedFields() {
        txtFixedName.setText("");
        txtFixedAmount.setText("");
        txtFixedDueDate.setText("");
    }

    private void clearVariableFields() {
        txtVariableName.setText("");
        txtVariableAmount.setText("");
        txtVariableDescription.setText("");
        txtVariableDate.setText(formatDate(LocalDate.now()));
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}