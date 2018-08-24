
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lt.lb.commons.Log;
import lt.lb.commons.reflect.ReflectNode;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Laimonas-Beniusis-PC
 */
public class ReflectTest {

    static class Cls {

        public Date publicDate = new Date();
        private String privateString = "private string";

        int packageInt = 10;

        protected Float protFloat = 13f;

        private Cls cls = null;
        
        
        
        

    }
    
    static class CCls extends Cls{
        public String publicString = "public string";
        
        
    }
    
    static class CClsOverride extends CCls{
        public Float protFloat = 15f;
    }
    
    static class CCls2Override extends CClsOverride{
        public Float protFloat;
        public Integer[] intArray = new Integer[]{1,2,3};
        public ArrayList<Integer> intList = Lists.newArrayList(3,2,1);
        private Map<String,Integer> intMap = new HashMap<>();
        public CCls2Override(){
            intMap.put("one", 1);
            intMap.put("two", 2);
            intList.ensureCapacity(intList.size()*10);
        }
        
    }
    
    public String formatValue(ReflectNode node){
        String str = "";
        do{
            str += node.getName()+"="+node.getValue();
            if(!node.isHiding()){
                break;
            }else{
                str += " hides: ";
                node = node.getHidden();
            }
        }while(true);
        return str;
    }

    public void keepPrinting(ReflectNode node, String indent) {
        if(node.isNull()){
            Log.print(node.getName()+" is null");
            return;
        }
        if(node.isArray()){
            Log.print(node.getName()+" is array");
        }
        Log.print(indent+node.getName()+" <v>");
        for (Map.Entry<String, ReflectNode> n : node.getAllValues().entrySet()) {
            ReflectNode value = n.getValue();
            Log.print(indent+" " + value.getParent().getName(), formatValue(value));

        }
        Log.print(indent+node.getName()+" </v>");
        Log.print(indent+node.getName()+" <c>");
        for (Map.Entry<String, ReflectNode> n : node.getAllChildren().entrySet()) {
            ReflectNode value = n.getValue();
            
            String suff = "";
            if(value.isNull()){
                suff += " = null";
            }
            Log.print(indent+" " +value.getName()+suff);
            if(!value.isNull()){
                keepPrinting(value, indent + "  " );
            }
            

        }
        Log.print(indent+node.getName()+" </c>");
    }

    @Test
    public void ok() throws Exception {

        Log.instant = true;
        Cls c = new Cls();
        Log.print("GO GO");
        ReflectNode node = new ReflectNode(c);

        keepPrinting(node,"");
        
        Log.print("");
        
        keepPrinting(new ReflectNode(new CCls()),"");
        
        
        Log.print("");
        
        keepPrinting(new ReflectNode(new CClsOverride()),"");
        
        Log.print("");
        
        ReflectNode rn2 = new ReflectNode(new CCls2Override());
        keepPrinting(rn2,"");
        
        Log.print();
        keepPrinting(rn2.getAllChildren().get("intList"),"");
        
        Log.await(1, TimeUnit.HOURS);
    }

}
