package cn.codethink.sponsor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 表示一个相关方
 *
 * @author Chuanwise
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operator {
    
    /**
     * 相关方编号
     */
    int operatorCode;
    
    /**
     * 相关方名
     */
    String name;
    
    /**
     * 相关方 URL，可以为 null
     */
    String url;
}
