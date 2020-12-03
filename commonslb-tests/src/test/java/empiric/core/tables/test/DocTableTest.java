/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core.tables.test;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lt.lb.commons.containers.tables.CellFormatBuilder;
import lt.lb.commons.containers.tables.CellTable;
import lt.lb.commons.F;
import lt.lb.commons.FastIDGen.FastID;
import lt.lb.commons.func.Lambda;
import lt.lb.commons.iteration.For;
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

        Map<FastID, List<Consumer>> formatterMap = table.selectCells()
                .withRectangleStartingAt(0, 1)
                .toRightBottomCornerAt(1, 1)
                .forEachCell(c -> {
                    System.out.println(c);
                    c.mapContent(s -> "[" + s + "]");
                })
                .addFormat(Lambda.L1.empty())
                .addToSelection()
                .withColumns(0,1)
                .forEachCell(c ->{
                    System.out.println(c);
                    c.mapContent(s -> "[" + s + "]");
                })
                .addToSelection()
                
                .withRowAndCol(1, 2)
                .forEachCell(c ->{
                    System.out.println(c);
                    c.mapContent(s -> "[" + s + "]");
                }).getFormatterMap();
        System.out.println("Map:"+formatterMap);

        table.renderRows((ri, cells) -> {

            For.elements().iterate(cells, (i, c) -> {
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
