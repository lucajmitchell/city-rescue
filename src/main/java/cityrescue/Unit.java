package cityrescue;

import cityrescue.enums.*;
import cityrescue.Incident;

// Base class for all units
public abstract class Unit {

    protected int unitId;
    protected UnitType type;
    protected UnitStatus status;
    protected int x;
    protected int y;
    protected int homeStationId;
    protected Incident assignedIncident;
    protected int ticksRemaining;

    public Unit(int unitId, UnitType type, int homeStationId, int x, int y) {
        this.unitId = unitId;
        this.type = type;
        this.status = UnitStatus.IDLE;
        this.x = x;
        this.y = y;
        this.homeStationId = homeStationId;
        this.assignedIncident = null;
        this.ticksRemaining = 0;
    }

    // Get methods

    public int getUnitId() {
        return unitId;
    }

    public UnitType getType() {
        return type;
    }

    public UnitStatus getStatus() {
        return status;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHomeStationId() {
        return homeStationId;
    }

    public Incident getAssignedIncident() {
        return assignedIncident;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    // Set methods

    public void setStatus(UnitStatus status) {
        this.status = status;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.x = y;
    }

    public void setHomeStationId(int newHomeStationId) {
        homeStationId = newHomeStationId;
    }

    public void assignIncident(Incident incident, int ticks) {
        assignedIncident = incident;
        ticksRemaining = ticks;
        status = UnitStatus.EN_ROUTE;
    }

    public void releaseIncident() {
        assignedIncident = null;
        ticksRemaining = 0;
        status = UnitStatus.IDLE;
    }

    // Other methods

    public void workOnIncident() {
        ticksRemaining--;
    }

    public void moveTowardsIncident(CityMap map) {
        int targetX = assignedIncident.getX();
        int targetY = assignedIncident.getY();

        int currentDistance = Math.abs(x - targetX) + Math.abs(y - targetY);

        // Directions in order: N, E, S, W
        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        // Attempt to reduce Manhattan distance
        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];

            // Check next direction if move is legal
            if (!map.isLegalMove(newX, newY)) {
                continue; // Move on to next direction if illegal
            }

            // Check if move reduces manhattan distance
            int newDistance = Math.abs(newX - targetX) + Math.abs(newY - targetY);
            if (newDistance < currentDistance) {
                x = newX;
                y = newY;
                return; // Stop searching as move has been determined
            }
        }

        // If no good move found, take first legal move
        for (int i = 0; i < 4; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (map.isLegalMove(newX, newY)) {
                x = newX;
                y = newY;
                return; // Stop searching as a legal move has been found
            }
        }

        // No move found so don't move
    }

    // Abstract methods

    public abstract boolean canHandle(IncidentType type);
    public abstract int getTicksToResolve(int severity);
}