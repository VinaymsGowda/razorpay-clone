package com.vinayms.razorpayclone.operations.settlement;

import com.vinayms.razorpayclone.common.enums.ChaosMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "settlement.simulator")
@Getter
@Setter
public class SettlementSimulatorConfig {

    private Integer pollIntervalMs = 5000;
    private Integer successRate = 80;
    private ChaosMode chaosMode = ChaosMode.NORMAL;
    private Integer batchSize = 50;
}
