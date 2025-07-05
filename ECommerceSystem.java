import java.util.*;

// Any item that can be shipped needs to say its name and weight
interface Shippable {
    String getName();
    double getWeight();
}

// Base class for all products
abstract class Item {
    String name;
    double price;
    int quantity;

    // Constructor
    Item(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // Check if we have enough quantity
    public boolean isAvailable(int requestedQuantity) {
        return quantity >= requestedQuantity;
    }

    // Reduce the quantity after purchase
    public void reduceQuantity(int q) {
        this.quantity -= q;
    }

    // To be implemented depending on product type
    public abstract boolean isExpired();
    public abstract boolean requiresShipping();
}

// This is for products that expire and need shipping like cheese
class ExpirableItem extends Item implements Shippable {
    boolean expired;
    double weight;

    ExpirableItem(String name, double price, int quantity, boolean expired, double weight) {
        super(name, price, quantity);
        this.expired = expired;
        this.weight = weight;
    }

    public boolean isExpired() { return expired; }
    public boolean requiresShipping() { return true; }
    public String getName() { return name; }
    public double getWeight() { return weight; }
}

// This holds a product and its quantity inside the cart
class CartItem {
    Item item;
    int quantity;

    CartItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }
    // Calculate the total price of this item
    public double getTotalPrice() {
        return item.price * quantity;
    }
}
class Cart {
    List<CartItem> items = new ArrayList<>();

    // Add a product to cart if enough quantity exists
    public void add(Item item, int quantity) {
        if (quantity > item.quantity) {
            System.out.println("Error: Quantity exceeds available stock.");
            return;
        }
        items.add(new CartItem(item, quantity));
    }
    // Check if the cart is empty
    public boolean isEmpty() {
        return items.isEmpty();
    }
    public List<CartItem> getItems() {
        return items;
    }
}
// The customer who pays and has a balance
class Customer {
    String name;
    double balance;

    Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public boolean canAfford(double amount) {
        return balance >= amount;
    }

    public void pay(double amount) {
        this.balance -= amount;
    }
    public double getBalance() {
        return balance;
    }
}
// Handles shipping: groups and prints all items to be shipped
class DeliveryHandler {
    public static void ship(List<Shippable> items) {
        if (items.isEmpty()) return;

        System.out.println("** Shipment notice **");
        double totalWeight = 0;
        Map<String, Integer> itemCount = new HashMap<>();
        Map<String, Double> itemWeight = new HashMap<>();

        // Count each item and calculate total weight
        for (Shippable s : items) {
            itemCount.put(s.getName(), itemCount.getOrDefault(s.getName(), 0) + 1);
            itemWeight.put(s.getName(), s.getWeight());
            totalWeight += s.getWeight();
        }
        for (String name : itemCount.keySet()) {
            double totalItemWeight = itemWeight.get(name) * itemCount.get(name);
            System.out.println(itemCount.get(name) + "x " + name + " " + (int)(totalItemWeight * 1000) + "g");
        }
        System.out.println("Total package weight " + totalWeight + "kg\n");
    }
}
// This class handles checkout: validation, payment, shipping, receipt
class OrderProcessor {
    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Error: Cart is empty.");
            return;
        }
        double subtotal = 0;
        double shippingFee = 0;
        List<Shippable> toShip = new ArrayList<>();
        // Loop through each item in the cart
        for (CartItem product : cart.getItems()) {
            Item p = product.item;
            if (p.isExpired()) {
                System.out.println("Error: Product " + p.name + " is expired.");
                return;
            }
            if (!p.isAvailable(product.quantity)) {
                System.out.println("Error: Product " + p.name + " is out of stock.");
                return;
            }
            subtotal += product.getTotalPrice();

            // If item needs shipping, add it to shipping list
            if (p.requiresShipping()) {
                for (int i = 0; i < product.quantity; i++) {
                    toShip.add((Shippable) p);
                    shippingFee += 10;  
                }
            }
        }

        double total = subtotal + shippingFee;

        if (!customer.canAfford(total)) {
            System.out.println("Error: Insufficient balance.");
            return;
        }
        customer.pay(total);
        for (CartItem product : cart.getItems()) {
            product.item.reduceQuantity(product.quantity);
        }
        DeliveryHandler.ship(toShip);
        System.out.println("** Checkout receipt **");
        for (CartItem product : cart.getItems()) {
            System.out.println(product.quantity + "x " + product.item.name + " " + (int)product.getTotalPrice());
        }
        System.out.println("----------------------");
        System.out.println("Subtotal " + (int)subtotal);
        System.out.println("Shipping " + (int)shippingFee);
        System.out.println("Amount " + (int)total);
    }
}
// Main program to test the system
public class ECommerceSystem {
    public static void main(String[] args) {
        Item cheese = new ExpirableItem("Cheese", 100, 10, false, 0.2);
        Item biscuits = new ExpirableItem("Biscuits", 150, 10, false, 0.7);

        Customer customer = new Customer("Malak", 1000);

        Cart cart = new Cart();
        cart.add(cheese, 2);
        cart.add(biscuits, 1);

        // Perform checkout
        OrderProcessor.checkout(customer, cart);
    }
}
