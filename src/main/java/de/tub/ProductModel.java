package de.tub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a product defined by the exchange (admin).
 * Offers are submitted by sellers and only contain price + quantity.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductModel {
    private String id;            // Assigned by the exchange
    private String name;          // Assigned by the exchange
    private String category;      // Assigned by the exchange
    private int totalQuantity;    // Managed by the exchange

    private List<ProductOffer> offers; // Submitted by sellers (price + quantity)

    private List<Double> priceHistory = new ArrayList<>(); // 💡 stores last 3 prices

    /**
     * Calculates the average price of all current offers (used as market price).
     */
    public double getMarketPrice() {
        return offers.stream()
                .mapToDouble(ProductOffer::getPrice)
                .average()
                .orElse(0.0);
    }

    /**
     * Adds a new price to the history and maintains only last 3 entries.
     */
    public void addPriceToHistory(double price) {
        priceHistory.add(price);
        if (priceHistory.size() > 3) {
            priceHistory.remove(0);
        }
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Product: %s | Category: %s | Market Price: %.2f€ | Offers: %d | Total Quantity: %d",
                id, name, category, getMarketPrice(), offers.size(), totalQuantity);
    }

    public boolean addOffer(ProductOffer offer) {
        // Защита от дублирующего продавца
        boolean exists = offers.stream()
                .anyMatch(o -> o.getSeller().equalsIgnoreCase(offer.getSeller()));
        if (exists) return false;

        offers.add(offer);
        return true;
    }
}

