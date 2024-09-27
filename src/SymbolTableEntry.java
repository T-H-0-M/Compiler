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

    // public SymbolTableEntry(String name, Tokeniser.TokenType type, int line, int
    // col) {
    // this.name = name;
    // this.type = type;
    // this.line = line;
    // this.col = col;
    // this.isInitialized = false;
    // this.isFunction = false;
    // this.isConstant = false;
    // this.isArray = false;
    // this.value = null;
    // }
    //
    // public SymbolTableEntry(String name, Tokeniser.TokenType type, int line, int
    // col, boolean isFunction,
    // boolean isConstant, boolean isArray) {
    // this.name = name;
    // this.type = type;
    // this.line = line;
    // this.col = col;
    // this.isInitialized = false;
    // this.isFunction = isFunction;
    // this.isConstant = isConstant;
    // this.isArray = isArray;
    // }

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

    // @Override
    // public String toString() {
    // return String.format(
    // "[name: %s, type: %s, line: %d, col: %d, isInitialized: %b, isFunction: %b,
    // isConstant: %b, isArray: %b]",
    // name, type, line, col, isInitialized, isFunction, isConstant, isArray);
    // }

    @Override
    public String toString() {
        System.out.println(
                this.type);
        return String.format(
                "[name: %s, value: %s, type: %s]",
                name, value, type.toString());
    }
}
