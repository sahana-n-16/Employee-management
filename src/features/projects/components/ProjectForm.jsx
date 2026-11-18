import { useState } from 'react';
import { addProject, updateProject } from '../projectService';
import { toast } from 'react-hot-toast';

export default function ProjectForm({ employeeId, projectToEdit, onSave, onCancel }) {
  const [formData, setFormData] = useState({
    projectCode: projectToEdit ? projectToEdit.projectCode : '',
    clientName: projectToEdit ? projectToEdit.clientName : '',
    startDate: projectToEdit ? projectToEdit.startDate : '',
    endDate: projectToEdit ? projectToEdit.endDate : '',
    reportingManagerId: projectToEdit && projectToEdit.reportingManager ? projectToEdit.reportingManager.id : '',
    tasks: projectToEdit ? projectToEdit.tasks : '',
    responsibilities: projectToEdit ? projectToEdit.responsibilities : ''
    // REMOVED: progress
  });
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      if (projectToEdit) {
        await updateProject(projectToEdit.id, formData);
        toast.success("Project updated successfully!");
      } else {
        await addProject(employeeId, formData);
        toast.success("Project added successfully!");
      }
      onSave();
    } catch (err) {
      if (err.response && err.response.data) {
        toast.error(err.response.data);
      } else {
        toast.error("Failed to save project.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1050 }}>
      <div className="modal-dialog modal-lg modal-dialog-centered">
        <div className="modal-content border-0 shadow-lg" style={{ borderRadius: '12px' }}>
          
          <div className="modal-header bg-light border-bottom-0" style={{ borderTopLeftRadius: '12px', borderTopRightRadius: '12px' }}>
            <h5 className="modal-title fw-bold" style={{ color: '#172E50' }}>
              {projectToEdit ? 'Edit Project' : 'Add New Project'}
            </h5>
            <button type="button" className="btn-close" onClick={onCancel}></button>
          </div>
          
          <form onSubmit={handleSubmit}>
            <div className="modal-body p-4">
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label fw-semibold small">Project Code *</label>
                  <input required type="text" name="projectCode" className="form-control" value={formData.projectCode} onChange={handleChange} placeholder="e.g. PRJ-001" autoComplete="off" />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold small">Client/Project Name *</label>
                  <input required type="text" name="clientName" className="form-control" value={formData.clientName} onChange={handleChange} placeholder="e.g. Acme Corp" />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold small">Start Date *</label>
                  <input required type="date" name="startDate" className="form-control" value={formData.startDate} onChange={handleChange} min={new Date().toISOString().split('T')[0]} />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold small">End Date</label>
                  <input type="date" name="endDate" className="form-control" value={formData.endDate} onChange={handleChange} min={new Date().toISOString().split('T')[0]} />
                </div>
                <div className="col-12">
                  <label className="form-label fw-semibold small">Employee ID *</label>
                  <input required type="number" name="reportingManagerId" className="form-control" value={formData.reportingManagerId} onChange={handleChange} placeholder="e.g. 1" />
                </div>

                <div className="col-12">
                  <label className="form-label fw-semibold small">Tasks</label>
                  <textarea name="tasks" className="form-control" rows="2" value={formData.tasks} onChange={handleChange} placeholder="e.g. Frontend Development"></textarea>
                </div>
                <div className="col-12">
                  <label className="form-label fw-semibold small">Responsibilities</label>
                  <textarea name="responsibilities" className="form-control" rows="2" value={formData.responsibilities} onChange={handleChange} placeholder="e.g. Leading the UI team"></textarea>
                </div>
                {/* REMOVED: Progress (%) input field */}
              </div>
            </div>
            
            <div className="modal-footer border-top-0 pt-0 pb-4 px-4">
              <button type="button" className="btn-cancel" onClick={onCancel}>Cancel</button>
              <button type="submit" className="btn-save" disabled={loading}>
                {loading ? 'Saving...' : (projectToEdit ? 'Update Project' : 'Save Project')}
              </button>
            </div>
          </form>
          
        </div>
      </div>
    </div>
  );
}