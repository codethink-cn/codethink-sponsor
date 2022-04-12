package cn.codethink.sponsor;

import lombok.Data;

/**
 * 一笔流水线的交易
 *
 * @author Chuanwise
 */
@Data
public class Transaction {
    
    /**
     * 交易类别
     */
    public enum Type {
    
        /**
         * 收入
         */
        INCOME,
    
        /**
         * 支出
         */
        OUTCOME,
    }
    
    /**
     * 交易类别
     */
    Type type;
    
    /**
     * 金额量
     */
    double value;
    
    /**
     * 交易编号
     */
    int transactionCode;
    
    /**
     * 交易时间戳
     */
    long timeStamp = System.currentTimeMillis();
    
    /**
     * 描述
     */
    String description;
    
    /**
     * 相关人员
     */
    int operatorCode;
}
