import java.util.*;

// Things that can be shipped (name + weight)
interface Shippable {
    String getName();
    double getWeight();
}

// Base class for all product types
abstract class Item {
    String name;
    double price;
    int quantity;

    Item(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Some products expire, others don’t
    abstract boolean isExpired();
    abstract boolean requiresShipping();
    abstract double getWeight();
}

// Products that can expire (like food)
class FoodProduct extends Item implements Shippable {
    private boolean expired;
    private double weight;

    FoodProduct(String name, double price, int quantity, double weight, boolean expired) {
        super(name, price, quantity);
        this.expired = expired;
        this.weight = weight;
    }

    boolean isExpired() { return expired; }
    boolean requiresShipping() { return true; }
    public double getWeight() { return weight; }
    public String getName() { return name; }
}

// Products that don’t expire (like electronics or cards)
class NonExpirableProduct extends Item implements Shippable {
    private boolean shippingRequired;
    private double weight;

    NonExpirableProduct(String name, double price, int quantity, boolean shippingRequired, double weight) {
        super(name, price, quantity);
        this.shippingRequired = shippingRequired;
        this.weight = weight;
    }

    boolean isExpired() { return false; }
    boolean requiresShipping() { return shippingRequired; }
    public double getWeight() { return weight; }
    public String getName() { return name; }
}

// Represents one item in the cart
class CartItem {
    Item item;
    int quantity;

    CartItem(Item item, int quantity) {
        if (quantity > item.quantity)
            throw new IllegalArgumentException("Not enough stock for " + item.name);
        this.item = item;
        this.quantity = quantity;
    }

    double getTotalPrice() {
        return item.price * quantity;
    }

    double getTotalWeight() {
        return item.getWeight() * quantity;
    }
}

// The cart that holds added items
class Cart {
    List<CartItem> items = new ArrayList<>();

    void add(Item item, int quantity) {
        if (quantity <= 0 || quantity > item.quantity)
            throw new IllegalArgumentException("Invalid quantity for " + item.name);

        items.add(new CartItem(item, quantity));
        item.quantity -= quantity;
    }

    boolean isEmpty() {
        return items.isEmpty();
    }

    List<CartItem> getItems() {
        return items;
    }
}

// A customer has a name and money
class Customer {
    String name;
    double balance;

    Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    boolean canPay(double amount) {
        return balance >= amount;
    }

    void pay(double amount) {
        balance -= amount;
    }
}

// Handles shipping logic
class ShippingService {
    static void ship(List<Shippable> items) {
        System.out.println("** Shipment notice **");
        double totalWeight = 0;
        Map<String, Integer> itemCount = new LinkedHashMap<>();
        Map<String, Double> itemWeight = new LinkedHashMap<>();

        for (Shippable item : items) {
            itemCount.put(item.getName(), itemCount.getOrDefault(item.getName(), 0) + 1);
            itemWeight.put(item.getName(), item.getWeight());
            totalWeight += item.getWeight();
        }

        for (String name : itemCount.keySet()) {
            System.out.printf("%dx %s %.0fg\n", itemCount.get(name), name, itemWeight.get(name) * 1000);
        }

        System.out.printf("Total package weight %.1fkg\n", totalWeight);
    }
}

// Main system
public class ECommerceSystem {

    // Handles full checkout process
    static void checkout(Customer customer, Cart cart) {
        try {
            if (cart.isEmpty())
                throw new IllegalStateException("Cart is empty.");

            double subtotal = 0, shipping = 0;
            List<Shippable> toShip = new ArrayList<>();

            for (CartItem product : cart.getItems()) {
                if (product.item.isExpired())
                    throw new IllegalStateException("Product " + product.item.name + " is expired.");

                subtotal += product.getTotalPrice();

                if (product.item.requiresShipping()) {
                    for (int i = 0; i < product.quantity; i++)
                        toShip.add((Shippable)product.item);

                    shipping += product.getTotalWeight() * 10; // add shipping fee for each 1kg
                }
            }

            double total = subtotal + shipping;

            if (!customer.canPay(total))
                throw new IllegalStateException("Insufficient balance.");

            if (!toShip.isEmpty()) ShippingService.ship(toShip);

            System.out.println("\n Checkout successful!");
            System.out.println("** Checkout receipt **");
            for (CartItem item : cart.getItems()) {
                System.out.println(item.quantity + "x " + item.item.name + "\t" + (int)item.getTotalPrice());
            }
            System.out.println("----------------------");
            System.out.println("Subtotal\t" + (int)subtotal);
            System.out.println("Shipping\t" + (int)shipping);
            System.out.println("Amount\t" + (int)total);

            customer.pay(total);
            System.out.println("Customer balance after payment: " + customer.balance);

        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Setup products and customer
        Item cheese = new FoodProduct("Cheese", 100, 5, 0.2, false);
        Item biscuits = new FoodProduct("Biscuits", 150, 2, 0.7, false);
        Item tv = new NonExpirableProduct("TV", 500, 2, true, 5.0);
        Item scratchCard = new NonExpirableProduct("Scratch Card", 50, 10, false, 0);
        Customer customer = new Customer("Malak", 9000);
        Cart cart = new Cart();

        // Try adding items to cart
        try { cart.add(cheese, 5); } catch (Exception e) { System.out.println(" Error: " + e.getMessage()); }
        try { cart.add(tv, 2); } catch (Exception e) { System.out.println(" Error: " + e.getMessage()); }
        try { cart.add(scratchCard, 3); } catch (Exception e) { System.out.println(" Error: " + e.getMessage()); }
        try { cart.add(biscuits, 4); } catch (Exception e) { System.out.println(" Error: " + e.getMessage()); }

        // Checkout normally
        checkout(customer, cart);

        // Expired product case
        System.out.println("\n **Expired Product**");
        Item expired = new FoodProduct("Milk", 50, 2, 0.5, true);
        Cart cart2 = new Cart();
        cart2.add(expired, 1);
        checkout(customer, cart2);

        // Stock too low
        System.out.println("\n **Not Enough Stock**");
        try {
            Cart cart3 = new Cart();
            cart3.add(biscuits, 5);
        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }

        // Cart is empty
        System.out.println("\n **Empty Cart**");
        Cart cart4 = new Cart();
        checkout(customer, cart4);

        // Not enough money
        System.out.println("\n **Insufficient Balance**");
        Customer poorCustomer = new Customer("Poor", 100);
        Cart cart5 = new Cart();
        try {
            cart5.add(tv, 1);
        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
        checkout(poorCustomer, cart5);
    }
}
