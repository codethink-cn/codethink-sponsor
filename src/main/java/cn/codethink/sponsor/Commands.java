package cn.codethink.sponsor;

import cn.chuanwise.command.annotation.Command;
import cn.chuanwise.command.annotation.EventHandler;
import cn.chuanwise.command.annotation.Parser;
import cn.chuanwise.command.annotation.Reference;
import cn.chuanwise.command.completer.Completer;
import cn.chuanwise.command.context.CompleteContext;
import cn.chuanwise.command.context.ParseContext;
import cn.chuanwise.command.event.*;
import cn.chuanwise.command.handler.AbstractClassHandler;
import cn.chuanwise.command.tree.CommandTreeFork;
import cn.chuanwise.command.tree.CommandTreeNode;
import cn.chuanwise.common.space.Container;
import cn.chuanwise.common.text.Alignment;
import cn.chuanwise.common.text.TableColumn;
import cn.chuanwise.common.text.TableView;
import cn.chuanwise.common.util.Collections;
import cn.chuanwise.common.util.Indexes;
import cn.chuanwise.common.util.Numbers;
import cn.chuanwise.common.util.Strings;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 赞助系统指令
 *
 * @author Chuanwise
 */
public class Commands {
    
    boolean debug;
    
    private static final TableView<Operator> OPERATOR_TABLE_VIEW = new TableView<>();
    
    private static final TableView<Transaction> TRANSACTION_TABLE_VIEW = new TableView<>();
    
    private static final TableView<Transaction> CASH_FLOW_TABLE_VIEW = new TableView<>();
    
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    private static final TableView<cn.chuanwise.command.command.Command> COMMAND_TABLE_VIEW = new TableView<>();
    
    static {
        OPERATOR_TABLE_VIEW.getColumns().addAll(Arrays.asList(
            new TableColumn<>("code", Operator::getOperatorCode),
            new TableColumn<>("name", Operator::getName),
            new TableColumn<>("url", Operator::getUrl)
        ));
    
        TRANSACTION_TABLE_VIEW.getColumns().addAll(Arrays.asList(
            new TableColumn<>("code", x -> "`" + x.getTransactionCode() + "`", Alignment.RIGHT),
            new TableColumn<>("time", x -> DATE_FORMAT.format(x.getTimeStamp())),
            new TableColumn<>("流水", x -> (x.getType() == Transaction.Type.INCOME ? "+" : "-") + x.getValue()),
            new TableColumn<>("description", Transaction::getDescription),
            new TableColumn<>("operator", x -> {
                final int operatorCode = x.getOperatorCode();
                if (operatorCode == -1) {
                    return "`Anonymous`";
                }
    
                final List<Operator> operators = SponsorSystem.getInstance().getOperators();
                if (Indexes.isLegal(operatorCode, operators.size())) {
                    final Operator operator = operators.get(operatorCode);
                    return "#" + operatorCode + " : " + operator.getName();
                } else {
                    return "#" + operatorCode;
                }
            })
        ));
    
        CASH_FLOW_TABLE_VIEW.getColumns().addAll(Arrays.asList(
            new TableColumn<>("流水号", Transaction::getTransactionCode, Alignment.RIGHT),
            new TableColumn<>("时间", x -> DATE_FORMAT.format(x.getTimeStamp())),
            new TableColumn<>("流水", x -> (x.getType() == Transaction.Type.INCOME ? "+" : "-") + x.getValue(), Alignment.CENTER),
            new TableColumn<>("类型", x -> x.getType() == Transaction.Type.INCOME ? "收入" : "支出"),
            new TableColumn<>("描述", x -> {
                final String description = x.getDescription();
                if (Strings.isEmpty(description) && x.getType() == Transaction.Type.INCOME) {
                    return "赞助";
                } else {
                    return description;
                }
            }),
            new TableColumn<>("相关方", x -> {
                final int operatorCode = x.getOperatorCode();
                if (operatorCode == -1) {
                    return "匿名";
                }
    
                final List<Operator> operators = SponsorSystem.getInstance().getOperators();
                if (Indexes.isLegal(operatorCode, operators.size())) {
                    final Operator operator = operators.get(operatorCode);
    
                    if (Strings.isEmpty(operator.getUrl())) {
                        return "#" + operatorCode + " : " + operator.getName();
                    } else {
                        return "#" + operatorCode + " : [" + operator.getName() + "](" + operator.getUrl() + ")";
                    }
                } else {
                    return "#" + operatorCode;
                }
            })
        ));
    
        COMMAND_TABLE_VIEW.getColumns().addAll(Arrays.asList(
            new TableColumn<>("name", cn.chuanwise.command.command.Command::getName),
            new TableColumn<>("format", cn.chuanwise.command.command.Command::getFormat)
        ));
    }
    
    @Command("operator list")
    void listOperators() {
        final List<Operator> operators = SponsorSystem.getInstance().getOperators();
    
        if (operators.isEmpty()) {
            System.out.println("no any operator");
        } else {
            final String string = OPERATOR_TABLE_VIEW.display(operators);
            System.out.println(string);
        }
    }
    
    @Command("operator add [name] [url?~]")
    void addOperator(@Reference("name") String name,
                     @Reference("url") String url) {
        final List<Operator> operators = SponsorSystem.getInstance().getOperators();
    
        final Operator sameNameOperator = Collections.firstIf(operators, x -> Objects.equals(name, x.getName()));
        if (Objects.nonNull(sameNameOperator)) {
            System.out.println("same name operator #" + sameNameOperator.getOperatorCode() + " already existed!");
            System.out.print("would you like to add a new one? (y/n) ");
    
            final Scanner scanner = new Scanner(System.in);
            final String choice = scanner.next();
            System.out.println();
            if (!Objects.equals("y", choice)) {
                System.out.println("operation cancelled");
                return;
            }
        }
    
        final int operatorCode = operators.size();
        final Operator operator = new Operator(operatorCode, name, Strings.isEmpty(url) ? null : url);
        operators.add(operator);
    
        System.out.println("operator #" + operatorCode + " added, called " + operator.getName());
    }
    
    @Command("operator lookup code [code]")
    void lookupByCode(@Reference(value = "code",
        completer = OperatorCodeHandler.class,
        parser = OperatorCodeHandler.class) Operator operator) {
        lookupByName(operator);
    }
    
    @Command("operator lookup name [name]")
    void lookupByName(@Reference(value = "name",
        completer = OperatorNameHandler.class,
        parser = OperatorNameHandler.class) Operator operator) {
    
        System.out.println("operator #" + operator.getOperatorCode() + " info: ");
        System.out.println(" -> name: " + operator.getName());
        System.out.println(" -> url: " + operator.getUrl());
    }
    
    @Command("transaction list")
    void listTransactions() {
        final List<Transaction> transactions = SponsorSystem.getInstance().getTransactions();
    
        if (transactions.isEmpty()) {
            System.out.println("no any transaction");
        } else {
            System.out.println(TRANSACTION_TABLE_VIEW.display(transactions));
            balance();
        }
    }
    
    @Command("transaction add income [value] [-date|d?] [-name?] [-description|d?]")
    void addIncomeTransaction(@Reference("value") String valueString,
                              @Reference("date") String date,
                              @Reference(value = "name", completer = OperatorNameHandler.class) String name,
                              @Reference("description") String description) throws ParseException, IOException {
        // parse value
        final double value = Double.parseDouble(valueString);
    
        // parse time stamp
        final long timeStamp;
        if (Strings.isEmpty(date)) {
            timeStamp = System.currentTimeMillis();
        } else {
            timeStamp = DATE_FORMAT.parse(date).getTime();
        }
        
        // parse name
        final int operatorCode;
        final SponsorSystem sponsorSystem = SponsorSystem.getInstance();
        if (Strings.isEmpty(name)) {
            operatorCode = -1;
        } else {
            final List<Operator> sameNameOperators = sponsorSystem
                .getOperators()
                .stream()
                .filter(x -> Objects.equals(x.getName(), name))
                .collect(Collectors.toList());
    
            if (sameNameOperators.isEmpty()) {
                System.out.println("no such operator: " + name);
                return;
            }
            if (sameNameOperators.size() != 1) {
                System.out.println("multiple operators");
                System.out.println(OPERATOR_TABLE_VIEW.display(sameNameOperators));
                return;
            }
            operatorCode = sameNameOperators.get(0).getOperatorCode();
        }
        
        final Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setType(Transaction.Type.INCOME);
        transaction.setValue(value);
        transaction.setTimeStamp(timeStamp);
        transaction.setOperatorCode(operatorCode);
    
        final List<Transaction> transactions = sponsorSystem.getTransactions();
        transaction.setTransactionCode(transactions.size() + 1);
        displayTransactionInfo(transaction);
        System.out.print("would you like to add it? (y/n) ");
    
        final Scanner scanner = new Scanner(System.in);
        final String choice = scanner.next();
        System.out.println();
        if (!Objects.equals(choice, "y")) {
            System.out.println("operation cancelled");
            return;
        }
        
        transactions.add(transaction);
        sponsorSystem.save();
        System.out.println("transaction added");
    }
    
    @Command("transaction add outcome [value] [-date|d?] [-name] [-description|d?]")
    void addOutcomeTransaction(@Reference("value") String valueString,
                               @Reference("date") String date,
                               @Reference("name") Operator operator,
                               @Reference("description") String description) throws ParseException, IOException {
        // parse value
        final double value = Double.parseDouble(valueString);
    
        // parse time stamp
        final long timeStamp;
        if (Strings.isEmpty(date)) {
            timeStamp = System.currentTimeMillis();
        } else {
            timeStamp = DATE_FORMAT.parse(date).getTime();
        }
        
        // parse name
        final int operatorCode = operator.getOperatorCode();
        final SponsorSystem sponsorSystem = SponsorSystem.getInstance();
        
        final Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setType(Transaction.Type.OUTCOME);
        transaction.setValue(value);
        transaction.setTimeStamp(timeStamp);
        transaction.setOperatorCode(operatorCode);
    
        final List<Transaction> transactions = sponsorSystem.getTransactions();
        transaction.setTransactionCode(transactions.size() + 1);
        displayTransactionInfo(transaction);
        System.out.print("would you like to add it? (y/n) ");
    
        final Scanner scanner = new Scanner(System.in);
        final String choice = scanner.next();
        System.out.println();
        if (!Objects.equals(choice, "y")) {
            System.out.println("operation cancelled");
            return;
        }
        
        transactions.add(transaction);
        sponsorSystem.save();
        System.out.println("transaction added");
    }
    
    @Command("transaction lookup [code]")
    void displayTransactionInfo(@Reference("code") Transaction transaction) {
    
        System.out.println("transaction #" + transaction.getTransactionCode());
        System.out.println(" -> time: " + DATE_FORMAT.format(transaction.getTimeStamp()));
        System.out.println(" -> type: " + transaction.getType());
        System.out.println(" -> operator: " + transaction.getOperatorCode());
        System.out.println(" -> description: " + transaction.getDescription());
    }
    
    @Command("reload")
    void reload() throws IOException {
        SponsorSystem.getInstance().reload();
        System.out.println("reloaded");
    }
    
    @Command("balance")
    void balance() {
        final List<Transaction> transactions = SponsorSystem.getInstance().getTransactions();
    
        double income = 0;
        double outcome = 0;
        for (Transaction transaction : transactions) {
            switch (transaction.getType()) {
                case INCOME:
                    income += transaction.getValue();
                    break;
                case OUTCOME:
                    outcome += transaction.getValue();
                    break;
                default:
                    throw new NoSuchElementException();
            }
        }
    
        System.out.println("balance: RMB " + (income - outcome));
        System.out.println(" -> income: RMB " + income);
        System.out.println(" -> outcome: RMB " + outcome);
    }
    
    @Command("render preview")
    void previewRender() {
        System.out.println(CASH_FLOW_TABLE_VIEW.display(SponsorSystem.getInstance().getTransactions()));
    }
    
    @Command("render save [-charset|c?UTF-8]")
    void renderMarkdownFile(@Reference("charset") Charset charset) throws IOException {
        final File markdownFile = new File("cash-flow.md");
        if (!markdownFile.isFile()) {
            markdownFile.createNewFile();
        }
        
        try (PrintWriter printWriter = new PrintWriter(markdownFile, charset.name())) {
            
            printWriter.println("**codethink-sponsor -> cash-flow**");
            printWriter.println("# 赞助金额流向公示");
            printWriter.println();
    
            printWriter.println("## 声明");
            printWriter.println();
            
            printWriter.println("更新日期：`" + DATE_FORMAT.format(System.currentTimeMillis()) + "`");
            printWriter.println();
            
            printWriter.println("* 所有支出均需实名，收入可以匿名");
            printWriter.println("* **本流水最终解释权归 `CodeThink` 所有**");
            printWriter.println();
    
            int totalOutcome = 0;
            int totalIncome = 0;
    
            final List<Transaction> transactions = SponsorSystem.getInstance().getTransactions();
            for (Transaction transaction : transactions) {
                switch (transaction.getType()) {
                    case INCOME:
                        totalIncome += transaction.getValue();
                        break;
                    case OUTCOME:
                        totalOutcome += transaction.getValue();
                        break;
                    default:
                        throw new NoSuchElementException();
                }
            }
            final int balance = totalIncome - totalOutcome;
    
            printWriter.println("## 流水");
            printWriter.println();
            
            printWriter.println("* 总收入：RMB `" + totalIncome + "`");
            printWriter.println("* 总支出：RMB `" + totalOutcome + "`");
            printWriter.println("* 余额：RMB `" + balance + "`");
            printWriter.println();
    
            printWriter.println(CASH_FLOW_TABLE_VIEW.display(transactions));
            
            printWriter.flush();
        }
    
        System.out.println(".md file rendered and saved");
    }
    
    @Command("debug")
    void debug() {
        debug = !debug;
        
        if (debug) {
            System.out.println("debug enabled");
        } else {
            System.out.println("debug disabled");
        }
    }
    
    @EventHandler
    void handleMismatchedFormat(MismatchedFormatEvent event) {
        final List<cn.chuanwise.command.command.Command> commands = event.getCommandTreeForks()
            .stream()
            .map(CommandTreeFork::getCommandTreeNode)
            .map(CommandTreeNode::getRelatedCommands)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    
        System.out.println("wrong command, likely formats: ");
        System.out.println(COMMAND_TABLE_VIEW.display(commands));
    }
    
    @EventHandler
    void handleMultipleCommandsMatched(MultipleCommandsMatchedEvent event) {
        final List<cn.chuanwise.command.command.Command> commands = event.getCommandTreeForks()
            .stream()
            .map(CommandTreeFork::getCommandTreeNode)
            .map(CommandTreeNode::getRelatedCommands)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    
        System.out.println("multiple formats matched: ");
        System.out.println(COMMAND_TABLE_VIEW.display(commands));
    }
    
    @EventHandler
    void handleUndefinedOption(UndefinedOptionValueEvent event) {
        System.out.println("undefined option value: " + event.getString() + " for optional values: " + event.getOptionInfo().getOptionalValues());
    }
    
    @EventHandler
    void handleLackRequiredOption(LackRequiredOptionEvent event) {
        System.out.println("lack required option: " + event.getOptionInfo().getName());
    }
    
    @EventHandler
    void handleReassignOption(ReassignOptionEvent event) {
        System.out.println("reassign option: " + event.getOptionInfo().getName());
    }
    
    @EventHandler
    void handleEvent(Object event) {
        if (debug) {
            System.out.println(" >>> debug: " + event);
        }
    }
    
    @cn.chuanwise.command.annotation.Completer(Charset.class)
    Set<String> completeCharsetName(CompleteContext context) {
        return Charset.availableCharsets().keySet();
    }
    
    @Parser
    Charset parseCharset(ParseContext context) {
        return Charset.forName(context.getParsingReferenceInfo().getString());
    }
    
    @cn.chuanwise.command.annotation.Completer(Operator.class)
    Set<String> completeOperatorName(CompleteContext context) throws Exception {
        return OperatorNameHandler.INSTANCE.complete(context);
    }
    
    @Parser
    @SuppressWarnings("all")
    Container<Operator> parseOperatorByName(ParseContext context) throws Exception {
        return (Container<Operator>) OperatorNameHandler.INSTANCE.parse(context);
    }
    
    public static class OperatorCodeHandler
        extends AbstractClassHandler<Operator>
        implements Completer {
        
        public static final OperatorCodeHandler INSTANCE = new OperatorCodeHandler();
        
        @Override
        protected Set<String> complete0(CompleteContext completeContext) throws Exception {
            final List<Operator> operators = SponsorSystem.getInstance().getOperators();
    
            return operators.stream()
                .map(Operator::getOperatorCode)
                .map(Objects::toString)
                .collect(Collectors.toSet());
        }
    
        @Override
        protected Container<Operator> parse0(ParseContext parseContext) throws Exception {
            final List<Operator> operators = SponsorSystem.getInstance().getOperators();
            final String string = parseContext.getParsingReferenceInfo().getString();
            final Integer integer = Numbers.parseInt(string);
        
            if (Objects.nonNull(integer) && Indexes.isLegal(integer, operators.size())) {
                return Container.ofNonNull(operators.get(integer));
            } else {
                System.out.println("no such operator: " + string + "!");
                return Container.empty();
            }
        }
    }
    
    public static class OperatorNameHandler
        extends AbstractClassHandler<Operator>
        implements Completer {
        
        public static final OperatorNameHandler INSTANCE = new OperatorNameHandler();
    
        @Override
        protected Set<String> complete0(CompleteContext completeContext) throws Exception {
            final List<Operator> operators = SponsorSystem.getInstance().getOperators();
        
            return operators.stream()
                .map(Operator::getName)
                .collect(Collectors.toSet());
        }
    
        @Override
        protected Container<Operator> parse0(ParseContext parseContext) throws Exception {
            final List<Operator> operators = SponsorSystem.getInstance().getOperators();
            final String string = parseContext.getParsingReferenceInfo().getString();
        
            final List<Operator> sameNameOperators = operators.stream()
                .filter(x -> Objects.equals(x.getName(), string))
                .collect(Collectors.toList());
        
            if (sameNameOperators.isEmpty()) {
                System.out.println("no such operator: " + string + "!");
                return null;
            }
            if (sameNameOperators.size() != 1) {
                System.out.println("multiple operators, please use operator code to cleaner");
                System.out.println(OPERATOR_TABLE_VIEW.display(sameNameOperators));
                return null;
            }
        
            return Container.ofNonNull(sameNameOperators.get(0));
        }
    }
}