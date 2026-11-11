    using {ordermanagement as db} from  '../db/schema';

    service OrderService {

        entity Customers as projection on db.Customers;

        entity Products as projection on db.Products;

        entity Orders as  projection on db.Orders;

        entity OrderItems as  projection on db.OrderItems;

        entity Payments as projection on db.Payments;

        entity Refunds as projection on db.Refunds;

    }