package view;

import controller.PayrollController;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;

public class PayrollFrame extends JFrame {

    private final PayrollController payrollController;

    private JSpinner startSpinner;
    private JSpinner endSpinner;
    private JComboBox<String> typeBox;
    private JTextArea resultArea;

    private final Color ORANGE = new Color(255, 102, 0);
    private final Color BLACK = new Color(18, 18, 18);
    private final Color WHITE = Color.WHITE;
    private final Color LIGHT_GRAY = new Color(245, 245, 245);
    private final Color BORDER_GRAY = new Color(220, 220, 220);
    private final Color TEXT_GRAY = new Color(90, 90, 90);

    public PayrollFrame() {
        payrollController = new PayrollController();

        setTitle("Sistema At Promo - Folha de Pagamento");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(null);
        header.setPreferredSize(new Dimension(950, 95));
        header.setBackground(BLACK);

        JLabel title = new JLabel("Folha de Pagamento");
        title.setForeground(WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setBounds(30, 20, 400, 34);
        header.add(title);

        JLabel subtitle = new JLabel("Calcule salário base, adicionais e descontos dos promotores por período.");
        subtitle.setForeground(new Color(210, 210, 210));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setBounds(32, 56, 650, 22);
        header.add(subtitle);

        JPanel line = new JPanel();
        line.setBackground(ORANGE);
        line.setBounds(30, 82, 160, 4);
        header.add(line);

        return header;
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(null);
        main.setBackground(LIGHT_GRAY);

        JPanel filterPanel = new JPanel(null);
        filterPanel.setBackground(WHITE);
        filterPanel.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        filterPanel.setBounds(30, 25, 875, 115);
        main.add(filterPanel);

        JLabel filterTitle = new JLabel("Filtros da folha");
        filterTitle.setForeground(BLACK);
        filterTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        filterTitle.setBounds(20, 12, 250, 25);
        filterPanel.add(filterTitle);

        JLabel startLabel = new JLabel("Data início");
        startLabel.setForeground(TEXT_GRAY);
        startLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        startLabel.setBounds(20, 48, 100, 22);
        filterPanel.add(startLabel);

        startSpinner = new JSpinner(new SpinnerDateModel());
        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "dd/MM/yyyy"));
        startSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        startSpinner.setBounds(20, 72, 135, 32);
        filterPanel.add(startSpinner);

        JLabel endLabel = new JLabel("Data fim");
        endLabel.setForeground(TEXT_GRAY);
        endLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        endLabel.setBounds(180, 48, 100, 22);
        filterPanel.add(endLabel);

        endSpinner = new JSpinner(new SpinnerDateModel());
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "dd/MM/yyyy"));
        endSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        endSpinner.setBounds(180, 72, 135, 32);
        filterPanel.add(endSpinner);

        JLabel typeLabel = new JLabel("Tipo de promotor");
        typeLabel.setForeground(TEXT_GRAY);
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        typeLabel.setBounds(340, 48, 150, 22);
        filterPanel.add(typeLabel);

        typeBox = new JComboBox<>(new String[]{"TODOS", "CLT", "MEI", "FERISTA"});
        typeBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeBox.setBounds(340, 72, 140, 32);
        filterPanel.add(typeBox);

        JButton btnGenerate = createPrimaryButton("Gerar Folha");
        btnGenerate.setBounds(610, 58, 135, 40);
        filterPanel.add(btnGenerate);

        JButton btnClear = createSecondaryButton("Limpar");
        btnClear.setBounds(755, 58, 95, 40);
        filterPanel.add(btnClear);

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(WHITE);
        resultPanel.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        resultPanel.setBounds(30, 160, 875, 365);
        main.add(resultPanel);

        JLabel resultTitle = new JLabel(" Resultado da folha");
        resultTitle.setForeground(BLACK);
        resultTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        resultTitle.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        resultPanel.add(resultTitle, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBackground(WHITE);
        resultArea.setForeground(BLACK);
        resultArea.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        resultArea.setText("Selecione o período e clique em \"Gerar Folha\".");

        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(null);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        btnGenerate.addActionListener(e -> generatePayroll());
        btnClear.addActionListener(e -> resultArea.setText(""));

        return main;
    }

    private void generatePayroll() {
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
        String result = payrollController.generatePayroll(start, end, type);

        if (result == null || result.isBlank()) {
            resultArea.setText("Nenhum dado encontrado para o período selecionado.");
        } else {
            resultArea.setText(result);
            resultArea.setCaretPosition(0);
        }
    }

    private LocalDate getStartDate() {
        java.util.Date date = (java.util.Date) startSpinner.getValue();

        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private LocalDate getEndDate() {
        java.util.Date date = (java.util.Date) endSpinner.getValue();

        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private JButton createPrimaryButton(String text) {
        JButton button = baseButton(text);
        button.setBackground(ORANGE);
        button.setForeground(WHITE);
        button.setBorderPainted(false);
        return button;
    }

    private JButton createSecondaryButton(String text) {
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