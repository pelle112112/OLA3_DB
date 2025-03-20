-- Exercise 1: Optimizing a Subquery into a Join
/*
 EXPLAIN
SELECT o.order_id,
       o.total_amount,
       c.name AS customer_name
FROM Orders o
JOIN Customers c ON o.customer_id = c.customer_id
WHERE o.total_amount > 100;


*/ -- Exercise 2: Optimizing a JOIN by Using an Index
/*
CREATE INDEX idx_orders_customer ON Orders(customer_id);
CREATE INDEX idx_orders_date ON Orders(order_date);

EXPLAIN
SELECT o.order_id,
       o.total_amount,
       c.name
FROM Orders o
JOIN Customers c ON o.customer_id = c.customer_id
WHERE o.order_date > '2023-01-01';
*/ -- Exercise 3: Identifying and Fixing the N+1 Query Problem

SELECT o.order_id,
       o.customer_id,
       c.name AS customer_name
FROM Orders o
JOIN Customers c ON o.customer_id = c.customer_id;


WHERE o.order_date > '2023-01-01';