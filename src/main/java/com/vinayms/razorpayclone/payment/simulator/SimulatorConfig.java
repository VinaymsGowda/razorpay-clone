package com.vinayms.razorpayclone.payment.simulator;


import com.vinayms.razorpayclone.common.enums.ChaosMode;
import com.vinayms.razorpayclone.common.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "payment.simulator")
@Getter
@Setter
public class SimulatorConfig {

    private Integer pollIntervalMs = 2000;
    private ChaosMode chaosMode = ChaosMode.NORMAL;
    private Map<String, MethodSimulatorConfig> methods = new HashMap<>();


    public SimulatorConfig.MethodSimulatorConfig getSimulatorConfig(PaymentMethod method) {
        return methods.getOrDefault(
                method.name(),
                new SimulatorConfig.MethodSimulatorConfig()
        );
    }

    @Getter
    @Setter
    public static class MethodSimulatorConfig {

        private Integer minDelaySeconds = 2;
        private Integer maxDelaySeconds = 6;
        private Integer successRate = 80;
    }
}