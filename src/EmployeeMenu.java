import java.util.Scanner;

public class EmployeeMenu {

    @SuppressWarnings("FieldMayBeFinal")
    private Scanner  sc;
    @SuppressWarnings("FieldMayBeFinal")
    private Employee employee; // the logged-in employee

    // constructor: takes the shared scanner and logged-in employee
    public EmployeeMenu(Scanner sc, Employee employee) {
        this.sc       = sc;
        this.employee = employee;
    }

    // shows the employee menu until they log out
    public void show() {
        System.out.println("\n  Welcome, " + employee.getEmployeeName() + "!");

        int choice;
        do {
            printMenu();
            choice = readInt();

            switch (choice) {
                case 1 -> submitTimeRecord();
                case 2 -> applyForLeaveOrOvertime();
                case 3 -> showFAQ();
                case 4 -> System.out.println("\n  Logging out... Goodbye, " + employee.getEmployeeName() + "!");
                default -> System.out.println("  Invalid choice. Please try again.");
            }
        } while (choice != 4);
    }

    // prints the menu options
    private void printMenu() {
        System.out.println();
        System.out.println("  ===== EMPLOYEE MENU =====");
        System.out.println("  Employee: " + employee.getEmployeeName() + " (ID: " + employee.getEmployeeId() + ")");
        System.out.println("  [1] Submit Time In / Time Out");
        System.out.println("  [2] Apply for Leave or Overtime");
        System.out.println("  [3] Frequently Asked Questions (FAQ)");
        System.out.println("  [4] Logout");
        System.out.print("  Enter choice: ");
    }

    // option 1: employee submits their time in/out for the cut-off
    private void submitTimeRecord() {
        System.out.println("\n  -- Submit Time Record --");
        System.out.println("  Use 24-hour decimal format:");
        System.out.println("    8.0  = 8:00 AM  |  8.5  = 8:30 AM");
        System.out.println("   17.0  = 5:00 PM  |  17.5 = 5:30 PM");
        System.out.println("  Enter 0 for BOTH Time In and Time Out if you were ABSENT.");
        System.out.println();

        // warn if there's already an unprocessed record
        for (AccountManager.TimeRecord existing : AccountManager.pendingTimeRecords) {
            if (existing.getEmployeeId().equals(employee.getEmployeeId())
                    && !existing.isProcessed()) {
                System.out.println("  WARNING: You already have a pending time record that the");
                System.out.println("  admin has not processed yet.");
                System.out.print("  Submit a new one anyway? (Y/N): ");
                String confirm = sc.nextLine().trim().toUpperCase();
                if (!confirm.equals("Y")) {
                    System.out.println("  Submission cancelled.");
                    return;
                }
                break;
            }
        }

        // ask which cut-off period this record is for
        System.out.println("  Which cut-off period are you submitting for?");
        System.out.println("  [1] 1st - 15th");
        System.out.println("  [2] 16th - 30th");
        System.out.print("  Enter choice: ");
        int cutoff = readInt();
        if (cutoff != 1 && cutoff != 2) {
            System.out.println("  Invalid choice. Submission cancelled.");
            return;
        }

        // collect 15 days of time entries
        System.out.println();
        double[] timeIn  = new double[15];
        double[] timeOut = new double[15];

        for (int i = 0; i < 15; i++) {
            System.out.printf("  Day %2d  Time In  (e.g. 8.0 for 8:00 AM)  : ", (i + 1));
            timeIn[i]  = readDouble();
            System.out.printf("  Day %2d  Time Out (e.g. 17.0 for 5:00 PM) : ", (i + 1));
            timeOut[i] = readDouble();
        }

        // build the time record using the arrays above
        AccountManager.TimeRecord record = new AccountManager.TimeRecord(
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                timeIn,
                timeOut,
                cutoff
        );

        // add to the shared queue for admin to process
        AccountManager.pendingTimeRecords.add(record);

        System.out.println();
        System.out.println("  Time record submitted successfully!");
        System.out.println("  The admin will process your payroll from this data.");
    }

    // option 2: employee files a leave or overtime request
    private void applyForLeaveOrOvertime() {
        System.out.println("\n  -- Apply for Leave / Overtime --");
        System.out.println("  [1] Apply for Leave");
        System.out.println("  [2] Apply for Overtime");
        System.out.print("  Enter choice: ");
        int choice = readInt();

        LeaveOvertimeRequest.RequestType type;
        String typeLabel;

        switch (choice) {
            case 1 -> {
                type      = LeaveOvertimeRequest.RequestType.LEAVE;
                typeLabel = "Leave";
            }
            case 2 -> {
                type      = LeaveOvertimeRequest.RequestType.OVERTIME;
                typeLabel = "Overtime";
            }
            default -> {
                System.out.println("  Invalid choice.");
                return;
            }
        }

        // ask employee to describe their request
        System.out.println("  Enter details of your " + typeLabel + " request.");
        System.out.println("  Example: '2 days leave for medical checkup' or '3 OT hours on Day 5'");
        System.out.print("  Details: ");
        String details = sc.nextLine().trim();

        if (details.isEmpty()) {
            System.out.println("  Details cannot be empty.");
            return;
        }

        // save request and add to shared list
        LeaveOvertimeRequest request = new LeaveOvertimeRequest(
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                type,
                details
        );
        AccountManager.requests.add(request);

        System.out.println("  Your " + typeLabel + " request has been submitted!");
        System.out.println("  Status: Pending (waiting for admin approval)");
    }

    // option 3: shows FAQ list, lets employee pick a question to read
    private void showFAQ() {
        String[][] faqs = {
            {
                "Q1: How do I enter my Time In and Time Out?", """
                                                               Use decimal format based on a 24-hour clock.
                                                                 Examples:
                                                                   8.0  = 8:00 AM  (on time)
                                                                   8.5  = 8:30 AM  (30 minutes late)
                                                                  17.0  = 5:00 PM  (regular end of shift)
                                                                  18.5  = 6:30 PM  (1.5 hours overtime)
                                                                 If you were ABSENT, enter 0 for BOTH Time In and Time Out."""},
            {
                "Q2: What is the standard work schedule?", """
                                                           The standard schedule is 8:00 AM to 5:00 PM (8.0 to 17.0).
                                                             A 1-hour lunch break (12:00 PM - 1:00 PM) is automatically
                                                             deducted from your daily hours worked.
                                                             This means a full regular workday counts as 8 hours."""},
            {
                "Q3: What happens if I come in late?", """
                                                       If your Time In is later than 8.0 (8:00 AM), the extra hours
                                                         are counted as late hours and deducted from your gross pay.
                                                         Example: If you arrive at 8.5 (8:30 AM), 0.5 hours will be
                                                         deducted based on your hourly rate."""},
            {
                "Q4: How many paid leaves do I get per cut-off period?", """
                                                                         You are entitled to up to 5 paid leave days per cut-off period.
                                                                           When you mark an absent day as 'Paid Leave', it will not be
                                                                           deducted from your salary. Once you have used all 5 paid leaves,
                                                                           any further absences will be treated as unpaid and deducted."""},
            {
                "Q5: How is overtime pay calculated?", """
                                                       Overtime applies to any hours worked past 5:00 PM (17.0).
                                                         Overtime pay = Overtime Hours x Hourly Rate x 1.25
                                                         (You receive a 25% premium on top of your regular hourly rate.)
                                                         Part-time employees also receive the same 1.25x multiplier
                                                         for hours worked beyond 5:00 PM."""},
            {
                "Q6: When will I receive my payslip?", """
                                                       Payslips are processed by the admin after you submit your
                                                         time record. There are two cut-off periods per month:
                                                           - 1st cut-off: Days 1 to 15
                                                           - 2nd cut-off: Days 16 to 30
                                                         Submit your time record on time so the admin can process it."""},
            {
                "Q7: What government deductions are taken from my salary?", """
                                                                            The following are automatically computed and deducted:
                                                                                SSS       - 4.5% of your Monthly Salary Credit (capped at PHP 35,000)
                                                                                PhilHealth - 5% of monthly salary (employee pays half), split semi-monthly
                                                                                Pag-IBIG  - 2% of monthly salary (max PHP 100), split semi-monthly
                                                                                Tax       - Withholding tax based on your taxable income bracket
                                                                              These deductions are required by law for all employee types."""},
            {
                "Q8: How do I apply for a leave or overtime request?", """
                                                                       Go to Option [2] - Apply for Leave or Overtime from the main menu.
                                                                         Choose whether it is a Leave or Overtime request, then provide
                                                                         the details (e.g., '2 days leave for medical checkup').
                                                                         Your request will be sent to the admin with a 'Pending' status.
                                                                         The admin will then approve or reject your request."""},
            {
                "Q9: Can I submit a new time record if I already have one pending?", """
                                                                                     Yes, but you will see a warning if you already have an unprocessed
                                                                                       time record waiting for the admin.
                                                                                       You may still choose to submit a new one, but note that both will
                                                                                       appear in the admin's queue. It is best to wait until the admin
                                                                                       has processed your existing record before submitting a new one."""},
            {
                "Q10: What if I forgot my Employee ID or password?", """
                                                                     Your Employee ID is a 6-digit number assigned by the admin.
                                                                       If you forgot your password or ID, please contact your admin
                                                                       directly. Only the admin can create or manage employee accounts.
                                                                       There is no self-service password reset in this system."""}
        };

        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════════╗");
        System.out.println("  ║           FREQUENTLY ASKED QUESTIONS (FAQ)              ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════╝");
        System.out.println("  Below are common questions new employees ask about the system.");
        System.out.println("  Enter the question number to read the answer, or 0 to go back.");
        System.out.println();

        // print all question titles
        for (int i = 0; i < faqs.length; i++) {
            System.out.println("  [" + (i + 1) + "] " + faqs[i][0]);
        }
        System.out.println("  [0] Back to Main Menu");
        System.out.println();

        boolean browsing = true;
        while (browsing) {
            System.out.print("  Select a question (0 to go back): ");
            int pick = readInt();

            if (pick == 0) {
                browsing = false;
            } else if (pick >= 1 && pick <= faqs.length) {
                System.out.println();
                System.out.println("  ──────────────────────────────────────────────────────────");
                System.out.println("  " + faqs[pick - 1][0]);
                System.out.println("  ──────────────────────────────────────────────────────────");
                System.out.println("  " + faqs[pick - 1][1]);
                System.out.println("  ──────────────────────────────────────────────────────────");
                System.out.println();
            } else {
                System.out.println("  Invalid selection. Please enter a number from 0 to " + faqs.length + ".");
            }
        }
    }

    // helper methods: safe input reading
    private int readInt() {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    private double readDouble() {
        try { return Double.parseDouble(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}