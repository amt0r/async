public class Chef implements Runnable {
    private final Kitchen kitchen;
    private final String name;

    public Chef(Kitchen kitchen, String name) {
        this.kitchen = kitchen;
        this.name = name;
    }

    @Override
    public void run() {
        System.out.println(name + " started.");
        while (true) {
            if (kitchen.isClosed() && kitchen.isOrdersListEmpty()) {
                break;
            }

            Order order = kitchen.getNextOrder();
            if (order != null) {
                kitchen.cookOrder(order, name);
            }
        }
        System.out.println(name + " finished and is going home.");
    }
}