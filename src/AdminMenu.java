// handles everything the admin can do: payroll, accounts, request approvals

import java.util.Scanner;

public class AdminMenu {

    // shared scanner passed in from LoginSystem
    @SuppressWarnings("FieldMayBeFinal")
    private Scanner sc;

    // constructor
    public AdminMenu(Scanner sc) {
        this.sc = sc;
    }

    // shows the admin menu until they log out
    public void show() {
        System.out.println("\n  Welcome, Admin!");

        int choice;
        do {
            printMenu();
            choice = readInt();

            switch (choice) {
                case 1 -> processPayroll();
                case 2 -> createEmployeeAccount();
                case 3 -> approveRequests();
                case 4 -> System.out.println("\n  Logging out... Goodbye, Admin!");
                default -> System.out.println("  Invalid choice. Please try again.");
            }
        } while (choice != 4);
    }

    // prints the admin menu options
    private void printMenu() {
        System.out.println();
        System.out.println("  ===== ADMIN MENU =====");
        System.out.println("  [1] Process Employee Payroll");
        System.out.println("  [2] Create Employee Account");
        System.out.println("  [3] Approve Leave / Overtime Requests");
        System.out.println("  [4] Logout");
        System.out.print("  Enter choice: ");
    }

    // option 1: process payroll from employee-submitted time records
    // admin picks a record, enters the loan, system computes and prints payslip
    private void processPayroll() {
        System.out.println("\n  -- Process Employee Payroll --");

        // count unprocessed records
        int unprocessedCount = 0;
        for (AccountManager.TimeRecord r : AccountManager.pendingTimeRecords) {
            if (!r.isProcessed()) unprocessedCount++;
        }

        if (unprocessedCount == 0) {
            System.out.println("  No pending time records from employees.");
            System.out.println("  Employees must submit their Time In/Out first from the Employee Menu.");
            return;
        }

        // show all unprocessed records for admin to pick
        System.out.println("  Pending Time Records from Employees:");
        System.out.println();

        int displayNumber = 1;
        for (AccountManager.TimeRecord r : AccountManager.pendingTimeRecords) {
            if (!r.isProcessed()) {
                System.out.println("  [" + displayNumber + "] " + r);
                displayNumber++;
            }
        }

        System.out.print("\n  Select a record to process (0 to go back): ");
        int pick = readInt();
        if (pick == 0) return;

        // find the selected record (counting only unprocessed ones)
        int count = 0;
        AccountManager.TimeRecord chosen = null;
        for (AccountManager.TimeRecord r : AccountManager.pendingTimeRecords) {
            if (!r.isProcessed()) {
                count++;
                if (count == pick) {
                    chosen = r;
                    break;
                }
            }
        }

        if (chosen == null) {
            System.out.println("  Invalid selection.");
            return;
        }

        // look up the matching employee
        Employee emp = AccountManager.findEmployeeById(chosen.getEmployeeId());
        if (emp == null) {
            System.out.println("  Error: Employee not found for this record. (ID: " + chosen.getEmployeeId() + ")");
            return;
        }

        System.out.println();
        System.out.println("  Processing payroll for: " + emp.getEmployeeName());
        System.out.println("  Type: " + emp.getEmployeeType());
        System.out.println("  Cut-off: " + (chosen.getCutoffPeriod() == 1 ? "1st-15th" : "16th-30th"));

        // admin only needs to enter the loan; time data already came from employee
        System.out.print("\n  Enter Loan Deduction (PHP, enter 0 if none): ");
        double loan = readDouble();

        // build payroll using employee-submitted time arrays
        Payroll payroll = new Payroll(
                emp,
                chosen.getTimeIn(),
                chosen.getTimeOut(),
                loan,
                chosen.getCutoffPeriod()
        );

        payroll.compute();
        payroll.printPayslip();

        // mark as done so it disappears from the queue
        chosen.markProcessed();
        System.out.println("  This time record has been marked as processed.");
    }

    // option 2: creates a new employee account (ID, name, type, salary, password)
    private void createEmployeeAccount() {
        System.out.println("\n  -- Create Employee Account --");

        // validate ID: must be exactly 6 digits
        System.out.print("  Employee ID   (6 digits): ");
        String id = sc.nextLine().trim();
        if (!id.matches("\\d{6}")) {
            System.out.println("  Invalid ID. Must be exactly 6 digits (e.g. 100003).");
            return;
        }

        // check if ID is already taken
        if (AccountManager.employeeAccounts.containsKey(id)) {
            System.out.println("  An account with that ID already exists.");
            return;
        }

        System.out.print("  Employee Name : ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("  Name cannot be empty.");
            return;
        }

        // pick employee type
        System.out.println("  Employee Type : [R] Regular  [P] Probationary  [C] Contractual  [T] Part-time");
        System.out.print("  Enter type    : ");
        String t = sc.nextLine().trim().toUpperCase();

        String type;
        double salary;

        switch (t) {
            case "R" -> { type = "Regular";      System.out.print("  Monthly Rate (PHP): "); salary = readDouble(); }
            case "P" -> { type = "Probationary"; System.out.print("  Monthly Rate (PHP): "); salary = readDouble(); }
            case "C" -> { type = "Contractual";  System.out.print("  Monthly Rate (PHP): "); salary = readDouble(); }
            case "T" -> { type = "Part-time";    System.out.print("  Hourly  Rate (PHP): "); salary = readDouble(); }
            default  -> { System.out.println("  Invalid type. Account not created."); return; }
        }

        System.out.print("  Set Password  : ");
        String password = sc.nextLine().trim();
        if (password.isEmpty()) {
            System.out.println("  Password cannot be empty.");
            return;
        }

        // save employee and their login credentials
        Employee newEmployee = new Employee(id, name, type, salary, 1);
        AccountManager.employees.add(newEmployee);
        AccountManager.employeeAccounts.put(id, password);

        System.out.println("  Account created successfully!");
        System.out.printf("  Name: %-20s | ID: %s | Type: %s%n", name, id, type);
    }

    // option 3: view pending requests and approve or reject them
    private void approveRequests() {
        while (true) {
            System.out.println("\n  -- Leave / Overtime Approval --");

            // check if any requests are pending
            boolean hasPending = false;
            for (LeaveOvertimeRequest req : AccountManager.requests) {
                if (req.getStatus().equals("Pending")) {
                    hasPending = true;
                    break;
                }
            }

            if (!hasPending) {
                System.out.println("  No pending leave or overtime requests at the moment.");
                return;
            }

            // show only pending requests, numbered for selection
            System.out.println("  Pending Requests:");
            System.out.println();
            int index = 1;
            for (LeaveOvertimeRequest req : AccountManager.requests) {
                if (req.getStatus().equals("Pending")) {
                    System.out.println("  [" + index + "] " + req);
                    index++;
                }
            }

            System.out.print("\n  Enter request number to act on (0 to go back): ");
            int pick = readInt();
            if (pick == 0) return;

            // find the selected request
            int count = 0;
            LeaveOvertimeRequest chosen = null;
            for (LeaveOvertimeRequest req : AccountManager.requests) {
                if (req.getStatus().equals("Pending")) {
                    count++;
                    if (count == pick) {
                        chosen = req;
                        break;
                    }
                }
            }

            if (chosen == null) {
                System.out.println("  Invalid selection.");
                continue;   // re-show the list instead of returning to menu
            }

            // show selected request and ask admin what to do
            System.out.println("\n  Selected: " + chosen);
            System.out.print("  [A] Approve   [R] Reject   [0] Cancel: ");
            String action = sc.nextLine().trim().toUpperCase();

            switch (action) {
                case "A" -> { chosen.setStatus("Approved"); System.out.println("  Request APPROVED."); }
                case "R" -> { chosen.setStatus("Rejected"); System.out.println("  Request REJECTED."); }
                default  ->   System.out.println("  Action cancelled.");
            }
            // loop back to show the updated pending list
        }
    }

    // safe int input — returns -1 if invalid
    private int readInt() {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    // safe double input — returns 0 if invalid
    private double readDouble() {
        try { return Double.parseDouble(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}