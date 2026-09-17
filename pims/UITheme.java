package pims;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UITheme {

    public static final Color HEADER_BG = new Color(30, 90, 140);
    public static final Color HEADER_FG = Color.WHITE;
    public static final Color ROW_EVEN = new Color(240, 248, 255);
    public static final Color ROW_ODD  = Color.WHITE;
    public static final Color SELECT_BG = new Color(70, 130, 180);
    public static final Color SELECT_FG = Color.WHITE;
    public static final Color DANGER = new Color(220, 53, 69);
    public static final Color WARNING = new Color(255, 193, 7);
    public static final Color SUCCESS = new Color(40, 167, 69);

    public static void styleTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(HEADER_FG);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(SELECT_BG);
        table.setSelectionForeground(SELECT_FG);
        table.setShowVerticalLines(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value,
                        isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }

    public static void styleButton(JButton button, Color bg) {
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
    }
}