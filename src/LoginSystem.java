import java.util.Scanner;

public class LoginSystem {

    // one scanner shared across the whole program
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        // load all saved data from disk before showing the menu
        FileManager.loadAll();

        // keeps the login screen running until the user exits
        boolean running = true;
        while (running) {
            running = showRoleSelection();
        }

        System.out.println("\n  Thank you for using ABC Company System. Goodbye!");
        sc.close();
    }

    private static boolean showRoleSelection() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║                ABC Company               ║");
        System.out.println("  ╠══════════════════════════════════════════╣");
        System.out.println("  ║   Please select your role to continue:   ║");
        System.out.println("  ║                                          ║");
        System.out.println("  ║   [1] Login as Admin                     ║");
        System.out.println("  ║   [2] Login as Employee                  ║");
        System.out.println("  ║   [3] Exit Program                       ║");
        System.out.println("  ╚══════════════════════════════════════════╝");
        System.out.print("  Enter choice: ");

        int choice = readInt();

        switch (choice) {
            case 1 -> {
                // try admin login; open admin menu if successful
                boolean loggedIn = adminLogin();
                if (loggedIn) {
                    AdminMenu adminMenu = new AdminMenu(sc);
                    adminMenu.show();
                }
                return true;
            }
            case 2 -> {
                // try employee login; open employee menu if successful
                Employee loggedInEmp = employeeLogin();
                if (loggedInEmp != null) {
                    EmployeeMenu empMenu = new EmployeeMenu(sc, loggedInEmp);
                    empMenu.show();
                }
                return true;
            }
            case 3 -> {
                return false; // exit the program
            }
            default -> {
                System.out.println("  Invalid choice. Please enter 1, 2, or 3.");
                return true;
            }
        }
    }

    private static boolean adminLogin() {
        System.out.println();
        System.out.println("  ===== ADMIN LOGIN =====");

        // admin gets 3 attempts before being locked out
        int attemptsLeft = 3;
        while (attemptsLeft > 0) {
            System.out.print("  Username: ");
            String username = sc.nextLine().trim();
            System.out.print("  Password: ");
            String password = sc.nextLine().trim();

            if (username.equals("admin") && password.equals("admin")) {
                System.out.println("  Login successful! Welcome, Admin.");
                return true;
            } else {
                attemptsLeft--;
                if (attemptsLeft > 0) {
                    System.out.println("  Incorrect credentials. " + attemptsLeft + " attempt(s) remaining.");
                } else {
                    System.out.println("  Too many failed attempts. Returning to main menu.");
                }
            }
        }
        return false;
    }

    private static Employee employeeLogin() {
        System.out.println();
        System.out.println("  ===== EMPLOYEE LOGIN =====");

        // no accounts yet — tell them to ask admin
        if (AccountManager.employeeAccounts.isEmpty()) {
            System.out.println("  No employee accounts exist yet.");
            System.out.println("  Please ask your admin to create an account for you.");
            return null;
        }

        // employee gets 3 attempts
        int attemptsLeft = 3;
        while (attemptsLeft > 0) {
            System.out.print("  Employee ID (6 digits): ");
            String id = sc.nextLine().trim();

            System.out.print("  Password              : ");
            String password = sc.nextLine().trim();

            // ID must be exactly 6 digits
            if (!id.matches("\\d{6}")) {
                System.out.println("  Employee ID must be exactly 6 digits.");
                attemptsLeft--;
                continue;
            }

            // check credentials
            if (AccountManager.isValidEmployeeLogin(id, password)) {
                Employee emp = AccountManager.findEmployeeById(id);
                System.out.println("  Login successful! Welcome, " + emp.getEmployeeName() + ".");
                return emp;
            } else {
                attemptsLeft--;
                if (attemptsLeft > 0) {
                    System.out.println("  Incorrect ID or password. " + attemptsLeft + " attempt(s) remaining.");
                } else {
                    System.out.println("  Too many failed attempts. Returning to main menu.");
                }
            }
        }
        return null;
    }

    // safe int input — returns -1 if invalid
    private static int readInt() {
        try { return Integer.parseInt(sc.nextLine().trim()); }
        catch (NumberFormatException e) { return -1; }
    }
}