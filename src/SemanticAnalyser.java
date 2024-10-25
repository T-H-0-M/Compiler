import java.util.List;
import java.util.ArrayList;

public class SemanticAnalyser {
    private SymbolTable symbolTable;
    private List<String> errors;
    private OutputController outputController;

    public SemanticAnalyser() {
        this.symbolTable = new SymbolTable();
        this.errors = new ArrayList<>();
        this.outputController = new OutputController();
    }

    public SemanticAnalyser(SymbolTable symbolTable, OutputController outputController) {
        this.symbolTable = symbolTable;
        this.errors = new ArrayList<>();
        this.outputController = outputController;
    }

    public SymbolTable analyse(Node root) {
        traverse(root);
        return this.symbolTable;
    }

    private void traverse(Node node) {
        if (node == null)
            return;
        String nodeType = node.getType();

        switch (nodeType) {
            case "NASGN":
                handleAssignmentChecks(node);
                break;
            case "NPLEQ":
                handleAssignmentChecks(node);
                break;
            case "NMNEQ":
                handleAssignmentChecks(node);
                break;
            case "NSTEQ":
                handleAssignmentChecks(node);
                break;
            case "NDVEQ":
                handleAssignmentChecks(node);
                break;
            default:
                for (Node child : node.getChildren()) {
                    traverse(child);
                }
                break;
        }
    }

    private void handleAssignmentChecks(Node node) {
        List<Node> children = node.getChildren();
        if (children.size() >= 2) {
            Node varNode = children.get(0);
            Node exprNode = children.get(1);
            String varName = varNode.getValue();
            if (!symbolTable.isDeclared(varName)) {
                errors.add("Variable '" + varName + "' is not declared.");
            }
            SymbolTableEntry currentEntry = this.symbolTable.find(varName);
            if (exprNode.getType().equals("NADD") || exprNode.getType().equals("NSUB")
                    || exprNode.getType().equals("NMUL") || exprNode.getType().equals("NDIV")
                    || exprNode.getType().equals("NMOD") || exprNode.getType().equals("NPOW")) {
                if (!checkTypeChildNodes(exprNode, currentEntry.getDataType())) {
                    outputController.addSemanticError("Type mismatch on - " + varNode.getValue(),
                            varNode.getCol(), varNode.getLine());
                }

            } else {
                // INFO: Type check
                if (checkTypeChildNodes(node, currentEntry.getDataType())) {
                    currentEntry.setInitialised(true);
                } else {
                    outputController.addSemanticError("Type mismatch on - " + varNode.getValue(),
                            varNode.getCol(), varNode.getLine());
                }
            }
        } else {
            errors.add("Assignment node does not have enough children.");
        }
    }

    private boolean checkTypeChildNodes(Node node, DataType dataType) {
        // TODO: Add type checking for bool
        List<Node> children = node.getChildren();
        Node child1 = children.get(0);
        Node child2 = children.get(1);

        // INFO: var + var
        if (child1.getType().equals("NSIMV") && child2.getType().equals("NSIMV")) {
            SymbolTableEntry entry1 = symbolTable.find(child1.getValue());
            SymbolTableEntry entry2 = symbolTable.find(child2.getValue());
            if (entry1.getDataType() == entry2.getDataType() && entry1.getDataType() == dataType) {
                if (entry1.getDataType() == DataType.INTEGER) {
                    return true;
                } else if (entry1.getDataType() == DataType.FLOAT) {
                    return true;
                }
            }
        }
        // INFO: var + float/int
        else if (child1.getType().equals("NSIMV") && child2.getType().equals("NILIT")
                || child2.getType().equals("NFLIT")) {
            SymbolTableEntry var = symbolTable.find(child1.getValue());
            if (var.getDataType() == DataType.INTEGER && child2.getType().equals("NILIT")
                    && var.getDataType() == dataType) {
                return true;
            } else if (var.getDataType() == DataType.FLOAT && child2.getType().equals("NFLIT")
                    && var.getDataType() == dataType) {
                return true;
            }
        }
        // INFO: float/int + var
        else if (child1.getType().equals("NILIT")
                || child1.getType().equals("NFLIT") && child2.getType().equals("NSIMV")) {
            SymbolTableEntry var = symbolTable.find(child2.getValue());
            if (child1.getType().equals("NILIT") && var.getDataType() == DataType.INTEGER
                    && var.getDataType() == dataType) {
                return true;
            } else if (var.getDataType() == DataType.FLOAT && child2.getValue().equals("NFLIT")
                    && var.getDataType() == dataType) {
                return true;
            }
        }
        // INFO: float/int + float/int
        else if (child1.getType().equals("NILIT")
                || child1.getType().equals("NFLIT") && child2.getType().equals("NILIT")
                || child2.getType().equals("NFLIT")) {
            if (child1.getType().equals("NILIT") && child1.getType().equals("NILIT")
                    && SymbolTableEntry.nodeTypeConversion(child1.getType()) == dataType) {
                return true;
            } else if (child1.getType().equals("NFLIT") && child1.getType().equals("NFLIT")
                    && SymbolTableEntry.nodeTypeConversion(child1.getType()) == dataType) {
                return true;
            }

        }
        return false;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public List<String> getErrors() {
        return this.errors;
    }
}
