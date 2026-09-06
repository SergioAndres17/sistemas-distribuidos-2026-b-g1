package com.synkrotech.mvp.config;

import java.math.BigDecimal;
import java.util.List;

import com.synkrotech.mvp.auth.Role;
import com.synkrotech.mvp.auth.User;
import com.synkrotech.mvp.auth.UserRepository;
import com.synkrotech.mvp.customers.Customer;
import com.synkrotech.mvp.customers.CustomerRepository;
import com.synkrotech.mvp.products.Category;
import com.synkrotech.mvp.products.CategoryRepository;
import com.synkrotech.mvp.products.Product;
import com.synkrotech.mvp.products.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fills empty tables on startup so the frontend has something to render on a
 * fresh database. Each block is skipped when its table already has rows, so
 * restarting never duplicates anything.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final CustomerRepository customers;
    private final CategoryRepository categories;
    private final ProductRepository products;

    public DataSeeder(UserRepository users, CustomerRepository customers,
            CategoryRepository categories, ProductRepository products) {
        this.users = users;
        this.customers = customers;
        this.categories = categories;
        this.products = products;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedCustomers();
        seedCatalogue();
    }

    private void seedUsers() {
        if (users.count() > 0) {
            return;
        }
        users.saveAll(List.of(
                new User("Ana Ramírez", Role.ADMIN),
                new User("Carlos Gómez", Role.SALESPERSON),
                new User("Laura Vidal", Role.INVENTORY)));
        log.info("Seeded 3 users for the login picker.");
    }

    private void seedCustomers() {
        if (customers.count() > 0) {
            return;
        }
        customers.saveAll(List.of(
                new Customer("Distribuidora El Roble", "900123456-1",
                        "compras@elroble.co", "3001112233", "Cra 5 #12-40, Neiva"),
                new Customer("Ferretería La Quinta", "901987654-7",
                        "ventas@laquinta.co", "3104445566", "Calle 8 #21-15, Neiva"),
                new Customer("Inversiones Vega S.A.S.", "830456789-3",
                        "contacto@vega.co", "3208889900", "Av. Circunvalar #3-90, Neiva")));
        log.info("Seeded 3 sample customers.");
    }

    private void seedCatalogue() {
        if (categories.count() > 0 || products.count() > 0) {
            return;
        }
        Category peripherals = categories.save(new Category("Periféricos"));
        Category computers = categories.save(new Category("Computadores"));
        Category accessories = categories.save(new Category("Accesorios"));

        products.saveAll(List.of(
                new Product("Teclado mecánico RGB", new BigDecimal("189900.00"), 25, peripherals),
                new Product("Mouse inalámbrico", new BigDecimal("79900.00"), 40, peripherals),
                new Product("Monitor 24\" IPS", new BigDecimal("649900.00"), 12, peripherals),
                new Product("Portátil 14\" i5 16GB", new BigDecimal("3299000.00"), 8, computers),
                new Product("Desktop torre i7 32GB", new BigDecimal("4750000.00"), 5, computers),
                new Product("Cable HDMI 2m", new BigDecimal("29900.00"), 100, accessories),
                new Product("Base refrigerante", new BigDecimal("119900.00"), 18, accessories)));
        log.info("Seeded 3 categories and 7 products.");
    }
}
