package cn.codethink.sponsor.table.test;

import cn.chuanwise.command.annotation.Command;
import cn.codethink.sponsor.Operator;
import cn.codethink.sponsor.SponsorSystem;
import cn.codethink.sponsor.table.TableColumn;
import cn.codethink.sponsor.table.TableView;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.List;

public class TableTest {
    
    public static void main(String[] args) {
        final TableView<Operator> operatorTableView = new TableView<>();
    
        operatorTableView.getColumns().add(new TableColumn<>("code", Operator::getOperatorCode));
        operatorTableView.getColumns().add(new TableColumn<>("name", Operator::getName));
        operatorTableView.getColumns().add(new TableColumn<>("url", Operator::getUrl));
    
        final List<Operator> operators = SponsorSystem.getInstance().getOperators();
        operators.add(new Operator(0, "Chuanwise", null));
        operators.add(new Operator(1, "ThymeChen", null));
        operators.add(new Operator(2, "Copper", null));
    
        if (operators.isEmpty()) {
            System.out.println("no any operator");
        } else {
            final PrintWriter printWriter = new PrintWriter(System.out);
            operatorTableView.display(printWriter, operators);
            printWriter.flush();
        }
    }
}