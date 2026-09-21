package com.plytagalvisx.roombooking;

public class OrderService {

    // Composition; we store a PaymentService interface instead of being dependent on one particular StripePaymentService class
    public PaymentService paymentService;

    public OrderService(PaymentService paymentService) { // Here we inject a dependency (PaymentService) into an OrderService class via Constructor.
        this.paymentService = paymentService;
    }

    public void placeOrder() { // in real life: (Order order)
        // var paymentService = new StripePaymentService();
        paymentService.processPayment(10);
    }
}
