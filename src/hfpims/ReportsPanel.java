package hfpims;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReportsPanel extends JPanel {
    public ReportsPanel() {
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Sales Report", createReportPanel(
                new String[]{"Sale ID", "Date", "Total", "Cashier"},
                "SELECT s.sale_id, s.sale_date, s.total_amount, u.full_name " +
                        "FROM sales s JOIN users u ON s.user_id=u.user_id " +
                        "ORDER BY s.sale_date DESC"));

        tabs.addTab("Item-Wise Sales", createReportPanel(
                new String[]{"Medicine", "Quantity Sold", "Revenue"},
                "SELECT m.name, SUM(si.quantity_sold) AS total_qty, " +
                        "SUM(si.quantity_sold * si.price_at_sale) AS total_revenue " +
                        "FROM sale_items si JOIN medicines m ON si.medicine_id=m.medicine_id " +
                        "GROUP BY m.medicine_id, m.name ORDER BY total_qty DESC"));

        tabs.addTab("Low Stock", createReportPanel(
                new String[]{"ID", "Medicine", "Stock", "Reorder Level"},
                "SELECT medicine_id, name, quantity_in_stock, reorder_level " +
                        "FROM medicines WHERE quantity_in_stock <= reorder_level " +
                        "ORDER BY quantity_in_stock"));

        tabs.addTab("Expiry Report", createReportPanel(
                new String[]{"ID", "Medicine", "Expiry Date", "Stock"},
                "SELECT medicine_id, name, expiry_date, quantity_in_stock " +
                        "FROM medicines WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 1 MONTH) " +
                        "ORDER BY expiry_date"));

        tabs.addTab("Sales Charts", createChartsPanel());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel createReportPanel(String[] columns, String sql) {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            try (Connection con = DBConnection.getConnection();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Object[] row = new Object[columns.length];
                    for (int i = 0; i < columns.length; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    model.addRow(row);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        panel.add(refreshBtn, BorderLayout.SOUTH);
        refreshBtn.doClick();
        return panel;
    }

    private JPanel createChartsPanel() {
        JPanel main = new JPanel(new BorderLayout());

        JComboBox<String> chartSelector = new JComboBox<>(new String[]{
                "Top 5 Best-Selling Medicines (Bar)",
                "Revenue Share by Medicine (Pie)",
                "Revenue by Medicine Type (Bar)",
                "Daily Sales Trend - Last 7 Days (Line)",
                "Low Stock Levels (Bar)",
                "Expiry Timeline - Next 6 Months (Bar)",
                "Sales by Cashier (Pie)",
                "Stock Value by Supplier (Bar)"
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Select Chart:"));
        top.add(chartSelector);

        JPanel chartContainer = new JPanel(new BorderLayout());

        JButton refreshBtn = new JButton("Refresh Chart");

        Runnable drawChart = () -> {
            int choice = chartSelector.getSelectedIndex();
            JFreeChart chart = null;
            switch (choice) {
                case 0: chart = buildTopSellingBarChart(); break;
                case 1: chart = buildRevenuePieChart(); break;
                case 2: chart = buildRevenueByTypeBarChart(); break;
                case 3: chart = buildDailySalesLineChart(); break;
                case 4: chart = buildLowStockBarChart(); break;
                case 5: chart = buildExpiryTimelineChart(); break;
                case 6: chart = buildSalesByCashierPieChart(); break;
                case 7: chart = buildStockValueBySupplierChart(); break;
            }
            chartContainer.removeAll();
            if (chart != null) {
                chartContainer.add(new ChartPanel(chart), BorderLayout.CENTER);
            } else {
                chartContainer.add(new JLabel("No data available for this chart."), BorderLayout.CENTER);
            }
            chartContainer.revalidate();
            chartContainer.repaint();
        };

        chartSelector.addActionListener(e -> drawChart.run());
        refreshBtn.addActionListener(e -> drawChart.run());

        top.add(refreshBtn);
        main.add(top, BorderLayout.NORTH);
        main.add(chartContainer, BorderLayout.CENTER);

        SwingUtilities.invokeLater(drawChart);

        return main;
    }

    private JFreeChart buildTopSellingBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String sql = "SELECT m.name, SUM(si.quantity_sold) AS qty " +
                "FROM sale_items si JOIN medicines m ON si.medicine_id=m.medicine_id " +
                "GROUP BY m.medicine_id, m.name ORDER BY qty DESC LIMIT 5";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                dataset.addValue(rs.getInt("qty"), "Units Sold", rs.getString("name"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Top 5 Best-Selling Medicines",
                "Medicine", "Units Sold",
                dataset, PlotOrientation.VERTICAL,
                false, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(34, 139, 34));
        return chart;
    }

    private JFreeChart buildRevenuePieChart() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        String sql = "SELECT m.name, SUM(si.quantity_sold * si.price_at_sale) AS revenue " +
                "FROM sale_items si JOIN medicines m ON si.medicine_id=m.medicine_id " +
                "GROUP BY m.medicine_id, m.name ORDER BY revenue DESC LIMIT 8";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                dataset.setValue(rs.getString("name"), rs.getDouble("revenue"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        return ChartFactory.createPieChart(
                "Revenue Share by Medicine",
                dataset, true, true, false);
    }

    private JFreeChart buildRevenueByTypeBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String sql = "SELECT m.medicine_type, SUM(si.quantity_sold * si.price_at_sale) AS revenue " +
                "FROM sale_items si JOIN medicines m ON si.medicine_id=m.medicine_id " +
                "GROUP BY m.medicine_type ORDER BY revenue DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                dataset.addValue(rs.getDouble("revenue"),
                        "Revenue", rs.getString("medicine_type"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        return ChartFactory.createBarChart(
                "Revenue by Medicine Type",
                "Type", "Revenue (R)",
                dataset, PlotOrientation.VERTICAL,
                false, true, false);
    }

    private JFreeChart buildDailySalesLineChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String sql = "SELECT DATE(sale_date) AS day, SUM(total_amount) AS total " +
                "FROM sales WHERE sale_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                "GROUP BY DATE(sale_date) ORDER BY day";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                dataset.addValue(rs.getDouble("total"),
                        "Daily Sales", rs.getString("day"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        return ChartFactory.createLineChart(
                "Daily Sales - Last 7 Days",
                "Date", "Revenue (R)",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);
    }

    private JFreeChart buildLowStockBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String sql = "SELECT name, quantity_in_stock, reorder_level " +
                "FROM medicines WHERE quantity_in_stock <= reorder_level " +
                "ORDER BY quantity_in_stock ASC LIMIT 10";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                dataset.addValue(rs.getInt("quantity_in_stock"), "Current Stock", name);
                dataset.addValue(rs.getInt("reorder_level"), "Reorder Level", name);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Low Stock vs Reorder Level",
                "Medicine", "Units",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.ORANGE);
        return chart;
    }

    private JFreeChart buildExpiryTimelineChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String sql = "SELECT DATE_FORMAT(expiry_date, '%Y-%m') AS month, " +
                "COUNT(*) AS count " +
                "FROM medicines " +
                "WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 6 MONTH) " +
                "GROUP BY DATE_FORMAT(expiry_date, '%Y-%m') " +
                "ORDER BY month";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                dataset.addValue(rs.getInt("count"), "Medicines Expiring", rs.getString("month"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Medicines Expiring in the Next 6 Months",
                "Month", "Number of Medicines",
                dataset, PlotOrientation.VERTICAL,
                false, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(255, 140, 0));
        return chart;
    }

    private JFreeChart buildSalesByCashierPieChart() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        String sql = "SELECT u.full_name, SUM(s.total_amount) AS revenue " +
                "FROM sales s JOIN users u ON s.user_id=u.user_id " +
                "GROUP BY u.user_id, u.full_name " +
                "ORDER BY revenue DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                dataset.setValue(rs.getString("full_name"), rs.getDouble("revenue"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        return ChartFactory.createPieChart(
                "Sales Revenue by Cashier",
                dataset, true, true, false);
    }

    private JFreeChart buildStockValueBySupplierChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String sql = "SELECT s.name AS supplier_name, " +
                "SUM(m.quantity_in_stock * m.price) AS stock_value " +
                "FROM medicines m JOIN suppliers s ON m.supplier_id=s.supplier_id " +
                "GROUP BY s.supplier_id, s.name " +
                "ORDER BY stock_value DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                dataset.addValue(rs.getDouble("stock_value"),
                        "Stock Value (R)", rs.getString("supplier_name"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Stock Value by Supplier",
                "Supplier", "Value (R)",
                dataset, PlotOrientation.VERTICAL,
                false, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(70, 130, 180));
        return chart;
    }
}