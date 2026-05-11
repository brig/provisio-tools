package ca.vanzyl.provisio.tools.command;

import static ca.vanzyl.provisio.tools.Provisio.*;

import io.quarkus.picocli.runtime.PicocliCommandLineFactory;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import jakarta.enterprise.inject.Produces;
import picocli.CommandLine;
import picocli.CommandLine.IVersionProvider;

@TopCommand
@CommandLine.Command(
    name = "provisio", mixinStandardHelpOptions = true,
    versionProvider = PropertiesVersionProvider.class,
    subcommands = {
        ProvisionCommand.class,
        InstallCommand.class,
        SelfUpdateCommand.class,
        ToolCommand.class,
        ProfileCommand.class
    })
public class Main {

  @Produces
  CommandLine getCommandLineInstance(PicocliCommandLineFactory factory) {
    return factory.create();
  }
}

class PropertiesVersionProvider implements IVersionProvider {
  public String[] getVersion() throws Exception {
    return new String[]{versionInfo()};
  }
}
