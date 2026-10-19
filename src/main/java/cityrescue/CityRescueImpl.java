package cityrescue;

import cityrescue.*;
import cityrescue.enums.*;
import cityrescue.exceptions.*;

import java.util.Arrays;

public class CityRescueImpl implements CityRescue {

    // Constants
    private static final int MAX_STATIONS = 20;
    private static final int MAX_UNITS = 50;
    private static final int MAX_INCIDENTS = 200;

    // Map
    private CityMap map;
    private int obstacleCount;

    // Stations
    private Station[] stations;
    private int stationCount;
    private int nextStationId;

    // Units
    private Unit[] units;
    private int unitCount;
    private int nextUnitId;

    // Incidents
    private Incident[] incidents;
    private int incidentCount;
    private int nextIncidentId;

    // Tick
    private int tick;

    @Override
    public void initialise(int width, int height) throws InvalidGridException {

        // "width" and "height" must be >= 0
        if (width <= 0 || height <= 0) {
            throw new InvalidGridException("Invalid grid dimensions");
        }

        // Initialise map
        map = new CityMap(width, height);
        obstacleCount = 0;

        // Initialise station variables
        stations = new Station[MAX_STATIONS];
        stationCount = 0;
        nextStationId = 1;

        // Initialise unit variables
        units = new Unit[MAX_UNITS];
        unitCount = 0;
        nextUnitId = 1;

        // Initialise incident variables
        incidents = new Incident[MAX_INCIDENTS];
        incidentCount = 0;
        nextIncidentId = 1;

        // Initialise tick
        tick = 0;
    }

    @Override
    public int[] getGridSize() {
        int width = map.getWidth();
        int height = map.getHeight();
        return new int[] { width, height };
    }

    @Override
    public void addObstacle(int x, int y) throws InvalidLocationException {

        // Location must be within map bounds
        if (!map.inBounds(x, y)) {
            throw new InvalidLocationException("Invalid location");
        }

        map.addObstacle(x, y);
        obstacleCount++;
    }

    @Override
    public void removeObstacle(int x, int y) throws InvalidLocationException {

        // Location must be within map bounds
        if (!map.inBounds(x, y)) {
            throw new InvalidLocationException("Invalid location");
        }

        map.removeObstacle(x, y);
        obstacleCount--;
    }

    @Override
    public int addStation(String name, int x, int y) throws CapacityExceededException, InvalidNameException, InvalidLocationException {

        // Must be less than the max allowed stations
        if (stationCount >= MAX_STATIONS) {
            throw new CapacityExceededException("Maximum number of stations reached");
        }

        // "name" must not be blank
        if (name == null || name.isBlank()) {
            throw new InvalidNameException("Invalid name");
        }

        // Location must be within map bounds and unblocked
        if (!map.inBounds(x, y) || map.isBlocked(x, y)) {
            throw new InvalidLocationException("Invalid location");
        }

        // Store original state of "nextStationId" for return
        int id = nextStationId;
        stations[stationCount++] = new Station(id, name, x, y, MAX_UNITS);
        nextStationId++;

        return id;
    }

    @Override
    public void removeStation(int stationId) throws IDNotRecognisedException, IllegalStateException {

        // Search for station object
        for (int i = 0; i < stationCount; i++) {
            if (stations[i].getStationId() == stationId) {

                // Station must have no units
                if (stations[i].getUnitCount() != 0) {
                    throw new IllegalStateException("Station has units");
                }

                // Remove station from "stations"
                stations[i] = stations[stationCount - 1];
                stations[stationCount - 1] = null;
                stationCount--;
                return;
            }
        }

        // Throws error if loop completes without finding the station
        throw new IDNotRecognisedException("Station not found");
    }

    @Override
    public void setStationCapacity(int stationId, int maxUnits) throws IDNotRecognisedException, InvalidCapacityException {

        // Search for station object
        for (int i = 0; i < stationCount; i++) {
            if (stations[i].getStationId() == stationId) {

                // "maxUnits" must be greater than 0 and greater than current unit count
                int stationUnitCount = stations[i].getUnitCount();
                if (maxUnits < 0 || maxUnits < stationUnitCount) {
                    throw new InvalidCapacityException("Invalid capacity");
                }

                stations[i].setCapacity(maxUnits);
                return;
            }
        }

        // Throws error if loop completes without finding the station
        throw new IDNotRecognisedException("Station not found");
    }

    @Override
    public int[] getStationIds() {

        // Create new array to store station IDs
        int[] stationIds = new int[stationCount];

        // For each station, add its ID to "stationIds"
        for (int i = 0; i < stationCount; i++) {
            stationIds[i] = stations[i].getStationId();
        }

        // Return array in ascending order
        Arrays.sort(stationIds);
        return stationIds;
    }

    @Override
    public int addUnit(int stationId, UnitType type) throws CapacityExceededException, IDNotRecognisedException, InvalidUnitException, IllegalStateException {

        // Must be less than the max allowed units
        if (unitCount >= MAX_UNITS) {
            throw new CapacityExceededException("Maximum amount of units reached");
        }

        // "type" must not be null
        if (type == null) {
            throw new InvalidUnitException("Invalid unit type");
        }

        // Station must exist
        Station station = null;
        for (int i = 0; i < stationCount; i++) {
            if (stations[i].getStationId() == stationId) {
                station = stations[i];
                break;
            }
        }
        if (station == null) {
            throw new IDNotRecognisedException("Station not found");
        }

        // Station must have space for a new unit
        if (station.getUnitCount() >= station.getCapacity()) {
            throw new IllegalStateException("Station capacity full");
        }

        // Create new unit
        int id = nextUnitId++;
        Unit unit = null;
        switch (type) {
            case AMBULANCE -> unit = new Ambulance(id, stationId, station.getX(), station.getY());
            case FIRE_ENGINE -> unit = new FireEngine(id, stationId, station.getX(), station.getY());
            case POLICE_CAR -> unit = new PoliceCar(id, stationId, station.getX(), station.getY());
        }

        // Add unit to "units" and "station"
        units[unitCount++] = unit;
        station.addUnit(unit);

        return id;
    }

    @Override
    public void decommissionUnit(int unitId) throws IDNotRecognisedException, IllegalStateException {
        
        // Unit must exist
        Unit unit = null;
        for (int i = 0; i < unitCount; i++) {
            if (units[i].getUnitId() == unitId) {
                unit = units[i];
                break;
            }
        }
        if (unit == null) {
            throw new IDNotRecognisedException("Unit not found");
        }

        // Unit must not be EN_ROUTE or AT_SCENE
        if (unit.getStatus() == UnitStatus.EN_ROUTE || unit.getStatus() == UnitStatus.AT_SCENE) {
            throw new IllegalStateException("Invalid unit state");
        }

        // Find home station object
        Station homeStation = null;
        for (int i = 0; i < stationCount; i++) {
            if (stations[i].getStationId() == unit.getHomeStationId()) {
                homeStation = stations[i];
            }
        }
        if (homeStation == null) {
            throw new IDNotRecognisedException("Station not found");
        }

        // Remove unit from "homeStation" and "units"
        homeStation.removeUnit(unit);
        for (int i = 0; i < unitCount; i++) {
            if (units[i] == unit) {
                units[i] = units[unitCount - 1];
                units[unitCount - 1] = null;
                unitCount--;
                return;
            }
        }
    }

    @Override
    public void transferUnit(int unitId, int newStationId) throws IDNotRecognisedException, IllegalStateException {
        
        // Unit must exist
        Unit unit = null;
        for (int i = 0; i < unitCount; i++) {
            if (units[i].getUnitId() == unitId) {
                unit = units[i];
                break;
            }
        }
        if (unit == null) {
            throw new IDNotRecognisedException("Unit not found");
        }

        // Get the current station and new station as objects
        int currentStationId = unit.getHomeStationId();
        Station currentStation = null;
        Station newStation = null;
        for (int i = 0; i < stationCount; i++) {
            if (stations[i].getStationId() == currentStationId) {
                currentStation = stations[i];
            } else if (stations[i].getStationId() == newStationId) {
                newStation = stations[i];
            }
        }

        // "currentStation" and "newStation" must exist
        if (currentStation == null || newStation == null) {
            throw new IDNotRecognisedException("Station not found");
        }

        // New station must have space for new unit
        if (newStation.getUnitCount() >= newStation.getCapacity()) {
            throw new IllegalStateException("Station capacity full");
        }

        // Unit must be idle
        if (unit.getStatus() != UnitStatus.IDLE) {
            throw new IllegalStateException("Invalid unit state");
        }

        // Transfer unit
        currentStation.removeUnit(unit);
        newStation.addUnit(unit);
        unit.setHomeStationId(newStationId);
        unit.setX(newStation.getX());
        unit.setY(newStation.getY());
    }

    @Override
    public void setUnitOutOfService(int unitId, boolean outOfService) throws IDNotRecognisedException, IllegalStateException {
                
        // Unit must exist
        Unit unit = null;
        for (int i = 0; i < unitCount; i++) {
            if (units[i].getUnitId() == unitId) {
                unit = units[i];
                break;
            }
        }
        if (unit == null) {
            throw new IDNotRecognisedException("Unit not found");
        }

        // Unit must be idle or out of service
        if (outOfService) {
            if (unit.getStatus() != UnitStatus.IDLE) {
                throw new IllegalStateException("Invalid unit state");
            }
            unit.setStatus(UnitStatus.OUT_OF_SERVICE);
        } else {
            if (unit.getStatus() != UnitStatus.OUT_OF_SERVICE) {
                throw new IllegalStateException("Invalid unit state");
            }
            unit.setStatus(UnitStatus.IDLE);
        }
    }

    @Override
    public int[] getUnitIds() {

        // Create new array to store unit IDs
        int[] unitIds = new int[unitCount];

        // For each unit, add its ID to "unitIds"
        for (int i = 0; i < unitCount; i++) {
            unitIds[i] = units[i].getUnitId();
        }

        // Return array in ascending order
        Arrays.sort(unitIds);
        return unitIds;
    }

    @Override
    public String viewUnit(int unitId) throws IDNotRecognisedException {
        
        // Unit must exist
        Unit unit = null;
        for (int i = 0; i < unitCount; i++) {
            if (units[i].getUnitId() == unitId) {
                unit = units[i];
                break;
            }
        }
        if (unit == null) {
            throw new IDNotRecognisedException("Unit not found");
        }

        // Constructing the output
        int incidentId = (unit.getAssignedIncident() == null) ? -1 : unit.getAssignedIncident().getIncidentId();
        return String.format(
            "U#%d TYPE=%S HOME=%d LOC=(%d,%d) STATUS=%S INCIDENT=%d WORK=%d",
            unit.getUnitId(),
            unit.getType().name(),
            unit.getHomeStationId(),
            unit.getX(),
            unit.getY(),
            unit.getStatus().name(),
            incidentId,
            unit.getTicksRemaining()
        );
    }

    @Override
    public int reportIncident(IncidentType type, int severity, int x, int y) throws CapacityExceededException, IllegalStateException, InvalidSeverityException, InvalidLocationException {
        
        // Must be less than the max allowed incidents
        if (incidentCount >= MAX_INCIDENTS) {
            throw new CapacityExceededException("Maximum number of incidents reached");
        }

        // "type" must not be null
        if (type == null) {
            throw new IllegalStateException("Invalid type");
        }

        // "severity" must be >= 1 and <= 5
        if (severity < 1 || severity > 5) {
            throw new InvalidSeverityException("Invalid severity");
        }

        // Location must be in bounds and not blocked
        if (!map.inBounds(x, y) || map.isBlocked(x, y)) {
            throw new InvalidLocationException("Invalid location");
        }

        // Store original state of "nextIncidentId" for return
        int id = nextIncidentId;
        incidents[incidentCount++] = new Incident(id, type, severity, x, y);
        nextIncidentId++;

        return id;
    }

    @Override
    public void cancelIncident(int incidentId) throws IDNotRecognisedException, IllegalStateException {
        
        // Find incident object
        Incident incident = null;
        for (int i = 0; i < incidentCount; i++) {
            if (incidents[i].getIncidentId() == incidentId) {
                incident = incidents[i];
                break;
            }
        }
        if (incident == null) {
            throw new IDNotRecognisedException("Incident not found");
        }

        // Only allowed if REPORTED or DISPATCHED
        if (!(incident.getStatus() == IncidentStatus.REPORTED || incident.getStatus() == IncidentStatus.DISPATCHED)) {
            throw new IllegalStateException("Invalid incident state");
        }

        // Release assigned unit
        incident.releaseUnit();

        // Set status to CANCELLED
        incident.setStatus(IncidentStatus.CANCELLED);
    }

    @Override
    public void escalateIncident(int incidentId, int newSeverity) throws IDNotRecognisedException, InvalidSeverityException, IllegalStateException {
        
        // Incident must exist
        Incident incident = null;
        for (int i = 0; i < incidentCount; i++) {
            if (incidents[i].getIncidentId() == incidentId) {
                incident = incidents[i];
                break;
            }
        }
        if (incident == null) {
            throw new IDNotRecognisedException("Incident not found");
        }

        // "newSeverity" must be >= 1 and <= 5
        if (newSeverity < 1 || newSeverity > 5) {
            throw new InvalidSeverityException("Invalid severity");
        }

        // Must not be RESOLVED or CANCELLLED
        if (incident.getStatus() == IncidentStatus.RESOLVED || incident.getStatus() == IncidentStatus.CANCELLED) {
            throw new IllegalStateException("Invalid incident state");
        }

        // Update severity
        incident.setSeverity(newSeverity);
    }

    @Override
    public int[] getIncidentIds() {

        // Create new array to store incident IDs
        int[] incidentIds = new int[incidentCount];

        // For each incident, add its ID to "incidentIds"
        for (int i = 0; i < incidentCount; i++) {
            incidentIds[i] = incidents[i].getIncidentId();
        }

        // Return array in ascending order
        Arrays.sort(incidentIds);
        return incidentIds;
    }

    @Override
    public String viewIncident(int incidentId) throws IDNotRecognisedException {
   
        // Incident must exist
        Incident incident = null;
        for (int i = 0; i < incidentCount; i++) {
            if (incidents[i].getIncidentId() == incidentId) {
                incident = incidents[i];
                break;
            }
        }
        if (incident == null) {
            throw new IDNotRecognisedException("Incident not found");
        }

        // Constructing the output
        int unitId = (incident.getAssignedUnit() == null) ? -1 : incident.getAssignedUnit().getUnitId();
        return String.format(
            "I#%d TYPE=%S SEV=%d LOC=(%d,%d) STATUS=%S UNIT=%d",
            incident.getIncidentId(),
            incident.getType().name(),
            incident.getSeverity(),
            incident.getX(),
            incident.getY(),
            incident.getStatus().name(),
            unitId
        );
    }

    @Override
    public void dispatch() {

        // Get incident objects ordered by ID
        int[] sortedIds = getIncidentIds();
        Incident[] sortedIncidents = new Incident[incidentCount];

        // For each incident ID, add the incident object to "sortedIncidents"
        for (int i = 0; i < incidentCount; i++) {
            int idToSort = sortedIds[i];

            for (int j = 0; j < incidentCount; j++) {
                int currentId = incidents[j].getIncidentId();

                if (currentId == idToSort) {
                    sortedIncidents[i] = incidents[j];
                    break;
                }
            }
        }

        // Assign unit for each incident
        for (int i = 0; i < incidentCount; i++) {
            
            Incident incident = sortedIncidents[i];
            int incidentX = incident.getX();
            int incidentY = incident.getY();

            UnitType requiredUnit = null;
            int ticks = -1;
            if (incident.getType() == IncidentType.MEDICAL) {
                requiredUnit = UnitType.AMBULANCE;
                ticks = 2;
            }
            if (incident.getType() == IncidentType.CRIME) {
                requiredUnit = UnitType.POLICE_CAR;
                ticks = 3;
            }
            if (incident.getType() == IncidentType.FIRE) {
                requiredUnit = UnitType.FIRE_ENGINE;
                ticks = 4;
            }

            // Work through each unit to find best
            Unit bestUnit = null;
            int bestManDist = Integer.MAX_VALUE; // Arbitrary large number
            
            for (int j = 0; j < unitCount; j++) {

                Unit unit = units[j];
                int unitX = unit.getX();
                int unitY = unit.getY();

                // Skip if incorrect type, out of service, or already assigned
                if (unit.getType() != requiredUnit || unit.getStatus() == UnitStatus.OUT_OF_SERVICE || unit.getAssignedIncident() != null) {
                    continue;
                }
                
                // Only change for better manhattan distance or same distance with lower ID
                int manDist = Math.abs(incidentX - unitX) + Math.abs(incidentY - unitY);
                if (
                    (manDist < bestManDist)
                    || (manDist == bestManDist && unit.getUnitId() < bestUnit.getUnitId())
                    || (manDist == bestManDist && unit.getUnitId() == bestUnit.getUnitId() && unit.getHomeStationId() < bestUnit.getHomeStationId())
                ) {
                    bestManDist = manDist;
                    bestUnit = unit;
                }
            }

            // Assign bestUnit to incident
            if (bestUnit != null) {
                bestUnit.assignIncident(incident, ticks);
                incident.assignUnit(bestUnit);
            }
        }
    }

    @Override
    public void tick() {
        tick++;

        // Get unit objects ordered by ID
        int[] sortedIds = getUnitIds();
        Unit[] sortedUnits = new Unit[unitCount];

        // For each unit ID, add the unit object to "sortedUnits"
        for (int i = 0; i < unitCount; i++) {
            int idToSort = sortedIds[i];

            for (int j = 0; j < unitCount; j++) {
                int currentId = units[j].getUnitId();

                if (currentId == idToSort) {
                    sortedUnits[i] = units[j];
                    break;
                }
            }
        }

        // Move EN_ROUTE units (in ascending id)
        for (int i = 0; i < unitCount; i++) {
            Unit unit = sortedUnits[i];
            if (unit.getStatus() == UnitStatus.EN_ROUTE) {
                unit.moveTowardsIncident(map);
            }
        }

        // Mark arrivals
        for (int i = 0; i < unitCount; i++) {
            Unit unit = sortedUnits[i];
            if (unit.getStatus() == UnitStatus.EN_ROUTE) {
                Incident incident = unit.getAssignedIncident();
                if (unit.getX() == incident.getX() && unit.getY() == incident.getY()) {
                    unit.setStatus(UnitStatus.AT_SCENE);
                    incident.setStatus(IncidentStatus.IN_PROGRESS);
                }
            }
        }

        // Process on-scene work
        for (int i = 0; i < unitCount; i++) {
            Unit unit = sortedUnits[i];
            if (unit.getStatus() == UnitStatus.AT_SCENE) {
                unit.workOnIncident();
            }
        }

        // Resolve completed incidents
        for (int i = 0; i < unitCount; i++) {
            Unit unit = sortedUnits[i];
                if (unit.getTicksRemaining() == 0 && unit.getStatus() == UnitStatus.AT_SCENE) {
                    Incident incident = unit.getAssignedIncident();

                    unit.releaseIncident();
                    incident.releaseUnit();

                    unit.setStatus(UnitStatus.IDLE);
                    incident.setStatus(IncidentStatus.RESOLVED);
                }
        }
    }

    @Override
    public String getStatus() {
        String status = "";
        
        status += "TICK=" + tick + "\n";
        status += "STATIONS=" + stationCount + " UNITS=" + unitCount + " INCIDENTS=" + incidentCount + " OBSTACLES=" + obstacleCount + "\n";
        
        status += "INCIDENTS\n";
        int[] incidentIds = getIncidentIds();
        for (int i = 0; i < incidentCount; i++) {

            String incidentString = "";
            try {
                incidentString = viewIncident(incidentIds[i]);
            } catch (IDNotRecognisedException e) {
                // Ignore
            }

            status += incidentString + "\n";
        }

        status += "UNITS\n";
        int[] unitIds = getUnitIds();
        for (int i = 0; i < unitCount; i++) {
            
            String unitString = "";
            try {
                unitString = viewUnit(unitIds[i]);
            } catch (IDNotRecognisedException e) {
                // Ignore
            }

            status += unitString + "\n";
        }

        return status;
    }
}
