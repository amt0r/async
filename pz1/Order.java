public class Order {
    private final int id;
    private final String dishName;

    public Order(int id, String dishName) {
        this.id = id;
        this.dishName = dishName;
    }

    public int getId() {
        return id;
    }

    public String getDishName() {
        return dishName;
    }

    @Override
    public String toString() {
        return "Order #" + id + " (" + dishName + ")";
    }
}