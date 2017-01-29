/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB;

import java.util.ArrayList;

/**
 *
 * @author Laimonas Beniušis
 */
public class OmniBase {
    public static final class OmniNumber{
            public final void debugPrint(){
                System.err.println("10 base:"+valueIn10);System.err.println(base+" base:"+number);System.err.println("Binary:"+inBinary);  
            }
            private final boolean saveInBinary;
            private final long base;
            public long valueIn10;
            public ArrayList<Integer> inBinary;
            public ArrayList<Integer> number;
            public OmniNumber(long base,Boolean...bol){
                init();
                if(bol.length>0){
                    saveInBinary = bol[0];
                }else{
                    saveInBinary = false;
                }
                this.base = base;
            }
            public OmniNumber(String numb,long base,Boolean...bol){
                if(bol.length>0){
                    saveInBinary = bol[0];
                }else{
                    saveInBinary = false;
                }
                this.base = base;
                init();
                for(Character c:numb.toCharArray()){
                    addDigit(Integer.parseInt(Character.toString(c)));
                }
                setUp();
            }
            public OmniNumber getNumber(ArrayList<Integer> numb, long base){
                OmniBase.OmniNumber newNumber = new OmniBase.OmniNumber(base);
                numb.stream().forEach((n) -> {
                    newNumber.addDigit(n);
                });
                newNumber.setUp();
                return newNumber;
            }
            private void init(){
                inBinary = new ArrayList<>();
                number = new ArrayList<>();
            }
            public final void addDigit(Integer digit){
                number.add(digit);
            }
            public final void setUp(){
                
                long value = 0;
                int power = 0;
                for(int i=number.size()-1; i>=0; i--){
                    int num = number.get(i);
                    value+= num * Math.pow(base, power++);
                }
                this.valueIn10 = value;
                inBinary.add(0);
                if(saveInBinary){
                    while(value!=0){
                        //System.err.print(value+" ");
                        add1InBinary();
                        value--;
                    }
                }
                
                //System.err.println(value);
            }
            public final void add1InBinary(){
                int i=0;
                boolean carry = true;
                while(carry){
                    if(inBinary.size()-1<i){
                            ArrayList<Integer> zeros = new ArrayList<>();
                            zeros.add(0);zeros.add(0);zeros.add(0);
                            inBinary.addAll(0, zeros); 
                        }
                    int numb = inBinary.get(i);
                    //System.err.println(i +" "+numb);
                    numb++;
                    if(numb > 1){
                        inBinary.set(i, 0);
                        carry = true;
                        
                    }else{
                        inBinary.set(i, 1);
                        
                        carry = false;
                    }
                    i++;
                }
            }
            public OmniBase.OmniNumber getInBase(long base){
                int power = 0;
                long tempValue = valueIn10;
                
                ArrayList<Integer> values = new ArrayList<>();
                if(valueIn10==0){
                    values.add(0);
                    return getNumber(values,base);
                }
                while(tempValue - Math.pow(base, power)>=0){
                    power++;
                }
                power--;
                while(power>=0){
                    int numb = 0;
                    while(tempValue - Math.pow(base, power)>=0){
                        tempValue -=Math.pow(base, power);
                        numb++;
                    }
                    values.add(numb);
                    power--;
                }
                return getNumber(values,base);
            }
            @Override
            public String toString(){
                return ""+valueIn10;
            }
        }
}
