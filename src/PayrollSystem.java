import java.util.ArrayList;
import java.util.Scanner;

// main class: handles menu display and program flow only
public class PayrollSystem {

    static Scanner sc = new Scanner(System.in);
    static ArrayList<Employee> employees = new ArrayList<>();

    public static void main(String[] args) {
        int choice;
        do {
            System.out.println();
            System.out.println("===== ABC Company Payroll System =====");
            System.out.println(" [1] Add Employee");
            System.out.println(" [2] Process Payroll");
            System.out.println(" [3] View Employees");
            System.out.println(" [4] Exit");
            System.out.print("Enter choice: ");
            choice = readInt();

            switch (choice) {
                case 1 -> addEmployee();
                case 2 -> processPayroll();
                case 3 -> viewEmployees();
                case 4 -> System.out.println("Goodbye!");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 4);
    }

    static void addEmployee() {
        System.out.println("\n-- Add Employee --");
        System.out.print("Employee ID   : "); String id = sc.nextLine().trim();

        // employee ID must be exactly 6 digits
        if (!id.matches("\\d{6}")) {
            System.out.println("Invalid Employee ID. Must be exactly 6 digits.");
            return;
        }

        System.out.print("Employee Name : "); String name = sc.nextLine().trim();
        System.out.println("Employee Type : [R]egular  [P]robationary  [C]ontractual  [T]Part-time");
        System.out.print("Enter type    : "); String t = sc.nextLine().trim().toUpperCase();

        String type;
        double salary;

        switch (t) {
            case "R" -> {
                type = "Regular";
                System.out.print("Monthly Rate (PHP): ");
                salary = readDouble();
            }
            case "P" -> {
                type = "Probationary";
                System.out.print("Monthly Rate (PHP): ");
                salary = readDouble();
            }
            case "C" -> {
                type = "Contractual";
                System.out.print("Monthly Rate (PHP): ");
                salary = readDouble();
            }
            case "T" -> {
                type = "Part-time";
                System.out.print("Hourly Rate  (PHP): ");
                salary = readDouble();
            }
            default -> {
                System.out.println("Invalid type.");
                return;
            }
        }

        employees.add(new Employee(id, name, type, salary, 0));
        System.out.println("Employee added!");
    }

    static void processPayroll() {
        if (employees.isEmpty()) { System.out.println("No employees yet."); return; }

        System.out.print("\nEnter Employee ID: ");
        String id = sc.nextLine().trim();

        // validate: must be exactly 6 digits
        if (!id.matches("\\d{6}")) {
            System.out.println("Invalid Employee ID format. Must be exactly 6 digits.");
            return;
        }

        // find the employee in the list
        Employee emp = null;
        for (Employee e : employees) {
            if (e.getEmployeeId().equalsIgnoreCase(id)) { emp = e; break; }
        }
        if (emp == null) { System.out.println("Employee not found."); return; }

        System.out.println("Cut-off Period: [1] 1st-15th   [2] 16th-30th");
        System.out.print("Enter choice  : ");
        int c = readInt();

        @SuppressWarnings("unused")
        String cutoff;
        switch (c) {
            case 1 -> cutoff = "1st - 15th";
            case 2 -> cutoff = "16th - 30th";
            default -> {
                System.out.println("Invalid choice.");
                return;
            }
        }

        System.out.print("Enter Loan Deduction (PHP): ");
        double loan = readDouble();

        System.out.println("\nEnter Time In / Time Out for 15 working days.");
        System.out.println("Fixed schedule : 8:00 AM (8.0) to 5:00 PM (17.0)");
        System.out.println("Lunch break is fixed from 12:00 PM to 1:00 PM (1 hour automatically deducted).");
        System.out.println("If Time In > 8.0, late hours will be deducted from salary.");
        System.out.println("Up to 5 absences are treated as paid leave; excess absences are deducted.");
        System.out.println("Enter 0 for both if ABSENT.\n");

        double[] timeIn  = new double[15];
        double[] timeOut = new double[15];

        for (int i = 0; i < 15; i++) {
            System.out.printf("Day %2d  Time In : ", (i + 1)); timeIn[i]  = readDouble();
            System.out.printf("Day %2d  Time Out: ", (i + 1)); timeOut[i] = readDouble();
        }

        Payroll payroll = new Payroll(emp, timeIn, timeOut, loan, c);
        payroll.compute();
        payroll.printPayslip();
    }

    static void viewEmployees() {
        if (employees.isEmpty()) { System.out.println("No employees on record."); return; }
        System.out.println("\n-- Employee List --");
        System.out.printf("%-15s %-20s %-15s %s%n", "ID", "Name", "Type", "Rate");
        System.out.println("-".repeat(60));
        for (Employee e : employees) {
            String unit = e.getEmployeeType().equals("Part-time") ? "/hr" : "/mo";
            System.out.printf("%-15s %-20s %-15s PHP %,.2f%s%n",
                    e.getEmployeeId(), e.getEmployeeName(), e.getEmployeeType(), e.getBasicSalary(), unit);
        }
    }

    // safe int input — returns -1 if invalid
    static int readInt() {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }

    // safe double input — returns 0 if invalid
    static double readDouble() {
        try { return Double.parseDouble(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return 0; }
    }
}