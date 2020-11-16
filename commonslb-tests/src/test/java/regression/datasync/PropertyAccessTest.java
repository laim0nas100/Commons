package regression.datasync;

import lt.lb.commons.datasync.extractors.Extractors;
import lt.lb.commons.datasync.extractors.Extractors.BasicBeanPropertyAccess;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;


/**
 *
 * @author laim0nas100
 */
public class PropertyAccessTest {
    public class SimpleClass {
        private boolean ok;
        private String myName;
        private String CapName;

        public boolean isOk() {
            return ok;
        }

        public void setOk(boolean ok) {
            this.ok = ok;
        }

        public String getMyName() {
            return myName;
        }

        public void setMyName(String myName) {
            this.myName = myName;
        }

        public String getCapName() {
            return CapName;
        }

        public void setCapName(String CapName) {
            this.CapName = CapName;
        }

        @Override
        public String toString() {
            return "SimpleClass{" + "ok=" + ok + ", myName=" + myName + ", CapName=" + CapName + '}';
        }
        
        
        
    }
    
    @Test
    public void testProperties(){
        SimpleClass s = new SimpleClass();
        s.ok = true;
        s.myName = "SOME NAME";
        s.CapName ="CAP NAME";
        
        BasicBeanPropertyAccess<SimpleClass,Boolean> propBoolean = new Extractors.BasicBeanPropertyAccess<>(s,"ok");
        
        propBoolean.set(false);
        assertThat(s.ok).isFalse();
        BasicBeanPropertyAccess<SimpleClass,String> propName = new Extractors.BasicBeanPropertyAccess<>(s,"myName");
        propName.set("CHANGED NAME");
        assertThat(s.myName).isEqualTo("CHANGED NAME");
        BasicBeanPropertyAccess<SimpleClass,String> capName = new Extractors.BasicBeanPropertyAccess<>(s,"CapName");
        capName.set("CHANGED CAP NAME");
        assertThat(s.CapName).isEqualTo("CHANGED CAP NAME");
        
        
    }
    
    
}
