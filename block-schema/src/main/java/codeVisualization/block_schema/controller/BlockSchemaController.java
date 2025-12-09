package codeVisualization.block_schema.controller;

import codeVisualization.block_schema.service.BlockSchemaService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:8082", allowCredentials = "true")
@RestController
@RequestMapping("/api/blockschema")
public class BlockSchemaController {

    private final BlockSchemaService service;

    public BlockSchemaController(BlockSchemaService service) {
        this.service = service;
    }

    /**
     * Принимает код C/C++ в теле запроса (text/plain) и возвращает SVG как image/svg+xml.
     */
    @PostMapping(value = "/generate", consumes = MediaType.TEXT_PLAIN_VALUE, produces = "image/svg+xml")
    public ResponseEntity<String> generate(@RequestBody String code) {
        try {
            String svg = service.generateDiagram(code);
            return ResponseEntity.ok(svg);
        } catch (Exception e) {
            // возвращаем текст ошибки в виде простого текста (можно улучшить)
            return ResponseEntity
                    .status(500)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Ошибка при генерации: " + e.getMessage());
        }
    }
}
