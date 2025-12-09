package codeVisualization.block_schema.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class BlockSchemaService {

    private static final Path YFLOWGEN_PATH =
            Paths.get("app", "bin", "yFlowgen", "yFlowGen_V5.3_EN", "yFlowGen.exe");

    private static final Path GRAPHVIZ_BIN_DIR =
            Paths.get("app", "bin", "graphviz");

    private static final Path WORKING_DIR =
            Paths.get("app", "bin");   // корневая bin папка

    private static final Path OUTPUT_DIR =
            WORKING_DIR.resolve("yflowgen_output"); // постоянная папка для результатов

    public String generateDiagram(String code) throws Exception {

        if (!Files.isRegularFile(YFLOWGEN_PATH)) {
            throw new IllegalStateException("Не найден yFlowGen: " + YFLOWGEN_PATH.toAbsolutePath());
        }
        if (!Files.isDirectory(GRAPHVIZ_BIN_DIR)) {
            throw new IllegalStateException("Не найдена папка Graphviz bin: " + GRAPHVIZ_BIN_DIR.toAbsolutePath());
        }
        if (!Files.isDirectory(WORKING_DIR)) {
            throw new IllegalStateException("Не найдена рабочая директория: " + WORKING_DIR.toAbsolutePath());
        }

        Files.createDirectories(OUTPUT_DIR);

        Path inputFile = OUTPUT_DIR.resolve("input.c");
        Files.write(inputFile, code.getBytes(StandardCharsets.UTF_8));

        System.out.println("=== DEBUG INFO ===");
        System.out.println("YFLOWGEN_PATH = " + YFLOWGEN_PATH.toAbsolutePath());
        System.out.println("Working dir   = " + WORKING_DIR.toAbsolutePath());
        System.out.println("Output dir    = " + OUTPUT_DIR.toAbsolutePath());
        System.out.println("Graphviz bin  = " + GRAPHVIZ_BIN_DIR.toAbsolutePath());
        System.out.println("===================");

        // Проверим реальное содержимое OUTPUT_DIR
        System.out.println("=== DIR (output dir) ===");
        ProcessBuilder dirCheck = new ProcessBuilder("cmd.exe", "/c", "dir");
        dirCheck.directory(OUTPUT_DIR.toFile());
        Process dirProcess = dirCheck.start();
        System.out.println(new String(dirProcess.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
        System.out.println("==========================");

        // Команда YFlowGen
        ProcessBuilder pb = new ProcessBuilder(
                YFLOWGEN_PATH.toAbsolutePath().toString(),
                "-f", inputFile.toAbsolutePath().toString(),
                "-format", GRAPHVIZ_BIN_DIR.toAbsolutePath().toString()
        );
        System.out.println("PATH = " + System.getenv("PATH"));
        pb.redirectErrorStream(true);
        Process p = pb.start();

        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        System.out.println("=== YFLOWGEN OUTPUT ===");
        System.out.println(output);
        System.out.println("=======================");

        int rc = p.waitFor();
        if (rc != 0) {
            throw new RuntimeException("yFlowGen завершился с ошибкой rc=" + rc + ". Вывод:\n" + output);
        }

        // Ищем файл SVG
        Path resultDir = YFLOWGEN_PATH.getParent().resolve("result_yFlowGen")
                .resolve("input.c"); // путь к папке с SVG
        Path svgFile = resultDir.resolve("main.dot.svg");

        if (!Files.isRegularFile(svgFile)) {
            throw new IllegalStateException("SVG файл не найден: " + svgFile.toAbsolutePath());
        }

        // Читаем SVG как строку и возвращаем
        return Files.readString(svgFile, StandardCharsets.UTF_8);
    }
}
