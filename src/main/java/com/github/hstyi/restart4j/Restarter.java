package com.github.hstyi.restart4j;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class Restarter {

    private static final String os = System.getProperty("os.name").toLowerCase();
    private static final String arch = System.getProperty("os.arch").toLowerCase();
    private static final boolean isWin = os.contains("win");
    private static final boolean isMac = os.contains("mac");
    private static final boolean isUnix = os.contains("nix") || os.contains("nux");
    private static final boolean isArm64 = arch.contains("aarch64");

    private static volatile Supplier<Integer> currentProcessHandler;
    private static volatile Supplier<Path> tempDirectoryHandler;
    private static volatile ExecCommandsHandler execCommandsHandler;

    static {
        setProcessHandler(() -> Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]));
        setTempDirectoryHandler(() -> {
            try {
                return Files.createTempDirectory("restarter-");
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
        setExecCommandsHandler(new ExecCommandsHandler() {
            private final File nul = new File(isWin ? "NUL" : "/dev/null");

            @Override
            public void exec(List<String> commands) throws IOException {
                final ProcessBuilder pb = new ProcessBuilder(commands);
                pb.redirectOutput(nul);
                pb.redirectError(nul);
                pb.directory(Paths.get(System.getProperty("user.home")).toFile());
                pb.start();
            }
        });
    }


    /**
     * @param commands The command to be executed after the current process is closed
     */
    public static void restart(String[] commands) throws IOException {
        final Path path = getRestarterPath();
        final List<String> cmds = new ArrayList<>();
        cmds.add(path.toAbsolutePath().toString());
        cmds.add(currentProcessHandler.get().toString());
        cmds.add(String.valueOf(commands.length));
        cmds.addAll(Arrays.asList(commands));

        execCommandsHandler.exec(cmds);

    }

    private static String getFilename() {
        return isWin ? "restarter.exe" : "restarter";
    }

    private static Path getRestarterPath() throws IOException {
        final String restarterPath = System.getProperty("restarter.path");
        if (restarterPath != null) {
            final Path path = Paths.get(restarterPath);
            if (Files.exists(path)) {
                return path;
            }
        }

        final String resourceName = getResourceName();
        final InputStream resource = Restarter.class.getResourceAsStream(resourceName);
        if (resource == null) {
            throw new FileNotFoundException(resourceName);
        }
        final Path dir = tempDirectoryHandler.get();
        final Path path = Paths.get(dir.toString(), getFilename());

        try (InputStream is = resource) {
            try (OutputStream os = Files.newOutputStream(path)) {
                final byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
            if (!Files.isExecutable(path)) {
                if (!path.toFile().setExecutable(true)) {
                    throw new IOException("Cannot set executable permissions on " + path);
                }
            }
        }

        return path;
    }

    private static String getResourceName() {
        final StringBuilder sb = new StringBuilder();
        sb.append('/');

        if (isWin) {
            sb.append("win32");
        } else if (isMac) {
            sb.append("darwin");
        } else if (isUnix) {
            sb.append("linux");
        } else {
            throw new RuntimeException("Unsupported operating system: " + os);
        }

        sb.append('/');

        if (isArm64) {
            sb.append("aarch64");
        } else {
            sb.append("x86_64");
        }

        sb.append('/');

        sb.append(getFilename());

        return sb.toString();
    }

    public static void setProcessHandler(Supplier<Integer> processHandler) {
        Restarter.currentProcessHandler = processHandler;
    }

    public static void setTempDirectoryHandler(Supplier<Path> tempDirectoryHandler) {
        Restarter.tempDirectoryHandler = tempDirectoryHandler;
    }

    /**
     * @param execCommandsHandler List<String> -> commands , Path -> temp dir
     */
    public static void setExecCommandsHandler(ExecCommandsHandler execCommandsHandler) {
        Restarter.execCommandsHandler = execCommandsHandler;
    }

}
