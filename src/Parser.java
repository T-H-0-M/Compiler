import java.util.Stack;

public class Parser {
    private final Scanner scanner;
    private Token currentToken;
    private Node rootNode;
    private Stack<SymbolTable> symbolTableStack;
    public Stack<SymbolTable> removedSymbolTableStack;

    public Parser() {
        this.scanner = null;
        this.currentToken = null;
        this.rootNode = null;
        this.symbolTableStack = new Stack<SymbolTable>();
        this.removedSymbolTableStack = new Stack<SymbolTable>();
    }

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.currentToken = scanner.nextToken();
        this.rootNode = null;
        this.symbolTableStack = new Stack<SymbolTable>();
        this.removedSymbolTableStack = new Stack<SymbolTable>();
    }

    private Node consume(Tokeniser.TokenType expectedType, Node parentNode) throws ParseException {
        if (currentToken.getType() == expectedType) {
            Token consumedToken = currentToken;
            currentToken = scanner.nextToken();
            Node node = new Node(consumedToken.getType().toString(), consumedToken.getLexeme());
            if (consumedToken.getType() == Tokeniser.TokenType.TIDEN) {
                parentNode.setValue(consumedToken.getLexeme());
            }
            addTokenToCurrentScope(currentToken.getLexeme(), currentToken.getType(), currentToken.getLine(), currentToken.getCol());
            return node;
        } else {
            String errorMsg = "Expected " + expectedType + ", but found " + currentToken.getType() +
                    " on line " + currentToken.getLine() + " and col " + currentToken.getCol();
            throw new ParseException(errorMsg);
        }
    }

    private void incrementScope() {
        SymbolTable symbolTable = new SymbolTable();
        this.symbolTableStack.push(symbolTable);
        System.out.println("New scope added, Symbol table size:" + this.symbolTableStack.size());
    }

    private void decrementScope() {
        if (this.symbolTableStack.size() < 1) {
            System.out.println("No scope to remove");
            return;
        }
        this.removedSymbolTableStack.push(this.symbolTableStack.peek());
        System.out.println("Removed Symbol table size:" + this.removedSymbolTableStack.size());
        this.symbolTableStack.peek().destroy();
        this.symbolTableStack.pop();
        System.out.println("Removed Symbol table:" + this.removedSymbolTableStack.peek().toString());
    }

    private void addTokenToCurrentScope(String tokenId, Tokeniser.TokenType type, int line, int col) {
        if (this.symbolTableStack.size() < 1) {
            System.out.println("No scope to add token to");
            return;
        }
        this.symbolTableStack.peek().enter(tokenId, type, line, col);
        System.out.println(this.symbolTableStack.peek().toString());
        System.out.println("Token added to current scope: " + type.toString());
    }

    // private Node consume(Tokeniser.TokenType expectedType, Node parentNode) {
    // if (currentToken.getType() == expectedType) {
    // Token consumedToken = currentToken;
    // currentToken = scanner.nextToken();
    // Node node = new Node(consumedToken.getType().toString(),
    // consumedToken.getLexeme());
    // if (consumedToken.getType().toString().equals("TIDEN")) {
    // parentNode.setValue(consumedToken.getLexeme());
    // }
    // // parentNode.addChild(node);
    // return node;
    // } else {
    // String errorMsg = "Expected " + expectedType + ", but found " +
    // currentToken.getType() +
    // " on line " + currentToken.getLine() + " and col " + currentToken.getCol();
    // System.out.println(errorMsg);
    // parentNode.addError(errorMsg);
    // parentNode.setType(errorMsg);
    // // TODO: Improve this with scope
    // currentToken = scanner.nextToken();
    // while (currentToken.getType().toString().equals("TSEMI")) {
    // currentToken = scanner.nextToken(); // Skip to next ;
    // }
    // return null;
    // }
    // }

    private boolean match(Tokeniser.TokenType expectedType) throws ParseException {
        return currentToken.getType() == expectedType;
    }

    public Node parse() throws ParseException {
        // try {
        rootNode = program();
        return rootNode;
        // } catch (ParseException e) {
        // System.err.println("Parse error: " + e.getMessage());
        // if (rootNode != null) {
        // rootNode.printPreOrderTraversal();
        // rootNode.printTree();
        // } else {
        // System.out.println("no tree");
        // }
        //
        // throw e;
        // }
    }

    private Node program() throws ParseException {
        Node node = new Node("NPROG", "");
        consume(Tokeniser.TokenType.TCD24, node);
        consume(Tokeniser.TokenType.TIDEN, node);
        node.addChild(globals());
        node.addChild(funcs());
        node.addChild(mainBody());
        return node;
    }

    private Node globals() throws ParseException {
        Node node = new Node("NGLOB", "");
        node.addChild(consts());
        node.addChild(types());
        node.addChild(arrays());
        return node;
    }

    // private Node consts() throws ParseException {
    // Node node = new Node("SPECIAL", "");
    // if (match(Tokeniser.TokenType.TCONS)) {
    // consume(Tokeniser.TokenType.TCONS, node);
    // node.addChild(initList());
    // return node;
    // }
    // return node;
    // }

    private Node consts() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCONS)) {
            consume(Tokeniser.TokenType.TCONS, node);
            node.addChild(initList());
            return node;
        }
        return node;
    }

    // TODO: check the NILIST here
    private Node initList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(init());
        node.addChild(initListTail());
        return node;
    }

    private Node initListTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NILIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(initList());
            return node;
        }
        return node;
    }

    private Node init() throws ParseException {
        System.out.println("NINIT hit");
        Node node = new Node("NINIT", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TEQUL, node);
        node.addChild(expr());
        return node;
    }

    private Node types() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTYPD)) {
            consume(Tokeniser.TokenType.TTYPD, node);
            node.addChild(typeList());
            return node;
        }
        return node;
    }

    private Node typeList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(type());
        node.addChild(typeListTail());
        return node;
    }

    private Node typeListTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TIDEN)) {
            node.setType("NTYPEL");
            return typeList();
        }
        return node;
    }

    private Node type() throws ParseException {
        Node node = new Node("NRTYPE", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TTDEF, node);
        if (match(Tokeniser.TokenType.TARAY)) {
            consume(Tokeniser.TokenType.TARAY, node);
            consume(Tokeniser.TokenType.TLBRK, node);
            node.addChild(expr());
            consume(Tokeniser.TokenType.TRBRK, node);
            consume(Tokeniser.TokenType.TTTOF, node);
            consume(Tokeniser.TokenType.TIDEN, node);
        } else {
            node.addChild(fields());
        }
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node fields() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(sDecl());
        node.addChild(fieldsTail());
        return node;
    }

    private Node fieldsTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NFLIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(fields());
            return node;
        }
        return node;
    }

    private Node arrays() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TARRD)) {
            consume(Tokeniser.TokenType.TARRD, node);
            node.addChild(arrDecls());
            return node;
        }
        return node;
    }

    private Node arrDecls() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(arrDecl());
        node.addChild(arrDeclsTail());
        return node;
    }

    private Node arrDeclsTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NALIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(arrDecls());
            return node;
        }
        return node;
    }

    private Node arrDecl() throws ParseException {
        Node node = new Node("NARRD", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TCOLN, node);
        consume(Tokeniser.TokenType.TIDEN, node);
        return node;
    }

    private Node funcs() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TFUNC)) {
            node.setType("NFUNCS");
            node.addChild(func());
            node.addChild(funcs());
            return node;
        }
        return node;
    }

    private Node func() throws ParseException {
        incrementScope();
        System.out.println("NFUND Hit");
        Node node = new Node("NFUND", "");
        consume(Tokeniser.TokenType.TFUNC, node);
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(pList());
        consume(Tokeniser.TokenType.TRPAR, node);
        consume(Tokeniser.TokenType.TCOLN, node);
        node.addChild(rType());
        node.addChild(funcBody());
        decrementScope();
        return node;
    }

    private Node rType() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TVOID)) {
            consume(Tokeniser.TokenType.TVOID, node);
            return node;
        } else {
            return sType();
        }
    }

    private Node pList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TCONS)) {
            return params();
        }
        return node;
    }

    private Node params() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(param());
        node.addChild(paramsTail());
        return node;
    }

    private Node paramsTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NPLIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(params());
            return node;
        }
        return node;
    }

    // TODO: find out what to do with array decl
    private Node param() throws ParseException {
        Node node = new Node("NSIMP", "");
        if (match(Tokeniser.TokenType.TCONS)) {
            node.setType("NARRC");
            node.addChild(new Node("TCONS", currentToken.getLexeme()));
            consume(Tokeniser.TokenType.TCONS, node);
            node.addChild(arrDecl());
            arrDecl();
        } else {
            node.addChild(sDecl());
        }
        return node;
    }

    // private Node param() throws ParseException {
    // Node node = new Node("", "");
    // if (match(Tokeniser.TokenType.TCONS)) {
    // node.setType("NARRC");
    // node.addChild(new Node("Constant", ""));
    // consume(Tokeniser.TokenType.TCONS, node);
    // node.addChild(arrDecl());
    // } else if (match(Tokeniser.TokenType.TIDEN)) {
    // node.addChild(new Node("Identifier", currentToken.getLexeme()));
    // consume(Tokeniser.TokenType.TIDEN, node);
    // if (match(Tokeniser.TokenType.TCOLN)) {
    // consume(Tokeniser.TokenType.TCOLN, node);
    // if (match(Tokeniser.TokenType.TIDEN)) {
    // node.addChild(new Node("IDENT", currentToken.getLexeme()));
    // consume(Tokeniser.TokenType.TIDEN, node);
    // } else {
    // node.addChild(sType());
    // }
    // } else {
    // consume(Tokeniser.TokenType.TCOLN, node);
    // node.addChild(new Node("Type", currentToken.getType().toString()));
    // consume(Tokeniser.TokenType.TIDEN, node);
    // }
    // } else {
    // throw new ParseException("Expected TCONS or TIDEN, but found " +
    // currentToken.getType());
    // }
    // return node;
    // }

    private Node funcBody() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(locals());
        consume(Tokeniser.TokenType.TBEGN, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node locals() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TIDEN)) {
            return dList();
        }
        return node;
    }

    private Node dList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(decl());
        node.addChild(dListTail());
        return node;
    }

    private Node dListTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NDLIST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(dList());
            return node;
        }
        return node;
    }

    // TODO: Complete this, the grammar is currently ambiguous
    private Node decl() throws ParseException {
        return new Node("SPECIAL", "");
    }

    private Node mainBody() throws ParseException {
        incrementScope();
        Node node = new Node("NMAIN", "");
        consume(Tokeniser.TokenType.TMAIN, node);
        node.addChild(sList());
        consume(Tokeniser.TokenType.TBEGN, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TTEND, node);
        consume(Tokeniser.TokenType.TCD24, node);
        consume(Tokeniser.TokenType.TIDEN, node);
        decrementScope();
        return node;
    }

    private Node sList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(sDecl());
        node.addChild(sListTail());
        return node;
    }

    private Node sListTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NSDLST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(sList());
            return node;
        }
        return node;
    }

    private Node sDecl() throws ParseException {
        Node node = new Node("NSDECL", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TCOLN, node);
        if (match(Tokeniser.TokenType.TIDEN)) {
            consume(Tokeniser.TokenType.TIDEN, node);
        } else {
            node.addChild(sType());
        }
        return node;
    }

    private Node sType() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TINTG)) {
            consume(Tokeniser.TokenType.TINTG, node);
        } else if (match(Tokeniser.TokenType.TFLOT)) {
            consume(Tokeniser.TokenType.TFLOT, node);
        } else {
            consume(Tokeniser.TokenType.TBOOL, node);
        }
        return node;
    }

    private Node stats() throws ParseException {
        Node node = new Node("SPECIAL", "");
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

    private Node statsTail() throws ParseException {
        incrementScope();
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTFOR) || match(Tokeniser.TokenType.TIFTH) || match(Tokeniser.TokenType.TSWTH)
                || match(Tokeniser.TokenType.TTTDO) || match(Tokeniser.TokenType.TREPT)
                || match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TINPT)
                || match(Tokeniser.TokenType.TRETN) || match(Tokeniser.TokenType.TPRLN)
                || match(Tokeniser.TokenType.TPRNT)) {
            node.setType("NSTATS");
            return stats();
        }
        decrementScope();
        return node;
    }

    private Node strStat() throws ParseException {
        Node node = new Node("SPECIAL", "");
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

    private Node stat() throws ParseException {
        // TODO: This is wrong, tf was i smoking
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TREPT)) {
            node.addChild(repStat());
            // TODO: fix this with symbol table - asgnstat and callstatt both start with
            // TIDEN
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            node.addChild(asgnStat());
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            node.addChild(callStat());
        } else if (match(Tokeniser.TokenType.TINPT) || match(Tokeniser.TokenType.TPRLN)
                || match(Tokeniser.TokenType.TPRNT)) {
            node.addChild(ioStat());
        } else {
            node.addChild(returnStat());
        }
        return node;
    }

    private Node forStat() throws ParseException {
        System.out.println("nfor hit");
        Node node = new Node("NFOR", "");
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

    private Node repStat() throws ParseException {
        System.out.println("nrept hit");
        Node node = new Node("NREPT", "");
        consume(Tokeniser.TokenType.TREPT, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(asgnList());
        consume(Tokeniser.TokenType.TRPAR, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TUNTL, node);
        node.addChild(bool());
        return node;
    }

    private Node doStat() throws ParseException {

        Node node = new Node("NDOWL", "");
        consume(Tokeniser.TokenType.TTTDO, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TWHIL, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(bool());
        consume(Tokeniser.TokenType.TRPAR, node);
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node asgnList() throws ParseException {
        if (match(Tokeniser.TokenType.TIDEN)) {
            return aList();
        }
        return new Node("SPECIAL", "");
    }

    private Node aList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(asgnStat());
        node.addChild(aListTail());
        return node;
    }

    private Node aListTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NASGNS");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(aList());
        }
        return node;
    }

    private Node ifStat() throws ParseException {
        System.out.println("NIFITH hit");
        Node node = new Node("NIFITH", "");
        consume(Tokeniser.TokenType.TIFTH, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(bool());
        consume(Tokeniser.TokenType.TRPAR, node);
        node.addChild(stats());
        if (match(Tokeniser.TokenType.TELSE)) {
            node.setType("NIFTE");
            consume(Tokeniser.TokenType.TELSE, node);
            node.addChild(stats());
        } else if (match(Tokeniser.TokenType.TELIF)) {
            node.setType("NIFEF");
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

    private Node switchStat() throws ParseException {
        Node node = new Node("NSWTCH", "");
        consume(Tokeniser.TokenType.TSWTH, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        node.addChild(expr());
        consume(Tokeniser.TokenType.TRPAR, node);
        consume(Tokeniser.TokenType.TBEGN, node);
        node.addChild(caseList());
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node caseList() throws ParseException {
        Node node = new Node("NCASLT", "");
        if (match(Tokeniser.TokenType.TCASE)) {
            consume(Tokeniser.TokenType.TCASE, node);
            node.addChild(expr());
            consume(Tokeniser.TokenType.TCOLN, node);
            node.addChild(stats());
            consume(Tokeniser.TokenType.TBREK, node);
            consume(Tokeniser.TokenType.TSEMI, node);
            node.addChild(caseList());
        } else {
            node.setType("SPECIAL");
            consume(Tokeniser.TokenType.TDFLT, node);
            consume(Tokeniser.TokenType.TCOLN, node);
            node.addChild(stats());
        }
        return node;
    }

    private Node asgnStat() throws ParseException {
        System.out.println("asgnStat hit");
        Node node = new Node("SPECIAL", "");
        node.addChild(var());
        node.addChild(asgnOp());
        node.addChild(bool());
        return node;
    }

    private Node asgnOp() throws ParseException {
        System.out.println("asgnop hit");
        Node node = new Node("", "");
        if (match(Tokeniser.TokenType.TPLEQ)) {
            node.setType("NPLEQ");
            consume(Tokeniser.TokenType.TPLEQ, node);
        } else if (match(Tokeniser.TokenType.TMNEQ)) {
            node.setType("NMNEQ");
            consume(Tokeniser.TokenType.TMNEQ, node);
        } else if (match(Tokeniser.TokenType.TSTEQ)) {
            node.setType("NSTEA");
            consume(Tokeniser.TokenType.TSTEQ, node);
        } else if (match(Tokeniser.TokenType.TDVEQ)) {
            node.setType("NDVEQ");
            consume(Tokeniser.TokenType.TDVEQ, node);
        } else {
            node.setType("NASGN");
            consume(Tokeniser.TokenType.TEQUL, node);
        }
        return node;
    }

    private Node ioStat() throws ParseException {
        System.out.println("iostat hist");
        Node node = new Node("NINPUT", "");
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

    private Node callStat() throws ParseException {
        System.out.println("NCALL hit");
        Node node = new Node("NCALL", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        if (match(Tokeniser.TokenType.TNOTT)) {
            node.addChild(eList());
        }
        consume(Tokeniser.TokenType.TRPAR, node);
        return node;
    }

    private Node returnStat() throws ParseException {
        Node node = new Node("NRETN", "");
        consume(Tokeniser.TokenType.TRETN, node);
        if (match(Tokeniser.TokenType.TVOID)) {
            consume(Tokeniser.TokenType.TVOID, node);
        } else {
            node.addChild(expr());
        }
        return node;
    }

    private Node vList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(var());
        node.addChild(vListTail());
        return node;
    }

    private Node vListTail() throws ParseException {
        Node node = new Node("NVLIST", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(vList());
            return node;
        }
        return node;
    }

    private Node var() throws ParseException {
        Node node = new Node("NSIMV", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        node.addChild(varTail());
        return node;
    }

    private Node varTail() throws ParseException {
        Node node = new Node("NVLIST", "");
        if (match(Tokeniser.TokenType.TLBRK)) {
            consume(Tokeniser.TokenType.TLBRK, node);
            node.addChild(expr());
            consume(Tokeniser.TokenType.TRBRK, node);
            node.addChild(varField());
            return node;
        }
        return node;
    }

    private Node varField() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TDOTT)) {
            consume(Tokeniser.TokenType.TDOTT, node);
            node.addChild(new Node("Identifier", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TIDEN, node);
            return node;
        }
        return node;
    }

    private Node eList() throws ParseException {
        Node node = new Node("NEXPL", "");
        node.addChild(bool());
        node.addChild(eListTail());
        return node;
    }

    private Node eListTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(eList());
            return node;
        }
        return node;
    }

    private Node bool() throws ParseException {
        Node node = new Node("NBOOL", "");
        if (match(Tokeniser.TokenType.TNOTT)) {
            consume(Tokeniser.TokenType.TNOTT, node);
            node.addChild(bool());
        } else {
            node.addChild(rel());
            node.addChild(boolTail());
        }
        return node;
    }

    private Node boolTail() throws ParseException {
        Node node = new Node("NBOOL", "");
        if (match(Tokeniser.TokenType.TTAND) || match(Tokeniser.TokenType.TTTOR) || match(Tokeniser.TokenType.TTXOR)) {
            node.addChild(logOp());
            node.addChild(rel());
            node.addChild(boolTail());
        }
        return node;
    }

    private Node rel() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(expr());
        node.addChild(relTail());
        return node;
    }

    private Node relTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TEQEQ) || match(Tokeniser.TokenType.TNEQL) || match(Tokeniser.TokenType.TGRTR)
                || match(Tokeniser.TokenType.TLEQL) || match(Tokeniser.TokenType.TLESS)
                || match(Tokeniser.TokenType.TGEQL)) {
            node.addChild(relOp());
            node.addChild(expr());
        }
        return node;
    }

    private Node logOp() throws ParseException {
        Node node = new Node("", "");
        if (match(Tokeniser.TokenType.TTAND)) {
            node.setType("NAND");
            consume(Tokeniser.TokenType.TTAND, node);
        } else if (match(Tokeniser.TokenType.TTTOR)) {
            node.setType("NOR");
            consume(Tokeniser.TokenType.TTTOR, node);
        } else {
            node.setType("NXOR");
            consume(Tokeniser.TokenType.TTXOR, node);
        }
        return node;
    }

    private Node relOp() throws ParseException {
        Node node = new Node("", "");
        if (match(Tokeniser.TokenType.TEQEQ)) {
            node.setType("NEQL");
            consume(Tokeniser.TokenType.TEQEQ, node);
        } else if (match(Tokeniser.TokenType.TNEQL)) {
            node.setType("NNEQ");
            consume(Tokeniser.TokenType.TNEQL, node);
        } else if (match(Tokeniser.TokenType.TGRTR)) {
            node.setType("NGRT");
            consume(Tokeniser.TokenType.TGRTR, node);
        } else if (match(Tokeniser.TokenType.TLEQL)) {
            node.setType("NLEQ");
            consume(Tokeniser.TokenType.TLEQL, node);
        } else if (match(Tokeniser.TokenType.TLESS)) {
            node.setType("NLSS");
            consume(Tokeniser.TokenType.TLESS, node);
        } else {
            node.setType("NGEQ");
            consume(Tokeniser.TokenType.TGEQL, node);
        }
        return node;
    }

    private Node expr() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(term());
        node.addChild(exprTail());
        return node;
    }

    private Node exprTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TPLUS)) {
            node.setType("NADD");
            consume(Tokeniser.TokenType.TPLUS, node);
            node.addChild(term());
            node.addChild(exprTail());
            return node;
        } else if (match(Tokeniser.TokenType.TMINS)) {
            node.setType("NSUB");
            consume(Tokeniser.TokenType.TMINS, node);
            node.addChild(term());
            node.addChild(exprTail());
            return node;
        }
        return node;
    }

    private Node term() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(fact());
        node.addChild(termTail());
        return node;
    }

    private Node termTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TSTAR)) {
            node.setType("NMUL");
            consume(Tokeniser.TokenType.TSTAR, node);
            node.addChild(fact());
            node.addChild(termTail());
        } else if (match(Tokeniser.TokenType.TDIVD)) {
            node.setType("NDIV");
            consume(Tokeniser.TokenType.TDIVD, node);
            node.addChild(fact());
            node.addChild(termTail());
        } else if (match(Tokeniser.TokenType.TPERC)) {
            node.setType("NMOD");
            consume(Tokeniser.TokenType.TPERC, node);
            node.addChild(fact());
            node.addChild(termTail());
        }
        // ε
        return node;
    }

    private Node fact() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(exponent());
        node.addChild(factTail());
        return node;
    }

    private Node factTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCART)) {
            node.setType("NPOW");
            consume(Tokeniser.TokenType.TCART, node);
            node.addChild(exponent());
            node.addChild(factTail());
        }
        return node;
    }

    private Node exponent() throws ParseException {
        // TODO: remove exponent
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTRUE)) {
            node.setType("NTRUE");
            consume(Tokeniser.TokenType.TTRUE, node);
        } else if (match(Tokeniser.TokenType.TFALS)) {
            node.setType("NFALS");
            consume(Tokeniser.TokenType.TFALS, node);
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            node.setType("SPECIAL");
            consume(Tokeniser.TokenType.TIDEN, node);
        } else if (match(Tokeniser.TokenType.TILIT)) {
            node.setType("NILIT");
            consume(Tokeniser.TokenType.TILIT, node);
        } else if (match(Tokeniser.TokenType.TFLIT)) {
            node.setType("NFLIT");
            consume(Tokeniser.TokenType.TFLIT, node);
        } else if (match(Tokeniser.TokenType.TLBRK)) {
            node.setType("SPECIAL");
            consume(Tokeniser.TokenType.TLBRK, node);
            node.addChild(bool());
            consume(Tokeniser.TokenType.TRBRK, node);
        } else {
            node.addChild(fnCall());
        }
        return node;
    }

    private Node fnCall() throws ParseException {
        System.out.println("FNCALL hit");
        Node node = new Node("NFCALL", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TLBRK, node);
        if (match(Tokeniser.TokenType.TNOTT)) {
            node.addChild(eList());
        }
        consume(Tokeniser.TokenType.TRBRK, node);
        return node;
    }

    private Node prList() throws ParseException {
        Node node = new Node("SPECIAL", "");
        node.addChild(printItem());
        node.addChild(prListTail());
        return node;
    }

    private Node prListTail() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NPRLST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(prList());
        }
        return node;
    }

    private Node printItem() throws ParseException {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TSTRG)) {
            node.setType("NSTRG");
            consume(Tokeniser.TokenType.TSTRG, node);
        } else {
            node.addChild(expr());
        }
        return node;
    }
}
