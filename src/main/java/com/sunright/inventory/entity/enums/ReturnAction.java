package com.sunright.inventory.entity.enums;

public enum ReturnAction {
    A1("Rework"), A2("Collect & Replace Rejects"),
    A3("Collect Excessive Parts"), A4("Deliver Shortage"),
    A5("Reject/Order Cancelled"), A6("Corrective Action"), A7("Other");
    private String desc;

    ReturnAction(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
