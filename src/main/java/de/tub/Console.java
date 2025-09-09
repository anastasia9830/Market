package de.tub;

import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Log
public class Console {

    // --- fields ---
    private final Market market;
    private final java.util.Scanner scanner;
    private Authorized_Users currentUser;

    // authorized users (sample data)
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

    // --- constructors ---
    public Console() {
        this(new Market(), new java.util.Scanner(System.in));
    }

    // constructor used by tests
    public Console(Market market, java.util.Scanner scanner) {
        this.market = Objects.requireNonNull(market);
        this.scanner = Objects.requireNonNull(scanner);
    }

    // --- API used by tests ---
    public void setCurrentUser(Authorized_Users user) { this.currentUser = user; }

    // input order (lines): id \n name \n category \n price \n qty
    // Lines OR one-line: "id name category price qty" or "id,name,category,price,qty"
public void addProduct() {
    String[] t = readFlexibleTokens(5);
    String id = t[0].trim();
    String name = t[1].trim();
    String category = t[2].trim();
    double price = parseDoubleUS(t[3].trim());
    int qty = Integer.parseInt(t[4].trim());

    market.addProductModel(id, name, category, 0);

    ProductOffer stock = ProductOffer.builder()
            .seller("Stock")
            .price(price)
            .quantity(qty)
            //.priceHistory(new ArrayList<>(List.of(price)))
            .build();
    market.addOfferToExistingProduct(name, stock);
}

// Lines OR one-line: "product qty price" or "product,qty,price"
// Accepts: "product qty price" OR "product price qty", in one line or three lines.
public void sellItem() {
    String[] t = readFlexibleTokens(3);
    String product = t[0].trim();
    String a = t[1].trim();
    String b = t[2].trim();

    // try to understand which is qty (int) and which is price (double)
    Integer qtyA = tryParseInt(a);
    Integer qtyB = tryParseInt(b);
    Double  priceA = tryParseDoubleUS(a);
    Double  priceB = tryParseDoubleUS(b);

    int qty;
    double price;

    if (qtyA != null && priceB != null) {        // product, qty, price
        qty = qtyA;  price = priceB;
    } else if (qtyB != null && priceA != null) { // product, price, qty
        qty = qtyB;  price = priceA;
    } else {
        // fallback heuristics
        if (a.contains(".") || a.contains(",")) { // looks like price
            price = priceA != null ? priceA : 0.0;
            qty   = qtyB != null ? qtyB : (int)Math.round(priceB != null ? priceB : 0.0);
        } else {
            price = priceB != null ? priceB : 0.0;
            qty   = qtyA != null ? qtyA : (int)Math.round(priceA != null ? priceA : 0.0);
        }
    }

    ProductOffer offer = ProductOffer.builder()
            .seller(currentUser != null ? currentUser.getLogin() : "unknown")
            .price(price)
            .quantity(qty)
            //.priceHistory(new ArrayList<>(List.of(price)))
            .build();

    boolean ok = market.addOfferToExistingProduct(product, offer);
    if (!ok) {
        market.updateOffer(product, offer.getSeller(), qty, price);
    }
}


// Lines OR one-line: "product seller qty" or "product,seller,qty"
public void buyItem() {
    String[] t = readFlexibleTokens(3); // если этого хелпера нет — можно оставить твоё чтение построчно
    String product = t[0].trim();
    String seller  = t[1].trim();
    int qty        = Integer.parseInt(t[2].trim());

    boolean ok = market.buyFromOffer(product, seller, qty);
    if (ok) {
        System.out.println("[OK] Bought " + qty + " of " + product + " from " + seller);
        ProductOffer o = market.getOffer(product, seller);
        if (o != null) {
            System.out.println("[INFO] New listed price: " + o.getPrice() + ", remaining qty: " + o.getQuantity());
            System.out.println("[INFO] Offer price history: " + o.getPriceHistory());
        }
    } else {
        // объясняем причину
        ProductModel m = market.findModelByName(product);
        if (m == null) {
            System.out.println("[FAIL] Product not found: " + product);
        } else {
            ProductOffer o = market.getOffer(product, seller);
            if (o == null) {
                System.out.println("[FAIL] Seller offer not found: " + seller + " for " + product);
                // Для удобства: покажем доступных продавцов
                System.out.println("[HINT] Available sellers: " + m.getOffers().stream().map(ProductOffer::getSeller).toList());
            } else if (qty <= 0) {
                System.out.println("[FAIL] Quantity must be positive.");
            } else if (o.getQuantity() < qty) {
                System.out.println("[FAIL] Not enough stock in seller offer. Available: " + o.getQuantity());
            } else {
                System.out.println("[FAIL] Purchase failed (unknown reason).");
            }
        }
    }
}



    // --- interactive console (not used by tests, but handy for real runs) ---

    public void start() {
        loadProductsFromCSV("list_of_products.csv");
        clearScreen();
        printBanner();

        while (true) {
            printMenu();
            int choice = readMenuChoice(1, 9);

            switch (choice) {
                case 1 -> {
                    printlnInfo("You chose: List all products");
                    listItems();
                    promptEnterToContinue();
                }
                case 2 -> {
                    printlnInfo("You chose: Search products");
                    searchItems();
                    promptEnterToContinue();
                }
                case 3 -> {
                    printlnInfo("You chose: Add product (admin)");
                    ensureLoggedIn("admin");
                    if (isRole("admin")) {
                        printlnHint("Enter, line by line: id, name, category, price, qty");
                        addProduct();
                    } else {
                        printlnError("Access denied (admin required).");
                    }
                    promptEnterToContinue();
                }
                case 4 -> {
                    printlnInfo("You chose: Manage your offers (seller)");
                    ensureLoggedIn("seller");
                    if (isRole("seller")) {
                        manageSellerOffers();
                    } else {
                        printlnError("Access denied (seller required).");
                    }
                    promptEnterToContinue();
                }
                case 5 -> {
                    printlnInfo("You chose: Buy product");
                    printlnHint("Enter, line by line: product, seller, how much do you want to buy?");
                    buyItem();
                    promptEnterToContinue();
                }
                case 6 -> {
                    printlnInfo("You chose: Sell product (seller)");
                    ensureLoggedIn("seller");
                    if (isRole("seller")) {
                        printlnHint("Enter, line by line: product, qty, price");
                        sellItem();
                    } else {
                        printlnError("Access denied (seller required).");
                    }
                    promptEnterToContinue();
                }
                case 7 -> {
                    printlnInfo("You chose: Show price history");
                    showHistory();
                    promptEnterToContinue();
                }
                case 8 -> {
                    printlnInfo("You chose: Login");
                    login();
                    promptEnterToContinue();
                }
                case 9 -> {
                    printlnInfo("Bye!");
                    return;
                }
                default -> printlnError("Unknown option.");
            }
            clearScreen();
        }
    }

    private boolean isRole(String role) {
        return currentUser != null && role.equalsIgnoreCase(currentUser.getRole());
    }

    private void ensureLoggedIn(String requiredRole) {
        if (!isRole(requiredRole)) login();
    }

    private void login() {
        System.out.print("Login: ");
        String login = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        currentUser = authorized_users.stream()
                .filter(u -> u.getLogin().equals(login) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (currentUser == null) {
            System.out.println("Invalid credentials.");
        } else {
            System.out.println("Logged in as " + currentUser.getLogin() + " (" + currentUser.getRole() + ")");
        }
    }

    private void listItems() {
        List<ProductModel> models = market.listAllModels();
        if (models.isEmpty()) {
            System.out.println("(no items)");
            return;
        }
        for (ProductModel m : models) {
            System.out.println(m);
            for (ProductOffer o : m.getOffers()) System.out.println("  -> " + o);
        }
    }

    private void searchItems() {
        System.out.print("Search by name or category: ");
        String q = scanner.nextLine().trim();

        // safer: rely on Market.searchModels(q) and then list offers
        List<ProductModel> results = market.searchModels(q);
        if (results == null || results.isEmpty()) {
            System.out.println("No results.");
            return;
        }
        System.out.println("Found:");
        for (ProductModel model : results) {
            System.out.println("  Product: " + model.getName() + " | Category: " + model.getCategory());
            if (model.getOffers() != null) {
                for (ProductOffer o : model.getOffers()) {
                    System.out.println("    -> Seller: " + o.getSeller()
                            + ", Price: " + o.getPrice()
                            + ", Quantity: " + o.getQuantity());
                }
            }
        }
    }

    private void showHistory() {
        System.out.print("Product name: ");
        String name = scanner.nextLine().trim();
        ProductModel model = market.findModelByName(name);
        if (model == null) {
            System.out.println("Not found.");
            return;
        }
        if (model.getOffers() == null || model.getOffers().isEmpty()) {
            System.out.println("No offers yet.");
            return;
        }
        System.out.println("Recent price history by offer:");
        for (ProductOffer o : model.getOffers()) {
            List<Double> hist = o.getPriceHistory();
            String s = (hist == null || hist.isEmpty())
                    ? "[]"
                    : hist.stream().map(d -> String.format(Locale.US, "%.2f", d)).collect(Collectors.joining(", ", "[", "]"));
            System.out.printf("  %s: %s%n", o.getSeller(), s);
        }
    }

    private void manageSellerOffers() {
        if (!isRole("seller")) {
            System.out.println("Access denied (seller only).");
            return;
        }
        System.out.print("Product: ");
        String product = scanner.nextLine().trim();
        System.out.print("New price: ");
        double price = Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
        System.out.print("Add quantity: ");
        int qty = Integer.parseInt(scanner.nextLine().trim());

        boolean ok = market.updateOffer(product, currentUser.getLogin(), qty, price);
        if (!ok) {
            ProductOffer offer = ProductOffer.builder()
                    .seller(currentUser.getLogin())
                    .price(price)
                    .quantity(qty)
                    //.priceHistory(new ArrayList<>(List.of(price)))
                    .build();
            market.addOfferToExistingProduct(product, offer);
        }
    }

    // CSV format: id,name,category,seller,price,quantity (with header)
    public void loadProductsFromCSV(String resourcePath) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(resourcePath))))) {

            String line;
            int lineNo = 0;

            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) continue;

                // support both comma and semicolon as delimiter
                String[] p = line.split(",");
                if (p.length < 6) p = line.split(";");
                if (p.length < 6) {
                    log.warning("Skip line " + lineNo + ": not enough columns");
                    continue;
                }

                // strip quotes and BOM
                for (int i = 0; i < p.length; i++) p[i] = unquote(p[i]);

                // skip header
                if (isHeaderRow(p)) continue;

                String id       = p[0].trim();
                String name     = p[1].trim();
                String category = p[2].trim();
                String seller   = p[3].trim();

                Double price = parseDoubleSafe(p[4]);
                Integer quantity = parseIntSafe(p[5]);

                if (price == null || quantity == null || price <= 0 || quantity <= 0) {
                    log.warning("Skip line " + lineNo + ": bad price/quantity");
                    continue;
                }

                if (market.findModelByName(name) == null) {
                    market.addProductModel(id, name, category, 0);
                }

                ProductOffer offer = ProductOffer.builder()
                        .seller(seller)
                        .price(price)
                        .quantity(quantity)
                        .priceHistory(new ArrayList<>(List.of(price)))
                        .build();

                market.addOfferToExistingProduct(name, offer);
            }

            log.info("Products loaded from: " + resourcePath);
        } catch (Exception e) {
            log.severe("Failed to load CSV: " + e.getMessage());
        }
    }

    // ------- helpers -------
    private static boolean isHeaderRow(String[] p) {
        return p[0].equalsIgnoreCase("id")
                || p[1].equalsIgnoreCase("name")
                || p[2].equalsIgnoreCase("category")
                || p[3].equalsIgnoreCase("seller")
                || p[4].equalsIgnoreCase("price")
                || p[5].equalsIgnoreCase("quantity");
    }

    private static String unquote(String s) {
        if (s == null) return "";
        s = s.replace("\uFEFF", "").trim(); // remove BOM
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static Double parseDoubleSafe(String s) {
        if (s == null) return null;
        s = unquote(s).replace(',', '.').trim();
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private static Integer parseIntSafe(String s) {
        if (s == null) return null;
        s = unquote(s).trim();
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    // UI helpers
    private void printBanner() {
        System.out.println("""
            ==========================================
                     Welcome to the Market
            ==========================================
            """);
    }

    private void printMenu() {
        System.out.println("""
            What do you want to do?
              1) List all products
              2) Search products
              3) Add product (admin)
              4) Manage your offers (seller)
              5) Buy product
              6) Sell product (seller)
              7) Show price history
              8) Login
              9) Exit
            """);
        if (currentUser != null) {
            System.out.println("Current user: " + currentUser.getLogin()
                    + " [" + currentUser.getRole() + "]");
        } else {
            System.out.println("You are not logged in. Some actions will require login.");
        }
        System.out.print("\nYour choice (1-9): ");
    }

    /** Safe menu input (bounded) */
    private int readMenuChoice(int min, int max) {
        while (true) {
            String raw = scanner.nextLine().trim();
            try {
                int n = Integer.parseInt(raw);
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException ignored) {}
            System.out.print("Enter a number from " + min + " to " + max + ": ");
        }
    }

    /** “Press Enter to continue…” pause */
    private void promptEnterToContinue() {
        System.out.print("\nPress Enter to continue…");
        scanner.nextLine();
    }

    /** Clear terminal via ANSI (works in most terminals) */
    private void clearScreen() {
        try {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        } catch (Exception ignored) {}
    }

    /** Message helpers */
    private void printlnInfo(String msg)  { System.out.println("[INFO] " + msg); }
    private void printlnError(String msg) { System.out.println("[ERROR] " + msg); }
    private void printlnHint(String msg)  { System.out.println("[HINT] " + msg); }

// Flexible tokenizer: reads one or more lines until it has 'expected' tokens.
// Accepts commas/semicolons/spaces as separators. If a line already contains
// all tokens (like "a,b,c"), it overrides previously collected partial input.


// Remove ANSI escape sequences (arrow keys, etc.) and BOM if present.
// Locale-agnostic parsing helpers
private static double parseDoubleUS(String s) {
    return Double.parseDouble(s.replace(',', '.'));
}

// --- parsing helpers ---
private static String sanitizeLine(String s) {
    if (s == null) return "";
    s = s.replaceAll("\\x1B\\[[;?0-9]*[A-Za-z]", ""); // strip ANSI
    s = s.replace("\u001B", ""); // ESC
    s = s.replace("\uFEFF", ""); // BOM
    return s;
}

private static List<String> splitTokens(String line) {
    line = sanitizeLine(line);
    String[] parts = line.trim().split("[,;\\s]+");
    List<String> out = new ArrayList<>();
    for (String p : parts) if (!p.isEmpty()) out.add(p);
    return out;
}

private static Integer tryParseInt(String s) {
    try { return Integer.valueOf(s.trim()); } catch (Exception e) { return null; }
}

private static Double tryParseDoubleUS(String s) {
    try { return Double.valueOf(s.trim().replace(',', '.')); } catch (Exception e) { return null; }
}

/** Reads tokens from either one line ("a,b,c") or multiple lines until we have `expected`. */
private String[] readFlexibleTokens(int expected) {
    List<String> acc = new ArrayList<>();
    while (acc.size() < expected) {
        String line = scanner.nextLine();
        List<String> toks = splitTokens(line);
        // If user typed everything on one line, prefer that line alone.
        if ((line.contains(",") || line.contains(";")) && toks.size() >= expected) {
            acc.clear();
            acc.addAll(toks);
            break;
        }
        acc.addAll(toks);
    }
    return acc.subList(0, expected).toArray(new String[0]);
}

}
