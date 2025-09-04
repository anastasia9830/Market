package de.tub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents an individual offer from a seller for a specific product model.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductOffer {
    private String seller;
    private double price;
    private int quantity;
    private List<Double> priceHistory;

    @Override
    public String toString() {
        return String.format("Seller: %s | Price: %.2f€ | Quantity: %d", seller, price, quantity);
    }
}
