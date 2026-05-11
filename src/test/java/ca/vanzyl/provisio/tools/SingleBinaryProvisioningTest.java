package ca.vanzyl.provisio.tools;

import static ca.vanzyl.provisio.tools.tool.ToolUrlBuilder.interpolateToolPath;
import static java.util.Objects.requireNonNull;
import static kr.motd.maven.os.Detector.ARCH;
import static kr.motd.maven.os.Detector.OS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;

import ca.vanzyl.provisio.tools.model.ImmutableToolProfile;
import ca.vanzyl.provisio.tools.model.ToolDescriptor;
import ca.vanzyl.provisio.tools.model.ToolProvisioningResult;
import ca.vanzyl.provisio.tools.tool.ToolUrlBuilder;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

// The {os} and {arch} variables are in the form that a given project uses them. We map them from:
//
// uname    -> mapping -> {os}
// uname -m -> mapping -> {arch}

public class SingleBinaryProvisioningTest extends ProvisioTestSupport {

  @Test
  public void provisioningBinaryFromTarGzWithSingleEntryWithLeadingDotSlash() throws Exception {
    // There are some archives published that only contain a single entry that needs to be extracted. The archive can contain
    // binaries for multiple platforms where we are only interested in extracting the binary for the particular platform,
    // or the single binary is just packaged oddly, and we need to know the entry name entry in order to rename it.
    //
    // -rwxr-xr-x  0 runner docker 6581936 Jun 12  2021 ./yq_darwin_amd64
    //
    validateInstallationProvisioning("yq", "4.9.6", "yq_{os}_{arch}.tar.gz");
  }

  // Note that with curl/bash/tar/gzip version it works fine with leading "./" for the single entry
  // or none, whereas with the provisio archiver the exact literal entry is required. The velero
  // single entry was "./velero-v1.6.2-darwin-amd64/velero" in the descriptor and it failed in
  // the Java version until leading "./" was removed.

  @Test
  public void provisioningBinaryFromTarGzWithSingleEntryWithNoLeadingDotSlash() throws Exception {
    //
    // -rw-rw-r--  0 1000   1000    10255 Nov 15  2019 velero-v1.6.2-darwin-amd64/LICENSE
    // -rw-rw-r--  0 1000   1000      476 Jul  8  2021 velero-v1.6.2-darwin-amd64/examples/README.md
    // drwxrwxr-x  0 1000   1000        0 Feb 20  2021 velero-v1.6.2-darwin-amd64/examples/minio
    // -rw-rw-r--  0 1000   1000     2599 Feb 20  2021 velero-v1.6.2-darwin-amd64/examples/minio/00-minio-deployment.yaml
    // drwxrwxr-x  0 1000   1000        0 Aug 14  2020 velero-v1.6.2-darwin-amd64/examples/nginx-app
    // -rw-rw-r--  0 1000   1000      521 Nov 15  2019 velero-v1.6.2-darwin-amd64/examples/nginx-app/README.md
    // -rw-rw-r--  0 1000   1000     1237 Aug 14  2020 velero-v1.6.2-darwin-amd64/examples/nginx-app/base.yaml
    // -rw-rw-r--  0 1000   1000     2375 Aug 14  2020 velero-v1.6.2-darwin-amd64/examples/nginx-app/with-pv.yaml
    // -rwxr-xr-x  0 1000   1000 64937056 Jul 20 10:05 velero-v1.6.2-darwin-amd64/velero
    //
    validateInstallationProvisioning("velero", "1.6.2", "velero-v1.6.2-{os}-{arch}.tar.gz");
  }

  @Test
  public void provisioningBinaryFromTarGz() throws Exception {
    //
    // -rw-r--r--  0 runner docker     1069 Mar  7  2021 LICENSE
    // -rw-r--r--  0 runner docker     9249 Mar  7  2021 README.md
    // -rwxr-xr-x  0 runner docker 12224056 Mar  7  2021 dive
    //
    validateInstallationProvisioning("dive", "0.10.0", "dive_0.10.0_{os}_{arch}.tar.gz");
  }

  @Test
  public void provisioningAwsCliFromOfficialZip() throws Exception {
    assumeThat(OS, is("Linux"));
    validateInstallationProvisioning("aws-cli", "2.34.45", "awscli-exe-linux-{arch}-2.34.45.zip");
  }

  @Test
  public void provisioningBinaryFromTarGzStrip() throws Exception {
    // drwxr-xr-x  0 circleci circleci        0 Jul 14 14:59 darwin-amd64/
    // -rwxr-xr-x  0 circleci circleci 50093232 Jul 14 14:59 darwin-amd64/helm
    // -rw-r--r--  0 circleci circleci    11373 Jul 14 14:59 darwin-amd64/LICENSE
    // -rw-r--r--  0 circleci circleci     3367 Jul 14 14:59 darwin-amd64/README.md
    //
    validateInstallationProvisioning("helm", "3.6.3", "helm-v3.6.3-{os}-{arch}.tar.gz");
  }

  @Test
  public void provisioningBinaryFromZip() throws Exception {
    validateInstallationProvisioning("terraform", "1.3.8", "terraform_1.3.8_{os}_{arch}.zip");
  }

  @Test
  public void provisioningBinaryFromFile() throws Exception {
    validateInstallationProvisioning("argocd", "2.6.1", "argocd-{os}-{arch}");
  }

  @Test
  public void provisioningInstallationUsingZipJunk() throws Exception {
    //
    // jbang
    // └── 0.79.0
    //     ├── jbang
    //     ├── jbang.cmd
    //     ├── jbang.jar
    //     └── jbang.ps1
    //
    validateInstallationProvisioning("jbang", "0.79.0", "jbang.zip",
        withPaths(
            "jbang",
            "jbang.cmd",
            "jbang.jar",
            "jbang.ps1"));
  }

  @Test
  public void provisioningInstallationUsingTarGzStrip() throws Exception {
    List<String> paths = withPaths(
        "release",
        "GRAALVM-README.md",
        "bin/java",
        "jmods/java.base.jmod");
    if(OS.equals("Darwin")) {
      paths = paths.stream().map(p -> "Contents/Home/" + p).collect(Collectors.toList());
    }
    validateInstallationProvisioning("graalvm", "22.3.1", "graalvm-ce-java17-{os}-{arch}-22.3.1.tar.gz", paths);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // Provisioning from an API endpoint where the tool descriptor has -> fileNameFromContentDisposition: true
  // -----------------------------------------------------------------------------------------------------------------------

  @Test
  public void provisioningInstallationUsingTarGzStripFromApiEndpoint() throws Exception {
    List<String> paths = withPaths(
        "release",
        "bin/java",
        "jmods/java.base.jmod");
    if(OS.equals("Darwin")) {
      paths = paths.stream().map(p -> "Contents/Home/" + p).collect(Collectors.toList());
    }
    validateInstallationProvisioning("java", "jdk-17.0.6+10", "OpenJDK17U-jdk_{arch}_{os}_hotspot_17.0.6_10.tar.gz", paths);
  }

  @Test
  public void provisioningBinaryFromGitHubTarGzStrip() throws Exception {
    // For script-based tools like jenv and dimg, fetching a tarball directly from source is often sufficient. Github
    // makes revisions of repositories available as tarballs with the api:
    //
    // https://api.github.com/repos/jvanzyl/dimg/tarball/{version}
    //
    validateInstallationProvisioning("dimg", "1.0.1", "jvanzyl-dimg-1.0.1-0-g5726729.tar.gz");
  }

  @Test
  public void provisioningDBeaver() throws Exception {
    assumeThat(System.getProperty("os.name"), is("Mac OS X"));
    List<String> paths = withPaths(
        "DBeaver.app/Contents/MacOS/dbeaver",
        "DBeaver.app/Contents/Eclipse/dbeaver.ini");
    validateInstallationProvisioning("dbeaver", "23.0.2", "dbeaver-ce-{version}-{os}.{arch}.tar.gz", paths);
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // Problems
  // -----------------------------------------------------------------------------------------------------------------------

  // There is a problem on Linux with the architecture mapping
  @Test
  public void provisioningKrew() throws Exception {
    //
    // -rw-r--r--  0 runner docker  11358 Dec 31  1999 ./LICENSE
    // -rwxr-xr-x  0 runner docker 12177858 Dec 31  1999 ./krew-darwin_arm64
    //
    validateInstallationProvisioning("krew", "0.4.3", "krew-{os}_{arch}.tar.gz");
  }

  // -----------------------------------------------------------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------------------------------------------------------

  public void validateInstallationProvisioning(String toolId, String version, String fileNameInCache) throws Exception {
    validateInstallationProvisioning(toolId, version, fileNameInCache, null);
  }

  protected void validateInstallationProvisioning(String toolId, String version, String fileNameInCache, List<String> paths) throws Exception {
    ToolDescriptor toolDescriptor = provisio.tool(toolId);
    String interpolatedFileNameInCache = interpolateToolPath(fileNameInCache, toolDescriptor, version, ARCH);
    ToolProvisioningResult result = provisio.provisionTool(ImmutableToolProfile.builder().build(), toolId, version);
    Path cachePath;
    if(toolDescriptor.fileNameFromContentDisposition()) {
      cachePath = ToolUrlBuilder.cachePathWithFileNameFor(provisio.cacheDirectory(), provisio.tool(toolId), version, interpolatedFileNameInCache);
    } else {
      cachePath = ToolUrlBuilder.cachePathFor(provisio.cacheDirectory(), provisio.tool(toolId), version, ARCH);
    }
    assertThat(cachePath).exists().isRegularFile().hasFileName(interpolatedFileNameInCache);
    assertThat(result.installation()).exists().isDirectory().hasFileName(version);

    if(paths != null) {
      paths.forEach(path -> assertThat(requireNonNull(result.installation()).resolve(path)).exists());
    } else {
      String executable;
      //
      // yq is layout=file, a single file that is extracted and symlinked to the main provisio path
      // krew is layout=directory and we add the krew installation directory to the path
      //
      if(toolDescriptor.tarSingleFileToExtract() != null) {
        executable = interpolateToolPath(toolDescriptor.tarSingleFileToExtract(), toolDescriptor, version, ARCH);
      } else {
        executable = toolDescriptor.executable();
      }
      assertThat(result.installation().resolve(executable)).exists().isExecutable();
    }
  }

  protected List<String> withPaths(String... paths) {
    return List.of(paths);
  }
}
