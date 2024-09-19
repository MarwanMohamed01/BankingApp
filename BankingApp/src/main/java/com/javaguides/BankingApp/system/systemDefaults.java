package com.javaguides.BankingApp.system;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "system_defaults")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class systemDefaults {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String key;

    private double value;

    public systemDefaults(String key, double value) {
        this.key = key;
        this.value= value;
    }
}
