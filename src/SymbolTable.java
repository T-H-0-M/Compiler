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
    private int globalOffset;

    public SymbolTable() {
        table = new HashMap<String, SymbolTableEntry>();
        this.globalOffset = 1000;
        // this.globalOffset = 0;
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
        if (!symbolTableEntry.getName().equals("")) {
            table.put(symbolTableEntry.getName(), symbolTableEntry);
        }
    }

    public SymbolTableEntry find(String name) {
        return table.get(name);
    }

    public SymbolTableEntry get_attributes(String name) {
        return table.get(name);
    }

    public boolean isDeclared(String name) {
        if (table.get(name) != null) {
            return true;
        }
        return false;
    }

    public DataType getDataType(String name) {
        return table.get(name).getDataType();
    }

    // TODO: Currently only allocating in a global scope
    public int allocateGlobal() {
        int currentOffset = globalOffset;
        globalOffset += 8;
        return currentOffset;

    }

    public String getScope(String name) {
        return "1";
    }

    public void setOffset(String name, int offset) {
        SymbolTableEntry entry = table.get(name);
        entry.setOffset(offset);
    }

    public int getOffset(String name) {
        return table.get(name).getOffset();
    }

    public void declare(String name) {
        SymbolTableEntry entry = new SymbolTableEntry(name);
        table.put(name, entry);
    }

    @Override
    public String toString() {
        if (table.isEmpty()) {
            return "Symbol table is empty";
        }
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, SymbolTableEntry> entry : table.entrySet()) {
            result.append(entry.toString() + "\n");
        }
        return result.toString();
    }
}
