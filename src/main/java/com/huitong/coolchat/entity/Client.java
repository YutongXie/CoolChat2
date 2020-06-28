package com.huitong.coolchat.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
public class Client {
    private String name;
    private List<PurchaseRecord> purchaseRecordList;
}
