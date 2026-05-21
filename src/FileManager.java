import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileManager {

    // file paths (relative to wherever the program is run from)
    private static final String EMPLOYEES_FILE    = "employees.txt";
    private static final String TIME_RECORDS_FILE = "time_records.txt";
    private static final String REQUESTS_FILE     = "requests.txt";
    private static final String PAYSLIPS_FILE     = "payslips.txt";

    // delimiter used to separate fields within one line
    private static final String SEP = "|";


    public static void loadAll() {
        loadEmployees();
        loadTimeRecords();
        loadRequests();
    }


    public static void saveEmployees() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(EMPLOYEES_FILE, false))) {
            for (Employee e : AccountManager.employees) {
                String password = AccountManager.employeeAccounts.getOrDefault(
                        e.getEmployeeId(), "");
                pw.println(
                    e.getEmployeeId()   + SEP +
                    e.getEmployeeName() + SEP +
                    e.getEmployeeType() + SEP +
                    e.getBasicSalary()  + SEP +
                    e.getCutoffPeriod() + SEP +
                    password
                );
            }
        } catch (IOException ex) {
            System.out.println("  [FileManager] Error saving employees: " + ex.getMessage());
        }
    }

    private static void loadEmployees() {
        File f = new File(EMPLOYEES_FILE);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\" + SEP, -1);
                if (parts.length < 6) continue;  // skip malformed lines

                String id       = parts[0];
                String name     = parts[1];
                String type     = parts[2];
                double salary   = parseDouble(parts[3]);
                int    cutoff   = parseInt(parts[4]);
                String password = parts[5];

                // avoid duplicates (in case loadAll() is called more than once)
                if (AccountManager.findEmployeeById(id) == null) {
                    AccountManager.employees.add(new Employee(id, name, type, salary, cutoff));
                    AccountManager.employeeAccounts.put(id, password);
                }
            }
        } catch (IOException ex) {
            System.out.println("  [FileManager] Error loading employees: " + ex.getMessage());
        }
    }


    public static void saveTimeRecords() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TIME_RECORDS_FILE, false))) {
            for (AccountManager.TimeRecord r : AccountManager.pendingTimeRecords) {
                pw.println(
                    r.getEmployeeId()   + SEP +
                    r.getEmployeeName() + SEP +
                    r.getCutoffPeriod() + SEP +
                    r.isProcessed()
                );
                pw.println(doubleArrayToString(r.getTimeIn()));
                pw.println(doubleArrayToString(r.getTimeOut()));
                pw.println("---");
            }
        } catch (IOException ex) {
            System.out.println("  [FileManager] Error saving time records: " + ex.getMessage());
        }
    }

    private static void loadTimeRecords() {
        File f = new File(TIME_RECORDS_FILE);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String headerLine;
            while ((headerLine = br.readLine()) != null) {
                headerLine = headerLine.trim();
                if (headerLine.isEmpty() || headerLine.equals("---")) continue;

                String[] header = headerLine.split("\\" + SEP, -1);
                if (header.length < 4) continue;

                String  id        = header[0];
                String  name      = header[1];
                int     cutoff    = parseInt(header[2]);
                boolean processed = Boolean.parseBoolean(header[3]);

                String timeInLine  = br.readLine();
                String timeOutLine = br.readLine();
                br.readLine(); // consume the "---" separator

                if (timeInLine == null || timeOutLine == null) break;

                double[] timeIn  = stringToDoubleArray(timeInLine.trim());
                double[] timeOut = stringToDoubleArray(timeOutLine.trim());

                AccountManager.TimeRecord record =
                        new AccountManager.TimeRecord(id, name, timeIn, timeOut, cutoff);
                if (processed) record.markProcessed();

                AccountManager.pendingTimeRecords.add(record);
            }
        } catch (IOException ex) {
            System.out.println("  [FileManager] Error loading time records: " + ex.getMessage());
        }
    }


    public static void saveRequests() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(REQUESTS_FILE, false))) {
            for (LeaveOvertimeRequest req : AccountManager.requests) {
                pw.println(
                    req.getEmployeeId()           + SEP +
                    req.getEmployeeName()          + SEP +
                    req.getRequestType().name()    + SEP +
                    req.getDetails()               + SEP +
                    req.getStatus()
                );
            }
        } catch (IOException ex) {
            System.out.println("  [FileManager] Error saving requests: " + ex.getMessage());
        }
    }

    private static void loadRequests() {
        File f = new File(REQUESTS_FILE);
        if (!f.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\" + SEP, -1);
                if (parts.length < 5) continue;

                String id      = parts[0];
                String name    = parts[1];
                String typeStr = parts[2];
                String details = parts[3];
                String status  = parts[4];

                LeaveOvertimeRequest.RequestType type;
                try {
                    type = LeaveOvertimeRequest.RequestType.valueOf(typeStr);
                } catch (IllegalArgumentException e) {
                    continue; // skip unrecognized type
                }

                LeaveOvertimeRequest req =
                        new LeaveOvertimeRequest(id, name, type, details);
                req.setStatus(status);
                AccountManager.requests.add(req);
            }
        } catch (IOException ex) {
            System.out.println("  [FileManager] Error loading requests: " + ex.getMessage());
        }
    }

    public static void appendPayslip(String payslipText) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(PAYSLIPS_FILE, true))) {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pw.println("Processed on: " + timestamp);
            pw.println(payslipText);
            pw.println(); // blank line between payslips
        } catch (IOException ex) {
            System.out.println("  [FileManager] Error saving payslip: " + ex.getMessage());
        }
    }


    // converts a double[] to a comma-separated string: "8.0,17.0,8.5,..."
    private static String doubleArrayToString(double[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    // parses a comma-separated string back to double[]; always returns 15 elements
    private static double[] stringToDoubleArray(String s) {
        String[] parts = s.split(",", -1);
        double[] arr   = new double[15];
        for (int i = 0; i < arr.length && i < parts.length; i++) {
            arr[i] = parseDouble(parts[i].trim());
        }
        return arr;
    }

    private static int    parseInt(String s)    { try { return Integer.parseInt(s.trim());    } catch (NumberFormatException e) { return 0; } }
    private static double parseDouble(String s) { try { return Double.parseDouble(s.trim());  } catch (NumberFormatException e) { return 0; } }
}