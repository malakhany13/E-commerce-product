import java.util.*;

// Interface for shippable products
interface Shippable {
    String getName();
    double getWeight();
}

// Base class for all products
abstract class Product {
    String name;
    double price;
    int quantity;

    Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public boolean isAvailable(int requestedQuantity) {
        return quantity >= requestedQuantity;
    }

    public void reduceQuantity(int q) {
        this.quantity -= q;
    }

    public abstract boolean isExpired();
    public abstract boolean requiresShipping();
}

// Product that can expire and requires shipping (like food)
class ExpirableShippableProduct extends Product implements Shippable {
    boolean expired;
    double weight;

    ExpirableShippableProduct(String name, double price, int quantity, boolean expired, double weight) {
        super(name, price, quantity);
        this.expired = expired;
        this.weight = weight;
    }

    public boolean isExpired() { return expired; }
    public boolean requiresShipping() { return true; }
    public String getName() { return name; }
    public double getWeight() { return weight; }
}

// Item inside the cart
class CartItem {
    Product product;
    int quantity;

    CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return product.price * quantity;
    }
}

// Shopping cart that holds items
class Cart {
    List<CartItem> items = new ArrayList<>();

    public void add(Product product, int quantity) {
        if (quantity > product.quantity) {
            System.out.println("Error: Quantity exceeds available stock.");
            return;
        }
        items.add(new CartItem(product, quantity));
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<CartItem> getItems() {
        return items;
    }
}

// Customer with a balance
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

// Shipping service for shippable products
class ShippingService {
    public static void ship(List<Shippable> items) {
        if (items.isEmpty()) return;

        System.out.println("** Shipment notice **");
        double totalWeight = 0;
        Map<String, Integer> itemCount = new HashMap<>();
        Map<String, Double> itemWeight = new HashMap<>();

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

// Checkout service that handles payment and shipping
class CheckoutService {
    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Error: Cart is empty.");
            return;
        }

        double subtotal = 0;
        double shippingFee = 0;
        List<Shippable> toShip = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            Product p = item.product;

            if (p.isExpired()) {
                System.out.println("Error: Product " + p.name + " is expired.");
                return;
            }

            if (!p.isAvailable(item.quantity)) {
                System.out.println("Error: Product " + p.name + " is out of stock.");
                return;
            }

            subtotal += item.getTotalPrice();

            if (p.requiresShipping()) {
                for (int i = 0; i < item.quantity; i++) {
                    toShip.add((Shippable) p);
                    shippingFee += 10;  // Shipping cost per item (adjusted to match desired output)
                }
            }
        }

        double total = subtotal + shippingFee;

        if (!customer.canAfford(total)) {
            System.out.println("Error: Insufficient balance.");
            return;
        }

        customer.pay(total);

        for (CartItem item : cart.getItems()) {
            item.product.reduceQuantity(item.quantity);
        }

        ShippingService.ship(toShip);

        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.getItems()) {
            System.out.println(item.quantity + "x " + item.product.name + " " + (int)item.getTotalPrice());
        }

        System.out.println("----------------------");
        System.out.println("Subtotal " + (int)subtotal);
        System.out.println("Shipping " + (int)shippingFee);
        System.out.println("Amount " + (int)total);
    }
}

// Main class that runs the program
public class ECommerceSystem {
    public static void main(String[] args) {
        // Products
        Product cheese = new ExpirableShippableProduct("Cheese", 100, 10, false, 0.2);
        Product biscuits = new ExpirableShippableProduct("Biscuits", 150, 10, false, 0.7);

        // Customer
        Customer customer = new Customer("Malak", 1000);

        // Cart
        Cart cart = new Cart();
        cart.add(cheese, 2);      // 2x Cheese = 200
        cart.add(biscuits, 1);    // 1x Biscuits = 150

        // Perform checkout
        CheckoutService.checkout(customer, cart);
    }
}
