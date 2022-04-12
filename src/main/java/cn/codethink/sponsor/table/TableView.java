package cn.codethink.sponsor.table;

import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Strings;
import lombok.Data;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * 元素列表
 *
 * @author Chuanwise
 */
@Data
public class TableView<T> {
    
    /**
     * 列表中的列
     */
    final List<TableColumn<T>> columns = new ArrayList<>();
    
    /**
     * 将列表内容显示到 PrintWriter 上
     *
     * @param printWriter PrintWriter
     * @param elements 元素
     */
    public void display(PrintWriter printWriter, Collection<T> elements) {
        Preconditions.objectNonNull(printWriter, "print stream");
        Preconditions.objectNonNull(elements, "elements");
    
        // no column to display
        if (columns.isEmpty()) {
            return;
        }
        
        // calculate column width
        int[] widths = new int[columns.size()];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = columns.get(i).getName().length();
        }
        
        // prepare strings
        final String[][] lines = new String[elements.size()][columns.size()];
        for (int i = 0; i < elements.size(); i++) {
            lines[i] = new String[columns.size()];
        }
    
        int elementIndex = 0;
        for (T element : elements) {
            for (int j = 0; j < columns.size(); j++) {
                final TableColumn<T> column = columns.get(j);
                final String string = Objects.toString(column.translator.apply(element));
        
                widths[j] = Math.max(widths[j], string.length());
                lines[elementIndex][j] = string;
            }
            
            elementIndex++;
        }
        
        // display
        // column name
        printWriter.print("|");
        for (int i = 0; i < columns.size(); i++) {
            final TableColumn<T> column = columns.get(i);
            printWriter.print(" " + column.alignmentType.apply(column.getName(), widths[i]) + " |");
        }
        printWriter.println();
    
        // line
        printWriter.print("|");
        for (int i = 0; i < columns.size(); i++) {
            final TableColumn<T> column = columns.get(i);
            final String delimiter;
            switch (column.alignmentType) {
                case LEFT:
                    delimiter = ":" + Strings.repeat('-', widths[i]) + "-";
                    break;
                case RIGHT:
                    delimiter = "-" + Strings.repeat('-', widths[i]) + ":";
                    break;
                case CENTER:
                    delimiter = ":" + Strings.repeat('-', widths[i]) + ":";
                    break;
                default:
                    throw new NoSuchElementException();
            }
            printWriter.print(delimiter + "|");
        }
        printWriter.println();
    
        for (String[] columns : lines) {
            printWriter.print("|");
    
            for (int i = 0; i < columns.length; i++) {
                final String string = columns[i];
                final TableColumn<T> column = this.columns.get(i);
                printWriter.print(" " + column.alignmentType.apply(string, widths[i]) + " |");
            }
            printWriter.println();
        }
    }
    
    /**
     * 将列表内容按照字符串显示
     *
     * @param elements 元素
     * @return 显示内容
     */
    public String displayAsString(Collection<T> elements) {
        Preconditions.objectNonNull(elements, "elements");
        
        final StringWriter stringWriter = new StringWriter();
        display(new PrintWriter(stringWriter), elements);
        return stringWriter.toString();
    }
}
