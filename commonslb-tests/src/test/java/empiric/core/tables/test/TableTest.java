package empiric.core.tables.test;

import java.util.ArrayList;
import lt.lb.commons.containers.tables.CellPrep;
import lt.lb.commons.containers.tables.CellTable;
import lt.lb.commons.containers.tables.Formatters;

/**
 *
 * @author Laimonas-Beniusis
 */
public class TableTest {

    public static class TC<C> {

        public int x, y;
        public C content;

        public TC(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "TC{" + "x=" + x + ", y=" + y + '}';
        }

    }

    public static class Row extends ArrayList<TC> {

    }


    public static void main(String[] args) {
        CellTable<TC, TC> table = new CellTable();

        int rows = 10;
        int cols = 4;
        for (int i = 0; i < rows; i++) {
            Row row = new Row();
            for (int j = 0; j < cols; j++) {
                row.add(new TC(j, i));
            }

            table.addRow(row);
        }

        Formatters<TC> format = table.selectCells()
                .withFullTable()
                .addFormat(c -> {
                    System.out.print("Full " + c);
                })
                .cleanSelectionStart()
                .withRows(0)
                .addFormat(c -> {
                    System.out.print(" header " + c);
                })
                
                .cleanSelectionStart()
                .withColumns(cols-1)
                .addFormat(c->{
                    System.out.println("--");
                })
                
                
                
                .getFormatterMap();
        
        

        table.renderRows(format, (f, i, row) -> {
            for (CellPrep<TC> tc : row) {
                f.applyApplicable(tc, tc.getContent().get());
            }
        });

    }

}
