package com.vinayms.razorpayclone.operations.settlement;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "settlement")
@Getter
@Setter
public class SettlementProperties {

    private int holdDays = 2;
    private BigDecimal feePercent = new BigDecimal("2.0");
    private BigDecimal gstPercent = new BigDecimal("2.5");
    private String zone = "Asia/Kolkata";
    private String createCron = "0 0 1 * * *";
    private int maxRetries = 3;
    private List<Integer> backoffSeconds = new ArrayList<>(List.of(300, 1800, 7200));
}
