import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class Tokeniser {

    public enum TokenType {
        // End of file
        TTEOF,

        // Keywords
        TCD24, TCONS, TTYPD, TTDEF, TARRD, TMAIN, TBEGN, TTEND, TARAY, TTTOF, TFUNC, TVOID,
        TCNST, TINTG, TFLOT, TBOOL, TTFOR, TREPT, TUNTL, TTTDO, TWHIL, TIFTH, TELSE, TELIF,
        TSWTH, TCASE, TDFLT, TBREK, TINPT, TPRNT, TPRLN, TRETN, TNOTT, TTAND, TTTOR, TTXOR,
        TTRUE, TFALS,

        // Operators and delimiters
        TCOMA, TLBRK, TRBRK, TLPAR, TRPAR, TEQUL, TPLUS, TMINS, TSTAR, TDIVD, TPERC, TCART,
        TLESS, TGRTR, TCOLN, TSEMI, TDOTT, TLEQL, TGEQL, TNEQL, TEQEQ, TPLEQ, TMNEQ, TSTEQ, TDVEQ,

        // Other token types
        TIDEN, TILIT, TFLIT, TSTRG, TUNDF
    }

    private static final EnumMap<TokenType, Integer> tokenCodeTable = new EnumMap<>(TokenType.class);
    private static final Map<String, TokenType> keywordTable = new HashMap<>();
    private static final Map<String, TokenType> operatorTable = new HashMap<>();

    static {
        // Initialize token codes
        tokenCodeTable.put(TokenType.TTEOF, 0);
        for (int i = 1; i <= 38; i++) {
            tokenCodeTable.put(TokenType.values()[i], i);
        }
        for (int i = 39; i <= 63; i++) {
            tokenCodeTable.put(TokenType.values()[i], i);
        }
        tokenCodeTable.put(TokenType.TIDEN, 64);
        tokenCodeTable.put(TokenType.TILIT, 65);
        tokenCodeTable.put(TokenType.TFLIT, 66);
        tokenCodeTable.put(TokenType.TSTRG, 67);
        tokenCodeTable.put(TokenType.TUNDF, 68);

        // Initialize keyword table
        keywordTable.put("cd24", TokenType.TCD24);
        keywordTable.put("constants", TokenType.TCONS);
        keywordTable.put("typedef", TokenType.TTYPD);
        keywordTable.put("def", TokenType.TTDEF);
        keywordTable.put("arraydef", TokenType.TARRD);
        keywordTable.put("main", TokenType.TMAIN);
        keywordTable.put("begin", TokenType.TBEGN);
        keywordTable.put("end", TokenType.TTEND);
        keywordTable.put("array", TokenType.TARAY);
        keywordTable.put("of", TokenType.TTTOF);
        keywordTable.put("func", TokenType.TFUNC);
        keywordTable.put("void", TokenType.TVOID);
        keywordTable.put("const", TokenType.TCNST);
        keywordTable.put("int", TokenType.TINTG);
        keywordTable.put("float", TokenType.TFLOT);
        keywordTable.put("bool", TokenType.TBOOL);
        keywordTable.put("for", TokenType.TTFOR);
        keywordTable.put("repeat", TokenType.TREPT);
        keywordTable.put("until", TokenType.TUNTL);
        keywordTable.put("do", TokenType.TTTDO);
        keywordTable.put("while", TokenType.TWHIL);
        keywordTable.put("if", TokenType.TIFTH);
        keywordTable.put("else", TokenType.TELSE);
        keywordTable.put("elif", TokenType.TELIF);
        keywordTable.put("switch", TokenType.TSWTH);
        keywordTable.put("case", TokenType.TCASE);
        keywordTable.put("default", TokenType.TDFLT);
        keywordTable.put("break", TokenType.TBREK);
        keywordTable.put("input", TokenType.TINPT);
        keywordTable.put("print", TokenType.TPRNT);
        keywordTable.put("printline", TokenType.TPRLN);
        keywordTable.put("return", TokenType.TRETN);
        keywordTable.put("not", TokenType.TNOTT);
        keywordTable.put("and", TokenType.TTAND);
        keywordTable.put("or", TokenType.TTTOR);
        keywordTable.put("xor", TokenType.TTXOR);
        keywordTable.put("true", TokenType.TTRUE);
        keywordTable.put("false", TokenType.TFALS);

        // Initialize operator table
        operatorTable.put(",", TokenType.TCOMA);
        operatorTable.put("[", TokenType.TLBRK);
        operatorTable.put("]", TokenType.TRBRK);
        operatorTable.put("(", TokenType.TLPAR);
        operatorTable.put(")", TokenType.TRPAR);
        operatorTable.put("=", TokenType.TEQUL);
        operatorTable.put("+", TokenType.TPLUS);
        operatorTable.put("-", TokenType.TMINS);
        operatorTable.put("*", TokenType.TSTAR);
        operatorTable.put("/", TokenType.TDIVD);
        operatorTable.put("%", TokenType.TPERC);
        operatorTable.put("^", TokenType.TCART);
        operatorTable.put("<", TokenType.TLESS);
        operatorTable.put(">", TokenType.TGRTR);
        operatorTable.put(":", TokenType.TCOLN);
        operatorTable.put(";", TokenType.TSEMI);
        operatorTable.put(".", TokenType.TDOTT);
        operatorTable.put("<=", TokenType.TLEQL);
        operatorTable.put(">=", TokenType.TGEQL);
        operatorTable.put("!=", TokenType.TNEQL);
        operatorTable.put("==", TokenType.TEQEQ);
        operatorTable.put("+=", TokenType.TPLEQ);
        operatorTable.put("-=", TokenType.TMNEQ);
        operatorTable.put("*=", TokenType.TSTEQ);
        operatorTable.put("/=", TokenType.TDVEQ);
    }
    // TODO: map other items to their tokens

    public static int getTokenCode(TokenType type) {
        return tokenCodeTable.get(type);
    }

    public static TokenType getKeywordTokenType(String keyword) {
        return keywordTable.get(keyword.toLowerCase());
    }

    public static TokenType getOperatorTokenType(String operator) {
        return operatorTable.get(operator);
    }

}
