# Personal Expense Tracking System

Personal Expense Tracking System is a Spring Boot web application for recording personal spending, setting monthly income, and reviewing financial performance in Rwandan francs (FRW). The system is designed for authenticated users who need a simple, focused way to understand where money is going each month and across the year.

## Features

- Secure user registration and login with Spring Security.
- Expense management with title, category, quantity, unit cost, date, and calculated total.
- Monthly income entry linked to the signed-in user.
- Dashboard summaries for income, expenses, and balance in FRW.
- Monthly and yearly filters.
- Histogram charts for month and year analysis.
- Monthly expense table on the dashboard.
- Yearly month-by-month table for income, expenses, and balance.
- Paginated expense list with edit and delete actions.
- Schema guard for keeping database tables aligned with project models.

## Technology Stack

- Java 17
- Spring Boot 3.2.5
- Spring MVC
- Spring Data JPA / Hibernate
- Spring Security
- Thymeleaf
- MySQL
- Chart.js
- Maven Wrapper

## Active Database Tables

The project architecture uses these model-backed tables:

| Model | Table | Purpose |
| --- | --- | --- |
| `User` | `users` | Stores account and profile details. |
| `Expense` | `expense` | Stores user expense records. |
| `Income` | `income` | Stores user monthly income records. |

Legacy duplicate tables are archived into `expensedb_archive` instead of being deleted.

## Configuration

Database settings are in:

```properties
src/main/resources/application.properties
```

Default connection:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/expensedb
spring.datasource.username=root
spring.datasource.password=
```

Update these values if your MySQL port, username, or password is different.

## Run The Project

From the project folder:

```powershell
.\mvnw.cmd spring-boot:run
```

Then open:

```text
http://localhost:8080
```

## Build And Verify

```powershell
.\mvnw.cmd test
```

The project currently has no dedicated test classes, so this command verifies compilation and the Maven lifecycle.

## Main Pages

- `/login` - sign in
- `/register` - create account
- `/` - paginated expense list
- `/new` - add expense
- `/dashboard` - monthly and yearly financial dashboard
- `/about` - project overview and team support details

## Team

Owners: Yvonne and Enock

Support email: mukabera255@gmail.com

Contact: +250 793 835 394
