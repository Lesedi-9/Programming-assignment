package hfpims;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.util.List;

public class BillFrame extends JFrame {
    public BillFrame(int saleId, List<String> items, java.math.BigDecimal total, String cashierName) {
        setTitle("Bill - Sale #" + saleId);
        setSize(450, 500);
        setLocationRelativeTo(null);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));

        StringBuilder sb = new StringBuilder();
        sb.append("        HealthFirst Pharmacy\n");
        sb.append("        ====================\n");
        sb.append("Sale ID: ").append(saleId).append("\n");
        sb.append("Cashier: ").append(cashierName).append("\n");
        sb.append("Date: ").append(new java.util.Date()).append("\n");
        sb.append("----------------------------------------\n");

        for (String line : items) {
            sb.append(line).append("\n");
        }

        sb.append("----------------------------------------\n");
        sb.append("TOTAL: ").append(total.setScale(2, java.math.BigDecimal.ROUND_HALF_UP)).append("\n");
        sb.append("Thank you! Get well soon.\n");

        area.setText(sb.toString());
        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton printBtn = new JButton("Print");
        JButton saveBtn = new JButton("Save");
        JButton closeBtn = new JButton("Close");
        buttons.add(printBtn);
        buttons.add(saveBtn);
        buttons.add(closeBtn);
        add(buttons, BorderLayout.SOUTH);

        printBtn.addActionListener(e -> {
            try {
                area.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage());
            }
        });

        saveBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new java.io.File("bill_" + saleId + ".txt"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
                    fw.write(area.getText());
                    JOptionPane.showMessageDialog(this, "Bill saved.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Save error: " + ex.getMessage());
                }
            }
        });

        closeBtn.addActionListener(e -> dispose());
    }
}
