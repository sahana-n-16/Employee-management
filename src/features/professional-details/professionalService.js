
const API_BASE_URL = "http://localhost:8080/api/professional";

// Get professional details
export const getProfessionalDetails = async (employeeId) => {
  const response = await fetch(`${API_BASE_URL}/${employeeId}`);

  if (!response.ok) {
    throw new Error(
      `Failed to fetch professional details. Status: ${response.status}`
    );
  }

  return response.json();
};

// Create professional details
export const createProfessionalDetails = async (employeeId, data) => {
  const response = await fetch(`${API_BASE_URL}/${employeeId}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    throw new Error(
      `Failed to create professional details. Status: ${response.status}`
    );
  }

  return response.json();
};

// Update professional details
export const updateProfessionalDetails = async (employeeId, data) => {
  const response = await fetch(`${API_BASE_URL}/${employeeId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    throw new Error(
      `Failed to update professional details. Status: ${response.status}`
    );
  }

  return response.json();
};

// Add previous employment
export const addEmploymentHistory = async (employeeId, data) => {
  const response = await fetch(
    `${API_BASE_URL}/${employeeId}/employment-history`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    }
  );

  if (!response.ok) {
    throw new Error(
      `Failed to add employment history. Status: ${response.status}`
    );
  }

  return response.json();
};

// Get previous employment records
export const getEmploymentHistory = async (employeeId) => {
  const response = await fetch(
    `${API_BASE_URL}/${employeeId}/employment-history`
  );

  if (!response.ok) {
    throw new Error(
      `Failed to fetch employment history. Status: ${response.status}`
    );
  }

  return response.json();
};

// Delete a saved previous employment record
export const deleteEmploymentHistory = async (employeeId, historyId) => {
  const response = await fetch(
    `${API_BASE_URL}/${employeeId}/employment-history/${historyId}`,
    {
      method: "DELETE",
    }
  );

  if (!response.ok) {
    throw new Error(
      `Failed to delete employment record. Status: ${response.status}`
    );
  }

  // DELETE normally returns 204 No Content
  return true;
};
