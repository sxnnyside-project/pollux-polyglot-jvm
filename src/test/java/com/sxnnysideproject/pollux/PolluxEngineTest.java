package com.sxnnysideproject.pollux;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolluxEngineTest {

    private static final String VALID_MANIFEST = """
            version: 1
            filesystem:
              read:
                - ./assets
                - /tmp
            """;

    private static final String INVALID_MANIFEST = """
            version: invalid
            """;

    @Test
    @DisplayName("loads engine and validates ABI version")
    void loadsAndValidatesAbi() {
        try (PolluxEngine engine = PolluxEngine.load(VALID_MANIFEST)) {
            assertThat(engine.getAbiVersion()).isEqualTo("pollux-abi/1");
            assertThat(engine.getCoreVersion()).isNotEmpty();
            assertThat(engine.isClosed()).isFalse();
        }
    }

    @Test
    @DisplayName("evaluates allowed filesystem read with allow verdict")
    void evaluatesAllowedRead() {
        try (PolluxEngine engine = PolluxEngine.load(VALID_MANIFEST)) {
            EvaluationResult result = engine.evaluate(Operation.fileRead("./assets"));
            assertThat(result.allowed()).isTrue();
            assertThat(result.outcome()).isEqualToIgnoringCase("allow");
            assertThat(result.traceJson()).contains("assets");
        }
    }

    @Test
    @DisplayName("evaluates unauthorized filesystem write with deny verdict")
    void evaluatesUnauthorizedWrite() {
        try (PolluxEngine engine = PolluxEngine.load(VALID_MANIFEST)) {
            EvaluationResult result = engine.evaluate(Operation.fileWrite("./assets"));
            assertThat(result.allowed()).isFalse();
            assertThat(result.outcome()).isEqualToIgnoringCase("deny");
        }
    }

    @Test
    @DisplayName("fails on malformed manifest YAML")
    void failsOnMalformedManifest() {
        assertThatThrownBy(() -> PolluxEngine.load(INVALID_MANIFEST))
                .isInstanceOf(PolluxException.ManifestException.class);
    }

    @Test
    @DisplayName("prevents evaluation after engine is closed")
    void preventsEvaluationAfterClose() {
        PolluxEngine engine = PolluxEngine.load(VALID_MANIFEST);
        engine.close();
        assertThat(engine.isClosed()).isTrue();

        assertThatThrownBy(() -> engine.evaluate(Operation.fileRead("./assets")))
                .isInstanceOf(PolluxException.DisposedException.class);
    }
}
