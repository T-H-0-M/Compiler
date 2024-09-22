import java.util.*;

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

    // INFO: Removed SPECIAL Nodes
    private void buildTreeString(Node node, List<String> lines, String prefix, String childrenPrefix) {
        if (!node.type.equals("SPECIAL")) {
            String content = node.type + (node.value.isEmpty() ? "" : " (" + node.value + ")");
            lines.add(prefix + content);
        }

        for (Iterator<Node> it = node.children.iterator(); it.hasNext();) {
            Node child = it.next();
            if (it.hasNext()) {
                buildTreeString(child, lines,
                        node.type.equals("SPECIAL") ? prefix : childrenPrefix + "├── ",
                        node.type.equals("SPECIAL") ? childrenPrefix : childrenPrefix + "│   ");
            } else {
                buildTreeString(child, lines,
                        node.type.equals("SPECIAL") ? prefix : childrenPrefix + "└── ",
                        node.type.equals("SPECIAL") ? childrenPrefix : childrenPrefix + "    ");
            }
        }
    }

    public void printPreOrderTraversal() {
        List<Node> output = new ArrayList<>(); // Store Node objects instead of strings
        preOrderTraversal(this, output);

        int columnCount = 0; // To keep track of columns in the current line

        for (int i = 0; i < output.size(); i++) {
            Node currentNode = output.get(i);
            String nodeOutput = formatNodeOutput(currentNode);
            System.out.print(nodeOutput + "\t"); // Using tab for separation

            // Determine how many columns this node occupies
            // Assuming nodes with identifiers occupy 2 columns, others 1
            if (currentNode.getType() != null && !currentNode.getValue().equals("")) {
                columnCount += 2;
            } else {
                columnCount += 1;
            }

            // Check if we've reached the column limit
            if (columnCount >= 10 || i == output.size() - 1) {
                System.out.println();
                columnCount = 0; // Reset for the next line
            }
        }

        // Print errors if any
        if (!errors.isEmpty()) {
            System.out.println("\nErrors:");
            for (String error : errors) {
                System.out.println(error);
            }
        }
    }

    // Revised preOrderTraversal method to handle Node objects
    private void preOrderTraversal(Node node, List<Node> output) {
        if (node.type.equals("SPECIAL")) {
            for (Node child : node.children) {
                preOrderTraversal(child, output);
            }
        } else {
            output.add(node); // Add the node itself
            for (Node child : node.children) {
                preOrderTraversal(child, output);
            }
        }
    }

    // public void printPreOrderTraversal() {
    // List<String> output = new ArrayList<>();
    // preOrderTraversal(this, output);
    //
    // for (int i = 0; i < output.size(); i++) {
    // System.out.print(output.get(i));
    // if ((i + 1) % 10 == 0 || i == output.size() - 1) {
    // System.out.println();
    // }
    // }
    //
    // if (!errors.isEmpty()) {
    // System.out.println("\nErrors:");
    // for (String error : errors) {
    // System.out.println(error);
    // }
    // }
    // }
    //
    // private void preOrderTraversal(Node node, List<String> output) {
    // if (node.type.equals("SPECIAL")) {
    // for (Node child : node.children) {
    // preOrderTraversal(child, output);
    // }
    // } else {
    // output.add(formatNodeOutput(node));
    // for (Node child : node.children) {
    // preOrderTraversal(child, output);
    // }
    // }
    // }

    private String formatNodeOutput(Node node) {
        String content = node.type;
        if (!node.value.isEmpty()) {
            content += " " + node.value + " ";
        }
        return padString(content, 7);
    }

    private String padString(String str, int multiple) {
        int paddingNeeded = multiple - (str.length() % multiple);
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
