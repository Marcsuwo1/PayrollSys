// employee model — stores basic info for one employee

public class Employee {

    // fields: data we keep for each employee
    private String employeeId;    // e.g. "123456"
    private String employeeName;  // e.g. "Juan dela Cruz"
    private String type;          // Regular / Probationary / Contractual / Part-time
    private double basicSalary;   // monthly rate (or hourly if Part-time)
    private int    cutoffPeriod;  // 1 = 1st–15th, 2 = 16th–30th

    // default constructor: creates an empty employee object
    public Employee() {
        this.employeeId   = "";
        this.employeeName = "";
        this.type         = "";
        this.basicSalary  = 0.0;
        this.cutoffPeriod = 1;
    }

    // parameterized constructor: use when all info is ready
    public Employee(String employeeId, String employeeName, String type,
                    double basicSalary, int cutoffPeriod) {
        this.employeeId   = employeeId;
        this.employeeName = employeeName;
        this.type         = type;
        this.basicSalary  = basicSalary;
        this.cutoffPeriod = cutoffPeriod;
    }

    // getters: read private fields from outside this class
    public String getEmployeeId()   { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getEmployeeType() { return type; }
    public double getBasicSalary()  { return basicSalary; }
    public int    getCutoffPeriod() { return cutoffPeriod; }

    // setters: update private fields from outside this class
    public void setEmployeeId(String id)    { this.employeeId = id; }
    public void setEmployeeName(String name){ this.employeeName = name; }
    public void setType(String type)        { this.type = type; }
    public void setBasicSalary(double s)    { this.basicSalary = s; }
    public void setCutoffPeriod(int c)      { this.cutoffPeriod = c; }
}