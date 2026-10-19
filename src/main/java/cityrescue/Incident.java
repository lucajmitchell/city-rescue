package cityrescue;

import cityrescue.enums.*;

public class Incident {

    private int incidentId;
    private IncidentType type;
    private int severity;
    private int x;
    private int y;
    private IncidentStatus status;
    private Unit assignedUnit;

    public Incident(int incidentId, IncidentType type, int severity, int x, int y) {
        this.incidentId = incidentId;
        this.type = type;
        this.severity = severity;
        this.x = x;
        this.y = y;
        this.status = IncidentStatus.REPORTED;
        this.assignedUnit = null;
    }

    // Get methods

    public int getIncidentId() {
        return incidentId;
    }

    public IncidentType getType() {
        return type;
    }

    public int getSeverity() {
        return severity;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public Unit getAssignedUnit() {
        return assignedUnit;
    }

    // Set methods

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public void assignUnit(Unit unit) {
        this.assignedUnit = unit;
    }

    public void releaseUnit() {
        this.assignedUnit = null;
    }
}