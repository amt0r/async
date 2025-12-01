public class RestaurantSimulation {
    public static void main(String[] args) {
        System.out.println("=== RESTAURANT OPENED ===");
        
        Kitchen kitchen = new Kitchen(4);

        Thread chef1 = new Thread(new Chef(kitchen, "Chef Oleg"));
        Thread chef2 = new Thread(new Chef(kitchen, "Chef Maria"));
        Thread chef3 = new Thread(new Chef(kitchen, "Chef Andriy"));
        Thread chef4 = new Thread(new Chef(kitchen, "Chef Roman"));
        Thread chef5 = new Thread(new Chef(kitchen, "Chef Mykola"));

        chef1.start();
        chef2.start();
        chef3.start();
        chef4.start();
        chef5.start();

        try {
            for (int i = 1; i <= 10; i++) {
                kitchen.submitOrder(new Order(i, getRandomDish()));
                Thread.sleep(200); 
            }

            Thread.sleep(2000);
            
            kitchen.closeKitchen();

            kitchen.submitOrder(new Order(11, "Burger"));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getRandomDish() {
        String[] dishes = {"Borsch", "Rice", "Steak", "Salad", "Pasta", "Soup"};
        return dishes[(int) (Math.random() * dishes.length)];
    }
}