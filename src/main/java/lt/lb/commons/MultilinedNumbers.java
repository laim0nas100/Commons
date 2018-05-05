package lt.lb.commons;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Laimonas Beniušis
 */
public class MultilinedNumbers {

    public static class Number {

        public final String repr;

        public Number(ArrayList<String> lines) {

            String val = "";
            for (String s : lines) {
                val += s + ",";
            }
            repr = val;
        }

        @Override
        public String toString() {
            return repr;
        }

        public String niceFormat() {
            String s = new String();
            String[] split = repr.split(",");
            for (String str : split) {
                s += str + "\n";
            }
            return s;
        }

        public ArrayList<String> format() {
            ArrayList<String> list = new ArrayList<>();
            String[] split = repr.split(",");
            list.addAll(Arrays.asList(split));
            return list;
        }
    }

    public static class Parser {

        public ArrayList<MultilinedNumbers.Number> numSet;
        private final int ln;
        private final int col;
        private final int base;

        public Parser(int lines, int columns, ArrayList<String> Lines) {
            ln = lines;
            col = columns;
            base = Lines.get(0).length() / col;
            numSet = new ArrayList<>();
            ArrayList<ArrayList<String>> numbers = new ArrayList<>();
            for (int i = 0; i < base; i++) {
                numbers.add(new ArrayList<>());
            }
            for (int i = 0; i < ln; i++) {
                String line = Lines.get(i);
                int index = 0;
                for (int j = col; j <= line.length(); j += col) {
                    numbers.get(index++).add(line.substring(j - col, j));
                }
            }
            numbers.forEach(num -> {
                this.numSet.add(new Number(num));
            });
        }

        public Long valueOf(MultilinedNumbers.Number number) {
            ArrayList<String> format = number.format();
            ArrayList<MultilinedNumbers.Number> digits = new ArrayList<>();
            int formatSize = format.size();
            for (int i = 0; i < formatSize / ln; i++) {
                ArrayList<String> numb = new ArrayList<>();
                for (int j = 0; j < ln; j++) {
                    numb.add(format.get(i * ln + j));
                }
                digits.add(new Number(numb));
            }
            OmniBase.OmniNumber value = new OmniBase.OmniNumber(base);

            for (int j = 0; j < digits.size(); j++) {
                for (int i = 0; i < numSet.size(); i++) {
                    if (digits.get(j).repr.equals(numSet.get(i).repr)) {
                        value.addDigit(i);
                    }
                }
            }
            value.setUp();
            return value.valueIn10;

        }

        public MultilinedNumbers.Number getDigit(int val) {
            return numSet.get(val);
        }

        public MultilinedNumbers.Number getNumber(long val) {

            String valueOf = String.valueOf(val);
            OmniBase.OmniNumber number = new OmniBase.OmniNumber(valueOf, 10);
            OmniBase.OmniNumber inBase = number.getInBase(base);
            ArrayList<MultilinedNumbers.Number> numbers = new ArrayList<>();
            inBase.number.stream().forEach((n) -> {
                numbers.add(getDigit(n));
            });
            ArrayList<String> strings = new ArrayList<>();
            numbers.stream().forEach((n) -> {
                strings.addAll(n.format());
            });
            inBase.debugPrint();
            MultilinedNumbers.Number finalNumber = new MultilinedNumbers.Number(strings);
            return finalNumber;
        }

        @Override
        public String toString() {
            String s = new String();
            int i = 0;
            for (Number num : numSet) {
                s += i++ + "\n" + num.niceFormat();
            }
            return s;
        }

    }

}
