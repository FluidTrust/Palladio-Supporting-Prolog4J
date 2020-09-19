package org.prolog4j.swicli.enabler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.prolog4j.swicli.SWIPrologExecutable;
import org.prolog4j.swicli.SWIPrologExecutableProvider;

@Component(scope = ServiceScope.SINGLETON, property = SWIPrologExecutableProvider.PRIORITY_PROPERTY + " = 998")
public class SWIPrologEmbeddedFallbackExecutableProvider implements SWIPrologExecutableProvider {

    private static final String DEFAULT_PATH = "swipl";
    private final Optional<SWIPrologExecutable> executable;

    public SWIPrologEmbeddedFallbackExecutableProvider() {
        executable = createExecutable();
    }

    @Override
    public Optional<SWIPrologExecutable> getExecutable() {
        return executable;
    }

    protected Optional<SWIPrologExecutable> createExecutable() {
        // we do not support other combinations yet
        if (!SystemUtils.OS_ARCH.contains("64") || !(SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_LINUX)) {
            return Optional.empty();
        }

        if (isSwiplInstalled()) {
            return Optional.empty();
        }

        var resourcePath = getFileName();
        var directory = extractArchive(resourcePath);
        if (directory.isEmpty()) {
            return Optional.empty();
        }
        File swiDir = directory.get();

        if (SystemUtils.IS_OS_WINDOWS) {
            return Optional.of(new SimpleSWIPrologExecutable(new File(swiDir, "bin/swipl"), swiDir));
        }
        if (SystemUtils.IS_OS_LINUX) {
            return Optional.of(new SimpleSWIPrologExecutable(new File(swiDir, "bin/x86_64-linux/swipl"), swiDir,
                    new File(swiDir, "lib/x86_64-linux")));
        }

        // we are out of luck
        return Optional.empty();
    }

    protected boolean isSwiplInstalled() {
        try {
            if (Runtime.getRuntime()
                .exec(DEFAULT_PATH + " --version")
                .exitValue() == 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    protected Optional<File> extractArchive(String resourcePath) {
        var cl = SWIPrologEmbeddedFallbackExecutableProvider.class.getClassLoader();
        if (cl.getResource(resourcePath) == null) {
            return Optional.empty();
        }
        try (InputStream archiveStream = cl.getResourceAsStream(resourcePath)) {
            try (GzipCompressorInputStream gzipIs = new GzipCompressorInputStream(archiveStream)) {
                try (TarArchiveInputStream tarIs = new TarArchiveInputStream(gzipIs)) {
                    File destinationDirectory = Files
                        .createTempDirectory(SWIPrologEmbeddedFallbackExecutableProvider.class.getSimpleName())
                        .toFile();
                    FileUtils.forceDeleteOnExit(destinationDirectory);
                    for (var tarEntry = tarIs.getNextTarEntry(); tarEntry != null; tarEntry = tarIs.getNextTarEntry()) {
                        var dst = new File(destinationDirectory, tarEntry.getName());
                        if (tarEntry.isDirectory()) {
                            dst.mkdirs();
                        } else {
                            try (FileOutputStream fos = new FileOutputStream(dst)) {
                                IOUtils.copyLarge(tarIs, fos, 0, tarEntry.getSize());
                            }
                            if (isExecutable(tarEntry.getMode())) {
                                dst.setExecutable(true);
                            }
                        }
                    }
                    return Optional.of(destinationDirectory);
                }
            }
        } catch (IOException e) {
            // error, return fallback value below
        }
        return Optional.empty();
    }

    protected boolean isExecutable(int fileMode) {
        // rwx rwx rwx
        // 001 001 001 => 73
        return (fileMode & 73) != 0;
    }

    protected String getFileName() {
        String os = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            os = "win";
        } else if (SystemUtils.IS_OS_LINUX) {
            os = "linux";
        }
        String version = "8.2.1";
        String variant = "regular";
        if (SystemUtils.IS_OS_LINUX) {
            variant = String.format("ncurses%d", getLibncursesVersion());
        }
        String architecture = "x64";
        return String.format("swipl-%s-%s-%s-%s.tar.gz", version, os, variant, architecture);
    }

    protected int getLibncursesVersion() {
        var pb = new ProcessBuilder(Arrays.asList("ldconfig", "-p"));
        pb.redirectErrorStream(true);
        String processOutput = null;

        try {
            var process = pb.start();
            try (var s = new Scanner(process.getInputStream()).useDelimiter("\\A")) {
                processOutput = s.hasNext() ? s.next() : "";
            }
            process.waitFor();
            String versionPrefix = "libncurses.so.";
            int index = processOutput.indexOf(versionPrefix);
            if (index > 0) {
                return Integer.parseInt("" + processOutput.charAt(index + versionPrefix.length()));
            }
        } catch (IOException | InterruptedException | NumberFormatException e) {
            // error, just return fallback value
        }

        return 0;
    }

}
