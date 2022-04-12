package cn.codethink.sponsor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 赞助系统
 *
 * @author Chuanwise
 */
@Data
public class SponsorSystem {
    
    private static final SponsorSystem INSTANCE = new SponsorSystem();
    
    public static SponsorSystem getInstance() {
        return INSTANCE;
    }
    
    /**
     * 相关方，编号就是表中的索引
     */
    final List<Operator> operators = new ArrayList<>();
    
    /**
     * 相关方文件
     */
    final File operatorsFile = new File("operators.yml");
    
    /**
     * 交易记录
     */
    final List<Transaction> transactions = new ArrayList<>();
    
    /**
     * 交易记录文件
     */
    final File transactionsFile = new File("transactions.yml");
    
    private SponsorSystem() {
    }
    
    public void reload() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        
        operators.clear();
        if (operatorsFile.isFile()) {
            operators.addAll(objectMapper.readValue(operatorsFile, new TypeReference<List<Operator>>() {}));
        }
        
        transactions.clear();
        if (transactionsFile.isFile()) {
            transactions.addAll(objectMapper.readValue(transactionsFile, new TypeReference<List<Transaction>>() {}));
        }
    }
    
    public void save() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    
        objectMapper.writeValue(operatorsFile, operators);
        objectMapper.writeValue(transactionsFile, transactions);
    }
}