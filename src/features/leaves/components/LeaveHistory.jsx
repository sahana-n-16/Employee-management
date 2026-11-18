import { useState } from 'react';
import { toast } from 'react-hot-toast';
import LeaveForm from './LeaveForm';

export default function LeaveHistory() {
  // MOCK DATA (We will replace this with Deva's API later)
  const [leaves, setLeaves] = useState([
    { id: 1, type: 'Sick Leave', startDate: '2026-10-01', endDate: '2026-10-02', reason: 'Fever', status: 'APPROVED' },
    { id: 2, type: 'Casual Leave', startDate: '2026-11-20', endDate: '2026-11-21', reason: 'Family function', status: 'PENDING' }
  ]);
  
  const [showForm, setShowForm] = useState(false);

  const handleApply = (newLeave) => {
    // This will eventually be an Axios call to Deva's API
    const leaveWithId = { ...newLeave, id: Date.now(), status: 'PENDING' };
    setLeaves([leaveWithId, ...leaves]);
    setShowForm(false);
    toast.success("Leave applied successfully!");
  };

  const getStatusBadge = (status) => {
    if (status === 'APPROVED') return <span className="badge bg-success">Approved</span>;
    if (status === 'REJECTED') return <span className="badge bg-danger">Rejected</span>;
    return <span className="badge bg-warning text-dark">Pending</span>;
  };

  return (
    <div className="card p-4 shadow-sm" style={{ border: '1px solid #B0B7C3', borderLeft: '6px solid #3478F6', borderRadius: '12px' }}>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h3 className="fw-bold m-0" style={{ color: '#172E50' }}>My Leaves</h3>
          <p className="text-muted m-0">View your leave history and apply for new leaves.</p>
        </div>
        {!showForm && (
          <button onClick={() => setShowForm(true)} className="btn text-white px-4 py-2" style={{ backgroundColor: '#3478F6' }}>
            + Apply for Leave
          </button>
        )}
      </div>

      {showForm && (
        <LeaveForm onSave={handleApply} onCancel={() => setShowForm(false)} />
      )}

      <div className="table-responsive mt-3">
        <table className="table table-hover table-bordered align-middle">
          <thead className="table-light">
            <tr>
              <th>Leave Type</th>
              <th>Dates</th>
              <th>Reason</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {leaves.length === 0 ? (
              <tr><td colSpan="4" className="text-center py-4">No leaves found.</td></tr>
            ) : (
              leaves.map((leave) => (
                <tr key={leave.id}>
                  <td className="fw-bold">{leave.type}</td>
                  <td>{leave.startDate} to {leave.endDate}</td>
                  <td>{leave.reason}</td>
                  <td>{getStatusBadge(leave.status)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}