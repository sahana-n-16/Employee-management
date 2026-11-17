# Attendance Module

## Scope and integration boundaries

Attendance is implemented in the existing Spring Boot application under
`com.ems.attendance`. It stores the internal employee ID as a `Long`; it does
not define an Employee entity, foreign key, authentication integration, leave
behavior, or frontend. The employee ID is currently supplied by the caller and
is not an authenticated identity.

The business timezone is `Asia/Kolkata`. Attendance dates and local timestamp
values are determined using that timezone. A record represents one employee's
attendance for one calendar day. Breaks are not tracked or deducted.

## API

All endpoints are relative to the backend base URL and use the existing `/api`
prefix. Check-in and check-out do not accept a request body or client timestamp;
the server records the time.

### Check in

```http
POST /api/attendance/{employeeId}/check-in
```

Returns **201 Created** and an `AttendanceResponse`. A second check-in for the
same employee on the same business date returns **409 Conflict**.

### Check out

```http
POST /api/attendance/{employeeId}/check-out
```

Returns **200 OK** and an `AttendanceResponse`. No check-in for today returns
**404 Not Found**; an already completed record returns **409 Conflict**.

### Daily history

```http
GET /api/attendance/{employeeId}/daily?date=2026-11-15
```

The date must use ISO `YYYY-MM-DD` format. A record is returned with **200 OK**;
if there is no record for the date, the endpoint returns **404 Not Found**.

### Monthly history

```http
GET /api/attendance/{employeeId}/monthly?year=2026&month=11
```

Returns **200 OK** with a JSON array, ordered by `attendanceDate` ascending.
Only recorded attendance is returned; the endpoint does not infer absences,
weekends, holidays, or leave.

### Response

Successful check-in, check-out, and daily history use this JSON shape:

```json
{
  "id": 42,
  "employeeId": 100001,
  "attendanceDate": "2026-11-15",
  "checkInAt": "2026-11-15T09:00:00",
  "checkOutAt": null,
  "workingMinutes": null,
  "status": "IN_PROGRESS"
}
```

For completed records, `checkOutAt` is set, `workingMinutes` is the elapsed
whole minutes between check-in and check-out, and `status` is `COMPLETED`.
Durations are not reduced for breaks.

Errors use this shape:

```json
{
  "message": "Attendance has already been checked in for this employee today.",
  "fieldErrors": {}
}
```

Validation errors return HTTP **400** and populate `fieldErrors`; conflicts
return **409**, and missing attendance returns **404**.

## Database setup

The production configuration uses `spring.jpa.hibernate.ddl-auto=validate`;
it must not be changed to silently create or update production tables. Provision
the table in the configured MySQL database using the team's approved schema
change process before deploying the application. The following is the expected
MySQL schema:

```sql
CREATE TABLE attendance_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_at DATETIME(6) NOT NULL,
    check_out_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_attendance_employee_date
        UNIQUE (employee_id, attendance_date)
) ENGINE=InnoDB;
```

The unique employee/date constraint is the final guard against concurrent
duplicate check-ins; duplicate-key failures for this constraint are returned
as HTTP **409 Conflict**. Its unique index also supports daily and monthly
employee/date lookups, so no duplicate secondary index is declared. The table
contains no foreign key to an Employee table so it does not assume or modify
the Employee Management module's schema.
