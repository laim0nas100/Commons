/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empiric.core.tables.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lt.lb.commons.containers.tables.CellFormatBuilder;
import lt.lb.commons.containers.tables.CellTable;
import lt.lb.commons.F;
import lt.lb.commons.containers.tables.Formatters;
import lt.lb.commons.func.Lambda;
import lt.lb.commons.iteration.For;
import lt.lb.fastid.FastID;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author laim0nas100
 */
public class DocTableTest {

    @Test
    public void test() throws Exception {
        CellTable<String, String> table = new CellTable<>();

        table.addRow("1", "2", "3");
        table.addRow("A", "B", "C");
        table.mergeHorizontal(0, 2, 1); // not implemented, depends on the renderer

        Formatters<String> formatterMap = table.selectCells()
                .withRectangleStartingAt(0, 1)
                .toRightBottomCornerAt(1, 1)
                .forEachCell(c -> {
                    c.mapContent(s -> "[" + s + "]");
                })
                .addFormat(Lambda.L1.empty())
                .addToSelection()
                .withColumns(0, 1)
                .forEachCell(c -> {
                    c.mapContent(s -> "[" + s + "]");
                })
                .addToSelection()
                .withRowAndCol(1, 2)
                .forEachCell(c -> {
                    c.mapContent(s -> "[" + s + "]");
                }).getFormatterMap();

        List<List<String>> expected = new ArrayList<>();
        expected.add(Arrays.asList("[[1]]", "[[[2]]]", "3"));
        expected.add(Arrays.asList("[[A]]", "[[[B]]]", "[C]"));

        List<List<String>> result = new ArrayList<>();
        table.renderRows(formatterMap, (from, ri, cells) -> {
            result.add(cells.stream().map(m -> m.getContent().orElse("")).collect(Collectors.toList()));
        });

        assertThat(expected).isEqualTo(result);

    }

}
