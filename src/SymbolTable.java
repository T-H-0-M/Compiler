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

    public SymbolTable copy() {
        SymbolTable copy = new SymbolTable();
        for (Map.Entry<String, SymbolTableEntry> entry : table.entrySet()) {
            copy.enter(entry.getValue());
        }
        return copy;
    }

    public void enter(SymbolTableEntry symbolTableEntry) {
        table.put(symbolTableEntry.getName(), symbolTableEntry);
    }

    public SymbolTableEntry find(String name) {
        return table.get(name);
    }

    // returns the completed object for validation
    public SymbolTableEntry set_attributes(String name, String type, int line, int col) {
        SymbolTableEntry entry = table.get(name);
        entry.setType(type);
        entry.setLine(line);
        entry.setCol(col);
        return entry;
    }

    public SymbolTableEntry get_attributes(String name) {
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
