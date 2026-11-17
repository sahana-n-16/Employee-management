
const API_BASE_URL = "http://localhost:8080/api/finance";

export const getFinanceDetails = async (employeeId) => {
  const response = await fetch(`${API_BASE_URL}/${employeeId}`);

  if (!response.ok) {
    throw new Error(
      `Failed to load finance details. Status: ${response.status}`
    );
  }

  return response.json();
};

export const createFinanceDetails = async (employeeId, data) => {
  const response = await fetch(`${API_BASE_URL}/${employeeId}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    throw new Error("Failed to save finance details.");
  }

  return response.json();
};

export const updateFinanceDetails = async (employeeId, data) => {
  const response = await fetch(`${API_BASE_URL}/${employeeId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    throw new Error("Failed to update finance details.");
  }

  return response.json();
};
