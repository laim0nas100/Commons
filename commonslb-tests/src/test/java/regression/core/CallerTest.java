package regression.core;

import empiric.core.StackOverflowTest.RecursionBuilder;
import java.math.BigInteger;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class CallerTest {

    @Test
    public void callerTest(){
        int exp = 20;
        BigInteger big = BigInteger.valueOf(100);
        BigInteger fibb = RecursionBuilder.fibb(BigInteger.valueOf(1), BigInteger.valueOf(1), big.pow(exp));
        BigInteger resolve = RecursionBuilder.fibbCaller(BigInteger.valueOf(1), BigInteger.valueOf(1), big.pow(exp)).resolve();
        assert fibb.equals(resolve);
        
        BigInteger m = BigInteger.valueOf(2);
        BigInteger n = BigInteger.valueOf(8);
        assert RecursionBuilder.ackermann(m, n).equals(RecursionBuilder.ackermannCaller(m, n).resolve());
    }

}
