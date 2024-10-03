import java.util.HashMap;
import java.util.Map;

/**
 * SymbolTable class
 * 
 * This class defines the underlying data structure and methods to be
 * used by the symbol table. This data structure resides within a stack
 * and represents a scope within the source program.
 * A single symbol table represents a scope with all contained entries
 * therefore attributed to that scope.
 * 
 * Date: 2024-09-27
 *
 * @author Thomas Bandy, Benjamin Rogers
 * @version 1.0
 */
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
