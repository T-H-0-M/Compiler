import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, SymbolTableEntry> table;

    public SymbolTable() {
        table = new HashMap<String, SymbolTableEntry>();
    }

    public void destroy() {
        table.clear();
    }

    public void enter (String name) {
        SymbolTableEntry entry = new SymbolTableEntry(name);
        table.put(name, entry);
    }

    public void enter (String name, Tokeniser.TokenType type, int line, int col) {
        SymbolTableEntry entry = new SymbolTableEntry(name, type, line, col);
        table.put(name, entry);
    }

    public SymbolTableEntry find (String name) {
        return table.get(name);
    }

    // returns the completed object for validation
    public SymbolTableEntry set_attributes(String name, Tokeniser.TokenType type, int line, int col) {
        SymbolTableEntry entry = table.get(name);
        entry.setType(type);
        entry.setLine(line);
        entry.setCol(col);
        return entry;
    }

    public SymbolTableEntry get_attributes (String name) {
        return table.get(name);
    }

    @Override
    public String toString() {
        if (table.isEmpty()) {
            return "Symbol table is empty";
        }
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, SymbolTableEntry> entry : table.entrySet()) {
            result.append(entry.toString());
        }
        return result.toString();
    }
}
