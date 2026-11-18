package ru.ifmo.coworkly.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.coworkly.user.UserRole;
import ru.ifmo.coworkly.user.UserService;

/** Local demonstration fixtures. Enabled explicitly with the demo profile. */
@Component
@Profile("demo")
public class DemoData implements CommandLineRunner {
    private final UserService users;
    private final JdbcTemplate jdbc;

    public DemoData(UserService users, JdbcTemplate jdbc) {
        this.users = users;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.findByEmail("admin@coworkly.test").isEmpty()) {
            users.register("admin@coworkly.test", "DemoAdmin123!", "Администратор", UserRole.ADMIN);
        }
        if (users.findByEmail("resident@coworkly.test").isEmpty()) {
            users.register("resident@coworkly.test", "DemoResident123!", "Анна Смирнова", UserRole.RESIDENT);
        }
        jdbc.update("""
                INSERT INTO location(name, address) VALUES ('Центр', 'ул. Главная, 1')
                ON CONFLICT (name) DO NOTHING
                """);
        jdbc.update("""
                INSERT INTO tariff_plan(name, base_price_cph) VALUES ('Демо', 4000)
                ON CONFLICT (name) DO NOTHING
                """);
        jdbc.update("""
                INSERT INTO space(location_id, name, capacity, type, tariff_plan_id)
                SELECT l.id, v.name, v.capacity, v.type, t.id
                FROM location l CROSS JOIN tariff_plan t
                CROSS JOIN (VALUES ('Open Desk #1', 1, 'OPEN_DESK'),
                                   ('Open Desk #2', 1, 'OPEN_DESK'),
                                   ('Meeting Room A', 6, 'MEETING_ROOM')) v(name, capacity, type)
                WHERE l.name = 'Центр' AND t.name = 'Демо'
                ON CONFLICT (location_id, name) DO NOTHING
                """);
    }
}
