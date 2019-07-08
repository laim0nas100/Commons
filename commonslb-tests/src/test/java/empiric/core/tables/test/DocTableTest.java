/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core.tables.test;

import lt.lb.commons.containers.tables.CellFormatBuilder;
import lt.lb.commons.containers.tables.CellTable;
import lt.lb.commons.F;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class DocTableTest {

    @Test
    public void test() throws Exception {
        CellTable<String> table = new CellTable<>();

        table.addRow("1", "2", "3");
        table.addRow("A", "B", "C");
        table.mergeHorizontal(0, 2, 1);

        CellFormatBuilder<String> toRightBottomCornerAt = table.selectCells()
                .withRectangleStartingAt(0, 1)
                .toRightBottomCornerAt(1, 1);
        toRightBottomCornerAt.forEachCell(c -> {
            System.out.println(c);
            c.mapContent(s -> "[" + s + "]");
        });
        
        table.selectCells().withIndex(0).andColumn(0).forEachCell(c ->{
            
        });
        table.renderRows((ri, cells) -> {

            F.iterate(cells, (i, c) -> {
                if (i != 0) {
                    System.out.print(" ");
                }
                c.getContent().ifPresent(co -> {
                    System.out.print(co);
                });

            });
            System.out.println();
        });

    }

}
