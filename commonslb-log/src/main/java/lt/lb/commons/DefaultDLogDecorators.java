package lt.lb.commons;

import java.util.Iterator;
import java.util.function.Supplier;
import lt.lb.commons.func.Lambda;
import lt.lb.commons.iteration.ReadOnlyIterator;

/**
 *
 * @author laim0nas100
 */
public class DefaultDLogDecorators {
    
    public static final int MAX_STACK_TRACE_SIZE = 1000;

    public static Lambda.L5R<DLog, String, String, Long, String, String> finalPrintDecorator() {
        return (DLog log, String trace, String name, Long millis, String string) -> {
            String timeSt = log.timeStamp ? DLog.getZonedDateTime(log.timeStringFormat, millis) : "";
            String threadSt = log.threadName ? "[" + name + "]" : "";
            if (!trace.isEmpty()) {
                int firstComma = trace.indexOf("(");
                int lastComma = trace.indexOf(")");
                if (firstComma > 0 && lastComma > firstComma && lastComma > 0) {
                    trace = "@" + trace.substring(firstComma + 1, lastComma) + ":";
                }
            }
            String str = log.surroundString ? "{" + string + "}" : string;
            return timeSt + threadSt + trace + str;
        };
    }

    public static Lambda.L1R<Object[], Supplier<String>> printLnDecorator() {
        return (Object[] objs) -> {
            return () -> {
                LineStringBuilder sb = new LineStringBuilder();
                if (objs.length == 1) {
                    sb.append(String.valueOf(objs[0]));
                } else if (objs.length > 1) {
                    for (Object s : objs) {
                        sb.appendLine(String.valueOf(s));
                    }
                    sb.removeFromEnd(sb.lineEnding.length());
                }
                return sb.toString();
            };
        };
    }

    public static Lambda.L1R<Object[], Supplier<String>> printDecorator() {
        return (Object[] objs) -> {
            return () -> {
                LineStringBuilder string = new LineStringBuilder();
                if (objs.length > 0) {
                    for (Object s : objs) {
                        string.append(", " + s);
                    }
                    string.delete(0, 2);
                }
                return string.toString();
            };

        };
    }

    public static Lambda.L1R<Iterable, Supplier<String>> printLinesDecorator() {
        return (Iterable col) -> {
            return () -> {
                LineStringBuilder string = new LineStringBuilder();
                boolean empty = true;
                for (Object s : col) {
                    empty = false;
                    string.appendLine(s);
                }
                if (!empty) {
                    string.prependLine();
                }
                return string.toString();
            };

        };
    }

    public static Lambda.L1R<Iterator, Supplier<String>> printIterDecorator() {
        return (Iterator col) -> {
            return () -> {
                LineStringBuilder string = new LineStringBuilder();
                ReadOnlyIterator iter = ReadOnlyIterator.of(col);
                if (iter.hasNext()) {
                    for (Object s : iter) {
                        string.appendLine(s);
                    }
                    string.prependLine();
                }
                return string.toString();
            };

        };
    }

    public static Lambda.L2R<Throwable, Integer, Supplier<String>> stackTraceSupplier() {
        return (th, i) -> () -> th.getStackTrace()[i].toString().replace(".java", "");
    }

    public static Lambda.L3R<Throwable, Integer, Integer, Supplier<String>> stackTraceFullSupplier() {

        return (th, depth, reduceBy) -> {
            StackTraceElement[] stack = th.getStackTrace();
            int preFinal = depth < 0 ? MAX_STACK_TRACE_SIZE : depth;

            final int maxDepth = Math.min(preFinal, stack.length);

            Supplier<String> string = () -> {
                StringBuilder b = new StringBuilder("Stack trace:");
                for (int i = reduceBy; i < maxDepth; i++) {
                    b.append("\n\t").append(stack[i]);
                }
                return b.toString();
            };

            return string;
        };

    }
}
