# ProcureFlow

### Order Management System

**A backend engineering project built with SAP Cloud Application
Programming Model (CAP) for Java**

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/)
[![Spring
Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot)](https://spring.io/projects/spring-boot)
[![SAP
CAP](https://img.shields.io/badge/SAP%20CAP%20Java-5.2.0-0FA6D5)](https://cap.cloud.sap/)
[![OData](https://img.shields.io/badge/API-OData%20V4-276EF1)](https://www.odata.org/)
[![Build](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven)](https://maven.apache.org/)

*A practical, progressively developed ecommerce backend focused on
correctness, transactional consistency, security, and maintainable
business logic.*
:::

------------------------------------------------------------------------

## 1. Project Overview

**ProcureFlow** is an Order Management System (OMS) designed around a
realistic ecommerce workflow. It models customers, products, orders,
order items, payments, and refunds, and provides an OData V4 service
through SAP CAP Java.

The project is being developed as a modular backend application first.
The initial goal is to implement reliable business workflows inside one
deployable application, understand CAP's persistence and transaction
model, and establish sound domain boundaries. Integrations, messaging,
and service extraction can be introduced later when there is a clear
architectural reason.

The application is intended to demonstrate practical backend engineering
concepts, including:

-   Domain modelling with CDS.
-   Service exposure through OData V4.
-   Custom CAP Java event handlers.
-   Generic CRUD versus custom business operations.
-   Server-side validation and price calculation.
-   Historical order-price snapshots.
-   Inventory consistency and concurrent stock reservation.
-   Database transactions and rollback.
-   Payment and refund lifecycle management.
-   Authentication, authorization, and customer data isolation.
-   Observability, containerization, and cloud deployment.

> **Project status:** Initial CAP project setup, domain model, service
> definition, and Product creation/validation have been completed and
> tested. Order creation is the next major implementation milestone.
> Features described as planned below are not yet claimed as
> implemented.

## 2. Goals and Engineering Principles

### Primary goals

1.  Build a working Order Management backend using SAP CAP Java.
2.  Understand how CDS models map to generated artifacts and database
    structures.
3.  Implement business workflows in Java rather than relying only on
    generated CRUD.
4.  Preserve financial and order history.
5.  Make multi-step operations transactionally consistent.
6.  Prevent race conditions in inventory and refund processing.
7.  Keep the codebase modular without adding abstractions that do not
    solve a real problem.
8.  Prepare the application for eventual deployment to SAP BTP.

### Design principles

-   **Server is authoritative:** the client supplies identifiers and
    requested quantities, not trusted prices, totals, stock values, or
    lifecycle statuses.
-   **Preserve history:** product price changes must not rewrite
    previously placed orders; payment and refund records must not
    disappear when an order is cancelled.
-   **Atomic business operations:** a failed order creation must not
    leave partial order, item, or inventory changes.
-   **Validate at the correct boundary:** use request validation,
    business-rule enforcement, and database constraints for their
    respective purposes.
-   **Concurrency is a correctness requirement:** a stock check followed
    by an ordinary update is not sufficient when multiple customers can
    purchase simultaneously.
-   **Prefer CAP capabilities:** use CAP's service, persistence, and
    transaction abstractions where appropriate.
-   **Keep the architecture proportional:** start as a modular
    application; introduce microservices, interfaces, factories, and
    distributed patterns only when justified.

## 3. Technology Stack

  ----------------------------------------------------------------------------
  Area               Technology        Purpose               Current position
  ------------------ ----------------- --------------------- -----------------
  Language           Java 25           Backend               Configured
                                       implementation        

  Application        Spring Boot 4.1.1 Application bootstrap Configured
  framework                            and Spring            
                                       integration           

  Backend            SAP CAP Java      Services, events,     Configured
  programming model  5.2.0             persistence           
                                       integration,          
                                       transactions          

  Domain modelling   SAP CDS           Entity and            In use
                                       relationship          
                                       definitions           

  API protocol       OData V4          Expose service        In use
                                       entities and          
                                       operations            

  Build              Apache Maven      Multi-module build    Configured
                                       and dependency        
                                       management            

  Local database     H2                Initial local         Dependency
                                       development and       configured
                                       testing               

  Target relational  PostgreSQL        Future database       Planned
  database                             option for            
                                       local/containerized   
                                       deployment            

  SAP cloud platform SAP Business      Intended cloud        Planned
                     Technology        deployment            
                     Platform (BTP)    environment           

  API testing        Postman           Manual endpoint and   In use
                                       validation testing    

  Frontend           Not fixed yet     Future consumer UI,   Not part of
                                       if required           current backend
                                                             milestone

  Messaging          Apache Kafka      Future asynchronous   Planned
                                       domain-event          
                                       integration           

  Containerization   Docker / Docker   Future reproducible   Planned
                     Compose           local infrastructure  

  Observability      Structured logs,  Production            Planned
                     metrics, tracing  diagnostics           
  ----------------------------------------------------------------------------

### Version management

The project currently uses the versions listed above. Do not upgrade
Java, Spring Boot, or SAP CAP independently without checking the
supported compatibility matrix and testing the complete Maven build. CAP
Java APIs used in implementation should be verified against the
project's actual CAP dependency version.

## 4. Architecture

### Current architectural direction

ProcureFlow starts as a **modular backend application**:

``` text
                    Client / Postman
                           |
                           v
                  OData V4 Endpoint
                           |
                           v
                    OrderService
                           |
                 CAP Event Lifecycle
                           |
             +-------------+-------------+
             |                           |
             v                           v
       Event Handlers              CAP Generic CRUD
             |
             v
       Business Services
             |
             v
       CAP Persistence APIs
             |
             v
          Database
          (H2 first)
```

The diagram is a logical view of the intended request flow. The exact
handler chaining, generic-handler interaction, and transaction behavior
must follow the CAP Java 5.2.0 runtime semantics.

### Responsibility boundaries

  -----------------------------------------------------------------------
  Layer                               Responsibility
  ----------------------------------- -----------------------------------
  CDS data model                      Defines entities, fields, keys,
                                      associations, and compositions

  CAP service definition              Exposes selected model entities and
                                      operations

  CAP event handler                   Connects service events to
                                      validation or custom business
                                      operations

  Business service                    Implements domain rules and
                                      coordinates use cases

  CAP persistence APIs                Executes database operations using
                                      CAP query abstractions

  Database                            Enforces durable data, constraints,
                                      and transactional guarantees
  -----------------------------------------------------------------------

### Suggested Java package structure

``` text
srv/src/main/java/
├── customer/
│   └── procureflow/
│       └── ProcureflowApplication.java
│
└── custom/
    ├── handlers/
    │   ├── ProductHandler.java
    │   └── OrderHandler.java
    │
    ├── services/
    │   └── OrderCreationService.java
    │
    └── utils/
        └── ResponseUtils.java
```

This is the proposed organization, not a claim that every class already
exists.

The application class currently uses:

``` java
@SpringBootApplication(
    scanBasePackages = {"customer.procureflow", "custom"}
)
```

This allows Spring component scanning across the application package and
the separate `custom` package. Spring component scanning and CAP service
discovery are related but distinct concerns; both must be verified when
adding handlers.

## 5. Domain Model

The current domain contains six main entities:

1.  Customers
2.  Products
3.  Orders
4.  OrderItems
5.  Payments
6.  Refunds

### Entity relationship overview

``` text
Customers 1 -------- * Orders
                         |
                         | 1
                         |
                         * OrderItems * -------- 1 Products

Orders 1 -------- * Payments
Payments 1 ------ * Refunds
```

-   A customer can place multiple orders.
-   An order belongs to one customer.
-   An order owns one or more order items.
-   Each order item references a product.
-   An order can have multiple payment records or attempts.
-   A payment can have multiple refund records.

### Association versus composition

  -----------------------------------------------------------------------
  Relationship            CDS relationship        Reason
  ----------------------- ----------------------- -----------------------
  Orders → Customers      Association             Customer has an
                                                  independent lifecycle

  Orders → OrderItems     Composition of many     Order owns its line
                                                  items

  OrderItems → Products   Association             Product exists
                                                  independently of an
                                                  order

  Orders → Payments       Association to many     Payment history is
                                                  independently
                                                  meaningful

  Payments → Refunds      Association to many     Refund records preserve
                                                  their own lifecycle
  -----------------------------------------------------------------------

Composition expresses ownership and lifecycle dependency. It should not
be used merely because two entities are related.

### Current CDS schema

File: `db/schema.cds`

``` cds
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

        order     : Association to Orders;
        product   : Association to Products;
        quantity  : Integer;
        unitPrice : Decimal(10,2);
}

entity Payments {
    key ID : UUID;
        amount : Decimal(10,2);
        status : String(20) enum {
            PENDING;
            SUCCESS;
            FAILED;
        };
        order : Association to Orders;

        refunds : Association to many Refunds
            on refunds.payment = $self;
}

entity Refunds {
    key ID     : UUID;
        amount : Decimal(10,2);
        status : String(20) enum {
            PENDING;
            SUCCESS;
            FAILED;
        };
        payment : Association to one Payments;
}
```

This is the latest schema shared during development. Confirm the actual
file before making subsequent changes.

### Important domain modelling decisions

#### Historical purchase prices

Three price-related fields have different meanings:

  ------------------------------------------------------------------------
  Field                    Meaning                 Authority
  ------------------------ ----------------------- -----------------------
  `Products.price`         Current product selling Database
                           price                   

  `OrderItems.unitPrice`   Price at the time the   Backend snapshot
                           order was placed        

  `Orders.totalAmount`     Total calculated for    Backend calculation
                           the order at purchase   
                           time                    
  ------------------------------------------------------------------------

Example:

-   2 MacBooks × ₹1,50,000 = ₹3,00,000
-   1 Mouse × ₹2,000 = ₹2,000
-   Order total = ₹3,02,000

If the MacBook price later changes to ₹1,70,000, the old order must
still show ₹1,50,000 per unit.

The backend must fetch the current product price and calculate the
snapshot. It must ignore client-supplied `unitPrice` and `totalAmount`.

#### Order, payment, and refund statuses

These are separate state machines.

For example:

-   Order status: `CANCELLED`
-   Payment status: `SUCCESS`
-   Refund status: `PENDING`

An order cancellation must not automatically erase or rewrite financial
history. Cancellation and refund processing are separate business
operations.

#### Money representation

Use `BigDecimal` in Java for monetary calculations. Avoid `float` and
`double` for currency.

`Decimal(10,2)` is used for individual prices and `Decimal(12,2)` for
the order total. Precision is the total number of digits, including the
digits after the decimal point. Confirm business limits and database
support before increasing monetary precision.

## 6. Service Definition and API

Service file:

`srv/order-service.cds`

Previously shared service definition:

``` cds
using {ordermanagement} from '../db/schema';

service OrderService {
    entity Customers as projection on ordermanagement.Customers;
    entity Products as projection on ordermanagement.Products;
    entity Orders as projection on ordermanagement.Orders;
    entity OrderItems as projection on ordermanagement.OrderItems;
    entity Payments as projection on ordermanagement.Payments;
}
```

The Refunds projection may still need to be added. Verify the actual
file before assuming it is exposed.

### Expected endpoints

Base URL for local development:

``` text
http://localhost:8080/odata/v4/OrderService
```

  Method   Endpoint            Purpose
  -------- ------------------- --------------------------------
  GET      `/Products`         List products
  GET      `/Products(<id>)`   Retrieve a product
  POST     `/Products`         Create a product
  GET      `/Customers`        List customers
  POST     `/Customers`        Create a customer
  GET      `/Orders`           List orders
  POST     `/Orders`           Create an order
  GET      `/OrderItems`       List order items, if exposed
  GET      `/Payments`         List payments, if exposed
  GET      `/Refunds`          List refunds, if exposed
  GET      `/$metadata`        Inspect OData service metadata

Entity exposure depends on the service definition. These are intended
endpoint shapes; verify generated metadata and actual runtime behavior.

## 7. Implemented Functionality

### Project setup

-   SAP CAP Java Maven project created.
-   Root Maven parent and `srv` child module configured.
-   Java 25, Spring Boot 4.1.1, and CAP Java 5.2.0 configured.
-   CDS Java build executed successfully.
-   OData V4 metadata generated.

### Product creation

A custom Product event handler has been implemented and tested.

The handler uses CAP's `@Before` event to validate Product creation
before generic persistence.

Implemented validation rules:

  -----------------------------------------------------------------------
  Field                               Rule
  ----------------------------------- -----------------------------------
  Product name                        Reject null, empty, or blank values

  Product price                       Reject null, zero, and negative
                                      values

  Product stock                       Intended rule: reject null and
                                      negative values; allow zero and
                                      positive values. Confirm
                                      implementation status.
  -----------------------------------------------------------------------

A utility method creates CAP `ServiceException` instances with
`ErrorStatuses.BAD_REQUEST`. The exception must be thrown to reject the
operation.

### Testing already completed

The user confirmed that:

-   The application starts successfully.
-   The OData metadata endpoint works.
-   Product creation works.
-   Invalid product creation is rejected.

Example valid request:

``` http
POST /odata/v4/OrderService/Products
Content-Type: application/json
```

``` json
{
  "name": "MacBook Pro",
  "description": "Apple laptop",
  "price": 150000.00,
  "stock": 10
}
```

Example invalid request:

``` json
{
  "name": "",
  "description": "Invalid product",
  "price": 1000,
  "stock": 5
}
```

The invalid request should produce an OData error response.

## 8. Order Creation --- Next Major Milestone

The next major use case is:

``` http
POST /odata/v4/OrderService/Orders
```

### Intended client request

``` json
{
  "customer_ID": "customer-uuid",
  "items": [
    {
      "product_ID": "product-uuid-1",
      "quantity": 2
    },
    {
      "product_ID": "product-uuid-2",
      "quantity": 1
    }
  ]
}
```

The client should not decide:

-   Order number.
-   Order status.
-   Unit prices.
-   Order total.
-   Product stock after the purchase.

### Backend responsibilities

1.  Validate the request.
2.  Validate the customer exists.
3.  Require at least one order item.
4.  Validate product identifiers.
5.  Validate that every quantity is greater than zero.
6.  Verify all products exist.
7.  Retrieve current product prices from the database.
8.  Verify sufficient stock.
9.  Calculate and preserve each `unitPrice` snapshot.
10. Calculate item subtotals.
11. Calculate `totalAmount`.
12. Generate a unique order number.
13. Set the initial order status.
14. Persist the order and its items.
15. Deduct stock safely.
16. Ensure all database changes are atomic.

### CAP Java concepts to learn before implementation

Teach and verify the actual CAP Java 5.2.0 APIs for:

-   `PersistenceService`
-   `CqnService`
-   CQN (Core Query Notation)
-   `Select`
-   `Insert`
-   `Update`
-   `Delete`
-   Generated CDS Java model classes
-   Event contexts
-   CAP managed transactions

Do not assume API signatures from a different CAP release.

### Event handling decision

CAP event annotations have distinct purposes:

  Annotation   Purpose
  ------------ ----------------------------------------------------
  `@Before`    Validate or prepare data before the main operation
  `@On`        Handle the operation itself
  `@After`     Run after the main operation

The intended design is a custom `@On` CREATE handler for Orders because
order creation is a multi-step business operation that needs controlled
persistence.

However, verify CAP Java 5.2.0 handler chaining, generic CRUD
interaction, and `proceed()` semantics before implementing. Do not
assume a custom `@On` handler automatically replaces generic persistence
without checking the runtime behavior.

### Suggested separation of responsibilities

`OrderHandler`:

-   Receives the CAP CREATE event.
-   Extracts request data.
-   Delegates to the business service.
-   Returns or sets the result using the supported CAP event API.

`OrderCreationService`:

-   Validates business rules.
-   Retrieves customers and products.
-   Calculates price snapshots and totals.
-   Coordinates order persistence and stock updates.

Avoid creating unnecessary repositories or interfaces unless a real
requirement justifies them.

## 9. Transactional Consistency

Order creation must be atomic.

Expected logical workflow:

``` text
BEGIN TRANSACTION
       |
       v
Validate customer and request
       |
       v
Retrieve products and prices
       |
       v
Validate stock
       |
       v
Calculate order total
       |
       v
Insert order
       |
       v
Insert order items
       |
       v
Deduct stock
       |
       v
COMMIT
```

If any required operation fails:

``` text
ROLLBACK
```

No partial order, order items, or stock changes should remain.

Use CAP-managed transaction boundaries. Verify the exact transaction
behavior and propagation rules in CAP Java 5.2.0 rather than manually
managing transactions without need.

### Concurrency requirement

Consider:

-   Product stock = 1.
-   Customer A requests quantity 1.
-   Customer B requests quantity 1.
-   Both requests arrive at nearly the same time.

Only one order may reserve the final unit.

A simple read-check-write sequence is unsafe because both requests may
read stock as 1 before either updates it.

Evaluate an appropriate strategy supported by the database and CAP Java,
such as:

-   Atomic conditional stock update.
-   Optimistic locking.
-   Pessimistic locking.
-   Suitable transaction isolation.

Test the chosen approach against the actual H2 and PostgreSQL behavior.
Do not claim concurrency safety without a correct database-level
mechanism.

## 10. Planned Feature Roadmap

### Phase 1 --- Product Management

-   [x] Product creation handler.
-   [x] Product name validation.
-   [x] Product price validation.
-   [x] Product creation tested.
-   [ ] Confirm stock validation.
-   [ ] Product update rules.
-   [ ] Product deletion policy.
-   [ ] Preserve products referenced by historical orders.
-   [ ] Understand generic CRUD versus custom event handling.

### Phase 2 --- Customer Management

-   [ ] Customer creation.
-   [ ] Required-field validation.
-   [ ] Email format validation.
-   [ ] Duplicate email prevention.
-   [ ] Database unique constraint.
-   [ ] Customer retrieval and update.
-   [ ] Referential integrity.

### Phase 3 --- Order Management

-   [ ] Order CREATE handler.
-   [ ] Customer validation.
-   [ ] Product validation.
-   [ ] Quantity validation.
-   [ ] Purchase-time price snapshots.
-   [ ] Order total calculation.
-   [ ] Order number generation.
-   [ ] Transactional order persistence.
-   [ ] Atomic stock deduction.
-   [ ] Concurrency testing.
-   [ ] Order retrieval with items.

### Phase 4 --- Order Lifecycle

Planned statuses:

``` text
CREATED
PENDING_PAYMENT
CONFIRMED
PROCESSING
SHIPPED
DELIVERED
CANCELLED
```

Implement explicit valid transitions. Do not allow clients to set
arbitrary statuses.

Cancellation must:

-   Preserve the order.
-   Preserve payment history.
-   Apply a defined inventory-restoration policy.
-   Reject invalid cancellation attempts, such as cancellation after
    delivery unless a separate return workflow exists.

### Phase 5 --- Payment Management

-   [ ] Payment creation.
-   [ ] Payment attempts and history.
-   [ ] Pending, successful, and failed states.
-   [ ] Idempotency.
-   [ ] Duplicate payment prevention.
-   [ ] Payment/order coordination.
-   [ ] Simulated payment service before real gateway integration.

Never store real card numbers or CVVs.

### Phase 6 --- Refund Management

-   [ ] Refund request.
-   [ ] Validate payment and payment status.
-   [ ] Validate refund amount.
-   [ ] Prevent over-refunding.
-   [ ] Define whether pending refunds reserve refundable balance.
-   [ ] Preserve refund history.
-   [ ] Prevent concurrent refund requests from exceeding the refundable
    amount.

Example:

``` text
Captured payment:        ₹3,000
Successful refunds:      ₹1,000
Remaining refundable:    ₹2,000
Requested refund:        ₹2,500
Result:                  REJECT
```

### Phase 7 --- Security

-   [ ] Authentication.
-   [ ] Authorization.
-   [ ] JWT.
-   [ ] OAuth 2.0.
-   [ ] SAP BTP security integration.
-   [ ] CAP/Spring Security integration.
-   [ ] Role-based access control.
-   [ ] Customer data isolation.
-   [ ] Admin and product-management permissions.
-   [ ] Sensitive-data protection.

### Phase 8 --- Database and Persistence

Start with H2 for local development. Later evaluate PostgreSQL and SAP
HANA Cloud for the target environment.

Learn and implement:

-   SQL generated by CAP.
-   Primary and foreign keys.
-   Unique constraints.
-   Indexes.
-   Transaction isolation.
-   Query plans.
-   Connection pooling.
-   Schema evolution.
-   Database migration strategy.
-   N+1 query prevention.
-   Data integrity constraints.

Understand the distinction between the CDS model, generated database
schema, and runtime persistence behavior.

### Phase 9 --- Messaging and Integrations

Potential future integrations:

-   REST integrations.
-   Kafka producer and consumer.
-   `OrderCreated` event.
-   Inventory service.
-   Notification service.
-   Outbox pattern.
-   Retry handling.
-   Dead-letter handling.
-   Idempotent consumers.
-   Eventual consistency.

Introduce distributed transactions and saga-style workflows only when
the system actually spans independent services.

### Phase 10 --- Production Readiness

-   [ ] Docker.
-   [ ] Docker Compose.
-   [ ] Kubernetes fundamentals.
-   [ ] CI/CD.
-   [ ] Centralized configuration.
-   [ ] Secrets management.
-   [ ] Structured logging.
-   [ ] Metrics.
-   [ ] Distributed tracing.
-   [ ] Health checks.
-   [ ] API gateway and reverse proxy concepts.
-   [ ] SAP BTP deployment.

## 11. Testing Strategy

Testing should grow alongside each feature.

  -----------------------------------------------------------------------
  Test level                          What it should verify
  ----------------------------------- -----------------------------------
  Handler/unit tests                  Business validation and
                                      calculations

  Persistence integration tests       CAP queries and database behavior

  API tests                           OData request and response behavior

  Transaction tests                   Rollback after a failure midway
                                      through order creation

  Concurrency tests                   No overselling and no
                                      over-refunding

  Security tests                      Unauthorized access and customer
                                      data isolation

  Regression tests                    Existing Product behavior remains
                                      correct
  -----------------------------------------------------------------------

Use deterministic test data and isolate tests from one another. Do not
rely only on manual Postman testing for critical business rules.

## 12. Local Development Commands

Run commands from the project root unless otherwise stated.

### Build CDS for Java

``` powershell
npx.cmd cds build --for java
```

### Maven validation

``` powershell
mvn validate
```

### Select the `srv` module

``` powershell
mvn -pl :procureflow validate
```

### Run the application

Use the project's configured Spring Boot/Maven execution command. Verify
the actual POM and run configuration before assuming a specific command.

### Inspect OData metadata

``` http
GET http://localhost:8080/odata/v4/OrderService/$metadata
```

### Inspect Products

``` http
GET http://localhost:8080/odata/v4/OrderService/Products
```

### Create a Product

``` http
POST http://localhost:8080/odata/v4/OrderService/Products
Content-Type: application/json
```

``` json
{
  "name": "MacBook Pro",
  "description": "Apple laptop",
  "price": 150000.00,
  "stock": 10
}
```

## 13. Important Architectural Topics to Master

Throughout implementation, explain the difference between:

-   CDS model and service definition.
-   Association and composition.
-   CAP generic CRUD and custom event handlers.
-   `@Before`, `@On`, and `@After`.
-   `PersistenceService` and `CqnService`.
-   CQN and SQL.
-   CAP transaction and database transaction.
-   Validation and business-rule enforcement.
-   Application validation and database constraints.
-   Current product price and historical order price.
-   Order status and payment status.
-   Authentication and authorization.
-   Optimistic and pessimistic locking.
-   Synchronous OData calls and asynchronous messaging.
-   API gateway and reverse proxy.
-   Modular monolith and microservices.
-   Local database and cloud-managed database.

## 14. Development and Code Review Rules

When reviewing implementation:

1.  Identify what is correct first.
2.  Classify findings as Critical, Major, Minor, or Optional.
3.  Explain the technical reason behind every finding.
4.  Show only the relevant corrected code.
5.  Let the developer make the correction.
6.  Explain CAP-specific behavior.
7.  Check edge cases, security, performance, transactions, and
    concurrency.
8.  Verify all CAP API usage against CAP Java 5.2.0.
9.  Avoid unnecessary design patterns and abstractions.
10. Never claim a feature is implemented until it has been verified.

The developer should write the implementation. The mentor should provide
the design reasoning, implementation steps, hints, and code review.

------------------------------------------------------------------------

## 15. Current Project Status and Immediate Next Step

### Completed

-   SAP CAP Java Maven project created.
-   Initial CDS domain model created.
-   Associations and compositions modelled.
-   OrderService defined.
-   Product creation handler implemented.
-   Product name and price validation implemented.
-   Product creation and invalid-request behavior tested.
-   Spring component scanning configuration inspected.
-   Historical purchase-price design established.
-   `Orders.totalAmount` added.
-   `OrderItems.unitPrice` added.
-   CDS Java build completed successfully.

Latest successful CDS build:

``` text
done > wrote output to:
   srv\src\main\resources\edmx\csn.json
   srv\src\main\resources\edmx\odata\v4\OrderService.xml

build completed in 246 ms
```

### Immediate next step

Start learning CAP Java persistence architecture:

1.  `PersistenceService`.
2.  `CqnService`.
3.  CQN.
4.  `Select`, `Insert`, `Update`, and `Delete`.
5.  Generated CDS Java classes.
6.  CAP event handling and generic CRUD interaction.
7.  CAP transaction management.

Then implement Order Creation incrementally.

**Do not restart the project, repeat Product creation, or generate the
entire OrderHandler.**

The purpose of ProcureFlow is not just to produce working endpoints. It
is to build a backend that is correct under real-world conditions and to
develop the engineering judgment required to design and maintain
production systems.

------------------------------------------------------------------------

::: {align="center"}
**ProcureFlow --- Build correctly. Preserve data. Design for
consistency.**

*SAP CAP Java · Spring Boot · OData V4 · Java 25*
:::
