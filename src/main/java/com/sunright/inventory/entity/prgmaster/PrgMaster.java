package com.sunright.inventory.entity.prgmaster;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "PRG_MASTER")
@Data
@NoArgsConstructor
public class PrgMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prgId;
    private String prgDesc;
    private String moduleCd;
    private String type;
    private String filePath;
    private String remarks;
    private String heading;
    private Integer seq;
    private String specialType;
    private String display;
}
