
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
    private String type;
    private String value;
    private int line;
    private int col;
    private boolean isInitialized;
    private boolean isFunction;
    private boolean isConstant;
    private boolean isArray;

    /* -------------- Constructors -------------- */
    public SymbolTableEntry(String name, String value) {
        this.name = name;
        this.type = null;
        this.line = -1;
        this.col = -1;
        this.isInitialized = false;
        this.isFunction = false;
        this.isConstant = false;
        this.isArray = false;
        this.value = value;
    }

    public SymbolTableEntry(String name) {
        this.name = name;
        this.type = null;
        this.line = -1;
        this.col = -1;
        this.isInitialized = false;
        this.isFunction = false;
        this.isConstant = false;
        this.isArray = false;
        this.value = null;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        System.out.println("setting node - " + type);
        this.type = type;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    public boolean isFunction() {
        return isFunction;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public void setFunction(boolean isFunction) {
        this.isFunction = isFunction;
    }

    public void setConstant(boolean isConstant) {
        this.isConstant = isConstant;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setIsArray(boolean isArray) {
        this.isArray = isArray;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        System.out.println(
                this.type);
        return String.format(
                "[name: %s, value: %s, type: %s]",
                name, value, type.toString());
    }
}
