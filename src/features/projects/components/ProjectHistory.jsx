import { useState, useEffect } from 'react';
import ProjectForm from './ProjectForm';
import { getEmployeeProjects, deleteProject } from '../projectService';
import { toast } from 'react-hot-toast';

export default function ProjectHistory() {
  const [showForm, setShowForm] = useState(false);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [projectToEdit, setProjectToEdit] = useState(null);
  const [viewProject, setViewProject] = useState(null);
  
  const employeeId = 1; 

  const fetchProjects = async () => {
    try {
      setLoading(true);
      const data = await getEmployeeProjects(employeeId);
      setProjects(data);
    } catch (error) {
      toast.error("Failed to load projects.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchProjects(); }, []);

  const isCurrentProject = (endDate) => (!endDate || new Date(endDate) > new Date());

  const handleDelete = async (projectId) => {
    if (window.confirm("Are you sure you want to delete this project?")) {
      try {
        await deleteProject(projectId);
        toast.success("Project deleted successfully!");
        fetchProjects();
      } catch (error) {
        toast.error("Failed to delete project.");
      }
    }
  };

  return (
    <div className="card p-4" style={{ border: '1px solid #B0B7C3', borderLeft: '6px solid #3478F6', borderRadius: '12px', boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)' }}>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h3 className="fw-bold m-0" style={{ color: '#172E50' }}>Project History</h3>
          <p className="text-muted m-0">Manage current and previous project assignments.</p>
        </div>
        {!showForm && (
          <button onClick={() => { setProjectToEdit(null); setShowForm(true); }} className="btn text-white px-4 py-2" style={{ backgroundColor: '#3478F6' }}>
            + Add New Project
          </button>
        )}
      </div>

      {showForm && (
        <ProjectForm 
          employeeId={employeeId}
          projectToEdit={projectToEdit} 
          onSave={() => { setShowForm(false); setProjectToEdit(null); fetchProjects(); }} 
          onCancel={() => { setShowForm(false); setProjectToEdit(null); }} 
        />
      )}

      <div className="table-responsive mt-3">
        <table className="table table-hover table-bordered align-middle">
          <thead className="table-light">
            <tr>
              <th>Status</th>
              <th>Project Details</th>
              <th>Timeline</th>
              <th>Employee</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="5" className="text-center py-4">Loading...</td></tr>
            ) : projects.length === 0 ? (
              <tr><td colSpan="5" className="text-center py-4">No projects found.</td></tr>
            ) : (
              projects.map((p) => (
                <tr key={p.id}>
                  <td>
                    <span className={`status-pill ${isCurrentProject(p.endDate) ? 'pill-active' : 'pill-completed'}`}>
                      {isCurrentProject(p.endDate) ? 'Active' : 'Completed'}
                    </span>
                  </td>
                  <td>
                    <div 
                      className="fw-bold" 
                      style={{ color: '#3478F6', cursor: 'pointer', textDecoration: 'underline' }}
                      onClick={() => setViewProject(p)}
                      title="Click to view Tasks & Responsibilities"
                    >
                      {p.projectCode}
                    </div>
                    <div className="text-muted small">{p.clientName}</div>
                  </td>
                  <td className="text-muted small">{p.startDate} → {p.endDate || 'Present'}</td>
                  <td>
                    <div className="d-flex align-items-center gap-2">
                      <span className="badge bg-light text-dark border rounded-circle p-2">
                        {p.reportingManager && p.reportingManager.fullName ? p.reportingManager.fullName.charAt(0).toUpperCase() : '?'}
                      </span>
                      {p.reportingManager && p.reportingManager.fullName ? p.reportingManager.fullName : 'N/A'}
                    </div>
                  </td>
                  <td>
                    <button onClick={() => { setProjectToEdit(p); setShowForm(true); }} className="btn btn-sm btn-outline-primary me-2">✎</button>
                    <button onClick={() => handleDelete(p.id)} className="btn btn-sm btn-outline-danger">🗑</button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {viewProject && (
        <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1050 }}>
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow-lg" style={{ borderRadius: '12px' }}>
              
              <div className="modal-header bg-light border-bottom-0" style={{ borderTopLeftRadius: '12px', borderTopRightRadius: '12px' }}>
                <h5 className="modal-title fw-bold" style={{ color: '#172E50' }}>
                  {viewProject.projectCode} - {viewProject.clientName}
                </h5>
                <button type="button" className="btn-close" onClick={() => setViewProject(null)}></button>
              </div>
              
              <div className="modal-body p-4">
                <div className="mb-4">
                  <h6 className="fw-bold d-flex align-items-center gap-2 mb-3" style={{ color: '#172E50' }}>
                    📋 Tasks
                  </h6>
                  <div className="p-3 rounded" style={{ backgroundColor: '#F8FAFC', borderLeft: '4px solid #3478F6' }}>
                    <p className="m-0 text-dark" style={{ lineHeight: '1.6' }}>
                      {viewProject.tasks || 'No tasks assigned yet.'}
                    </p>
                  </div>
                </div>
                
                <div>
                  <h6 className="fw-bold d-flex align-items-center gap-2 mb-3" style={{ color: '#172E50' }}>
                    🎯 Responsibilities
                  </h6>
                  <div className="p-3 rounded" style={{ backgroundColor: '#F8FAFC', borderLeft: '4px solid #10b981' }}>
                    <p className="m-0 text-dark" style={{ lineHeight: '1.6' }}>
                      {viewProject.responsibilities || 'No responsibilities assigned yet.'}
                    </p>
                  </div>
                </div>
              </div>
              
              <div className="modal-footer border-top-0">
                <button type="button" className="btn btn-secondary" onClick={() => setViewProject(null)}>Close</button>
              </div>
              
            </div>
          </div>
        </div>
      )}

    </div>
  );
}