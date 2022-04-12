package cn.codethink.sponsor.table;

import cn.chuanwise.common.util.Preconditions;
import lombok.Data;

import java.util.Objects;
import java.util.function.Function;

/**
 * 表格中的列
 *
 * @author Chuanwise
 */
@Data
public class TableColumn<T> {
    
    /**
     * 列名
     */
    String name;
    
    /**
     * 将类型转化为字符串的工具
     */
    Function<T, ?> translator = Objects::toString;
    
    /**
     * 对齐方式
     */
    AlignmentType alignmentType = AlignmentType.LEFT;
    
    public TableColumn() {
        name = "";
    }
    
    public TableColumn(String name) {
        Preconditions.nonNull(name, "name");
        
        this.name = name;
    }
    
    public TableColumn(String name, Function<T, ?> translator) {
        Preconditions.objectNonNull(name, "name");
        Preconditions.objectNonNull(translator, "shower");
        
        this.name = name;
        this.translator = translator;
    }
    
    public TableColumn(String name, Function<T, ?> translator, AlignmentType alignmentType) {
        Preconditions.objectNonNull(name, "name");
        Preconditions.objectNonNull(translator, "translator");
        Preconditions.objectNonNull(alignmentType, "alignmentType");
        
        this.name = name;
        this.translator = translator;
        this.alignmentType = alignmentType;
    }
}
