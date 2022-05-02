package ca.vanzyl.provisio.tools.command;

import ca.vanzyl.provisio.tools.generator.ToolDescriptorGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

// TODO: various utilties to list tools, add tools to a profile

@Command(name = "tool", mixinStandardHelpOptions = true)
public class ToolCommand extends ProvisioCommandSupport {

  @Option(names = {"-u", "--url"}, description = "URL to analyze and from which to generate a tool descriptor.")
  String url;

  @Override
  public void execute() throws Exception {
    ToolDescriptorGenerator generator = new ToolDescriptorGenerator();
    generator.analyzeAndGenerate(url);
  }
}
