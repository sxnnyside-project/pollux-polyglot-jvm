package com.sxnnysideproject.pollux;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OperationTest {

    @Test
    @DisplayName("creates filesystem read operation")
    void createsFsRead() {
        Operation op = Operation.fileRead("/etc/hosts");
        assertThat(op.capability()).isEqualTo("read");
        assertThat(op.resourceDomain()).isEqualTo("filesystem");
        assertThat(op.resourceValue()).isEqualTo("/etc/hosts");
        assertThat(op.toJson()).contains("\"capability\":\"read\"").contains("\"resource_domain\":\"filesystem\"");
    }

    @Test
    @DisplayName("creates filesystem write operation")
    void createsFsWrite() {
        Operation op = Operation.fileWrite("./output.bin");
        assertThat(op.capability()).isEqualTo("write");
        assertThat(op.resourceDomain()).isEqualTo("filesystem");
        assertThat(op.resourceValue()).isEqualTo("./output.bin");
    }

    @Test
    @DisplayName("creates network connect operation")
    void createsNetConnect() {
        Operation op = Operation.netConnect("api.example.com");
        assertThat(op.capability()).isEqualTo("connect");
        assertThat(op.resourceDomain()).isEqualTo("network");
        assertThat(op.resourceValue()).isEqualTo("api.example.com");
    }

    @Test
    @DisplayName("creates process spawn operation")
    void createsProcSpawn() {
        Operation op = Operation.procSpawn("/bin/ls");
        assertThat(op.capability()).isEqualTo("spawn");
        assertThat(op.resourceDomain()).isEqualTo("process");
        assertThat(op.resourceValue()).isEqualTo("/bin/ls");
    }
}
