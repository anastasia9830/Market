package de.tub;

import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Log
public class Console {
    // В начало класса добавь:
    private Market market;
    private Scanner scanner;

    public Console() {
        this.market = new Market();
        this.scanner = new Scanner(System.in);
    }

    // Конструктор для тестов
    public Console(Market market, Scanner scanner) {
        this.market = market;
        this.scanner = scanner;
    }

    // Сеттер для тестов
    public void setCurrentUser(Authorized_Users user) {
        this.currentUser = user;
    }

    //private final Market market = new Market();
    //private final Scanner scanner = new Scanner(System.in);

    private final List<Authorized_Users> authorized_users = List.of(
            new Authorized_Users("claudia_schmidt", "1234", "admin"),
            new Authorized_Users("maria_morozova", "5678", "admin"),
            new Authorized_Users("felix_becker", "1742", "admin"),
            new Authorized_Users("mark_edelstein", "3458", "seller"),
            new Authorized_Users("vkussvill", "1111", "seller"),
            new Authorized_Users("fresh_farms", "1151", "seller"),
            new Authorized_Users("prospekt", "4511", "seller"),
            new Authorized_Users("pyaterochka", "1781", "seller"),
            new Authorized_Users("lenta", "9811", "seller"),
            new Authorized_Users("zhabka", "1561", "seller"),
            new Authorized_Users("sofia_miller", "4561", "seller"),
            new Authorized_Users("karl_fisher", "1987", "seller"),
            new Authorized_Users("mia_mecklenburg", "2022", "seller"),
            new Authorized_Users("ivan_nowak", "1997", "seller"),
            new Authorized_Users("kristina_tarakanova", "2002", "seller"),
            new Authorized_Users("life_gmbh", "4567", "seller"),
            new Authorized_Users("frish", "5674", "seller"),
            new Authorized_Users("gesundheit_gmbh", "1987", "seller"),
            new Authorized_Users("oleniy_kopyta", "1923", "seller"),
            new Authorized_Users("mir", "2027", "seller"),
            new Authorized_Users("molto_bene", "2019", "seller"),
            new Authorized_Users("tvoy_den", "2025", "seller"),
            new Authorized_Users("arizona", "2021", "seller")

    );

    private Authorized_Users currentUser;

    public void start() {
        market = new Market();
        loadProductsFromCSV("list_of_products.csv");
        log.info("Welcome to the market!");

        while (true) {
            log.severe("""
            What do you want to do?
            1. Display all products
            2. Search for product
            3. Add product (for admin only)
            4. Manage your offers (for sellers only)
            5. Buy product
            6. Sell product (for sellers only)
            7. Show price history
            8. Exit
            """);

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {
                    loadProductsFromCSV("list_of_products.csv");
                    listItems();
                }
                case "2" -> searchItems();
                case "3" -> {
                    ensureLoggedIn("admin");
                    if (currentUser != null && currentUser.getRole().equalsIgnoreCase("admin")) {
                        addProduct();
                    }
                }
                case "4" -> {
                    ensureLoggedIn("seller");
                    if (currentUser != null && currentUser.getRole().equalsIgnoreCase("seller")) {
                        manageSellerOffers();
                    }
                }
                case "5" -> buyItem();
                case "6" -> {
                    ensureLoggedIn("seller");
                    if (currentUser != null && currentUser.getRole().equalsIgnoreCase("seller")) {
                        sellItem();
                    }
                }
                case "7" -> showHistory();
                case "8" -> {
                    log.info("Exiting...");
                    return;
                }
                default -> log.severe("Invalid option.");
            }
        }
    }
    private void ensureLoggedIn(String requiredRole) {
        if (currentUser == null || !currentUser.getRole().equalsIgnoreCase(requiredRole)) {
            currentUser = null; // 💡 сбрасываем, если роль не подходит
            login();
        }
    }

    private void login() {

        //if (!scanner.hasNextLine()) {
            //log.severe("No input available — exiting.");
            //return;
        //}
        while (true) {
            log.severe("Enter login:");
            String login = scanner.nextLine().trim();

            log.severe("Enter password:");
            String password = scanner.nextLine().trim();

            for (Authorized_Users user : authorized_users) {
                if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                    currentUser = user;
                    log.info("Logged in as: " + currentUser.getLogin());
                    return;
                }
            }

            log.severe("Invalid credentials.");
        }
    }

    public void addProduct() {

        if (!currentUser.getRole().equalsIgnoreCase("admin")) {
            log.severe("Access denied. Only admins can add products.");
            return;
        }

        log.severe("Product ID:");
        String id = scanner.nextLine().trim();

        log.severe("Product name:");
        String name = scanner.nextLine();

        log.severe("Product category:");
        String category = scanner.nextLine();

        log.severe("Initial price:");
        double price = Double.parseDouble(scanner.nextLine());
        if (price <= 0) {
            log.severe("Price must be greater than 0.");
            return;
        }

        log.severe("Initial quantity:");
        int quantity = Integer.parseInt(scanner.nextLine());
        if (quantity <= 0) {
            log.severe("Quantity must be greater than 0.");
            return;
        }

        //ProductOffer offer = ProductOffer.builder()
                //.seller(currentUser.getLogin())
                //.price(price)
                //.quantity(quantity)
                //.priceHistory(new ArrayList<>(List.of(price)))
                //.build();

        market.addProductModel(id, name, category, quantity);
        log.info("New product added.");
    }

    public void sellItem() {
        if (!currentUser.getRole().equalsIgnoreCase("seller")) {
            log.severe("Access denied. Only sellers can sell products.");
            return;
        }

        log.severe("Enter product name:");
        String name = scanner.nextLine();

        ProductOffer existingOffer = market.getOffer(name, currentUser.getLogin());

        if (existingOffer != null) {
            log.warning("You already have an offer for this product.");
            log.severe("""
            What would you like to do?
            1. Update existing offer
            2. Cancel
            """);

            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                log.severe("Enter new quantity:");
                int quantity = Integer.parseInt(scanner.nextLine());

                log.severe("Enter new price:");
                double price = Double.parseDouble(scanner.nextLine());

                boolean success = market.updateOffer(name, currentUser.getLogin(), quantity, price);
                if (success) {
                    log.info("Offer updated.");
                } else {
                    log.severe("Failed to update your offer.");
                }
            } else {
                log.info("Operation cancelled.");
            }

        } else {
            log.severe("Enter quantity to offer:");
            int quantity = Integer.parseInt(scanner.nextLine());
            if (quantity <= 0) {
                log.severe("Quantity must be greater than 0.");
                return;
            }

            log.severe("Enter price:");
            double price = Double.parseDouble(scanner.nextLine());
            if (price <= 0) {
                log.severe("Price must be greater than 0.");
                return;
            }

            ProductOffer offer = ProductOffer.builder()
                    .seller(currentUser.getLogin())
                    .quantity(quantity)
                    .price(price)
                    .priceHistory(new ArrayList<>(List.of(price)))
                    .build();

            boolean success = market.addOfferToExistingProduct(name, offer);
            if (success) {
                log.info("New offer created.");
            } else {
                log.severe("Product not found or error while adding offer.");
            }
        }
    }

    private void listItems() {
        List<ProductModel> models = market.listAllModels();
        if (models.isEmpty()) {
            log.info("No products available.");
            return;
        }
        for (ProductModel model : models) {
            log.info(model.toString());
            for (ProductOffer offer : model.getOffers()) {
                log.info("  -> " + offer);
            }
        }
    }

    private void searchItems() {
        log.severe("Enter search term:");
        String query = scanner.nextLine().trim();

        List<ProductModel> results = market.searchModels(query);
        if (results.isEmpty()) {
            log.info("No matching products found.");
            return;
        }

        for (ProductModel model : results) {
            log.info("Product: " + model.getName() + " | Category: " + model.getCategory());
            for (ProductOffer offer : model.getOffers()) {
                log.info("  -> Seller: " + offer.getSeller() + ", Price: " + offer.getPrice() + ", Quantity: " + offer.getQuantity());
            }
        }
    }

    public void buyItem() {
        log.severe("Enter product name:");
        String name = scanner.nextLine().trim();

        List<ProductOffer> offers = market.getOffersByNameOrCategory(name);
        if (offers.isEmpty()) {
            log.severe("No offers found for this product.");
            return;
        }

        log.info("Available sellers for product: " + name);
        for (ProductOffer offer : offers) {
            log.info("  - " + offer.getSeller() + ": " +
                    String.format("%.2f€", offer.getPrice()) +
                    ", " + offer.getQuantity() + " units");
        }

        log.severe("Enter seller name:");
        String seller = scanner.nextLine();

        log.severe("Enter quantity to buy:");
        int amount = Integer.parseInt(scanner.nextLine());
        if (amount <= 0) {
            log.severe("Quantity must be greater than 0.");
            return;
        }

        boolean success = market.buyFromOffer(name, seller, amount);
        if (success) {
            log.info("Purchase successful.");
        } else {
            log.severe("Purchase failed.");
        }
    }

    private void showHistory() {
        log.severe("Enter product name:");
        String name = scanner.nextLine();
        ProductModel model = market.findModelByName(name);

        if (model == null) {
            log.warning("Product not found.");
            return;
        }

        if (model.getOffers() == null || model.getOffers().isEmpty()) {
            log.info("This product has no offers yet.");
            return;
        }

        List<Double> history = model.getPriceHistory();
        if (history == null || history.isEmpty()) {
            log.info("No sales yet. Price history is empty.");
        } else {
            log.info("Recent market prices for \"" + name + "\": " + history);
        }
    }

    private void manageSellerOffers() {
        if (!currentUser.getRole().equalsIgnoreCase("seller")) {
            log.severe("Access denied. Only sellers can manage their offers.");
            return;
        }

        List<ProductModel> allProducts = market.listAllModels();
        List<ProductModel> ownedProducts = new ArrayList<>();

        for (ProductModel model : allProducts) {
            boolean hasOwnOffer = model.getOffers().stream()
                    .anyMatch(offer -> offer.getSeller().equalsIgnoreCase(currentUser.getLogin()));
            if (hasOwnOffer) {
                ownedProducts.add(model);
            }
        }

        if (ownedProducts.isEmpty()) {
            log.info("You have no active offers.");
            return;
        }

        log.info("Your current offers:");
        for (ProductModel model : ownedProducts) {
            ProductOffer offer = market.getOffer(model.getName(), currentUser.getLogin());
            log.info("Product: " + model.getName() + " | Your offer -> " + offer);
        }

        log.severe("Enter product name to manage:");
        String productName = scanner.nextLine().trim();

        ProductOffer offer = market.getOffer(productName, currentUser.getLogin());
        if (offer == null) {
            log.severe("No offer found for this product.");
            return;
        }

        log.severe("""
            What would you like to change?
            1. Update quantity
            2. Update price
            3. Cancel
            """);
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                log.severe("Enter additional quantity to add:");
                int qty = Integer.parseInt(scanner.nextLine());
                if (qty <= 0) {
                    log.severe("Quantity must be positive.");
                    return;
                }
                boolean success = market.updateOffer(productName, currentUser.getLogin(), qty, offer.getPrice());
                if (success) {
                    log.info("Quantity updated.");
                } else {
                    log.severe("Failed to update quantity.");
                }
            }
            case "2" -> {
                log.severe("Enter new price:");
                double newPrice = Double.parseDouble(scanner.nextLine());
                if (newPrice <= 0) {
                    log.severe("Price must be positive.");
                    return;
                }
                boolean success = market.updateOffer(productName, currentUser.getLogin(), 0, newPrice);
                if (success) {
                    log.info("Price updated.");
                } else {
                    log.severe("Failed to update price.");
                }
            }
            case "3" -> log.info("Cancelled.");
            default -> log.severe("Invalid input.");
        }
    }

    public void loadProductsFromCSV(String resourcePath) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(resourcePath))))) {

            br.readLine(); // пропустить заголовок
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;

                String id = p[0].trim();
                String name = p[1].trim();
                String category = p[2].trim();
                String seller = p[3].trim();
                double price = Double.parseDouble(p[4].trim());
                int quantity = Integer.parseInt(p[5].trim());

                if (price <= 0 || quantity <= 0) continue;

                ProductOffer offer = ProductOffer.builder()
                        .seller(seller)
                        .price(price)
                        .quantity(quantity)
                        .priceHistory(new ArrayList<>(List.of(price)))
                        .build();

                ProductModel model = market.listAllModels().stream()
                        .filter(m -> m.getName().equalsIgnoreCase(name))
                        .findFirst()
                        .orElse(null);

                if (model == null)
                    market.addProductModel(id, name, category, quantity); // передаем id
                else
                    market.addOfferToExistingProduct(name, offer);
            }

            log.info("Products loaded from resource: " + resourcePath);

        } catch (Exception e) {
            log.severe("Failed to load CSV from resources: " + e.getMessage());
        }
    }
}