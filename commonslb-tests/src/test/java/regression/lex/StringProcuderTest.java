package regression.lex;

import java.text.SimpleDateFormat;
import java.util.Date;
import lt.lb.commons.parsing.StringProducer;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class StringProcuderTest {
    
    public static class SimpleCtx {
        
        String name;
        Date now = new Date();
    }
    
    public static class FullCtx extends SimpleCtx{
        String ok;
    }
    
    @Test
    public void test() throws Exception {
        SimpleCtx context = new SimpleCtx();
        context.name = "Sailor";
        
        StringProducer<SimpleCtx> hello = StringProducer.ofConstant("Hello");
        StringProducer<SimpleCtx> space = StringProducer.ofConstant(" ");
        StringProducer<SimpleCtx> name = ctx -> ctx.name;
        StringProducer<SimpleCtx> date = ctx -> new SimpleDateFormat("YY").format(ctx.now);
        
        StringProducer<SimpleCtx> ofAll = StringProducer.ofAll(hello, space, name, StringProducer.ofConstant(" in year "), date);
        
        assertThat(ofAll.apply(context)).isEqualTo("Hello Sailor in year " + new SimpleDateFormat("YY").format(context.now));
        
    }
}
