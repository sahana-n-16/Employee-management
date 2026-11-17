# Personal Details Module

## 1. Overview and responsibilities

The Personal Details module stores and retrieves an employee's date of birth,
gender, contact details, emergency contact, and current/permanent addresses.
The backend associates a record with an internal `employeeId`.

Employee full name and Employment Code belong to Employee Management. They are
not stored in the Personal Details entity or accepted by its API. In the
current frontend, those fields are read-only and show that Employee Management
integration is pending.

The frontend currently uses the explicitly named local demo ID `100001` for
Personal Details API requests. This is temporary configuration, not an
Employment Code and not an authenticated employee identity.

## 2. Technologies

- React 19 and Vite 8 frontend
- Java 21 and Spring Boot 4.1.2 backend
- Spring Web MVC for HTTP endpoints
- Spring Data JPA and Hibernate for persistence
- MySQL with MySQL Connector/J
- Jakarta Bean Validation for request validation

The frontend dependencies and scripts are listed in `frontend/package.json`;
backend dependencies are listed in `pom.xml`.

## 3. Frontend component structure

- `frontend/src/main.jsx` mounts the React application.
- `frontend/src/App.jsx` supplies the application shell and renders
  `PersonalDetailsForm`.
- `frontend/src/features/personal-details/components/PersonalDetailsForm.jsx`
  owns form state, validation, age calculation, API loading/saving, messages,
  and Reset behavior.
- `frontend/src/features/personal-details/components/AddressForm.jsx` renders
  current and permanent address fields and the same-as-current control.

The form maps backend flat address properties (for example,
`currentAddressLine1`) to nested address form state. When existing current and
permanent addresses match, the form initializes its same-address checkbox as
selected.

Vite's development server proxies `/api` requests to
`http://localhost:8081` in `frontend/vite.config.js`.

## 4. Backend package and class structure

The backend source is under `src/main/java/com/ems`:

- `com.ems.employee_management_system.EmployeeManagementSystemApplication`
  starts Spring Boot and configures component, entity, and repository scans.
- `com.ems.personal.controller.PersonalDetailsController` exposes the REST
  endpoints.
- `com.ems.personal.dto.PersonalDetailsRequest` defines and validates POST
  input.
- `com.ems.personal.service.PersonalDetailsService` performs lookup,
  create/update, and duplicate-conflict handling.
- `com.ems.personal.repository.PersonalDetailsRepository` is the Spring Data
  repository.
- `com.ems.personal.entity.PersonalDetails` is the JPA entity.

There is also an unused, empty class at
`src/main/java/com/ems/personal/PersonalDetails.java`. It is not the JPA entity;
the entity is in `com.ems.personal.entity`. Do not confuse their package names.

## 5. Database table and constraints

The JPA entity maps to MySQL table `personal_details`. The current entity
defines these columns:

| Column | Java type | Mapping notes |
|---|---|---|
| `id` | `Long` | Primary key, generated identity |
| `employee_id` | `Long` | Non-null; JPA entity declares unique constraint `uq_personal_details_employee_id` |
| `date_of_birth` | `LocalDate` | Date |
| `gender` | `String` | Maximum length 50 |
| `mobile_number` | `String` | Maximum length 20 |
| `personal_email` | `String` | Maximum length 254 |
| `emergency_contact_name` | `String` | Maximum length 150 |
| `emergency_contact_mobile` | `String` | Maximum length 20 |
| `current_address_line1`, `current_address_line2` | `String` | Maximum length 255 each |
| `current_city` | `String` | Maximum length 100 |
| `current_pin_code` | `String` | Maximum length 6 |
| `permanent_address_line1`, `permanent_address_line2` | `String` | Maximum length 255 each |
| `permanent_city` | `String` | Maximum length 100 |
| `permanent_pin_code` | `String` | Maximum length 6 |
| `created_at` | `LocalDateTime` | Non-null, not updatable; set on insert |
| `updated_at` | `LocalDateTime` | Non-null; set on insert and update |

The JPA entity declares a table-level unique constraint on `employee_id`.
Application of `uq_personal_details_employee_id` in the local MySQL database
has not been confirmed by the latest metadata verification. The entity has no
JPA relationship or foreign key to an Employee table yet. Most detail columns
are nullable in the entity mapping; required API fields are enforced by
request validation instead.

`src/main/resources/application.properties` configures the MySQL schema
`ems_personal_details`, port 8081, and
`spring.jpa.hibernate.ddl-auto=update`. Hibernate may attempt to update the
schema at application startup to reflect the entity mapping. Review/apply
production schema changes through the team's approved migration process.

## 6. API endpoints

Base URL: `http://localhost:8081`

### Retrieve by internal employee ID

```http
GET /api/personal-details/{employeeId}
```

Example:

```sh
curl -i http://localhost:8081/api/personal-details/100001
```

A successful response is the serialized `PersonalDetails` entity, including
the fields below and generated `id`, `createdAt`, and `updatedAt` values.
If there is no record for the ID, the service returns HTTP 404.

- **HTTP 200:** a Personal Details record was found.
- **HTTP 404:** no record was found for that employee ID.

Representative HTTP 200 response (example values; property names match the
serialized entity):

```json
{
  "id": 1,
  "employeeId": 100001,
  "dateOfBirth": "1990-01-15",
  "gender": "female",
  "mobileNumber": "9876543210",
  "personalEmail": "employee@example.com",
  "emergencyContactName": "Emergency Contact",
  "emergencyContactMobile": "9876543211",
  "currentAddressLine1": "12 Main Road",
  "currentAddressLine2": "",
  "currentCity": "Bengaluru",
  "currentPinCode": "560001",
  "permanentAddressLine1": "45 Lake Road",
  "permanentAddressLine2": "",
  "permanentCity": "Mysuru",
  "permanentPinCode": "570001",
  "createdAt": "2026-11-10T10:00:00",
  "updatedAt": "2026-11-10T10:00:00"
}
```

### Create or update personal details

```http
POST /api/personal-details
Content-Type: application/json
```

The service looks up the record by `employeeId`, updates it if found, or
creates one otherwise. Example:

```sh
curl -i -X POST http://localhost:8081/api/personal-details \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 100001,
    "dateOfBirth": "1990-01-15",
    "gender": "female",
    "mobileNumber": "9876543210",
    "personalEmail": "employee@example.com",
    "emergencyContactName": "Emergency Contact",
    "emergencyContactMobile": "9876543211",
    "currentAddressLine1": "12 Main Road",
    "currentAddressLine2": "",
    "currentCity": "Bengaluru",
    "currentPinCode": "560001",
    "permanentAddressLine1": "45 Lake Road",
    "permanentAddressLine2": "",
    "permanentCity": "Mysuru",
    "permanentPinCode": "570001"
  }'
```

The response is the saved entity with the same fields as the representative
GET response above.

- **HTTP 200:** Personal Details were successfully created or updated.
- **HTTP 409:** a duplicate `employeeId` violates the named unique constraint.

`employeeId` is the internal employee database ID. `employmentCode` and
`fullName` are not part of this request or response contract. The 409 status
is produced when the service recognizes a violation of the named employee-ID
constraint.

## 7. Validations and business rules

Backend POST validation (`PersonalDetailsRequest`):

- `employeeId` and `dateOfBirth` are required; date of birth cannot be in the
  future.
- `gender`, `mobileNumber`, `personalEmail`, emergency contact name and mobile,
  current address line 1 and city, permanent address line 1 and city are
  required.
- Mobile and emergency mobile numbers must each contain exactly 10 digits.
- Personal email must have a valid email format.
- Current and permanent PIN codes, when supplied, must each contain exactly
  6 digits.
- Address line 2 is optional.

Frontend form rules (`PersonalDetailsForm.jsx`):

- Repeats required-field, future DOB, email, 10-digit phone, and six-digit PIN
  checks; the required age must be positive and no more than 999.
- Calculates age from date of birth; age is not part of the backend API.
- The permanent address is not required separately when the user marks it as
  the same as the current address. Changes to the current address are copied
  to the permanent address while that option is selected.
- Reset clears the form and form messages.
- Loading treats HTTP 404 as an empty form. Other load errors and save errors
  are displayed. Successful POST shows a success message.
- Employment Code and employee full name are read-only pending Employee
  Management integration; they are not validated or submitted by this module.

The API currently trusts the `employeeId` provided by its caller. No
authentication/authorization system or Employee foreign key is implemented in
this project.

## 8. Starting the applications

Prerequisites: Java 21, MySQL with the configured
`ems_personal_details` schema available, and Node.js/npm for the frontend.
Database credentials are configured locally in
`src/main/resources/application.properties`; do not commit secrets.

From the project root, start the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

In another terminal, start the frontend:

```powershell
Set-Location .\frontend
npm run dev
```

Open the Vite URL printed in the terminal. The Vite development proxy forwards
frontend `/api` requests to the backend on port 8081. The backend can also be
tested directly at `http://localhost:8081`.

## 9. Recorded testing results

The following results were recorded in earlier validation runs; they were not
rerun while preparing this documentation:

- Backend: `.\mvnw.cmd clean test "-Dspring.jpa.hibernate.ddl-auto=none"`
  passed with 6 tests, 0 failures, and 0 errors. Hibernate schema updates were
  disabled for that run.
- Frontend: `npm run lint` passed from `frontend`.
- Frontend: `npm run build` passed from `frontend`.
- A prior live integration check through Vite's proxy successfully loaded
  employee `100001` and verified POST followed by GET for a synthetic test
  employee.

The live integration check is historical and is not a test result for the
current state. Rerun the commands after subsequent code or configuration
changes.

## 10. Pending Employee Management integration

The following are not implemented in the current project and must be agreed
with Employee Management before integration:

- **Employee relationship:** Personal Details stores an internal `employeeId`
  value and enforces uniqueness, but has no Employee entity relationship,
  existence check, or foreign key.
- **Employment Code:** a separate 6-digit Employee Management value. The
  current Personal Details form displays a read-only placeholder; it does not
  retrieve or save Employment Code.
- **Employee full name:** owned by Employee Management. The form displays a
  read-only placeholder; the Personal Details table and API do not store or
  return the name.
- **Authentication and authorization:** there is no Spring Security
  configuration, login/logout API, JWT/session implementation, principal, or
  role-based access control in this source. The frontend's `100001` is only a
  temporary local demo ID.

For integration, use Employee Management's existing employee model and
authentication contract. The backend should ultimately derive the employee
identity from a trusted authenticated principal and enforce access rules
server-side; do not treat Employment Code as `employeeId` or trust a client
supplied ID as proof of identity.
