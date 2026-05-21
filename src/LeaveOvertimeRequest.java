// model for one leave or overtime request, admin can approve or reject it

public class LeaveOvertimeRequest {

    // either LEAVE or OVERTIME
    public enum RequestType { LEAVE, OVERTIME }

    // fields
    @SuppressWarnings("FieldMayBeFinal")
    private String      employeeId;   // who made the request
    @SuppressWarnings("FieldMayBeFinal")
    private String      employeeName; // for easy display
    @SuppressWarnings("FieldMayBeFinal")
    private RequestType requestType;  // LEAVE or OVERTIME
    @SuppressWarnings("FieldMayBeFinal")
    private String      details;      // e.g. "2 days leave" or "3 OT hours"
    private String      status;       // Pending, Approved, or Rejected

    // all new requests start as Pending
    public LeaveOvertimeRequest(String employeeId, String employeeName,
                                 RequestType requestType, String details) {
        this.employeeId   = employeeId;
        this.employeeName = employeeName;
        this.requestType  = requestType;
        this.details      = details;
        this.status       = "Pending";
    }

    // getters
    public String      getEmployeeId()   { return employeeId; }
    public String      getEmployeeName() { return employeeName; }
    public RequestType getRequestType()  { return requestType; }
    public String      getDetails()      { return details; }
    public String      getStatus()       { return status; }

    // setter: only status can change after creation
    public void setStatus(String status) { this.status = status; }

    // how this request looks when printed
    @Override
    public String toString() {
        return String.format("  [%s] %s (%s) - %s | Status: %s",
                requestType, employeeName, employeeId, details, status);
    }
}