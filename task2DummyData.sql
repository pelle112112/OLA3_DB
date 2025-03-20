-- Husk at udkommentere korrekt og køre dem én ad gangen.
 /*
-- Indsæt dummy data i Orders-tabellen
INSERT INTO Orders (customer_id, order_date) VALUES
(1, '2024-03-01'),
(2, '2024-03-02'),
(3, '2024-03-03'),
(1, '2024-03-04'),
(2, '2024-03-05');
*/ /*
-- Indsæt dummy data i OrderDetails-tabellen
INSERT INTO OrderDetails (order_id, product_id, quantity, price) VALUES
(21, 101, 2, 19.99),  -- Order 1
(21, 102, 1, 49.99),
(22, 103, 3, 15.50),  -- Order 2
(22, 104, 2, 9.99),
(23, 101, 1, 19.99),  -- Order 3
(23, 105, 5, 5.99),
(24, 102, 2, 49.99),  -- Order 4
(25, 103, 4, 15.50);  -- Order 5
*/ /*
-- Opdater total_amount i Orders-tabellen efter denormalisering
UPDATE Orders o
SET total_amount = (
    SELECT SUM(quantity * price)
    FROM OrderDetails
    WHERE order_id = o.order_id
);
*/