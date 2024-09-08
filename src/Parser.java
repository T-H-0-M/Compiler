public class Parser {
    private final Scanner scanner;
    private Token currentToken;
    private Node rootNode;

    public Parser() {
        this.scanner = null;
        this.currentToken = null;
        this.rootNode = null;
    }

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.currentToken = scanner.nextToken();
        this.rootNode = null;
    }

    private Node consume(Tokeniser.TokenType expectedType, Node parentNode) {
        if (currentToken.getType() == expectedType) {
            Token consumedToken = currentToken;
            currentToken = scanner.nextToken();
            Node node = new Node(expectedType.toString(), consumedToken.getType().toString());
            parentNode.addChild(node);
            return node;
        } else {
            String errorMsg = "Expected " + expectedType + ", but found " + currentToken.getType() +
                    " on line " + currentToken.getLine() + " and col " + currentToken.getCol();
            System.out.println(errorMsg);
            parentNode.addError(errorMsg);
            parentNode.setType(errorMsg);
            currentToken = scanner.nextToken(); // Skip the unexpected token
            return null;
        }
    }

    private boolean match(Tokeniser.TokenType expectedType) {
        return currentToken.getType() == expectedType;
    }

    public Node parse() {
        try {
            rootNode = program();
            return rootNode;
        } catch (ParseException e) {
            System.err.println("Parse error: " + e.getMessage());
            if (rootNode != null) {
                System.out.println("Partial parse tree:");
                rootNode.printTree();
            } else {
                System.out.println("no tree");
            }

            throw e;
        }
    }

    private Node program() {
        Node node = new Node("NPROG", "");
        consume(Tokeniser.TokenType.TCD24, node);
        node.addChild(new Node("Identifier", currentToken.getType().toString()));
        consume(Tokeniser.TokenType.TIDEN, node);
        node.addChild(globals());
        node.addChild(funcs());
        node.addChild(mainBody());
        return node;
    }

    private Node globals() {
        Node node = new Node("NGLOB", "");
        node.addChild(consts());
        node.addChild(types());
        node.addChild(arrays());
        return node;
    }

    private Node consts() {
        if (match(Tokeniser.TokenType.TCONS)) {
            Node node = new Node("Special", "");
            consume(Tokeniser.TokenType.TCONS, node);
            node.addChild(initList());
            return node;
        }
        return new Node("Special", "");
    }

    // TODO: check the NILIST here
    private Node initList() {
        Node node = new Node("SPECIAL", "");
        node.addChild(init());
        node.addChild(initListTail(node));
        return node;
    }

    private Node initListTail(Node node) {
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NILIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(initList());
            return node;
        }
        return node;
    }

    private Node init() {
        Node node = new Node("NINIT", "");
        node.addChild(new Node("TIDEN", currentToken.getLexeme()));
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TEQUL, node);
        node.addChild(expr());
        return node;
    }

    private Node types() {
        if (match(Tokeniser.TokenType.TTYPD)) {
            Node node = new Node("SPECIAL", "");
            consume(Tokeniser.TokenType.TTYPD, node);
            node.addChild(typeList());
            return node;
        }
        return new Node("SPECIAL", "");
    }

    private Node typeList() {
        Node node = new Node("SPECIAL", "");
        node.addChild(type());
        node.addChild(typeListTail(node));
        return node;
    }

    private Node typeListTail(Node node) {
        if (match(Tokeniser.TokenType.TIDEN)) {
            node.setType("NTYPEL");
            return typeList();
        }
        return node;
    }

    private Node type() {
        Node node = new Node("NRTYPE", "");
        node.addChild(new Node("TIDEN", currentToken.getLexeme()));
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TTDEF, node);
        if (match(Tokeniser.TokenType.TARAY)) {
            consume(Tokeniser.TokenType.TARAY, node);
            consume(Tokeniser.TokenType.TLBRK, node);
            node.addChild(expr());
            consume(Tokeniser.TokenType.TRBRK, node);
            consume(Tokeniser.TokenType.TTTOF, node);
            node.addChild(new Node("TIDEN", currentToken.getLexeme()));
            consume(Tokeniser.TokenType.TIDEN, node);
        } else {
            node.addChild(fields());
        }
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node fields() {
        Node node = new Node("SPECIAL", "");
        node.addChild(sDecl());
        node.addChild(fieldsTail(node));
        return node;
    }

    private Node fieldsTail(Node node) {
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NFLIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(fields());
            return node;
        }
        return node;
    }

    private Node arrays() {
        if (match(Tokeniser.TokenType.TARRD)) {
            Node node = new Node("Arrays", "");
            consume(Tokeniser.TokenType.TARRD, node);
            node.addChild(arrDecls());
            return node;
        }
        return new Node("Arrays", "");
    }

    private Node arrDecls() {
        Node node = new Node("SPECIAL", "");
        node.addChild(arrDecl());
        node.addChild(arrDeclsTail(node));
        return node;
    }

    private Node arrDeclsTail(Node node) {
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NALIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(arrDecls());
            return node;
        }
        return node;
    }

    private Node arrDecl() {
        Node node = new Node("NARRD", "");
        node.addChild(new Node("TIDEN", currentToken.getLexeme()));
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TCOLN, node);
        node.addChild(new Node("TIDEN", currentToken.getLexeme()));
        consume(Tokeniser.TokenType.TIDEN, node);
        return node;
    }

    private Node funcs() {
        if (match(Tokeniser.TokenType.TFUNC)) {
            Node node = new Node("NFUNCS", "");
            node.addChild(func());
            node.addChild(funcs());
            return node;
        }
        return new Node("SPECIAL", "");
    }

    private Node func() {
        Node node = new Node("NFUND", "");
        consume(Tokeniser.TokenType.TFUNC, node);
        node.addChild(new Node("IDENT", currentToken.getLexeme()));
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(pList());
        consume(Tokeniser.TokenType.TRPAR, node);
        consume(Tokeniser.TokenType.TCOLN, node);
        node.addChild(rType());
        node.addChild(funcBody());
        return node;
    }

    private Node rType() {
        if (match(Tokeniser.TokenType.TVOID)) {
            Node node = new Node("Special", "TVOID");
            consume(Tokeniser.TokenType.TVOID, node);
            return node;
        } else {
            return sType();
        }
    }

    private Node pList() {
        if (match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TCONS)) {
            return params();
        }
        return new Node("SPECIAL", "");
    }

    private Node params() {
        Node node = new Node("SPECIAL", "");
        node.addChild(param());
        node.addChild(paramsTail(node));
        return node;
    }

    private Node paramsTail(Node node) {
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NPLIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(params());
            return node;
        }
        return node;
    }

    private Node param() {
        Node node = new Node("", "");
        if (match(Tokeniser.TokenType.TCONS)) {
            node.setType("NARRC");
            node.addChild(new Node("Constant", ""));
            consume(Tokeniser.TokenType.TCONS, node);
            node.addChild(arrDecl());
        } else if (match(Tokeniser.TokenType.TIDEN)) {

            node.addChild(new Node("Identifier", currentToken.getLexeme()));
            consume(Tokeniser.TokenType.TIDEN, node);
            if (match(Tokeniser.TokenType.TCOLN)) {
                consume(Tokeniser.TokenType.TCOLN, node);
                if (match(Tokeniser.TokenType.TIDEN)) {
                    node.addChild(new Node("IDENT", currentToken.getLexeme()));
                    consume(Tokeniser.TokenType.TIDEN, node);
                } else {
                    node.addChild(sType());
                }
            } else {
                consume(Tokeniser.TokenType.TCOLN, node);
                node.addChild(new Node("Type", currentToken.getType().toString()));
                consume(Tokeniser.TokenType.TIDEN, node);
            }
        } else {
            throw new ParseException("Expected TCONS or TIDEN, but found " + currentToken.getType());
        }
        return node;
    }

    private Node funcBody() {
        Node node = new Node("FunctionBody", "");
        node.addChild(locals());
        consume(Tokeniser.TokenType.TBEGN, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node locals() {
        if (match(Tokeniser.TokenType.TIDEN)) {
            return dList();
        }
        return new Node("Locals", "");
    }

    private Node dList() {
        Node node = new Node("DeclarationList", "");
        node.addChild(decl());
        node.addChild(dListTail());
        return node;
    }

    private Node dListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            Node node = new Node("DeclarationListTail", "");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(dList());
            return node;
        }
        return new Node("DeclarationListTail", "");
    }

    // TODO: Complete this, the grammar is currently ambiguous
    private Node decl() {
        return new Node("Declaration", "");
    }

    private Node mainBody() {
        Node node = new Node("MainBody", "");
        consume(Tokeniser.TokenType.TMAIN, node);
        node.addChild(sList());
        consume(Tokeniser.TokenType.TBEGN, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TTEND, node);
        consume(Tokeniser.TokenType.TCD24, node);
        node.addChild(new Node("Identifier", currentToken.getType().toString()));
        consume(Tokeniser.TokenType.TIDEN, node);
        return node;
    }

    private Node sList() {
        Node node = new Node("StatementList", "");
        node.addChild(sDecl());
        node.addChild(sListTail());
        return node;
    }

    private Node sListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            Node node = new Node("StatementListTail", "");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(sList());
            return node;
        }
        return new Node("StatementListTail", "");
    }

    private Node sDecl() {
        Node node = new Node("SimpleDeclaration", "");
        node.addChild(new Node("Identifier", currentToken.getType().toString()));
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TCOLN, node);
        if (match(Tokeniser.TokenType.TIDEN)) {
            node.addChild(new Node("Type", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TIDEN, node);
        } else {
            node.addChild(sType());
        }
        return node;
    }

    private Node sType() {
        Node node = new Node("SimpleType", "");
        if (match(Tokeniser.TokenType.TILIT)) {
            node.addChild(new Node("IntegerLiteral", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TILIT, node);
        } else if (match(Tokeniser.TokenType.TFLIT)) {
            node.addChild(new Node("FloatLiteral", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TFLIT, node);
        } else {
            node.addChild(new Node("Boolean", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TBOOL, node);
        }
        return node;
    }

    private Node stats() {
        Node node = new Node("Statements", "");
        if (match(Tokeniser.TokenType.TTFOR) || match(Tokeniser.TokenType.TIFTH) || match(Tokeniser.TokenType.TSWTH)
                || match(Tokeniser.TokenType.TTTDO)) {
            node.addChild(strStat());
        } else {
            node.addChild(stat());
            consume(Tokeniser.TokenType.TSEMI, node);
        }
        node.addChild(statsTail());
        return node;
    }

    private Node statsTail() {
        if (match(Tokeniser.TokenType.TTFOR) || match(Tokeniser.TokenType.TIFTH) || match(Tokeniser.TokenType.TSWTH)
                || match(Tokeniser.TokenType.TTTDO) || match(Tokeniser.TokenType.TREPT)
                || match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TINPT)
                || match(Tokeniser.TokenType.TRETN)) {
            return stats();
        }
        return new Node("StatementsTail", "");
    }

    private Node strStat() {
        Node node = new Node("StructuredStatement", "");
        if (match(Tokeniser.TokenType.TTFOR)) {
            node.addChild(forStat());
        } else if (match(Tokeniser.TokenType.TIFTH)) {
            node.addChild(ifStat());
        } else if (match(Tokeniser.TokenType.TSWTH)) {
            node.addChild(switchStat());
        } else {
            node.addChild(doStat());
        }
        return node;
    }

    private Node stat() {
        Node node = new Node("Statement", "");
        if (match(Tokeniser.TokenType.TTTDO)) {
            node.addChild(repStat());
        } else if (match(Tokeniser.TokenType.TREPT)) {
            node.addChild(asgnStat());
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            node.addChild(ioStat());
        } else if (match(Tokeniser.TokenType.TINPT)) {
            node.addChild(callStat());
        } else {
            node.addChild(returnStat());
        }
        return node;
    }

    private Node forStat() {
        Node node = new Node("ForStatement", "");
        consume(Tokeniser.TokenType.TTFOR, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(asgnList());
        consume(Tokeniser.TokenType.TSEMI, node);
        node.addChild(bool());
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node repStat() {
        Node node = new Node("RepeatStatement", "");
        consume(Tokeniser.TokenType.TREPT, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(asgnList());
        consume(Tokeniser.TokenType.TRPAR, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TUNTL, node);
        node.addChild(bool());
        return node;
    }

    private Node doStat() {
        Node node = new Node("DoStatement", "");
        consume(Tokeniser.TokenType.TTTDO, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TWHIL, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(bool());
        consume(Tokeniser.TokenType.TRPAR, node);
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node asgnList() {
        if (match(Tokeniser.TokenType.TIDEN)) {
            return aList();
        }
        return new Node("AssignmentList", "");
    }

    private Node aList() {
        Node node = new Node("AssignmentList", "");
        node.addChild(asgnStat());
        node.addChild(aListTail());
        return node;
    }

    private Node aListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            Node node = new Node("AssignmentListTail", "");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(aList());
            return node;
        }
        return new Node("AssignmentListTail", "");
    }

    private Node ifStat() {
        Node node = new Node("IfStatement", "");
        consume(Tokeniser.TokenType.TIFTH, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(bool());
        consume(Tokeniser.TokenType.TRPAR, node);
        node.addChild(stats());
        if (match(Tokeniser.TokenType.TELSE)) {
            consume(Tokeniser.TokenType.TELSE, node);
            node.addChild(stats());
        } else if (match(Tokeniser.TokenType.TELIF)) {
            Node elifNode = new Node("ElseIf", "");
            consume(Tokeniser.TokenType.TLPAR, node);
            elifNode.addChild(bool());
            consume(Tokeniser.TokenType.TRPAR, node);
            elifNode.addChild(stats());
            node.addChild(elifNode);
        }
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node switchStat() {
        Node node = new Node("SwitchStatement", "");
        consume(Tokeniser.TokenType.TSWTH, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(expr());
        consume(Tokeniser.TokenType.TRPAR, node);
        consume(Tokeniser.TokenType.TBEGN, node);
        node.addChild(caseList());
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node caseList() {
        Node node = new Node("CaseList", "");
        if (match(Tokeniser.TokenType.TCASE)) {
            Node caseNode = new Node("Case", "");
            consume(Tokeniser.TokenType.TCASE, node);
            caseNode.addChild(expr());
            consume(Tokeniser.TokenType.TCOLN, node);
            caseNode.addChild(stats());
            node.addChild(caseNode);
            node.addChild(caseList());
        } else {
            Node defaultNode = new Node("Default", "");
            consume(Tokeniser.TokenType.TDFLT, node);
            consume(Tokeniser.TokenType.TCOLN, node);
            defaultNode.addChild(stats());
            node.addChild(defaultNode);
        }
        return node;
    }

    private Node asgnStat() {
        Node node = new Node("AssignmentStatement", "");
        node.addChild(var());
        node.addChild(asgnOp());
        node.addChild(bool());
        return node;
    }

    private Node asgnOp() {
        Node node = new Node("AssignmentOperator", "");
        if (match(Tokeniser.TokenType.TPLEQ)) {
            node.addChild(new Node("PlusEqual", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TPLEQ, node);
        } else if (match(Tokeniser.TokenType.TMNEQ)) {
            node.addChild(new Node("MinusEqual", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TMNEQ, node);
        } else if (match(Tokeniser.TokenType.TSTEQ)) {
            node.addChild(new Node("StarEqual", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TSTEQ, node);
        } else if (match(Tokeniser.TokenType.TDVEQ)) {
            node.addChild(new Node("DivideEqual", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TDVEQ, node);
        } else {
            node.addChild(new Node("Equal", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TEQUL, node);
        }
        return node;
    }

    private Node ioStat() {
        Node node = new Node("IOStatement", "");
        if (match(Tokeniser.TokenType.TINPT)) {
            consume(Tokeniser.TokenType.TINPT, node);
            node.addChild(vList());
        } else if (match(Tokeniser.TokenType.TPRNT)) {
            consume(Tokeniser.TokenType.TPRNT, node);
            node.addChild(prList());
        } else {
            consume(Tokeniser.TokenType.TPRLN, node);
            node.addChild(prList());
        }
        return node;
    }

    private Node callStat() {
        Node node = new Node("CallStatement", "");
        node.addChild(new Node("Identifier", currentToken.getType().toString()));
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        if (match(Tokeniser.TokenType.TNOTT)) {
            node.addChild(eList());
        }
        consume(Tokeniser.TokenType.TRPAR, node);
        return node;
    }

    private Node returnStat() {
        Node node = new Node("ReturnStatement", "");
        consume(Tokeniser.TokenType.TRETN, node);
        if (match(Tokeniser.TokenType.TVOID)) {
            node.addChild(new Node("Void", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TVOID, node);
        } else {
            node.addChild(expr());
        }
        return node;
    }

    private Node vList() {
        Node node = new Node("VariableList", "");
        node.addChild(var());
        node.addChild(vListTail());
        return node;
    }

    private Node vListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            Node node = new Node("VariableListTail", "");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(vList());
            return node;
        }
        return new Node("VariableListTail", "");
    }

    private Node var() {
        Node node = new Node("Variable", "");
        node.addChild(new Node("Identifier", currentToken.getType().toString()));
        consume(Tokeniser.TokenType.TIDEN, node);
        node.addChild(varTail());
        return node;
    }

    private Node varTail() {
        if (match(Tokeniser.TokenType.TLBRK)) {
            Node node = new Node("VariableTail", "");
            consume(Tokeniser.TokenType.TLBRK, node);
            node.addChild(expr());
            consume(Tokeniser.TokenType.TRBRK, node);
            node.addChild(varField());
            return node;
        }
        return new Node("VariableTail", "");
    }

    private Node varField() {
        if (match(Tokeniser.TokenType.TDOTT)) {
            Node node = new Node("VariableField", "");
            consume(Tokeniser.TokenType.TDOTT, node);
            node.addChild(new Node("Identifier", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TIDEN, node);
            return node;
        }
        return new Node("VariableField", "");
    }

    private Node eList() {
        Node node = new Node("ExpressionList", "");
        node.addChild(bool());
        node.addChild(eListTail());
        return node;
    }

    private Node eListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            Node node = new Node("ExpressionListTail", "");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(eList());
            return node;
        }
        return new Node("ExpressionListTail", "");
    }

    private Node bool() {
        Node node = new Node("Boolean", "");
        if (match(Tokeniser.TokenType.TNOTT)) {
            node.addChild(new Node("Not", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TNOTT, node);
            node.addChild(bool());
        } else {
            node.addChild(rel());
            node.addChild(boolTail());
        }
        return node;
    }

    private Node boolTail() {
        if (match(Tokeniser.TokenType.TTAND) || match(Tokeniser.TokenType.TTTOR) || match(Tokeniser.TokenType.TTXOR)) {
            Node node = new Node("BooleanTail", "");
            node.addChild(logOp());
            node.addChild(rel());
            node.addChild(boolTail());
            return node;
        }
        return new Node("BooleanTail", "");
    }

    private Node rel() {
        Node node = new Node("Relation", "");
        node.addChild(expr());
        node.addChild(relTail());
        return node;
    }

    private Node relTail() {
        if (match(Tokeniser.TokenType.TEQEQ) || match(Tokeniser.TokenType.TNEQL) || match(Tokeniser.TokenType.TGRTR)
                || match(Tokeniser.TokenType.TLEQL) || match(Tokeniser.TokenType.TLESS)
                || match(Tokeniser.TokenType.TGEQL)) {
            Node node = new Node("RelationTail", "");
            node.addChild(relOp());
            node.addChild(expr());
            return node;
        }
        return new Node("RelationTail", "");
    }

    private Node logOp() {
        Node node = new Node("LogicalOperator", "");
        if (match(Tokeniser.TokenType.TTAND)) {
            node.addChild(new Node("And", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TTAND, node);
        } else if (match(Tokeniser.TokenType.TTTOR)) {
            node.addChild(new Node("Or", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TTTOR, node);
        } else {
            node.addChild(new Node("Xor", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TTXOR, node);
        }
        return node;
    }

    private Node relOp() {
        Node node = new Node("RelationalOperator", "");
        if (match(Tokeniser.TokenType.TEQEQ)) {
            node.addChild(new Node("Equal", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TEQEQ, node);
        } else if (match(Tokeniser.TokenType.TNEQL)) {
            node.addChild(new Node("NotEqual", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TNEQL, node);
        } else if (match(Tokeniser.TokenType.TGRTR)) {
            node.addChild(new Node("GreaterThan", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TGRTR, node);
        } else if (match(Tokeniser.TokenType.TLEQL)) {
            node.addChild(new Node("LessThanOrEqual", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TLEQL, node);
        } else if (match(Tokeniser.TokenType.TLESS)) {
            node.addChild(new Node("LessThan", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TLESS, node);
        } else {
            node.addChild(new Node("GreaterThanOrEqual", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TGEQL, node);
        }
        return node;
    }

    private Node expr() {
        Node node = new Node("Expression", "");
        node.addChild(term());
        node.addChild(exprTail());
        return node;
    }

    private Node exprTail() {
        if (match(Tokeniser.TokenType.TPLUS)) {
            Node node = new Node("ExpressionTail", "");
            node.addChild(new Node("Plus", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TPLUS, node);
            node.addChild(term());
            node.addChild(exprTail());
            return node;
        } else if (match(Tokeniser.TokenType.TMINS)) {
            Node node = new Node("ExpressionTail", "");
            node.addChild(new Node("Minus", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TMINS, node);
            node.addChild(term());
            node.addChild(exprTail());
            return node;
        }
        return new Node("ExpressionTail", "");
    }

    private Node term() {
        Node node = new Node("Term", "");
        node.addChild(fact());
        node.addChild(termTail());
        return node;
    }

    private Node termTail() {
        if (match(Tokeniser.TokenType.TSTAR)) {
            Node node = new Node("TermTail", "");
            node.addChild(new Node("Multiply", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TSTAR, node);
            node.addChild(fact());
            node.addChild(termTail());
            return node;
        } else if (match(Tokeniser.TokenType.TDIVD)) {
            Node node = new Node("TermTail", "");
            node.addChild(new Node("Divide", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TDIVD, node);
            node.addChild(fact());
            node.addChild(termTail());
            return node;
        } else if (match(Tokeniser.TokenType.TPERC)) {
            Node node = new Node("TermTail", "");
            node.addChild(new Node("Modulo", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TPERC, node);
            node.addChild(fact());
            node.addChild(termTail());
            return node;
        }
        return new Node("TermTail", "");
    }

    private Node fact() {
        Node node = new Node("Factor", "");
        node.addChild(exponent());
        node.addChild(factTail());
        return node;
    }

    private Node factTail() {
        if (match(Tokeniser.TokenType.TCART)) {
            Node node = new Node("FactorTail", "");
            node.addChild(new Node("Caret", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TCART, node);
            node.addChild(exponent());
            node.addChild(factTail());
            return node;
        }
        return new Node("FactorTail", "");
    }

    private Node exponent() {
        Node node = new Node("Exponent", "");
        if (match(Tokeniser.TokenType.TTRUE)) {
            node.addChild(new Node("True", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TTRUE, node);
        } else if (match(Tokeniser.TokenType.TFALS)) {
            node.addChild(new Node("False", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TFALS, node);
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            node.addChild(new Node("Identifier", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TIDEN, node);
        } else if (match(Tokeniser.TokenType.TILIT)) {
            node.addChild(new Node("IntegerLiteral", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TILIT, node);
        } else if (match(Tokeniser.TokenType.TFLIT)) {
            node.addChild(new Node("FloatLiteral", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TFLIT, node);
        } else if (match(Tokeniser.TokenType.TLBRK)) {
            consume(Tokeniser.TokenType.TLBRK, node);
            node.addChild(bool());
            consume(Tokeniser.TokenType.TRBRK, node);
        } else {
            node.addChild(fnCall());
        }
        return node;
    }

    private Node fnCall() {
        Node node = new Node("FunctionCall", "");
        node.addChild(new Node("Identifier", currentToken.getType().toString()));
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TLBRK, node);
        if (match(Tokeniser.TokenType.TNOTT)) {
            node.addChild(eList());
        }
        consume(Tokeniser.TokenType.TRBRK, node);
        return node;
    }

    private Node prList() {
        Node node = new Node("PrintList", "");
        node.addChild(printItem());
        node.addChild(prListTail());
        return node;
    }

    private Node prListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            Node node = new Node("PrintListTail", "");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(prList());
            return node;
        }
        return new Node("PrintListTail", "");
    }

    private Node printItem() {
        Node node = new Node("PrintItem", "");
        if (match(Tokeniser.TokenType.TSTRG)) {
            node.addChild(new Node("StringLiteral", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TSTRG, node);
        } else {
            node.addChild(expr());
        }
        return node;
    }
}
