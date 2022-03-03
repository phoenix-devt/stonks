package fr.lezoo.stonks.share;

public class OrderInfo {
    private double leverage,amount,minPrice,maxPrice;


    public OrderInfo() {
        leverage=1;
        amount=0;
        minPrice=-1;
        maxPrice=-1;
    }


    public boolean hasAmount() {
        return amount!=0;
    }
    public boolean hasMaxPrice(){
        return maxPrice!=-1;
    }

    public boolean hasMinPrice(){
        return minPrice!=-1;
    }

    public void setLeverage(double leverage) {
        this.leverage = leverage;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public double getLeverage() {
        return leverage;
    }

    public double getAmount() {
        return amount;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }
}
