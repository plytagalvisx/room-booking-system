package com.codewithmosh.store;

//public class StripePaymentService {
//    public void processPayment(double amount) { // in real life: (Payment payment)
//        System.out.println("STRIPE");
//        System.out.println("Amount: " + amount);
//    }
//}

// A decoupled version of StripePaymentService class:
public class StripePaymentService implements PaymentService {
    @Override
    public void processPayment(double amount) { // in real life: (Payment payment)
        System.out.println("STRIPE");
        System.out.println("Amount: " + amount);
    }
}