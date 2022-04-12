package cn.codethink.sponsor;

import cn.chuanwise.command.Commander;
import cn.chuanwise.command.completer.JLineCommanderCompleter;
import cn.chuanwise.command.context.DispatchContext;
import cn.chuanwise.common.util.Strings;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

/**
 * 赞助系统主类
 *
 * @author Chuanwise
 */
public class SponsorSystemLauncher {
    
    public static void main(String[] args) {
        final SponsorSystem sponsorSystem = SponsorSystem.getInstance();
    
        System.out.println("loading files...");
        try {
            sponsorSystem.reload();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    
        // build commander
        final Commander commander = new Commander()
            .register(new Commands());
    
        // prepare terminal
        final Terminal terminal;
        try {
            System.out.println("preparing terminal...");
            terminal = TerminalBuilder.builder()
                .system(true)
                .build();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    
        // prepare line reader
        final LineReader lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .completer(new JLineCommanderCompleter(commander))
            .build();
    
        // prompt loop
        while (true) {
            final String line;
            try {
                line = lineReader.readLine("> ");
            } catch (UserInterruptException | EndOfFileException e) {
                System.out.println("stopping...");
                break;
            }
    
            if (Strings.isEmpty(line)) {
                continue;
            }
    
            commander.execute(new DispatchContext(commander, line));
        }
    
        System.out.println("saving files...");
        try {
            sponsorSystem.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
