=========================================================
HEALTHFIRST PHARMACY INVENTORY MANAGEMENT SYSTEM (PIMS)
=========================================================

1. DEFAULT LOGIN CREDENTIALS
----------------------------
Use the following credentials to log in to the application:

Admin Account:
  Username: admin
  Password: admin123

Cashier Account:
  Username: cashier
  Password: cash123

Note: Administrators can create additional Admin and Cashier accounts
from within the application via the "Manage Users" tab.

2. SYSTEM REQUIREMENTS
----------------------
To run this application, you need the following:

Operating System:
- Windows 10 or higher

Software:
- Java Runtime Environment (JRE) 8 or higher
- MySQL Server (via XAMPP or standalone installation)

Required Libraries (included in the /lib folder):
- mysql-connector-j-8.x.x.jar (JDBC Driver for MySQL connectivity)
- jfreechart-1.5.6.jar (Charting library for reports)
- jcommon-1.0.24.jar (Required dependency for JFreeChart)

3. SETUP AND INSTALLATION INSTRUCTIONS
--------------------------------------
Follow these steps to set up and run the system:

Step 1: Start the Database
- Open the XAMPP Control Panel.
- Click "Start" next to the MySQL module.
- Ensure MySQL is running on port 3306 (the default).
- (Optional) Start Apache if you want to use phpMyAdmin.

Step 2: Import the Database
- Open your web browser and go to: http://localhost/phpmyadmin
- Click "New" on the left sidebar to create a new database named: pims
- Click the "Import" tab at the top.
- Click "Choose File" and select the "database.sql" file provided in this package.
- Click "Go" to import the tables and sample data.

Step 3: Run the Application
- Locate the executable file: [yourname]_pims.exe
- Double-click the .exe file to launch the application.
- Use the default credentials listed in Section 1 to log in.

Step 4: Running from Source Code (Optional)
- Open the project in NetBeans or any Java IDE.
- Ensure the JAR files in the /lib folder are added to the project libraries.
- Compile and run the LoginFrame.java file.

4. KEY FEATURES
---------------
Authentication Module:
- Secure role-based login for Admin and Cashier users.
- Automatic redirection to the appropriate dashboard based on user role.
- Error messages for failed login attempts.

Administrator Module:
- Summary Dashboard: Overview of key metrics including total medicines,
  total suppliers, total users, low stock count, expiring items, and
  today's sales total.
- Manage Medicines: Full CRUD operations for medicine inventory.
  The table uses color-coding for at-a-glance status:
    - Red row    = Medicine is expired
    - Orange row = Stock is at or below reorder level
    - Yellow row = Expiring within the next 30 days
- Manage Suppliers: Full CRUD operations for supplier details, with a
  "Medicines Supplied" column showing how many medicines each supplier
  provides.
- Manage Users: Full CRUD operations for BOTH Admin and Cashier accounts
  (Create, Read, Update, Delete). Administrators can:
    - Add new Admin or Cashier users.
    - Update usernames, passwords, full names, and roles.
    - Delete accounts (with safeguards against deleting your own account).
    - The Role column is color-coded: pink for Admin, green for Cashier.
- Reports: View sales, item-wise, low stock, and expiry reports.
- Sales Charts: Visual analytics using JFreeChart, including:
    - Top 5 Best-Selling Medicines (Bar Chart)
    - Revenue Share by Medicine (Pie Chart)
    - Revenue by Medicine Type (Bar Chart)
    - Daily Sales Trend - Last 7 Days (Line Chart)
    - Low Stock Levels (Bar Chart)
    - Expiry Timeline - Next 6 Months (Bar Chart)
    - Sales by Cashier (Pie Chart)
    - Stock Value by Supplier (Bar Chart)

Cashier Module:
- Point of Sale (POS) interface for processing customer sales.
- Shopping cart functionality with add, remove, and clear options.
- Automatic bill generation with print and save capabilities.
- Quick stock check to verify medicine availability and pricing.

5. COLOR LEGEND
---------------
The interface uses color coding for clarity:

Table Rows:
  - Red    = Expired medicine (urgent action required)
  - Orange = Stock at or below reorder level
  - Yellow = Expiring within 30 days
  - White / Light blue stripes = Standard alternating rows

Role Column (Manage Users):
  - Pink   = Administrator account
  - Green  = Cashier account

Action Buttons:
  - Green  = Add / Create
  - Blue   = Update / Refresh
  - Red    = Delete
  - Grey   = Neutral / Refresh

6. TROUBLESHOOTING
------------------
If the application fails to connect to the database:
- Ensure XAMPP MySQL is running (check XAMPP Control Panel).
- Ensure the "pims" database has been imported via phpMyAdmin.
- Ensure port 3306 is not being blocked by a firewall.
- Verify the DBConnection.java settings:
  - URL: jdbc:mysql://localhost:3306/pims
  - Username: root
  - Password: (leave blank)

If the charts do not display:
- Ensure jfreechart-1.5.6.jar and jcommon-1.0.24.jar are added
  to the project libraries.

If the application does not launch:
- Ensure Java Runtime Environment (JRE) 8 or higher is installed.
- Right-click the .exe and select "Run as Administrator" if permission
  errors occur.

7. PROJECT STRUCTURE
--------------------
YourName_StudentNumber_PRO732/
├── database.sql
├── README.txt
├── [yourname]_pims.exe
├── lib/
│   ├── mysql-connector-j-8.x.x.jar
│   ├── jfreechart-1.5.6.jar
│   └── jcommon-1.0.24.jar
├── screenshots/
│   ├── login.png
│   ├── admin_summary.png
│   ├── manage_medicines.png
│   ├── manage_suppliers.png
│   ├── manage_users.png
│   ├── cashier_pos.png
│   ├── bill_window.png
│   ├── report_sales.png
│   ├── report_item_wise.png
│   ├── report_low_stock.png
│   ├── report_expiry.png
│   └── sales_charts.png
└── src/
    └── pims/
        ├── DBConnection.java
        ├── UITheme.java
        ├── LoginFrame.java
        ├── AdminDashboard.java
        ├── CashierDashboard.java
        ├── ManageMedicinesPanel.java
        ├── ManageSuppliersPanel.java
        ├── ManageUsersPanel.java
        ├── POSPanel.java
        ├── StockCheckPanel.java
        ├── BillFrame.java
        └── ReportsPanel.java

8. GITHUB REPOSITORY
--------------------
Public Repository Link: https://github.com/Lesedi-9/Programming-assignment.git

=========================================================
END OF DOCUMENT
=========================================================
