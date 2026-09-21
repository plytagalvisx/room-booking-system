package com.plytagalvisx.roombooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RoomBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoomBookingApplication.class, args);

//        var orderService = new OrderService(new StripePaymentService());
//        orderService.placeOrder();
//        System.out.println("\nHello World!");
    }

}
