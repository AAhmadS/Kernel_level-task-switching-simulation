package util.process;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

public class Program {

    private final StringJoiner command;

    public Program(List<String> commonArgs) {
        command = new StringJoiner(" ");
        commonArgs.forEach(command::add);
    }

    public Program addArgument(String arg) {
        command.add(arg);
        return this;
    }

    public Process run(Class<?> clazz) throws IOException {
        command.add(clazz.getName());
        return Runtime.getRuntime().exec(command.toString());
    }
}
