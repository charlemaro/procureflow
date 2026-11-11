namespace ordermanagement;

entity Customers {
    key ID   : UUID;
        name : String(100);
        email: String(150);
}

entity Products {
    key ID          : UUID;
        name        : String(100);
        description : String(500);
        price       : Decimal(10,2);
        stock       : Integer;
}

entity Orders {
    key ID          : UUID;
        orderNumber : String(30);
        status      : String(20);
        customer    : Association to Customers;

        items       : Composition of many OrderItems
            on items.order = $self;

        payments    : Association to many Payments
                on payments.order = $self;
        totalAmount : Decimal(12,2);
}

entity OrderItems {
    key ID : UUID;

        order    : Association to Orders;
        product  : Association to Products;
        quantity : Integer;
        unitPrice : Decimal(10,2);
}

entity Payments  {
    key ID : UUID;
        amount : Decimal(10,2);
        status : String(20) enum {
            PENDING;
            SUCCESS;
            FAILED;
        };
        order : Association to Orders;
        refunds : Association  to many Refunds
        on refunds.payment = $self;
}

entity Refunds {
    key ID     : UUID;
        amount : Decimal(10,2);
        status : String(20) enum{
            PENDING;
            SUCCESS;
            FAILED;
        };
        payment : Association  to one Payments;
}