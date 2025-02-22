package com.github.hstyi.restart4j;

import java.io.IOException;
import java.util.List;

@FunctionalInterface
public interface ExecCommandsHandler {
    void exec(List<String> commands) throws IOException;
}
