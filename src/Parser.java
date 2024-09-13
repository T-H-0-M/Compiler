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
            Node node = new Node(consumedToken.getType().toString(), consumedToken.getLexeme());
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
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCONS)) {
            consume(Tokeniser.TokenType.TCONS, node);
            node.addChild(initList());
            return node;
        }
        return node;
    }

    // TODO: check the NILIST here
    private Node initList() {
        Node node = new Node("SPECIAL", "");
        node.addChild(init());
        node.addChild(initListTail());
        return node;
    }

    private Node initListTail() {
        Node node = new Node("SPECIAL", "");
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
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TEQUL, node);
        node.addChild(expr());
        return node;
    }

    private Node types() {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTYPD)) {
            consume(Tokeniser.TokenType.TTYPD, node);
            node.addChild(typeList());
            return node;
        }
        return node;
    }

    private Node typeList() {
        Node node = new Node("SPECIAL", "");
        node.addChild(type());
        node.addChild(typeListTail());
        return node;
    }

    private Node typeListTail() {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TIDEN)) {
            node.setType("NTYPEL");
            return typeList();
        }
        return node;
    }

    private Node type() {
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

    private Node fields() {
        Node node = new Node("SPECIAL", "");
        node.addChild(sDecl());
        node.addChild(fieldsTail());
        return node;
    }

    private Node fieldsTail() {
        Node node = new Node("SPECIAL", "");
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
        node.addChild(arrDeclsTail());
        return node;
    }

    private Node arrDeclsTail() {
        Node node = new Node("SPECIAL", "");
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
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TCOLN, node);
        consume(Tokeniser.TokenType.TIDEN, node);
        return node;
    }

    private Node funcs() {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TFUNC)) {
            node.setType("NFUNCS");
            node.addChild(func());
            node.addChild(funcs());
            return node;
        }
        return node;
    }

    private Node func() {
        Node node = new Node("NFUND", "");
        consume(Tokeniser.TokenType.TFUNC, node);
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
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TVOID)) {
            consume(Tokeniser.TokenType.TVOID, node);
            return node;
        } else {
            return sType();
        }
    }

    private Node pList() {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TCONS)) {
            return params();
        }
        return node;
    }

    private Node params() {
        Node node = new Node("SPECIAL", "");
        node.addChild(param());
        node.addChild(paramsTail());
        return node;
    }

    private Node paramsTail() {
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
    private Node param() {
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

    // private Node param() {
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

    private Node funcBody() {
        Node node = new Node("SPECIAL", "");
        node.addChild(locals());
        consume(Tokeniser.TokenType.TBEGN, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TTEND, node);
        return node;
    }

    private Node locals() {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TIDEN)) {
            return dList();
        }
        return node;
    }

    private Node dList() {
        Node node = new Node("SPECIAL", "");
        node.addChild(decl());
        node.addChild(dListTail());
        return node;
    }

    private Node dListTail() {
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
    private Node decl() {
        return new Node("SPECIAL", "");
    }

    private Node mainBody() {
        Node node = new Node("NMAIN", "");
        consume(Tokeniser.TokenType.TMAIN, node);
        node.addChild(sList());
        consume(Tokeniser.TokenType.TBEGN, node);
        node.addChild(stats());
        consume(Tokeniser.TokenType.TTEND, node);
        consume(Tokeniser.TokenType.TCD24, node);
        consume(Tokeniser.TokenType.TIDEN, node);
        return node;
    }

    private Node sList() {
        Node node = new Node("SPECIAL", "");
        node.addChild(sDecl());
        node.addChild(sListTail());
        return node;
    }

    private Node sListTail() {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NSDLST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(sList());
            return node;
        }
        return node;
    }

    private Node sDecl() {
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

    private Node sType() {
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

    private Node stats() {
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

    private Node statsTail() {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TTFOR) || match(Tokeniser.TokenType.TIFTH) || match(Tokeniser.TokenType.TSWTH)
                || match(Tokeniser.TokenType.TTTDO) || match(Tokeniser.TokenType.TREPT)
                || match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TINPT)
                || match(Tokeniser.TokenType.TRETN)) {
            node.setType("NSTATS");
            return stats();
        }
        return node;
    }

    private Node strStat() {
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

    private Node stat() {
        Node node = new Node("SPECIAL", "");
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

    private Node repStat() {
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

    private Node doStat() {
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

    private Node asgnList() {
        if (match(Tokeniser.TokenType.TIDEN)) {
            return aList();
        }
        return new Node("SPECIAL", "");
    }

    private Node aList() {
        Node node = new Node("SPECIAL", "");
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
        return new Node("NASGNS", "");
    }

    private Node ifStat() {
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

    private Node switchStat() {
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

    private Node caseList() {
        Node node = new Node("NCASLT", "");
        if (match(Tokeniser.TokenType.TCASE)) {
            Node caseNode = new Node("Case", "");
            consume(Tokeniser.TokenType.TCASE, node);
            caseNode.addChild(expr());
            consume(Tokeniser.TokenType.TCOLN, node);
            caseNode.addChild(stats());
            node.addChild(caseNode);
            node.addChild(caseList());
        } else {
            node.setType("SPECIAL");
            consume(Tokeniser.TokenType.TDFLT, node);
            consume(Tokeniser.TokenType.TCOLN, node);
            node.addChild(stats());
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
        Node node = new Node("", "");
        if (match(Tokeniser.TokenType.TPLEQ)) {
            node.setType("NPLEQ");
            node.addChild(new Node("PlusEqual", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TPLEQ, node);
        } else if (match(Tokeniser.TokenType.TMNEQ)) {
            node.setType("NMNEQ");
            node.addChild(new Node("MinusEqual", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TMNEQ, node);
        } else if (match(Tokeniser.TokenType.TSTEQ)) {
            node.setType("NSTEA");
            node.addChild(new Node("StarEqual", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TSTEQ, node);
        } else if (match(Tokeniser.TokenType.TDVEQ)) {
            node.setType("NDVEQ");
            node.addChild(new Node("DivideEqual", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TDVEQ, node);
        } else {
            node.setType("NASGN");
            node.addChild(new Node("Equal", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TEQUL, node);
        }
        return node;
    }

    private Node ioStat() {
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

    private Node callStat() {
        Node node = new Node("NCALL", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TLPAR, node);
        if (match(Tokeniser.TokenType.TNOTT)) {
            node.addChild(eList());
        }
        consume(Tokeniser.TokenType.TRPAR, node);
        return node;
    }

    private Node returnStat() {
        Node node = new Node("NRETN", "");
        consume(Tokeniser.TokenType.TRETN, node);
        if (match(Tokeniser.TokenType.TVOID)) {
            consume(Tokeniser.TokenType.TVOID, node);
        } else {
            node.addChild(expr());
        }
        return node;
    }

    private Node vList() {
        Node node = new Node("SPECIAL", "");
        node.addChild(var());
        node.addChild(vListTail());
        return node;
    }

    private Node vListTail() {
        Node node = new Node("NVLIST", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(vList());
            return node;
        }
        return node;
    }

    private Node var() {
        Node node = new Node("NSIMV", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        node.addChild(varTail());
        return node;
    }

    private Node varTail() {
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

    private Node varField() {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TDOTT)) {
            consume(Tokeniser.TokenType.TDOTT, node);
            node.addChild(new Node("Identifier", currentToken.getType().toString()));
            consume(Tokeniser.TokenType.TIDEN, node);
            return node;
        }
        return node;
    }

    private Node eList() {
        Node node = new Node("NEXPL", "");
        node.addChild(bool());
        node.addChild(eListTail());
        return node;
    }

    private Node eListTail() {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(eList());
            return node;
        }
        return node;
    }

    private Node bool() {
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

    private Node boolTail() {
        Node node = new Node("NBOOL", "");
        if (match(Tokeniser.TokenType.TTAND) || match(Tokeniser.TokenType.TTTOR) || match(Tokeniser.TokenType.TTXOR)) {
            node.addChild(logOp());
            node.addChild(rel());
            node.addChild(boolTail());
        }
        return node;
    }

    private Node rel() {
        Node node = new Node("SPECIAL", "");
        node.addChild(expr());
        node.addChild(relTail());
        return node;
    }

    private Node relTail() {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TEQEQ) || match(Tokeniser.TokenType.TNEQL) || match(Tokeniser.TokenType.TGRTR)
                || match(Tokeniser.TokenType.TLEQL) || match(Tokeniser.TokenType.TLESS)
                || match(Tokeniser.TokenType.TGEQL)) {
            node.addChild(relOp());
            node.addChild(expr());
        }
        return node;
    }

    private Node logOp() {
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

    private Node relOp() {
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

    private Node expr() {
        Node node = new Node("", "");
        node.addChild(term());
        node.addChild(exprTail());
        return node;
    }

    private Node exprTail() {
        Node node = new Node("", "");
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

    private Node term() {
        Node node = new Node("SPECIAL", "");
        node.addChild(fact());
        node.addChild(termTail());
        return node;
    }

    private Node termTail() {
        Node node = new Node("", "");
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
        return node;
    }

    private Node fact() {
        Node node = new Node("SPECIAL", "");
        node.addChild(exponent());
        node.addChild(factTail());
        return node;
    }

    private Node factTail() {
        Node node = new Node("", "");
        if (match(Tokeniser.TokenType.TCART)) {
            node.setType("NPOW");
            consume(Tokeniser.TokenType.TCART, node);
            node.addChild(exponent());
            node.addChild(factTail());
        }
        return node;
    }

    private Node exponent() {
        // TODO: remove exponent
        Node node = new Node("EXPONENT", "");
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
            node.addChild(bool());
            consume(Tokeniser.TokenType.TRBRK, node);
        } else {
            node.addChild(fnCall());
        }
        return node;
    }

    private Node fnCall() {
        Node node = new Node("NFCALL", "");
        consume(Tokeniser.TokenType.TIDEN, node);
        consume(Tokeniser.TokenType.TLBRK, node);
        if (match(Tokeniser.TokenType.TNOTT)) {
            node.addChild(eList());
        }
        consume(Tokeniser.TokenType.TRBRK, node);
        return node;
    }

    private Node prList() {
        Node node = new Node("SPECIAL", "");
        node.addChild(printItem());
        node.addChild(prListTail());
        return node;
    }

    private Node prListTail() {
        Node node = new Node("SPECIAL", "");
        if (match(Tokeniser.TokenType.TCOMA)) {
            node.setType("NPRLST");
            consume(Tokeniser.TokenType.TCOMA, node);
            node.addChild(prList());
        }
        return node;
    }

    private Node printItem() {
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
