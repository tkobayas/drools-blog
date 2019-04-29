package org.example;

import java.math.BigDecimal;

public class Order {

    private Person consumer;

    private String itemName;

    private int itemPrice;

    private BigDecimal pointRate; // ポイント率(パーセント)
    private int extraPoint; // 特別追加ポイント

    public Order() {
    }

    public Order(Person consumer, String itemName, int itemPrice) {
        this.consumer = consumer;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.pointRate = new BigDecimal("0.0");
        this.extraPoint = 0;
    }

    public Person getConsumer() {
        return consumer;
    }

    public void setConsumer(Person consumer) {
        this.consumer = consumer;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }

    public BigDecimal getPointRate() {
        return pointRate;
    }

    public void setPointRate(BigDecimal pointRate) {
        this.pointRate = pointRate;
    }

    public int getExtraPoint() {
        return extraPoint;
    }

    public void setExtraPoint(int extraPoint) {
        this.extraPoint = extraPoint;
    }

    public int getTotalPoint() {
        BigDecimal rate = pointRate.divide(new BigDecimal("100"));
        BigDecimal result = new BigDecimal(itemPrice).multiply(rate).add(new BigDecimal(extraPoint));
        return result.intValue();
    }

    @Override
    public String toString() {
        return "Order [consumer=" + consumer.getName() + ", itemName=" + itemName + ", itemPrice=" + itemPrice + "]";
    }
}
