package de.tub;

import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Core marketplace logic using models and offers.
 * Each product model can have multiple offers (from different sellers).
 */
@Log
public class Market {

    private final List<ProductModel> models = new ArrayList<>();

    /**
     * Admin-only: adds a new product model with the first offer.
     */
    public void addProductModel(String id, String name, String category, int totalQuantity) {
        ProductModel model = findModelByName(name);

        if (model != null) {
            log.warning("Product already exists. Admin cannot add duplicate product.");
            return;
        }

        ProductModel newModel = ProductModel.builder()
                .id(id)
                .name(name)
                .category(category)
                .totalQuantity(totalQuantity)
                .offers(new ArrayList<>()) // пусто — офферы только от продавцов
                .build();

        models.add(newModel);
    }


    /**
     * Lists all product models.
     */
    public List<ProductModel> listAllModels() {
        return models;
    }

    /**
     * Searches product models by name or category.
     */
    public List<ProductModel> searchModels(String query) {
        return models.stream()
                .filter(m -> m.getName().toLowerCase().contains(query.toLowerCase())
                        || m.getCategory().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    /**
     * Returns a specific offer by model name and seller.
     */
    public ProductOffer getOffer(String productName, String seller) {
        ProductModel model = findModelByName(productName);
        if (model == null) return null;

        return model.getOffers().stream()
                .filter(o -> o.getSeller().equalsIgnoreCase(seller))
                .findFirst()
                .orElse(null);
    }

    /**
     * Handles a purchase from a seller's offer.
     */
    public boolean buyFromOffer(String productName, String seller, int amount) {
        if (amount <= 0) {
            log.warning("Invalid amount: must be positive.");
            return false;
        }

        ProductOffer offer = getOffer(productName, seller);
        if (offer == null || offer.getQuantity() < amount) {
            log.warning("Insufficient stock or offer not found.");
            return false;
        }

        int previousQty = offer.getQuantity();
        offer.setQuantity(previousQty - amount);

        double newPrice = PriceCalculator.calculateNewPrice(offer.getPrice(), amount, previousQty);
        offer.setPrice(newPrice);

        ProductModel model = findModelByName(productName);
        if (model != null) {
            model.addPriceToHistory(newPrice);
        }

        return true;
    }

    /**
     * Seller can create or update their own offer for an existing product.
     * If offer already exists, it's updated. Else, it's created.
     */
    public boolean updateOffer(String productName, String seller, int addedQuantity, double newPrice) {


        ProductModel model = findModelByName(productName);
        if (model == null) {
            log.warning("Product not found.");
            return false;
        }

        ProductOffer existing = model.getOffers().stream()
                .filter(o -> o.getSeller().equalsIgnoreCase(seller))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            // 🔒 Проверка на недопустимое уменьшение количества
            if (addedQuantity < -existing.getQuantity()) {
                log.warning("Cannot reduce quantity below 0.");
                return false;
            }

            // 🔁 Обновление количества и цены
            existing.setQuantity(existing.getQuantity() + addedQuantity);
            existing.setPrice(newPrice);
            updatePriceHistory(existing, newPrice);
            log.info("Offer updated.");
        } else {
            // ➕ Создание нового оффера
            if (addedQuantity <= 0) {
                log.warning("Initial quantity must be positive.");
                return false;
            }

            ProductOffer newOffer = ProductOffer.builder()
                    .seller(seller)
                    .price(newPrice)
                    .quantity(addedQuantity)
                    .priceHistory(new ArrayList<>(List.of(newPrice)))
                    .build();

            return model.addOffer(newOffer);
        }

        return true;
    }

    /**
     * Adds a new offer to an existing product (used in sellItem when no previous offer exists).
     */
    public boolean addOfferToExistingProduct(String name, ProductOffer offer) {
        ProductModel model = findModelByName(name);
        if (model == null) return false;

        return model.addOffer(offer); // вызываем метод из ProductModel
    }

    /**
     * Returns price history of a specific seller's offer.
     */
    public List<Double> getOfferPriceHistory(String productName, String seller) {
        ProductOffer offer = getOffer(productName, seller);
        return (offer != null) ? offer.getPriceHistory() : null;
    }

    /**
     * Keeps only the last 3 price entries.
     */
    private void updatePriceHistory(ProductOffer offer, double newPrice) {
        List<Double> history = offer.getPriceHistory();
        if (history.size() >= 3) {
            history.remove(0);
        }
        history.add(newPrice);
    }

    /**
     * Finds product model by name (case-insensitive).
     */
    public ProductModel findModelByName(String name) {
        return models.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<ProductOffer> getOffersByNameOrCategory(String query) {
        return models.stream()
                .filter(m -> m.getName().equalsIgnoreCase(query) || m.getCategory().equalsIgnoreCase(query))
                .flatMap(m -> m.getOffers().stream())
                .toList();
    }
}
