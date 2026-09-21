package com.codewithmosh.store;

public class CodeExcerptExamples {
}


// While you can inject dependencies manually by wiring objects up in your main method,
// enterprise Java developers almost exclusively rely on DI Frameworks (Containers) to
// automate object creation and lifecycle management:

// Spring Framework: The most popular ecosystem in modern Java. It uses annotations
// like @Component and @Autowired to manage dependencies seamlessly at runtime.

// Dependency Injection: (Constructor Injection):
//public class Car {
//    private final Engine engine; // final means this variable is immutable
//
//    // The dependency is injected here
//    public Car(Engine engine) {
//        this.engine = engine;
//    }
//
//    public void start() {
//        engine.run();
//    }
//}

// Setter Injection:
//public class Car {
//    private Engine engine;
//
//    // A setter method allows an external tool/class to inject the engine
//    public void setEngine(Engine engine) {
//        this.engine = engine;
//    }
//}

