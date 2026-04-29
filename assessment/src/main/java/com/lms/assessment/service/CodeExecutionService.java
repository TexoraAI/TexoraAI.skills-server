




package com.lms.assessment.service;

import com.lms.assessment.exception.CodeExecutionException;
import com.lms.assessment.model.CodeSubmission.ExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

@Service
public class CodeExecutionService {

    private static final Logger log              = LoggerFactory.getLogger(CodeExecutionService.class);
    private static final long   TIMEOUT_SECONDS  = 30L;
    private static final long   MAX_OUTPUT_BYTES = 100_000L;
    private static final String TEMP_DIR_PREFIX  = "lms_exec_";

    private static final boolean IS_WINDOWS =
        System.getProperty("os.name").toLowerCase().contains("win");

    // ── Entry point ───────────────────────────────
    public ExecutionResult execute(String language, String code, String inputOrStudentId) {
        return switch (language.toUpperCase()) {
            case "JAVA"       -> executeJava(code, inputOrStudentId);
            case "PYTHON"     -> executePython(code, inputOrStudentId);
            case "JAVASCRIPT" -> executeJavaScript(code, inputOrStudentId);
            case "MYSQL"      -> executeMySQL(code, inputOrStudentId);
            case "BASH"       -> executeBash(code, inputOrStudentId);
            default -> throw new CodeExecutionException(
                ExecutionStatus.INVALID_LANGUAGE,
                "Unsupported language: " + language
            );
        };
    }

    // ── Java ──────────────────────────────────────
    private ExecutionResult executeJava(String code, String testInput) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
            Path sourceFile = tempDir.resolve("Main.java");
            Files.writeString(sourceFile, code);

            ProcessResult compile = runProcess(
                new ProcessBuilder("javac", sourceFile.toAbsolutePath().toString())
                    .directory(tempDir.toFile()).redirectErrorStream(true),
                TIMEOUT_SECONDS, null
            );
            if (compile.exitCode() != 0)
                return new ExecutionResult(compile.getOutput(),
                    ExecutionStatus.COMPILE_ERROR, compile.elapsedMs());

            ProcessResult run = runProcess(
                new ProcessBuilder("java", "-Xmx256m", "-Xss8m",
                    "-cp", tempDir.toAbsolutePath().toString(), "Main")
                    .directory(tempDir.toFile()).redirectErrorStream(true),
                TIMEOUT_SECONDS, testInput
            );
            return result(run);

        } catch (TimeoutException e) { return timeout();
        } catch (IOException e) {
            log.error("Java IO error", e);
            throw new CodeExecutionException(ExecutionStatus.RUNTIME_ERROR, e.getMessage());
        } finally { deleteTempDir(tempDir); }
    }

    // ── Python ────────────────────────────────────
    private ExecutionResult executePython(String code, String testInput) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
            Path scriptFile = tempDir.resolve("script.py");
            Files.writeString(scriptFile, code);

            ProcessResult run = runProcess(
                new ProcessBuilder(resolvePythonCommand(), "-u",
                    scriptFile.toAbsolutePath().toString())
                    .directory(tempDir.toFile()).redirectErrorStream(true),
                TIMEOUT_SECONDS, testInput
            );
            return result(run);

        } catch (TimeoutException e) { return timeout();
        } catch (IOException e) {
            log.error("Python IO error", e);
            throw new CodeExecutionException(ExecutionStatus.RUNTIME_ERROR, e.getMessage());
        } finally { deleteTempDir(tempDir); }
    }

    // ── JavaScript ────────────────────────────────
    private ExecutionResult executeJavaScript(String code, String testInput) {
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
            Path scriptFile = tempDir.resolve("script.js");
            Files.writeString(scriptFile, code);

            ProcessResult run = runProcess(
                new ProcessBuilder("node", "--max-old-space-size=256",
                    scriptFile.toAbsolutePath().toString())
                    .directory(tempDir.toFile()).redirectErrorStream(true),
                TIMEOUT_SECONDS, testInput
            );
            return result(run);

        } catch (TimeoutException e) { return timeout();
        } catch (IOException e) {
            log.error("JavaScript IO error", e);
            throw new CodeExecutionException(ExecutionStatus.RUNTIME_ERROR, e.getMessage());
        } finally { deleteTempDir(tempDir); }
    }

    // ── MySQL (persistent per student) ───────────
    private ExecutionResult executeMySQL(String code, String studentId) {
        String dbName    = "student_" + sanitizeId(studentId);
        String mysqlHost = System.getenv().getOrDefault("CODE_EXEC_MYSQL_HOST", "localhost");
        String mysqlUser = System.getenv().getOrDefault("CODE_EXEC_MYSQL_USER", "lms_runner");
        String mysqlPass = System.getenv().getOrDefault("CODE_EXEC_MYSQL_PASS", "Runner@Str0ng!");
        String mysqlExe  = resolveMysqlCommand();
        Path   tempDir   = null;

        try {
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
            Path optFile = writeMyCnf(tempDir, mysqlHost, mysqlUser, mysqlPass);
            String optArg = "--defaults-extra-file=" + optFile.toAbsolutePath();

            // Ensure student DB exists (creates only if not exists)
            Process createDb = new ProcessBuilder(
                mysqlExe, optArg,
                "--connect-timeout=10",
                "-e", "CREATE DATABASE IF NOT EXISTS `" + dbName + "`;"
            ).redirectErrorStream(true).start();

            boolean created = createDb.waitFor(15, TimeUnit.SECONDS);
            String createOutput = new String(
                createDb.getInputStream().readAllBytes()).trim();

            if (!created || createDb.exitValue() != 0) {
                return new ExecutionResult(
                    "MySQL connection failed.\n\n" +
                    "Check:\n" +
                    "1. MySQL is running\n" +
                    "2. User 'lms_runner'@'localhost' exists\n" +
                    "3. application.yml has correct credentials\n\n" +
                    "Error: " + createOutput,
                    ExecutionStatus.RUNTIME_ERROR, 0
                );
            }

            // Run student SQL in their persistent DB
            ProcessResult run = runProcess(
                new ProcessBuilder(
                    mysqlExe, optArg,
                    "--table",
                    "--safe-updates",
                    "--connect-timeout=10",
                    dbName
                ).redirectErrorStream(true),
                TIMEOUT_SECONDS,
                code
            );

            return result(run);

        } catch (TimeoutException e) {
            return timeout();
        } catch (IOException | InterruptedException e) {
            log.error("MySQL execution error", e);
            throw new CodeExecutionException(
                ExecutionStatus.RUNTIME_ERROR, e.getMessage());
        } finally {
            deleteTempDir(tempDir); // only deletes temp cnf dir, NOT student DB
        }
    }

    // ── MySQL Reset ───────────────────────────────
    public ExecutionResult resetMySQLDatabase(String studentId) {
        String dbName    = "student_" + sanitizeId(studentId);
        String mysqlHost = System.getenv().getOrDefault("CODE_EXEC_MYSQL_HOST", "localhost");
        String mysqlUser = System.getenv().getOrDefault("CODE_EXEC_MYSQL_USER", "lms_runner");
        String mysqlPass = System.getenv().getOrDefault("CODE_EXEC_MYSQL_PASS", "Runner@Str0ng!");
        String mysqlExe  = resolveMysqlCommand();
        Path   tempDir   = null;

        try {
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
            Path optFile = writeMyCnf(tempDir, mysqlHost, mysqlUser, mysqlPass);
            String optArg = "--defaults-extra-file=" + optFile.toAbsolutePath();

            String sql = "DROP DATABASE IF EXISTS `" + dbName + "`; " +
                         "CREATE DATABASE `" + dbName + "`;";

            Process reset = new ProcessBuilder(
                mysqlExe, optArg,
                "--connect-timeout=10",
                "-e", sql
            ).redirectErrorStream(true).start();

            boolean done = reset.waitFor(15, TimeUnit.SECONDS);
            String output = new String(
                reset.getInputStream().readAllBytes()).trim();

            if (!done || reset.exitValue() != 0) {
                return new ExecutionResult(
                    "Reset failed.\n\nError: " + output,
                    ExecutionStatus.RUNTIME_ERROR, 0
                );
            }

            return new ExecutionResult(
                "✅ Database `" + dbName + "` reset successfully. All tables dropped.",
                ExecutionStatus.SUCCESS, 0
            );

        } catch (IOException | InterruptedException e) {
            log.error("MySQL reset error", e);
            throw new CodeExecutionException(
                ExecutionStatus.RUNTIME_ERROR, e.getMessage());
        } finally {
            deleteTempDir(tempDir);
        }
    }

    // ── MySQL DB State ────────────────────────────
    public ExecutionResult getMySQLDatabaseState(String studentId) {
        String dbName    = "student_" + sanitizeId(studentId);
        String mysqlHost = System.getenv().getOrDefault("CODE_EXEC_MYSQL_HOST", "localhost");
        String mysqlUser = System.getenv().getOrDefault("CODE_EXEC_MYSQL_USER", "lms_runner");
        String mysqlPass = System.getenv().getOrDefault("CODE_EXEC_MYSQL_PASS", "Runner@Str0ng!");
        String mysqlExe  = resolveMysqlCommand();
        Path   tempDir   = null;

        try {
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
            Path optFile = writeMyCnf(tempDir, mysqlHost, mysqlUser, mysqlPass);
            String optArg = "--defaults-extra-file=" + optFile.toAbsolutePath();

            // Check DB exists
            Process checkDb = new ProcessBuilder(
                mysqlExe, optArg,
                "--connect-timeout=10",
                "--skip-column-names",
                "-e", "SHOW DATABASES LIKE '" + dbName + "';"
            ).redirectErrorStream(true).start();
            checkDb.waitFor(10, TimeUnit.SECONDS);
            String dbCheck = new String(
                checkDb.getInputStream().readAllBytes()).trim();

            if (!dbCheck.contains(dbName)) {
                return new ExecutionResult(
                    "No database found yet. Run some SQL first to create tables!",
                    ExecutionStatus.SUCCESS, 0
                );
            }

            // Fetch tables with row counts
            String sql =
                "SELECT TABLE_NAME, TABLE_ROWS, CREATE_TIME " +
                "FROM information_schema.TABLES " +
                "WHERE TABLE_SCHEMA = '" + dbName + "' " +
                "ORDER BY CREATE_TIME;";

            ProcessResult run = runProcess(
                new ProcessBuilder(
                    mysqlExe, optArg,
                    "--table",
                    "--connect-timeout=10",
                    "information_schema"
                ).redirectErrorStream(true),
                TIMEOUT_SECONDS,
                sql
            );

            String output = run.getOutput() == null || run.getOutput().isBlank()
                ? "Database `" + dbName + "` exists but has no tables yet."
                : "📦 Database: " + dbName + "\n\n" + run.getOutput();

            return new ExecutionResult(output, ExecutionStatus.SUCCESS, run.elapsedMs());

        } catch (TimeoutException e) {
            return timeout();
        } catch (IOException | InterruptedException e) {
            log.error("MySQL state error", e);
            throw new CodeExecutionException(
                ExecutionStatus.RUNTIME_ERROR, e.getMessage());
        } finally {
            deleteTempDir(tempDir);
        }
    }

//    // ── Bash ──────────────────────────────────────
//    private ExecutionResult executeBash(String code, String testInput) {
//        Path tempDir = null;
//        try {
//            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
//            Path scriptFile = tempDir.resolve("script.sh");
//
//            String normalized = code.replace("\r\n", "\n").replace("\r", "\n");
//            Files.writeString(scriptFile, normalized);
//            scriptFile.toFile().setExecutable(true);
//
//            ProcessBuilder pb;
//
//            if (IS_WINDOWS) {
//                String bashExe = resolveBashWindows();
//                String absPath     = scriptFile.toAbsolutePath().toString();
//                String driveLetter = absPath.substring(0, 1).toLowerCase();
//                String unixPath    = "/" + driveLetter
//                    + absPath.substring(2).replace("\\", "/");
//
//                log.debug("Bash exe: {}", bashExe);
//                log.debug("Script unix path: {}", unixPath);
//
//                pb = new ProcessBuilder(bashExe, unixPath)
//                    .directory(tempDir.toFile())
//                    .redirectErrorStream(true);
//            } else {
//                String scriptPath = scriptFile.toAbsolutePath().toString();
//                pb = new ProcessBuilder(
//                    "bash", "-c",
//                    "ulimit -f 10000 -u 50 -v 524288 2>/dev/null; bash " + scriptPath
//                ).directory(tempDir.toFile()).redirectErrorStream(true);
//
//                pb.environment().put("PATH", "/usr/local/bin:/usr/bin:/bin");
//                pb.environment().put("HOME", tempDir.toAbsolutePath().toString());
//                pb.environment().put("TMPDIR", tempDir.toAbsolutePath().toString());
//            }
//
//            sandboxEnv(pb);
//
//            ProcessResult run = runProcess(pb, TIMEOUT_SECONDS, testInput);
//            return result(run);
//
//        } catch (TimeoutException e) {
//            return timeout();
//        } catch (IOException e) {
//            log.error("Bash IO error", e);
//            throw new CodeExecutionException(ExecutionStatus.RUNTIME_ERROR, e.getMessage());
//        } finally {
//            deleteTempDir(tempDir);
//        }
//    }

    private static final Path WORKSPACES_ROOT = Path.of(
    	    System.getProperty("user.home"), "lms_workspaces"
    	);

    private ExecutionResult executeBash(String code, String studentId) {
        try {
            // Persistent folder per student — never deleted
            Path studentHome = WORKSPACES_ROOT.resolve("student_" + sanitizeId(studentId));
            Files.createDirectories(studentHome);

            // Write script into their home dir
            Path scriptFile = studentHome.resolve("_run.sh");
            String normalized = code.replace("\r\n", "\n").replace("\r", "\n");
            Files.writeString(scriptFile, normalized);
            scriptFile.toFile().setExecutable(true);

            ProcessBuilder pb;

            // ✅ THIS IS WHERE YOUR CODE GOES
            if (IS_WINDOWS) {
                String bashExe = resolveBashWindows();
                String absPath = scriptFile.toAbsolutePath().toString();
                String driveLetter = absPath.substring(0, 1).toLowerCase();
                String unixPath = "/" + driveLetter + absPath.substring(2).replace("\\", "/");

                // ✅ --login flag makes Git Bash load its own PATH setup automatically
                pb = new ProcessBuilder(bashExe, "--login", unixPath)
                    .directory(studentHome.toFile())
                    .redirectErrorStream(true);

                // No need to set PATH manually anymore — --login handles it!
            }
            else {
        	   
                pb = new ProcessBuilder("bash", scriptFile.toAbsolutePath().toString())
                    .directory(studentHome.toFile())
                    .redirectErrorStream(true);

                pb.environment().put("PATH", "/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin");
                pb.environment().put("HOME", studentHome.toAbsolutePath().toString());
            }

            pb.environment().put("TMPDIR", studentHome.toAbsolutePath().toString());
            sandboxEnv(pb);

            ProcessResult run = runProcess(pb, TIMEOUT_SECONDS, null);
            return result(run);

        } catch (TimeoutException e) { return timeout();
        } catch (IOException e) {
            log.error("Bash IO error", e);
            throw new CodeExecutionException(ExecutionStatus.RUNTIME_ERROR, e.getMessage());
        }
        // ✅ No deleteTempDir — student folder is kept!
    }
    
    
    // ── Resolve commands ──────────────────────────
    private String resolveMysqlCommand() {
        String env = System.getenv("MYSQL_EXECUTABLE");
        if (env != null && new File(env).exists()) return env;

        if (IS_WINDOWS) {
            String[] winPaths = {
                "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysql.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysql.exe",
                "C:\\xampp\\mysql\\bin\\mysql.exe",
                "C:\\wamp64\\bin\\mysql\\mysql8.0.31\\bin\\mysql.exe"
            };
            for (String p : winPaths) {
                if (new File(p).exists()) {
                    log.info("MySQL found at: {}", p);
                    return p;
                }
            }
        }
        return "mysql";
    }

    private String resolveBashWindows() {
        String env = System.getenv("BASH_EXECUTABLE");
        if (env != null && new File(env).exists()) return env;

        String[] paths = {
            "C:\\msys64\\usr\\bin\\bash.exe",
            "C:\\msys32\\usr\\bin\\bash.exe",
            "C:\\Program Files\\Git\\bin\\bash.exe",
            "C:\\Program Files\\Git\\usr\\bin\\bash.exe",
            "C:\\Git\\bin\\bash.exe"
        };
        for (String p : paths) {
            if (new File(p).exists()) {
                log.info("Bash found at: {}", p);
                return p;
            }
        }
        throw new CodeExecutionException(ExecutionStatus.RUNTIME_ERROR,
            "Bash not found.\n" +
            "Install MSYS2 from https://www.msys2.org or Git for Windows.\n" +
            "Or set BASH_EXECUTABLE env variable to your bash.exe path."
        );
    }

    private String resolvePythonCommand() {
        String env = System.getenv("PYTHON_EXECUTABLE");
        if (env != null && new File(env).exists()) return env;
        for (String cmd : new String[]{"python3", "python"}) {
            try {
                Process p = new ProcessBuilder(cmd, "--version")
                    .redirectErrorStream(true).start();
                p.waitFor();
                if (p.exitValue() == 0) return cmd;
            } catch (Exception ignored) {}
        }
        throw new CodeExecutionException(ExecutionStatus.RUNTIME_ERROR,
            "No Python interpreter found.");
    }

    // ── MySQL helpers ─────────────────────────────
    private Path writeMyCnf(Path dir, String host, String user, String pass)
            throws IOException {
        Path optFile = dir.resolve("my.cnf");
        Files.writeString(optFile,
            "[client]\n" +
            "user="     + user + "\n" +
            "password=" + pass + "\n" +
            "host="     + host + "\n"
        );
        return optFile;
    }

    private String sanitizeId(String id) {
        // Prevent SQL injection in DB name
        return id.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    // ── Sandbox env ───────────────────────────────
    private void sandboxEnv(ProcessBuilder pb) {
        Map<String, String> env = pb.environment();
        env.remove("LD_PRELOAD");
        env.remove("LD_LIBRARY_PATH");
        env.remove("JAVA_TOOL_OPTIONS");
        env.remove("PYTHONSTARTUP");
        env.remove("PYTHONPATH");
        env.remove("NODE_OPTIONS");
        env.remove("AWS_ACCESS_KEY_ID");
        env.remove("AWS_SECRET_ACCESS_KEY");
        env.remove("DATABASE_URL");
        env.remove("SPRING_DATASOURCE_PASSWORD");
        env.remove("CODE_EXEC_MYSQL_PASS");
    }
    
//    private String resolveWindowsBashPath(String bashExe) {
//        File root = new File(bashExe).getParentFile();
//        if (root.getName().equalsIgnoreCase("bin") &&
//            root.getParentFile().getName().equalsIgnoreCase("usr")) {
//            root = root.getParentFile().getParentFile();
//        } else {
//            root = root.getParentFile();
//        }
//        return new File(root, "usr\\bin") + ";" + new File(root, "bin");
//    }
    private String resolveWindowsBashPath(String bashExe) {
        try {
            // bash.exe is at: C:\Program Files\Git\\bin\bash.exe
            // We walk UP to find Git root, then add all bin folders
            File bashFile = new File(bashExe).getCanonicalFile();
            File dir = bashFile.getParentFile();

            // Walk up max 3 levels to find Git root
            // (contains both "usr" and "bin" folders)
            File gitRoot = null;
            for (int i = 0; i < 3; i++) {
                if (new File(dir, "usr\\bin").exists()
                        && new File(dir, "bin").exists()) {
                    gitRoot = dir;
                    break;
                }
                dir = dir.getParentFile();
                if (dir == null) break;
            }

            if (gitRoot == null) {
                log.warn("Could not find Git root from: {}", bashExe);
                // fallback — use parent of bash.exe
                gitRoot = bashFile.getParentFile().getParentFile();
            }

            String usrBin   = new File(gitRoot, "usr\\bin").getAbsolutePath();
            String bin      = new File(gitRoot, "bin").getAbsolutePath();
            String mingw    = new File(gitRoot, "mingw64\\bin").getAbsolutePath();

            String resolved = usrBin + ";" + bin + ";" + mingw;
            log.info("Resolved Git Bash PATH: {}", resolved);
            return resolved;

        } catch (Exception e) {
            log.error("Failed to resolve Windows bash path", e);
            // last resort fallback
            return "C:\\Program Files\\Git\\usr\\bin"
                 + ";C:\\Program Files\\Git\\bin"
                 + ";C:\\Program Files\\Git\\mingw64\\bin";
        }
    }

    // ── Core process runner ───────────────────────
    private ProcessResult runProcess(ProcessBuilder builder,
                                     long timeoutSeconds,
                                     String input)
            throws IOException, TimeoutException {

        long    start = System.currentTimeMillis();
        Process proc  = builder.start();

        if (input != null && !input.isEmpty()) {
            Thread t = new Thread(() -> {
                try (OutputStream os = proc.getOutputStream()) {
                    os.write(input.getBytes());
                    os.write('\n');
                    os.flush();
                } catch (IOException ignored) {}
            });
            t.setDaemon(true);
            t.start();
        } else {
            proc.getOutputStream().close();
        }

        ExecutorService reader = Executors.newSingleThreadExecutor();
        Future<String> outputFuture = reader.submit(() -> {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                    if (sb.length() > MAX_OUTPUT_BYTES) {
                        sb.append("\n[Output truncated — 100KB limit]");
                        proc.destroyForcibly();
                        break;
                    }
                }
                return sb.toString().trim();
            }
        });

        try {
            boolean done = proc.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!done) {
                proc.destroyForcibly();
                throw new TimeoutException("Exceeded " + timeoutSeconds + "s");
            }
            String output = outputFuture.get(5, TimeUnit.SECONDS);
            return new ProcessResult(output, proc.exitValue(),
                System.currentTimeMillis() - start);
        } catch (InterruptedException | ExecutionException |
                 java.util.concurrent.TimeoutException e) {
            proc.destroyForcibly();
            throw new TimeoutException("Process interrupted");
        } finally {
            reader.shutdownNow();
        }
    }

    // ── Helpers ───────────────────────────────────
    private ExecutionResult result(ProcessResult r) {
        return new ExecutionResult(r.getOutput(),
            r.exitCode() == 0 ? ExecutionStatus.SUCCESS : ExecutionStatus.RUNTIME_ERROR,
            r.elapsedMs());
    }

    private ExecutionResult timeout() {
        return new ExecutionResult(
            "Execution timed out after " + TIMEOUT_SECONDS + "s",
            ExecutionStatus.TIMEOUT, TIMEOUT_SECONDS * 1000
        );
    }

    private void deleteTempDir(Path dir) {
        if (dir == null) return;
        try {
            Files.walk(dir).sorted(Comparator.reverseOrder())
                .map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            log.warn("Failed to clean temp dir: {}", dir, e);
        }
    }

    // ── Result types ──────────────────────────────
    public static class ExecutionResult {
        private final String output;
        private final ExecutionStatus status;
        private final long elapsedMs;
        public ExecutionResult(String o, ExecutionStatus s, long ms) {
            output = o; status = s; elapsedMs = ms;
        }
        public String getOutput()          { return output; }
        public ExecutionStatus getStatus() { return status; }
        public long getElapsedMs()         { return elapsedMs; }
    }

    private static class ProcessResult {
        private final String output;
        private final int    exitCode;
        private final long   elapsedMs;
        ProcessResult(String o, int c, long ms) {
            output = o; exitCode = c; elapsedMs = ms;
        }
        public String getOutput() { return output; }
        public int    exitCode()  { return exitCode; }
        public long   elapsedMs() { return elapsedMs; }
    }
}