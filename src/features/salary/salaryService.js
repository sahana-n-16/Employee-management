
const API_URL = "http://localhost:8080/api/salary";

async function apiRequest(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
  });

  if (!response.ok) {
    let errorMessage = `Request failed (${response.status})`;

    try {
      const responseText = await response.text();

      if (responseText) {
        try {
          const errorData = JSON.parse(responseText);

          errorMessage =
            errorData.message ||
            errorData.detail ||
            errorData.title ||
            errorData.error ||
            errorData.errors
              ?.map((item) =>
                item.defaultMessage || item.message || item
              )
              .join(", ") ||
            errorMessage;
        } catch {
          errorMessage = responseText;
        }
      }
    } catch {
      // Retain the default error message.
    }

    throw new Error(errorMessage);
  }

  if (response.status === 204) return null;

  return response.json();
}

// ==========================================
// SALARY STRUCTURE APIs
// ==========================================

export const createSalaryStructure = (employeeId, data) =>
  apiRequest(`${API_URL}/${employeeId}`, {
    method: "POST",
    body: JSON.stringify(data),
  });

export const getSalaryStructures = (employeeId) =>
  apiRequest(`${API_URL}/${employeeId}`);

export const getActiveSalaryStructures = (employeeId) =>
  apiRequest(`${API_URL}/${employeeId}/active`);

export const updateSalaryStructure = (id, data) =>
  apiRequest(`${API_URL}/structure/${id}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

// ==========================================
// SALARY COMPONENT APIs
// ==========================================

export const createSalaryComponent = (salaryStructureId, data) =>
  apiRequest(`${API_URL}/${salaryStructureId}/components`, {
    method: "POST",
    body: JSON.stringify(data),
  });

export const getSalaryComponents = (salaryStructureId) =>
  apiRequest(`${API_URL}/${salaryStructureId}/components`);

export const updateSalaryComponent = (
  salaryStructureId,
  componentId,
  data
) =>
  apiRequest(
    `${API_URL}/${salaryStructureId}/components/${componentId}`,
    {
      method: "PUT",
      body: JSON.stringify(data),
    }
  );

export const deleteSalaryComponent = (
  salaryStructureId,
  componentId
) =>
  apiRequest(
    `${API_URL}/${salaryStructureId}/components/${componentId}`,
    {
      method: "DELETE",
    }
  );

export const deleteAllSalaryComponents = (salaryStructureId) =>
  apiRequest(`${API_URL}/${salaryStructureId}/components`, {
    method: "DELETE",
  });

// ==========================================
// MONTHLY SALARY APIs
// ==========================================

// Create monthly salary record
export const createMonthlySalary = (employeeId, data) =>
  apiRequest(`${API_URL}/${employeeId}/monthly`, {
    method: "POST",
    body: JSON.stringify(data),
  });

// Get all monthly salary records for an employee
export const getMonthlySalaries = (employeeId) =>
  apiRequest(`${API_URL}/${employeeId}/monthly`);

// Get monthly salary for a specific month and year
export const getMonthlySalaryByPeriod = (
  employeeId,
  month,
  year
) =>
  apiRequest(
    `${API_URL}/${employeeId}/monthly/${month}/${year}`
  );

// Update a monthly salary record
export const updateMonthlySalary = (
  employeeId,
  salaryId,
  data
) =>
  apiRequest(
    `${API_URL}/${employeeId}/monthly/${salaryId}`,
    {
      method: "PUT",
      body: JSON.stringify(data),
    }
  );

// Mark monthly salary as paid
export const markMonthlySalaryAsPaid = (
  employeeId,
  salaryId,
  paymentDate
) =>
  apiRequest(
    `${API_URL}/${employeeId}/monthly/${salaryId}/payment?paymentDate=${encodeURIComponent(paymentDate)}`,
    {
      method: "PATCH",
    }
  );

// Delete a pending monthly salary record
export const deleteMonthlySalary = (employeeId, salaryId) =>
  apiRequest(
    `${API_URL}/${employeeId}/monthly/${salaryId}`,
    {
      method: "DELETE",
    }
  );
