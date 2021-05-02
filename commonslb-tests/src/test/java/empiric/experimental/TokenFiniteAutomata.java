/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.experimental;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lt.lb.commons.DLog;
import lt.lb.commons.F;
import lt.lb.commons.Ins;
import lt.lb.commons.Lazy;
import lt.lb.commons.containers.values.Value;
import lt.lb.uncheckedutils.SafeOpt;
import lt.lb.commons.interfaces.StringBuilderActions.ILineAppender;
import lt.lb.commons.iteration.For;
import lt.lb.commons.iteration.ReadOnlyIterator;
import lt.lb.commons.misc.UUIDgenerator;
import lt.lb.commons.misc.compare.ComparatorBuilder;
import lt.lb.commons.misc.compare.Compare;
import lt.lb.commons.misc.compare.Compare.SimpleCompare;
import lt.lb.commons.parsing.Lexer;
import lt.lb.commons.parsing.token.Literal;
import lt.lb.commons.parsing.StringOp;
import lt.lb.commons.parsing.token.Token;
import lt.lb.commons.parsing.token.TokenPos;
import lt.lb.commons.parsing.token.TokenProducer;
import lt.lb.commons.parsing.token.match.DefaultMatchedTokenProducer;
import lt.lb.commons.parsing.token.match.MatchedTokenProducer;
import lt.lb.commons.parsing.token.match.MatchedTokens;
import lt.lb.commons.parsing.token.match.TokenMatcher;
import lt.lb.commons.parsing.token.match.TokenMatchers;

/**
 *
 * @author laim0nas100
 */
public class TokenFiniteAutomata {

    public static final String OPERATOR_AND = "AND";
    public static final String OPERATOR_OR = "OR";
    public static final String OPERATOR_NOT = "NOT";

    public static final String OPERATOR_WILD_QUESTION = "?";
    public static final String OPERATOR_WILD_STAR = "*";
    public static final String OPERATOR_WILD_QUESTION_ESC = "\\?";
    public static final String OPERATOR_WILD_STAR_ESC = "\\*";


    public static void main(String[] args) throws Exception {
        Lexer lexer = new Lexer();
        lexer.addKeywordBreaking(OPERATOR_WILD_QUESTION, OPERATOR_WILD_QUESTION_ESC, OPERATOR_WILD_STAR, OPERATOR_WILD_STAR_ESC);

        String term = "*hell?o\\?" + "__REV__" + "**something else";
        lexer.resetLines(Arrays.asList(term));
        ArrayList<Token> tokens = lexer.getRemainingTokens();

        TokenMatcher wildStar = TokenMatchers.exact(OPERATOR_WILD_STAR).named("wild_star");
        TokenMatcher wildQuestion = TokenMatchers.exact(OPERATOR_WILD_QUESTION).named("wild_question");
        TokenMatcher wildStarEsc = TokenMatchers.exact(OPERATOR_WILD_STAR_ESC).named("wild_star_esc");
        TokenMatcher wildQuestionEsc = TokenMatchers.exact( OPERATOR_WILD_QUESTION_ESC).named("wild_question_escape");
        TokenMatcher literal = TokenMatchers.literalType();

        TokenMatcher wildCard = TokenMatchers.or(wildStar, wildQuestion).named("wild_card");
        TokenMatcher wildCard_Literal = TokenMatchers.concat(wildCard, literal);
        TokenMatcher literal_wildCard = TokenMatchers.concat(literal, wildCard);
        TokenMatcher wildCard_Literal_wildcard = TokenMatchers.concat(wildCard, literal, wildCard);

        DLog.main().async = false;
        DLog.printLines(tokens);
        List<TokenMatcher> asList = Arrays.asList(wildCard_Literal_wildcard, literal_wildCard, wildCard_Literal, wildCard, wildStar, wildQuestion, wildStarEsc, wildQuestionEsc, literal);
        DefaultMatchedTokenProducer producer = new DefaultMatchedTokenProducer(tokens.iterator(), asList);
        
        producer.forEachRemaining(DLog::print);

        DLog.close();
    }

}
