package org.example;

public class Order {

    private Person consumer;

    private String itemName;

    private int itemPrice;

    private int extraPoint; // 特別追加ポイント

    private boolean specialPointOrder;

    public Order() {
    }

    public Order(Person consumer, String itemName, int itemPrice) {
        this.consumer = consumer;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.extraPoint = 0;
        specialPointOrder = false;
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

    public int getExtraPoint() {
        return extraPoint;
    }

    public void setExtraPoint(int extraPoint) {
        this.extraPoint = extraPoint;
    }

    public void addExtraPoint(int point) {
        this.extraPoint += point;
    }

    public boolean isSpecialPointOrder() {
        return specialPointOrder;
    }

    public void setSpecialPointOrder(boolean specialPointOrder) {
        this.specialPointOrder = specialPointOrder;
    }

    @Override
    public String toString() {
        return "Order [consumer=" + consumer.getName() + ", itemName=" + itemName + ", itemPrice=" + itemPrice + ", specialPointOrder=" + specialPointOrder + "]";
    }
}
