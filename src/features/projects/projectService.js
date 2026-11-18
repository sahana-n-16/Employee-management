import axios from 'axios';

const API_URL = 'http://localhost:8080/api/projects';

export const getEmployeeProjects = async (employeeId) => {
  const response = await axios.get(`${API_URL}/employee/${employeeId}`);
  return response.data;
};

export const addProject = async (employeeId, projectData) => {
  const response = await axios.post(`${API_URL}/employee/${employeeId}`, projectData);
  return response.data;
};

export const updateProject = async (projectId, projectData) => {
  const response = await axios.put(`${API_URL}/${projectId}`, projectData);
  return response.data;
};

export const deleteProject = async (projectId) => {
  const response = await axios.delete(`${API_URL}/${projectId}`);
  return response.data;
};