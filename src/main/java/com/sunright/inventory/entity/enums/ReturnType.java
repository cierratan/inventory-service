package com.sunright.inventory.entity.enums;

public enum ReturnType {
    R1("Failed Quality Check"), R2("Damage"),
    R3("Quantity Shortage"), R4("Quantity Excess"),
    R5("Wrong Parts Delivered"), R6("Others");

    private String desc;

    ReturnType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
