import axios from 'axios';

const API_URL = 'http://localhost:8080/api/leaves';

export const getLeaveBalances = async (employeeId = 1) => {
  const response = await axios.get(`${API_URL}/balance/${employeeId}`);
  return response.data;
};

export const getLeaveHistory = async (employeeId = 1) => {
  const response = await axios.get(`${API_URL}/${employeeId}`);
  return response.data;
};

export const applyForLeave = async (leaveData) => {
  // Format data exactly as Deva's Postman expects
  const formattedData = {
    employeeId: 1,
    leaveType: leaveData.type.split(' ')[0].toUpperCase(), // Converts "Casual Leave" to "CASUAL"
    startDate: leaveData.fromDate,
    endDate: leaveData.toDate,
    reason: leaveData.reason
  };
  const response = await axios.post(`${API_URL}/apply`, formattedData);
  return response.data;
};

export const getPendingRequests = async () => {
  const response = await axios.get(`${API_URL}/admin/pending`);
  return response.data;
};

export const updateLeaveStatus = async (id, status) => {
  const endpoint = status === 'APPROVED' ? 'approve' : 'reject';
  const remarks = status === 'APPROVED' ? 'Approved' : 'Insufficient justification';
  const response = await axios.put(`${API_URL}/admin/${id}/${endpoint}?remarks=${remarks}`);
  return response.data;
};