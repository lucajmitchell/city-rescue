package cityrescue;

import cityrescue.enums.*;

public class PoliceCar extends Unit {

    public PoliceCar(int unitId, int homeStationId, int x, int y) {
        super(unitId, UnitType.POLICE_CAR, homeStationId, x, y);
    }

    @Override
    public boolean canHandle(IncidentType type) {
        return type == IncidentType.CRIME;
    }

    @Override
    public int getTicksToResolve(int severity) {
        return 3; // PoliceCar always takes 3 ticks
    }
}