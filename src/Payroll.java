// service class — handles all payroll computation and payslip display

import java.util.Scanner;

public class Payroll {

    // inputs
    @SuppressWarnings("FieldMayBeFinal")
    private Employee employee;
    @SuppressWarnings("FieldMayBeFinal")
    private double[] timeIn;       // time in per day (e.g. 8.5 = 8:30 AM)
    @SuppressWarnings("FieldMayBeFinal")
    private double[] timeOut;      // time out per day
    @SuppressWarnings("FieldMayBeFinal")
    private double   loanDeduction;
    @SuppressWarnings("FieldMayBeFinal")
    private int      cutoffPeriod;

    // computed results — filled in by compute()
    private double totalHoursWorked;
    private double overtimeHours;
    private double undertimeHours;
    private double absences;
    private double lateHours;
    private double paidLeaveDays;
    private double grossPay;
    private double sss;
    private double philhealth;
    private double pagibig;
    private double tax;
    private double netPay;

    // scanner used to ask about paid leave during timekeeping
    @SuppressWarnings("FieldMayBeFinal")
    private Scanner sc = new Scanner(System.in);

    // constructor
    public Payroll(Employee employee, double[] timeIn, double[] timeOut,
                   double loanDeduction, int cutoffPeriod) {
        this.employee      = employee;
        this.timeIn        = timeIn;
        this.timeOut       = timeOut;
        this.loanDeduction = loanDeduction;
        this.cutoffPeriod  = cutoffPeriod;
    }

    // runs all calculations; call this before printPayslip()
    public void compute() {
        computeTimekeeping();   // step 1: hours, absences, late
        computeGrossPay();      // step 2: gross pay
        computeDeductions();    // step 3: government deductions
        netPay = grossPay - sss - philhealth - pagibig - tax - loanDeduction;
    }

    // step 1: loops through each day and tallies hours, absences, late, overtime
    private void computeTimekeeping() {
        // reset all counters
        totalHoursWorked = 0;
        overtimeHours    = 0;
        undertimeHours   = 0;
        absences         = 0;
        lateHours        = 0;
        paidLeaveDays    = 0;

        for (int i = 0; i < timeIn.length; i++) {

            // absent day: both values are 0
            if (timeIn[i] == 0 && timeOut[i] == 0) {
                System.out.printf("Day %2d is absent. Is this a Paid Leave? (Y/N): ", (i + 1));
                String answer = sc.nextLine().trim().toUpperCase();

                if (answer.equals("Y") && paidLeaveDays < 5) {
                    // still has paid leave credits
                    paidLeaveDays++;
                    System.out.println("   → Paid Leave granted. (" + (int)paidLeaveDays + "/5 used)");
                } else {
                    // no credits left or chose N
                    if (paidLeaveDays >= 5 && answer.equals("Y")) {
                        System.out.println("   → Paid leave limit (5) reached. Counted as unpaid absence.");
                    } else {
                        System.out.println("   → Counted as unpaid absence.");
                    }
                    absences++;
                    undertimeHours += 8.0; // full absent day = 8 hours undertime
                }
                continue;
            }

            // present day: check if late (standard start = 8:00 AM)
            if (timeIn[i] > 8.0) {
                lateHours += (timeIn[i] - 8.0);
            }

            // raw time span
            double span = timeOut[i] - timeIn[i];
            if (span < 0) span = 0;

            // deduct lunch break overlap (12:00 PM – 1:00 PM)
            double lunchStart   = 12.0;
            double lunchEnd     = 13.0;
            double overlapStart = Math.max(timeIn[i], lunchStart);
            double overlapEnd   = Math.min(timeOut[i], lunchEnd);
            double lunchDeduct  = Math.max(0.0, overlapEnd - overlapStart);

            double worked = span - lunchDeduct;
            if (worked < 0) worked = 0;

            // overtime: any work past 5:00 PM
            double overtime = 0;
            if (timeOut[i] > 17.0) {
                overtime = timeOut[i] - 17.0;
            }

            // undertime: worked less than 8 hours
            double undertime = 0;
            if (worked < 8.0) {
                undertime = 8.0 - worked;
            }

            // add to running totals
            totalHoursWorked += worked;
            overtimeHours    += overtime;
            undertimeHours   += undertime;
        }
    }

    // step 2: computes gross pay based on employee type
    private void computeGrossPay() {
        String type      = employee.getEmployeeType();
        double salary    = employee.getBasicSalary();
        double hourlyRate;

        if (type.equals("Part-time")) {
            // part-time: paid only for actual hours worked
            hourlyRate = salary;
            grossPay   = totalHoursWorked * hourlyRate;
            grossPay  += overtimeHours * hourlyRate * 1.25;

        } else {
            // regular/probationary/contractual: semi-monthly base pay
            hourlyRate = ((salary / 4.38) / 7) / 8.0;
            grossPay   = salary / 2.0;
            grossPay  += overtimeHours * hourlyRate * 1.25;
            grossPay  -= undertimeHours * hourlyRate;
            grossPay  -= lateHours * hourlyRate;
            if (grossPay < 0) grossPay = 0;
        }
    }

    // step 3: computes SSS, PhilHealth, Pag-IBIG, and withholding tax
    private void computeDeductions() {
        double monthlySalary = employee.getBasicSalary();

        // for part-time, estimate monthly by doubling this period's gross
        if (employee.getEmployeeType().equals("Part-time")) {
            monthlySalary = grossPay * 2;
        }

        // SSS: 4.5% of MSC, rounded to nearest 500, capped 5k–35k, split semi-monthly
        double msc = Math.min(Math.max(Math.round(monthlySalary / 500.0) * 500.0, 5000), 35000);
        sss = (msc * 0.045) / 2.0;

        // PhilHealth: 5% of salary, employee pays half, split semi-monthly
        philhealth = (Math.min(monthlySalary, 100000) * 0.05) / 2.0 / 2.0;

        // Pag-IBIG: 2% of salary, max PHP 100, split semi-monthly
        pagibig = Math.min(monthlySalary * 0.02, 100.0) / 2.0;

        // withholding tax based on semi-monthly taxable income
        double taxable = grossPay - sss - philhealth - pagibig;

        if      (taxable <= 10417)  tax = 0;
        else if (taxable <= 16667)  tax = (taxable - 10417) * 0.15;
        else if (taxable <= 33333)  tax = 937.50  + (taxable - 16667) * 0.20;
        else if (taxable <= 83333)  tax = 4270.83 + (taxable - 33333) * 0.25;
        else                         tax = 16770.83 + (taxable - 83333) * 0.30;
    }

    // prints the formatted payslip
    public void printPayslip() {
        String period = (cutoffPeriod == 1) ? "1st (1st–15th)" : "2nd (16th–30th)";

        System.out.println();
        System.out.println("========================================================");
        System.out.println("                   ABC Company");
        System.out.println("               Employee Payroll System");
        System.out.println("========================================================");
        System.out.println("  Employee ID   : " + employee.getEmployeeId());
        System.out.println("  Employee Name : " + employee.getEmployeeName());
        System.out.println("  Employee Type : " + employee.getEmployeeType());
        System.out.println("  Cut-off Period: " + period);
        System.out.println("--------------------------------------------------------");
        System.out.println("  TIMEKEEPING SUMMARY");
        System.out.printf ("  Hours Worked  : %.2f hrs%n",    totalHoursWorked);
        System.out.printf ("  Overtime      : %.2f hrs%n",    overtimeHours);
        System.out.printf ("  Undertime     : %.2f hrs%n",    undertimeHours);
        System.out.printf ("  Absences      : %.0f day(s)%n", absences);
        System.out.printf ("  Paid Leaves   : %.0f day(s)%n", paidLeaveDays);
        System.out.printf ("  Late          : %.2f hrs%n",    lateHours);
        System.out.println("--------------------------------------------------------");
        System.out.println("  EARNINGS");
        System.out.printf ("  Gross Pay     : PHP %,12.2f%n", grossPay);
        System.out.println("--------------------------------------------------------");
        System.out.println("  DEDUCTIONS");
        System.out.printf ("  SSS           : PHP %,12.2f%n", sss);
        System.out.printf ("  PhilHealth    : PHP %,12.2f%n", philhealth);
        System.out.printf ("  Pag-IBIG      : PHP %,12.2f%n", pagibig);
        System.out.printf ("  Tax           : PHP %,12.2f%n", tax);
        System.out.printf ("  Loan Deduction: PHP %,12.2f%n", loanDeduction);
        System.out.println("========================================================");
        System.out.printf ("  NET PAY       : PHP %,12.2f%n", netPay);
        System.out.println("========================================================");
    }

    // getter so other classes can read the net pay result
    public double getNetPay() { return netPay; }
}