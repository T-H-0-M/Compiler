/**
 * SymbolTableEntry class
 * 
 * A child class of SymbolTable.
 * Each entry stores the information of a parsed node.
 * An entry may have incomplete fields at time of construction.
 * Fields will be filled in as the parser progresses.
 * 
 * Date: 2024-09-27
 *
 * @author Thomas Bandy, Benjamin Rogers
 * @version 1.0
 */

public class SymbolTableEntry {
    private String name;
    private SymbolType symbolType;
    private String memoryLocation;
    private String value;
    private DataType dataType;
    private boolean isInitialised;

    /* -------------- Constructors -------------- */
    public SymbolTableEntry() {
        this.name = null;
        this.symbolType = null;
        this.memoryLocation = null;
        this.value = null;
        this.dataType = null;
        this.isInitialised = false;
    }

    public SymbolTableEntry(String name) {
        this.name = name;
        this.symbolType = null;
        this.memoryLocation = null;
        this.value = null;
        this.dataType = null;
        this.isInitialised = false;
    }

    public SymbolTableEntry(String name, SymbolType symbolType, String value, DataType dataType) {
        this.name = name;
        this.symbolType = symbolType;
        this.memoryLocation = null;
        this.value = value;
        this.dataType = dataType;
        this.isInitialised = false;
    }

    /* -------------- Getters -------------- */
    public String getName() {
        return name;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public String getMemoryLocation() {
        return memoryLocation;
    }

    public String getValue() {
        return value;
    }

    public DataType getDataType() {
        return dataType;
    }

    public boolean isInitialised() {
        return isInitialised;
    }

    /* -------------- Setters -------------- */
    public void setName(String name) {
        this.name = name;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public void setMemoryLocation(String memoryLocation) {
        this.memoryLocation = memoryLocation;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public void setInitialised(boolean isInitialised) {
        this.isInitialised = isInitialised;
    }

    @Override
    public String toString() {
        return String.format(
                "[name: %s, value: %s, type: %s, memoryLocation: %s, dataType: %s, isInitialised: %s]",
                name, value, symbolType != null ? symbolType.toString() : "null", memoryLocation,
                dataType != null ? dataType.toString() : "null", isInitialised);
    }
}
