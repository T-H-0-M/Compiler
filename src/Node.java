import java.util.*;

/**
 * Node class
 * 
 * Represents a node in the abstract syntax tree (AST).
 * Each node stores its type, value, a list of child nodes, and any associated
 * errors.
 * Provides methods for constructing and traversing the AST.
 * 
 * Date: 2024-09-27
 *
 * @author Thomas Bandy, Benjamin Rogers
 * @version 1.0
 */
public class Node {
    private String type;
    private String value;
    private List<Node> children;
    private List<String> errors;

    public Node(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public void addChild(Node child) {
        if (child.type.equals("SPECIAL")) {
            for (Node grandChild : child.children) {
                this.addChild(grandChild);
            }
        } else {
            children.add(child);
        }
    }

    public void printTree() {
        List<String> lines = new ArrayList<>();
        buildTreeString(this, lines, "", "");
        for (String line : lines) {
            System.out.println(line);
        }
    }

    // INFO: Prints all nodes
    private void buildTreeString(Node node, List<String> lines, String prefix, String childrenPrefix) {
        String content = node.type + (node.value.isEmpty() ? "" : " (" + node.value + ")");
        lines.add(prefix + content);

        for (Iterator<Node> it = node.children.iterator(); it.hasNext();) {
            Node child = it.next();
            if (it.hasNext()) {
                buildTreeString(child, lines,
                        childrenPrefix + "├── ",
                        childrenPrefix + "│   ");
            } else {
                buildTreeString(child, lines,
                        childrenPrefix + "└── ",
                        childrenPrefix + "    ");
            }
        }
    }

    public void printPreOrderTraversal() {
        List<Node> output = new ArrayList<>();
        preOrderTraversal(this, output);

        int columnCount = 0;

        for (int i = 0; i < output.size(); i++) {
            Node currentNode = output.get(i);
            String nodeOutput = formatNodeOutput(currentNode);
            System.out.print(nodeOutput);

            if (currentNode.getType() != null && !currentNode.getValue().equals("")) {
                columnCount += 2;
            } else {
                columnCount += 1;
            }

            if (columnCount >= 10 || i == output.size() - 1) {
                System.out.println();
                columnCount = 0;
            }
        }

        if (!errors.isEmpty()) {
            System.out.println("\nErrors:");
            for (String error : errors) {
                System.out.println(error);
            }
        }
    }

    private void preOrderTraversal(Node node, List<Node> output) {
        if (node.type.equals("SPECIAL")) {
            for (Node child : node.children) {
                preOrderTraversal(child, output);
            }
        } else {
            output.add(node);
            for (Node child : node.children) {
                preOrderTraversal(child, output);
            }
        }
    }

    private String formatNodeOutput(Node node) {
        String typeContent = padString(node.type, 7);
        String valueContent = "";
        if (!node.value.isEmpty()) {
            valueContent = padString(node.value, 7);
        }
        return typeContent + valueContent;
    }

    private String padString(String str, int multiple) {
        str = str.trim();
        str += " ";

        int length = str.length();
        int paddingNeeded = multiple - (length % multiple);
        if (paddingNeeded == multiple) {
            paddingNeeded = 0;
        }
        return str + " ".repeat(paddingNeeded);
    }

    public void addError(String error) {
        errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public boolean isSpecial() {
        return this.type.equals("SPECIAL");
    }

    public void setValue(String value) {
        this.value = value;
    }
}
