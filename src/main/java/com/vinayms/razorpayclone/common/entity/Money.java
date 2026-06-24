package com.vinayms.razorpayclone.common.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Money {

    private int amountUnits;

    private String currency;
}

