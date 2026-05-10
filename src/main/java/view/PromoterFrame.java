package view;

import controller.PromoterController;
import model.Promoter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PromoterFrame extends JFrame {

    private final PromoterController promoterController;

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    private final Color ORANGE = new Color(255, 102, 0);
    private final Color BLACK = new Color(18, 18, 18);
    private final Color WHITE = Color.WHITE;
    private final Color LIGHT_GRAY = new Color(245, 245, 245);
    private final Color BORDER_GRAY = new Color(220, 220, 220);
    private final Color TEXT_GRAY = new Color(90, 90, 90);
    private final Color RED = new Color(190, 40, 40);

    public PromoterFrame() {
        promoterController = new PromoterController();

        setTitle("Sistema At Promo - Promotores");
        setSize(1120, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadPromoters();

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(null);
        header.setPreferredSize(new Dimension(1120, 95));
        header.setBackground(BLACK);

        JLabel title = new JLabel("Gerenciamento de Promotores");
        title.setForeground(WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setBounds(30, 20, 450, 34);
        header.add(title);

        JLabel subtitle = new JLabel("Cadastre, filtre, busque, edite e controle o status dos promotores da empresa.");
        subtitle.setForeground(new Color(210, 210, 210));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setBounds(32, 56, 760, 22);
        header.add(subtitle);

        JPanel line = new JPanel();
        line.setBackground(ORANGE);
        line.setBounds(30, 82, 210, 4);
        header.add(line);

        return header;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(null);
        main.setBackground(LIGHT_GRAY);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        actionPanel.setBackground(WHITE);
        actionPanel.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        actionPanel.setBounds(25, 20, 1185, 65);
        main.add(actionPanel);

        JButton btnList = createDarkButton("Listar");
        JButton btnFilter = createSecondaryButton("Filtrar");
        JButton btnSearch = createSecondaryButton("Buscar");
        JButton btnInactive = createDangerButton("Inativar");
        JButton btnActivate = createPrimaryButton("Ativar");
        JButton btnRegister = createPrimaryButton("Cadastrar");

        actionPanel.add(btnList);
        actionPanel.add(btnFilter);
        actionPanel.add(btnSearch);
        actionPanel.add(btnInactive);
        actionPanel.add(btnActivate);
        actionPanel.add(btnRegister);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        tablePanel.setBounds(30, 115, 1045, 430);
        main.add(tablePanel);

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(WHITE);
        tableHeader.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel tableTitle = new JLabel("Lista de promotores");
        tableTitle.setForeground(BLACK);
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableHeader.add(tableTitle, BorderLayout.WEST);

        statusLabel = new JLabel("Carregando...");
        statusLabel.setForeground(TEXT_GRAY);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableHeader.add(statusLabel, BorderLayout.EAST);

        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"ID", "Nome", "CPF", "Telefone", "Tipo", "Salário", "Status", "Editar"};

        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) {
                return c == 7;
            }
        };

        table = createStyledTable();
        table.setModel(tableModel);

        table.getColumn("Editar").setCellRenderer(new ButtonRenderer());
        table.getColumn("Editar").setCellEditor(new ButtonEditor(new JCheckBox(), this));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        tablePanel.add(scroll, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(null);
        infoPanel.setBackground(WHITE);
        infoPanel.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        infoPanel.setBounds(30, 565, 1045, 55);
        main.add(infoPanel);

        JLabel infoText = new JLabel("Dica: use Buscar para localizar rapidamente pelo nome. Use o botão ✏️ para editar os dados do promotor.");
        infoText.setForeground(TEXT_GRAY);
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoText.setBounds(18, 15, 900, 25);
        infoPanel.add(infoText);

        btnList.addActionListener(e -> loadPromoters());
        btnFilter.addActionListener(e -> openFilterDialog());
        btnSearch.addActionListener(e -> searchByName());
        btnInactive.addActionListener(e -> actionInactivate());
        btnActivate.addActionListener(e -> actionActivate());
        btnRegister.addActionListener(e -> openRegisterDialog());

        return main;
    }

    private JTable createStyledTable() {
        JTable styledTable = new JTable();
        styledTable.setRowHeight(30);
        styledTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        styledTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        styledTable.getTableHeader().setBackground(BLACK);
        styledTable.getTableHeader().setForeground(WHITE);
        styledTable.setSelectionBackground(new Color(255, 225, 205));
        styledTable.setSelectionForeground(BLACK);
        styledTable.setGridColor(new Color(235, 235, 235));
        styledTable.setShowVerticalLines(false);
        return styledTable;
    }

    private void loadPromoters() {
        fillTable(promoterController.getAll());
        statusLabel.setText("Exibindo todos os promotores");
    }

    private void openFilterDialog() {
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"TODOS", "CLT", "MEI"});
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"TODOS", "ATIVO", "INATIVO"});

        styleComboBox(typeBox);
        styleComboBox(statusBox);

        Object[] fields = {
                "Tipo:", typeBox,
                "Status:", statusBox
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Filtrar Promotores",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option != JOptionPane.OK_OPTION) return;

        String type = typeBox.getSelectedItem().toString();
        String status = statusBox.getSelectedItem().toString();

        List<Promoter> list = promoterController.getAll();

        List<Promoter> filtered = list.stream()
                .filter(p -> {
                    boolean matchType = type.equals("TODOS") || p.getType().equalsIgnoreCase(type);
                    boolean matchStatus = status.equals("TODOS")
                            || (status.equals("ATIVO") && p.isActive())
                            || (status.equals("INATIVO") && !p.isActive());

                    return matchType && matchStatus;
                })
                .toList();

        fillTable(filtered);
        statusLabel.setText("Filtro aplicado: " + type + " / " + status);
    }

    private void searchByName() {
        JTextField searchField = createTextField();

        DefaultListModel<PromoterItem> listModel = new DefaultListModel<>();
        JList<PromoterItem> promoterList = new JList<>(listModel);
        promoterList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        promoterList.setSelectionBackground(new Color(255, 225, 205));
        promoterList.setSelectionForeground(BLACK);

        JScrollPane listScroll = new JScrollPane(promoterList);
        listScroll.setPreferredSize(new Dimension(340, 130));

        searchField.addCaretListener(e -> {
            String text = searchField.getText().trim();
            listModel.clear();

            if (text.length() < 2) return;

            List<Promoter> promoters = promoterController.searchByName(text);

            for (Promoter p : promoters) {
                listModel.addElement(new PromoterItem(p.getId(), p.getName()));
            }
        });

        Object[] fields = {
                "Buscar promotor pelo nome:", searchField,
                "Resultados:", listScroll
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Buscar Promotor",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            PromoterItem selected = promoterList.getSelectedValue();

            if (selected == null) {
                showWarning("Selecione um promotor.");
                return;
            }

            Promoter promoter = promoterController.findById(selected.getId());

            tableModel.setRowCount(0);

            if (promoter != null) {
                addRow(promoter);
                statusLabel.setText("Resultado da busca: " + promoter.getName());
            }
        }
    }

    private void actionInactivate() {
        int id = getSelectedId();

        if (id == -1) {
            showWarning("Selecione um promotor.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deseja inativar este promotor?",
                "Confirmar inativação",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            promoterController.inactivate(id);
            loadPromoters();
            showSuccess("Promotor inativado com sucesso!");
        }
    }

    private void actionActivate() {
        int id = getSelectedId();

        if (id == -1) {
            showWarning("Selecione um promotor.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deseja ativar este promotor?",
                "Confirmar ativação",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            promoterController.activate(id);
            loadPromoters();
            showSuccess("Promotor ativado com sucesso!");
        }
    }

    public void openEditDialog(int id) {
        Promoter p = promoterController.findById(id);

        if (p == null) {
            showWarning("Promotor não encontrado.");
            return;
        }

        JTextField name = createTextField(p.getName());
        JTextField phone = createTextField(p.getPhone());
        JTextField salary = createTextField(p.getSalary().toString());

        JComboBox<String> type = new JComboBox<>(new String[]{"CLT", "MEI"});
        type.setSelectedItem(p.getType());
        styleComboBox(type);

        Object[] fields = {
                "Nome:", name,
                "Telefone:", phone,
                "Salário:", salary,
                "Tipo:", type
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Editar Promotor",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                if (name.getText().trim().isBlank() || phone.getText().trim().isBlank()) {
                    showWarning("Nome e telefone são obrigatórios.");
                    return;
                }

                promoterController.update(
                        id,
                        name.getText().trim(),
                        phone.getText().trim(),
                        new BigDecimal(salary.getText().trim().replace(",", ".")),
                        type.getSelectedItem().toString()
                );

                showSuccess("Promotor atualizado!");
                loadPromoters();

            } catch (Exception e) {
                showError("Dados inválidos.");
            }
        }
    }

    private void openRegisterDialog() {
        JTextField name = createTextField();
        JTextField cpf = createTextField();
        JTextField phone = createTextField();
        JTextField salary = createTextField();

        JSpinner birthSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor birthEditor = new JSpinner.DateEditor(birthSpinner, "dd/MM/yyyy");
        birthSpinner.setEditor(birthEditor);
        birthSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JComboBox<String> type = new JComboBox<>(new String[]{"CLT", "MEI"});
        styleComboBox(type);

        Object[] fields = {
                "Nome:", name,
                "CPF:", cpf,
                "Telefone:", phone,
                "Nascimento:", birthSpinner,
                "Salário:", salary,
                "Tipo:", type
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                fields,
                "Cadastrar Promotor",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                if (name.getText().trim().isBlank()
                        || cpf.getText().trim().isBlank()
                        || phone.getText().trim().isBlank()) {
                    showWarning("Nome, CPF e telefone são obrigatórios.");
                    return;
                }

                java.util.Date birthDate = (java.util.Date) birthSpinner.getValue();

                LocalDate dateBirth = birthDate.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();

                promoterController.register(
                        name.getText().trim(),
                        cpf.getText().trim(),
                        phone.getText().trim(),
                        dateBirth,
                        new BigDecimal(salary.getText().trim().replace(",", ".")),
                        type.getSelectedItem().toString()
                );

                showSuccess("Promotor cadastrado!");
                loadPromoters();

            } catch (Exception e) {
                showError("Dados inválidos.");
            }
        }
    }

    private void fillTable(List<Promoter> list) {
        tableModel.setRowCount(0);

        for (Promoter p : list) {
            addRow(p);
        }

        statusLabel.setText(list.size() + " promotor(es) exibido(s)");
    }

    private void addRow(Promoter p) {
        tableModel.addRow(new Object[]{
                p.getId(),
                p.getName(),
                formatCpf(p.getCpf()),
                p.getPhone(),
                p.getType(),
                formatMoney(p.getSalary()),
                p.isActive() ? "ATIVO" : "INATIVO",
                "✏️"
        });
    }

    private int getSelectedId() {
        int row = table.getSelectedRow();

        if (row == -1) {
            return -1;
        }

        int modelRow = table.convertRowIndexToModel(row);
        return (int) tableModel.getValueAt(modelRow, 0);
    }

    private String formatCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;

        return cpf.substring(0, 3) + "."
                + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-"
                + cpf.substring(9);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "R$ 0,00";

        return "R$ " + value.setScale(2, java.math.RoundingMode.HALF_UP)
                .toString()
                .replace(".", ",");
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

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(WHITE);
        comboBox.setForeground(BLACK);
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

    private JButton createSecondaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(WHITE);
        button.setForeground(BLACK);
        button.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
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
        button.setPreferredSize(new Dimension(120, 38));
        button.setFocusPainted(false);
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

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {

        public ButtonRenderer() {
            setText("✏️");
            setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            setFocusPainted(false);
            setBorderPainted(false);
            setBackground(WHITE);
            setForeground(BLACK);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        public Component getTableCellRendererComponent(
                JTable t,
                Object v,
                boolean s,
                boolean f,
                int r,
                int c
        ) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {

        private final JButton button;
        private int row;
        private final PromoterFrame frame;

        public ButtonEditor(JCheckBox checkBox, PromoterFrame frame) {
            super(checkBox);
            this.frame = frame;

            button = new JButton("✏️");
            button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setBackground(ORANGE);
            button.setForeground(WHITE);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));

            button.addActionListener(e -> {
                int modelRow = table.convertRowIndexToModel(row);
                int id = (int) tableModel.getValueAt(modelRow, 0);
                fireEditingStopped();
                frame.openEditDialog(id);
            });
        }

        public Component getTableCellEditorComponent(
                JTable t,
                Object v,
                boolean s,
                int r,
                int c
        ) {
            row = r;
            return button;
        }

        public Object getCellEditorValue() {
            return "✏️";
        }
    }
}