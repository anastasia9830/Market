package de.tub;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductModelTest {

    @Test
    void getMarketPrice_shouldReturnZeroWhenNoOffers() {
        ProductModel model = ProductModel.builder()
                .name("Banana")
                .offers(new ArrayList<>())
                .build();

        assertEquals(0.0, model.getMarketPrice(), 0.0001);
    }

    @Test
    void getMarketPrice_shouldReturnSingleOfferPrice() {
        ProductModel model = ProductModel.builder()
                .name("Banana")
                .offers(new ArrayList<>(List.of(
                        new ProductOffer("alice", 1.5, 10, List.of(1.5))
                )))
                .build();

        assertEquals(1.5, model.getMarketPrice(), 0.0001);
    }

    @Test
    void getMarketPrice_shouldReturnAverageOfMultipleOffers() {
        ProductModel model = ProductModel.builder()
                .name("Banana")
                .offers(new ArrayList<>(List.of(
                        new ProductOffer("alice", 1.0, 10, List.of(1.0)),
                        new ProductOffer("bob", 2.0, 5, List.of(2.0))
                )))
                .build();

        assertEquals(1.5, model.getMarketPrice(), 0.0001);
    }

    @Test
    void addOffer_shouldAddNewSeller() {
        ProductModel model = ProductModel.builder()
                .offers(new ArrayList<>())
                .build();

        boolean added = model.addOffer(new ProductOffer("alice", 1.0, 10, List.of(1.0)));
        assertTrue(added);
        assertEquals(1, model.getOffers().size());
    }

    @Test
    void category_shouldBeCorrectlyStored() {
        ProductModel model = ProductModel.builder()
                .name("Milk")
                .category("Dairy")
                .offers(new ArrayList<>())
                .build();

        assertEquals("Dairy", model.getCategory());
    }
}


