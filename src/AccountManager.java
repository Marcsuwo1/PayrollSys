// shared data store — all classes read/write from here using static fields

import java.util.ArrayList;
import java.util.HashMap;

public class AccountManager {

    // list of all employees (used for payroll)
    public static ArrayList<Employee> employees = new ArrayList<>();

    // employee login credentials: key = employee ID, value = password
    public static HashMap<String, String> employeeAccounts = new HashMap<>();

    // all leave and overtime requests waiting for admin action
    public static ArrayList<LeaveOvertimeRequest> requests = new ArrayList<>();

    // time records submitted by employees, waiting to be processed
    public static ArrayList<TimeRecord> pendingTimeRecords = new ArrayList<>();


    // inner class: holds one complete time submission from an employee
    public static class TimeRecord {

        // who submitted this record
        @SuppressWarnings("FieldMayBeFinal")
        private String   employeeId;
        @SuppressWarnings("FieldMayBeFinal")
        private String   employeeName;

        // 15 days of time entries
        @SuppressWarnings("FieldMayBeFinal")
        private double[] timeIn;
        @SuppressWarnings("FieldMayBeFinal")
        private double[] timeOut;

        // 1 = 1st-15th, 2 = 16th-30th
        @SuppressWarnings("FieldMayBeFinal")
        private int cutoffPeriod;

        // false until the admin processes this record
        private boolean processed;

        // constructor: new records start as unprocessed
        public TimeRecord(String employeeId, String employeeName,
                          double[] timeIn, double[] timeOut, int cutoffPeriod) {
            this.employeeId   = employeeId;
            this.employeeName = employeeName;
            this.timeIn       = timeIn;
            this.timeOut      = timeOut;
            this.cutoffPeriod = cutoffPeriod;
            this.processed    = false;
        }

        // getters
        public String   getEmployeeId()   { return employeeId; }
        public String   getEmployeeName() { return employeeName; }
        public double[] getTimeIn()       { return timeIn; }
        public double[] getTimeOut()      { return timeOut; }
        public int      getCutoffPeriod() { return cutoffPeriod; }
        public boolean  isProcessed()     { return processed; }

        // marks this record as done so it won't show in the queue again
        public void markProcessed()       { this.processed = true; }

        // how this record looks when printed in a list
        @Override
        public String toString() {
            String period = (cutoffPeriod == 1) ? "1st-15th" : "16th-30th";
            return employeeName + " (ID: " + employeeId + ") | Cut-off: " + period;
        }
    }


    // finds an employee by ID; returns null if not found
    public static Employee findEmployeeById(String id) {
        for (Employee e : employees) {
            if (e.getEmployeeId().equalsIgnoreCase(id)) {
                return e;
            }
        }
        return null;
    }

    // checks if the given ID and password match a saved account
    public static boolean isValidEmployeeLogin(String id, String password) {
        if (!employeeAccounts.containsKey(id)) return false;
        return employeeAccounts.get(id).equals(password);
    }
}