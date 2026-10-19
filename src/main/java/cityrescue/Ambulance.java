package cityrescue;

import cityrescue.enums.*;

public class Ambulance extends Unit {

    public Ambulance(int unitId, int homeStationId, int x, int y) {
        super(unitId, UnitType.AMBULANCE, homeStationId, x, y);
    }

    @Override
    public boolean canHandle(IncidentType type) {
        return type == IncidentType.MEDICAL;
    }

    @Override
    public int getTicksToResolve(int severity) {
        return 2; // Ambulance always takes 2 ticks on scene
    }
}