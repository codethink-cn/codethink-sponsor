package cn.codethink.sponsor.table;

import cn.chuanwise.common.util.Preconditions;
import cn.chuanwise.common.util.Strings;

/**
 * 列表对齐方式
 *
 * @author Chuanwise
 */
public enum AlignmentType {
    
    /**
     * 左对齐
     */
    LEFT {
        @Override
        public String apply(String string, int width) {
            Preconditions.argument(string.length() <= width);
    
            final int distance = width - string.length();
            return string + Strings.repeat(' ', distance);
        }
    },
    
    /**
     * 右对齐
     */
    RIGHT {
        @Override
        public String apply(String string, int width) {
            Preconditions.argument(string.length() <= width);
    
            final int distance = width - string.length();
            return Strings.repeat(' ', distance) + string;
        }
    },
    
    /**
     * 中心对齐
     */
    CENTER {
        @Override
        public String apply(String string, int width) {
            Preconditions.argument(string.length() <= width);
    
            final int distance = width - string.length();
            final String spaces = Strings.repeat(' ', distance / 2);
            return spaces + string + spaces;
        }
    };
    
    /**
     * 将这种对齐方式应用在指定的输入上
     *
     * @param string 输入内容
     * @param width 最大宽度
     * @return 应用后的内容
     */
    public abstract String apply(String string, int width);
}
