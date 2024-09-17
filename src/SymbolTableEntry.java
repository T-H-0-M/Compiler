import java.lang.reflect.Field;

public class SymbolTableEntry {
    private String name;
    private Tokeniser.TokenType type;
    private int line;
    private int col;
    private boolean isInitialized;
    private boolean isFunction;
    private boolean isConstant;

    public SymbolTableEntry(String name) {
        this.name = name;
        this.type = Tokeniser.TokenType.TUNDF;
        this.line = -1;
        this.col = -1;
        this.isInitialized = false;
        this.isFunction = false;
        this.isConstant = false;
    }

    public SymbolTableEntry(String name, Tokeniser.TokenType type, int line, int col) {
        this.name = name;
        this.type = type;
        this.line = line;
        this.col = col;
        this.isInitialized = false;
        this.isFunction = false;
        this.isConstant = false;
    }

    public String getName() {
        return name;
    }

    public Tokeniser.TokenType getType() {
        return type;
    }

    public void setType(Tokeniser.TokenType type) {
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

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("[");
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                result.append(field.getName()).append(": ").append(field.get(this)).append(", ");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        result.delete(result.length() - 2, result.length());
        result.append("]");
        return result.toString();
    }
}
