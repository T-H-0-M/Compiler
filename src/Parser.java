
public class Parser {
    private final Scanner scanner;
    private Token currentToken;

    public Parser() {
        this.scanner = null;
        this.currentToken = null;
    }

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.currentToken = scanner.nextToken();
    }

    private void consume(Tokeniser.TokenType expectedType) {
        if (currentToken.getType() == expectedType) {
            currentToken = scanner.nextToken();
        } else {
            throw new ParseException("Expected " + expectedType + ", but found " + currentToken.getType() + " on line "
                    + currentToken.getLine() + " and col " + currentToken.getCol());
        }
    }

    private boolean match(Tokeniser.TokenType expectedType) {
        return currentToken.getType() == expectedType;
    }

    public void parse() {
        program();
    }

    private void program() {
        consume(Tokeniser.TokenType.TCD24);
        consume(Tokeniser.TokenType.TIDEN);
        globals();
        funcs();
        mainBody();
    }

    private void globals() {
        consts();
        types();
        arrays();
    }

    private void consts() {
        if (match(Tokeniser.TokenType.TCONS)) {
            consume(Tokeniser.TokenType.TCONS);
            initList();
        }
        // ε case: do nothing
    }

    private void initList() {
        init();
        initListTail();
    }

    private void initListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA);
            initList();
        }
        // ε case: do nothing
    }

    private void init() {
        consume(Tokeniser.TokenType.TIDEN);
        consume(Tokeniser.TokenType.TEQUL);
        expr();
    }

    private void types() {
        if (match(Tokeniser.TokenType.TTYPD)) {
            consume(Tokeniser.TokenType.TTYPD);
            typeList();
        }
        // ε case: do nothing
    }

    private void typeList() {
        type();
        typeListTail();
    }

    // TODO: unsure of the tokens that map to struct id
    private void typeListTail() {
        if (match(Tokeniser.TokenType.TIDEN)) {
            typeList();
        }
        // ε case: do nothing
    }

    private void type() {
        consume(Tokeniser.TokenType.TIDEN);
        consume(Tokeniser.TokenType.TTDEF);
        if (match(Tokeniser.TokenType.TARAY)) {
            consume(Tokeniser.TokenType.TARAY);
            consume(Tokeniser.TokenType.TLBRK);
            expr();
            consume(Tokeniser.TokenType.TRBRK);
            consume(Tokeniser.TokenType.TTTOF);
            consume(Tokeniser.TokenType.TIDEN);
        } else {
            // INFO: else it is a structdef
            fields();
        }
        consume(Tokeniser.TokenType.TTEND);
    }

    private void fields() {
        sDecl();
        fieldsTail();
    }

    private void fieldsTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA);
            fields();
        }
        // ε case: do nothing
    }

    private void arrays() {
        if (match(Tokeniser.TokenType.TARRD)) {
            consume(Tokeniser.TokenType.TARRD);
            arrDecls();
        }
        // ε case: do nothing
    }

    private void arrDecls() {
        arrDecl();
        arrDeclsTail();
    }

    private void arrDeclsTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA);
            arrDecls();
        }
        // ε case: do nothing
    }

    private void arrDecl() {
        consume(Tokeniser.TokenType.TIDEN);
        consume(Tokeniser.TokenType.TCOLN);
        consume(Tokeniser.TokenType.TIDEN);
    }

    private void funcs() {
        if (match(Tokeniser.TokenType.TFUNC)) {
            func();
            funcs();
        }
        // ε case: do nothing
    }

    private void func() {
        consume(Tokeniser.TokenType.TFUNC);
        consume(Tokeniser.TokenType.TIDEN);
        consume(Tokeniser.TokenType.TLPAR);
        pList();
        consume(Tokeniser.TokenType.TRPAR);
        consume(Tokeniser.TokenType.TCOLN);
        rType();
        funcBody();
    }

    // TODO: Double check this
    private void rType() {
        if (match(Tokeniser.TokenType.TVOID)) {
            consume(Tokeniser.TokenType.TVOID);
        } else {
            sType();
        }
    }

    private void pList() {
        if (match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TCONS)) {
            params();
        }
        // ε case: do nothing
    }

    private void params() {
        param();
        paramsTail();
    }

    private void paramsTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            params();
        }
        // ε case: do nothing
    }

    private void param() {
        if (match(Tokeniser.TokenType.TCONS)) {
            consume(Tokeniser.TokenType.TCONS);
            arrDecl();
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            consume(Tokeniser.TokenType.TIDEN);
            if (match(Tokeniser.TokenType.TCOLN)) {
                consume(Tokeniser.TokenType.TCOLN);
                if (match(Tokeniser.TokenType.TIDEN)) {
                    consume(Tokeniser.TokenType.TIDEN);
                } else {
                    sType();
                }
            } else {
                consume(Tokeniser.TokenType.TCOLN);
                consume(Tokeniser.TokenType.TIDEN);
            }
        } else {
            throw new ParseException("Expected TCONS or TIDEN, but found " + currentToken.getType());
        }
    }

    private void funcBody() {
        locals();
        consume(Tokeniser.TokenType.TBEGN);
        stats();
        consume(Tokeniser.TokenType.TTEND);
    }

    private void locals() {
        if (match(Tokeniser.TokenType.TIDEN)) {
            dList();
        }
        // ε case: do nothing
    }

    private void dList() {
        decl();
        dListTail();
    }

    private void dListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA);
            dList();
        }
        // ε case: do nothing
    }

    // TODO: Complete this, the grammar is currently ambiguous
    private void decl() {
    }

    private void mainBody() {
        consume(Tokeniser.TokenType.TMAIN);
        sList();
        consume(Tokeniser.TokenType.TBEGN);
        stats();
        consume(Tokeniser.TokenType.TTEND);
        consume(Tokeniser.TokenType.TCD24);
        consume(Tokeniser.TokenType.TIDEN);
    }

    private void sList() {
        sDecl();
        sListTail();
    }

    private void sListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA);
            sList();
        }
        // ε case: do nothing
    }

    private void sDecl() {
        consume(Tokeniser.TokenType.TIDEN);
        consume(Tokeniser.TokenType.TCOLN);
        if (match(Tokeniser.TokenType.TIDEN)) {
            consume(Tokeniser.TokenType.TIDEN);
        } else {
            sType();
        }
    }

    private void sType() {
        if (match(Tokeniser.TokenType.TILIT)) {
            consume(Tokeniser.TokenType.TILIT);

        } else if (match(Tokeniser.TokenType.TFLIT)) {
            consume(Tokeniser.TokenType.TFLIT);
        } else {
            consume(Tokeniser.TokenType.TBOOL);
        }
    }

    private void stats() {
        if (match(Tokeniser.TokenType.TTFOR) || match(Tokeniser.TokenType.TIFTH) || match(Tokeniser.TokenType.TSWTH)
                || match(Tokeniser.TokenType.TTTDO)) {
            strStat();
        } else {
            stat();
            consume(Tokeniser.TokenType.TSEMI);
        }
        statsTail();
    }

    // TODO: Double check this statement
    private void statsTail() {
        if (match(Tokeniser.TokenType.TTFOR) || match(Tokeniser.TokenType.TIFTH) || match(Tokeniser.TokenType.TSWTH)
                || match(Tokeniser.TokenType.TTTDO) || match(Tokeniser.TokenType.TREPT)
                || match(Tokeniser.TokenType.TIDEN) || match(Tokeniser.TokenType.TINPT)
                || match(Tokeniser.TokenType.TRETN)) {
            stats();
        }
        // ε case: do nothing
    }

    private void strStat() {
        if (match(Tokeniser.TokenType.TTFOR)) {
            forStat();
        } else if (match(Tokeniser.TokenType.TIFTH)) {
            ifStat();
        } else if (match(Tokeniser.TokenType.TSWTH)) {
            switchStat();
        }
        doStat();
    }

    private void stat() {
        if (match(Tokeniser.TokenType.TTTDO)) {
            repStat();
        } else if (match(Tokeniser.TokenType.TREPT)) {
            asgnStat();
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            ioStat();
        } else if (match(Tokeniser.TokenType.TINPT)) {
            callStat();
        }
        returnStat();
    }

    private void forStat() {
        consume(Tokeniser.TokenType.TTFOR);
        consume(Tokeniser.TokenType.TLPAR);
        asgnList();
        consume(Tokeniser.TokenType.TSEMI);
        bool();
        consume(Tokeniser.TokenType.TLPAR);
        stats();
        consume(Tokeniser.TokenType.TTEND);
    }

    private void repStat() {
        consume(Tokeniser.TokenType.TREPT);
        consume(Tokeniser.TokenType.TLPAR);
        asgnList();
        consume(Tokeniser.TokenType.TRPAR);
        stats();
        consume(Tokeniser.TokenType.TUNTL);
        bool();
    }

    private void doStat() {
        consume(Tokeniser.TokenType.TTTDO);
        stats();
        consume(Tokeniser.TokenType.TWHIL);
        consume(Tokeniser.TokenType.TLPAR);
        bool();
        consume(Tokeniser.TokenType.TRPAR);
        consume(Tokeniser.TokenType.TTEND);
    }

    private void asgnList() {
        if (match(Tokeniser.TokenType.TIDEN)) {
            aList();
        }
        // ε case: do nothing
    }

    private void aList() {
        asgnStat();
        aListTail();
    }

    private void aListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA);
            aList();
        }
        // ε case: do nothing
    }

    private void ifStat() {
        consume(Tokeniser.TokenType.TIFTH);
        consume(Tokeniser.TokenType.TLPAR);
        bool();
        consume(Tokeniser.TokenType.TRPAR);
        stats();
        if (match(Tokeniser.TokenType.TELSE)) {
            consume(Tokeniser.TokenType.TELSE);
            stats();
        } else if (match(Tokeniser.TokenType.TELIF)) {
            consume(Tokeniser.TokenType.TLPAR);
            bool();
            consume(Tokeniser.TokenType.TRPAR);
            stats();
        }
        consume(Tokeniser.TokenType.TTEND);
    }

    private void switchStat() {
        consume(Tokeniser.TokenType.TSWTH);
        consume(Tokeniser.TokenType.TLPAR);
        expr();
        consume(Tokeniser.TokenType.TRPAR);
        consume(Tokeniser.TokenType.TBEGN);
        caseList();
        consume(Tokeniser.TokenType.TTEND);
    }

    private void caseList() {
        if (match(Tokeniser.TokenType.TCASE)) {
            consume(Tokeniser.TokenType.TCASE);
            expr();
            consume(Tokeniser.TokenType.TCOLN);
            stats();
            caseList();
        } else {
            consume(Tokeniser.TokenType.TDFLT);
            consume(Tokeniser.TokenType.TCOLN);
            stats();
        }
    }

    private void asgnStat() {
        var();
        asgnOp();
        bool();
    }

    private void asgnOp() {
        if (match(Tokeniser.TokenType.TPLEQ)) {
            consume(Tokeniser.TokenType.TPLEQ);
        } else if (match(Tokeniser.TokenType.TMNEQ)) {
            consume(Tokeniser.TokenType.TMNEQ);
        } else if (match(Tokeniser.TokenType.TSTEQ)) {
            consume(Tokeniser.TokenType.TSTEQ);
        } else if (match(Tokeniser.TokenType.TDVEQ)) {
            consume(Tokeniser.TokenType.TDVEQ);
        }
        consume(Tokeniser.TokenType.TEQUL);
    }

    private void ioStat() {
        if (match(Tokeniser.TokenType.TINPT)) {
            consume(Tokeniser.TokenType.TINPT);
            vList();
        } else if (match(Tokeniser.TokenType.TPRNT)) {
            consume(Tokeniser.TokenType.TPRNT);
            prList();
        } else {
            consume(Tokeniser.TokenType.TPRLN);
            prList();
        }
    }

    private void callStat() {
        consume(Tokeniser.TokenType.TIDEN);
        consume(Tokeniser.TokenType.TLPAR);
        if (match(Tokeniser.TokenType.TNOTT)) {
            eList();
        }
        consume(Tokeniser.TokenType.TRPAR);
    }

    private void returnStat() {
        consume(Tokeniser.TokenType.TRETN);
        if (match(Tokeniser.TokenType.TVOID)) {
            consume(Tokeniser.TokenType.TVOID);
        }
        expr();
    }

    private void vList() {
        var();
        vListTail();
    }

    private void vListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA);
            vList();
        }
        // ε case: do nothing
    }

    private void var() {
        consume(Tokeniser.TokenType.TIDEN);
        varTail();
    }

    private void varTail() {
        if (match(Tokeniser.TokenType.TLBRK)) {
            consume(Tokeniser.TokenType.TLBRK);
            expr();
            consume(Tokeniser.TokenType.TRBRK);
            varField();
        }
        // ε case: do nothing
    }

    private void varField() {
        if (match(Tokeniser.TokenType.TDOTT)) {
            consume(Tokeniser.TokenType.TDOTT);
            consume(Tokeniser.TokenType.TIDEN);
        }
        // ε case: do nothing
    }

    private void eList() {
        bool();
        eListTail();
    }

    private void eListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA);
            eList();
        }
        // ε case: do nothing
    }

    private void bool() {
        if (match(Tokeniser.TokenType.TNOTT)) {
            consume(Tokeniser.TokenType.TNOTT);
            bool();
        } else {
            rel();
            boolTail();
        }
    }

    private void boolTail() {
        if (match(Tokeniser.TokenType.TTAND) || match(Tokeniser.TokenType.TTTOR) || match(Tokeniser.TokenType.TTXOR)) {
            logOp();
            rel();
            boolTail();
        }
        // ε case: do nothing
    }

    private void rel() {
        expr();
        relTail();
    }

    private void relTail() {
        if (match(Tokeniser.TokenType.TEQEQ) || match(Tokeniser.TokenType.TNEQL) || match(Tokeniser.TokenType.TGRTR)
                || match(Tokeniser.TokenType.TLEQL) || match(Tokeniser.TokenType.TLESS)
                || match(Tokeniser.TokenType.TGEQL)) {
            relOp();
            expr();
        }
        // ε case: do nothing
    }

    private void logOp() {
        if (match(Tokeniser.TokenType.TTAND)) {
            consume(Tokeniser.TokenType.TTAND);
        } else if (match(Tokeniser.TokenType.TTTOR)) {
            consume(Tokeniser.TokenType.TTTOR);
        } else {
            consume(Tokeniser.TokenType.TTXOR);
        }
    }

    private void relOp() {
        if (match(Tokeniser.TokenType.TEQEQ)) {
            consume(Tokeniser.TokenType.TEQEQ);
        } else if (match(Tokeniser.TokenType.TNEQL)) {
            consume(Tokeniser.TokenType.TNEQL);
        } else if (match(Tokeniser.TokenType.TGRTR)) {
            consume(Tokeniser.TokenType.TGRTR);
        } else if (match(Tokeniser.TokenType.TLEQL)) {
            consume(Tokeniser.TokenType.TLEQL);
        } else if (match(Tokeniser.TokenType.TLESS)) {
            consume(Tokeniser.TokenType.TLESS);
        } else {
            consume(Tokeniser.TokenType.TGEQL);
        }
    }

    private void expr() {
        term();
        exprTail();
    }

    private void exprTail() {
        if (match(Tokeniser.TokenType.TPLUS)) {
            consume(Tokeniser.TokenType.TPLUS);
            term();
            exprTail();
        } else if (match(Tokeniser.TokenType.TMINS)) {
            consume(Tokeniser.TokenType.TPLUS);
            term();
            exprTail();
        }
        // ε case: do nothing
    }

    private void term() {
        fact();
        termTail();
    }

    private void termTail() {
        if (match(Tokeniser.TokenType.TSTAR)) {
            consume(Tokeniser.TokenType.TSTAR);
            fact();
            termTail();
        } else if (match(Tokeniser.TokenType.TDIVD)) {
            consume(Tokeniser.TokenType.TDIVD);
            fact();
            termTail();
        } else if (match(Tokeniser.TokenType.TPERC)) {
            consume(Tokeniser.TokenType.TPERC);
            fact();
            termTail();
        }
        // ε case: do nothing
    }

    private void fact() {
        exponent();
        factTail();
    }

    private void factTail() {
        if (match(Tokeniser.TokenType.TCART)) {
            consume(Tokeniser.TokenType.TCART);
            exponent();
            factTail();
        }
        // ε case: do nothing
    }

    private void exponent() {
        if (match(Tokeniser.TokenType.TTRUE)) {
            consume(Tokeniser.TokenType.TTRUE);
        } else if (match(Tokeniser.TokenType.TFALS)) {
            consume(Tokeniser.TokenType.TFALS);
        } else if (match(Tokeniser.TokenType.TIDEN)) {
            consume(Tokeniser.TokenType.TIDEN);
        } else if (match(Tokeniser.TokenType.TILIT)) {
            consume(Tokeniser.TokenType.TILIT);
        } else if (match(Tokeniser.TokenType.TFLIT)) {
            consume(Tokeniser.TokenType.TFLIT);
        } else if (match(Tokeniser.TokenType.TLBRK)) {
            consume(Tokeniser.TokenType.TLBRK);
            bool();
            consume(Tokeniser.TokenType.TRBRK);
        } else {
            fnCall();
        }
    }

    private void fnCall() {
        consume(Tokeniser.TokenType.TIDEN);
        consume(Tokeniser.TokenType.TLBRK);
        if (match(Tokeniser.TokenType.TNOTT)) {
            eList();
        }
        consume(Tokeniser.TokenType.TRBRK);
    }

    private void prList() {
        printItem();
        prListTail();
    }

    private void prListTail() {
        if (match(Tokeniser.TokenType.TCOMA)) {
            consume(Tokeniser.TokenType.TCOMA);
            prList();
        }
        // ε case: do nothing
    }

    private void printItem() {
        if (match(Tokeniser.TokenType.TSTRG)) {
            consume(Tokeniser.TokenType.TSTRG);
        } else {
            expr();
        }
    }
}
