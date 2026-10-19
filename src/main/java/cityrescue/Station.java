package cityrescue;

import cityrescue.Unit;

public class Station {

    private int stationId;
    private String name;
    private int x;
    private int y;
    private int capacity;
    private Unit[] units;
    private int unitCount;

    public Station(int stationId, String name, int x, int y, int capacity) {
        this.stationId = stationId;
        this.name = name;
        this.x = x;
        this.y = y;
        this.capacity = capacity;
        this.units = new Unit[capacity];
        this.unitCount = 0;
    }

    // Get methods

    public int getStationId() {
        return stationId;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCapacity() {
        return capacity;
    }

    public Unit[] getUnits() {
        return units;
    }

    public int getUnitCount() {
        return unitCount;
    }

    // Set methods

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // Other methods

    public void addUnit(Unit unit) {
        units[unitCount++] = unit;
    }

    // Replace unit to be removed with last unit
    // Set last unit to null
    // Reduce unitCount accordingly
    public void removeUnit(Unit unit) {
        for (int i = 0; i < unitCount; i++) {
            if (units[i] == unit) {
                units[i] = units[unitCount - 1];
                units[unitCount - 1] = null;
                unitCount--;
                return;
            }
        }
    }
}