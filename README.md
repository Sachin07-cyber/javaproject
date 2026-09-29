# javaproject

## Visitor Pass

Spring Boot application for managing visitor approvals, OTP verification, residents, and entry logs.

### Run locally

Requires Java 25 and MySQL. The application connects to `visitorpass_db` on `localhost:3306` and can create the database if the configured MySQL user has permission.

Set `DB_USERNAME` and `DB_PASSWORD` for your MySQL credentials. Email delivery also requires `MAIL_USERNAME` and `MAIL_PASSWORD` for a Gmail SMTP account; leave them unset to disable authenticated email delivery.

On Windows, start the application with:

```bat
mvnw.cmd spring-boot:run
```

The application runs at `http://localhost:8080`.