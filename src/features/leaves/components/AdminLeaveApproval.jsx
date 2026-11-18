import { useState, useEffect } from 'react';
import { getPendingRequests, updateLeaveStatus } from '../leaveService';
import { toast } from 'react-hot-toast';

export default function AdminLeaveApproval() {
  const [requests, setRequests] = useState([]);

  const fetchRequests = async () => {
    try {
      const data = await getPendingRequests();
      setRequests(data);
    } catch (error) {
      toast.error("Failed to load requests.");
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const handleAction = async (id, status) => {
    if (window.confirm(`Are you sure you want to ${status.toLowerCase()} this leave?`)) {
      try {
        await updateLeaveStatus(id, status);
        toast.success(`Leave ${status.toLowerCase()} successfully!`);
        fetchRequests(); // Refresh list
      } catch (error) {
        toast.error(error.response?.data?.message || "Action failed.");
      }
    }
  };

  return (
    <div className="card p-4 shadow-sm" style={{ border: '1px solid #B0B7C3', borderLeft: '6px solid #f59e0b', borderRadius: '12px' }}>
      <div className="mb-4">
        <h3 className="fw-bold m-0" style={{ color: '#172E50' }}>Pending Leave Requests</h3>
        <p className="text-muted m-0">Review and manage employee leave applications.</p>
      </div>

      <div className="table-responsive">
        <table className="table table-hover table-bordered align-middle">
          <thead className="table-light">
            <tr>
              <th>Employee ID</th>
              <th>Leave Type</th>
              <th>Dates</th>
              <th>Days</th>
              <th>Reason</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {requests.length === 0 ? (
              <tr><td colSpan="6" className="text-center py-4">No pending requests right now.</td></tr>
            ) : (
              requests.map((req) => (
                <tr key={req.id}>
                  <td className="fw-bold">EMP-{req.employeeId}</td>
                  <td>{req.leaveType}</td>
                  <td>{req.startDate} to {req.endDate}</td>
                  <td>{req.totalDays}</td>
                  <td>{req.reason}</td>
                  <td>
                    <button 
                      onClick={() => handleAction(req.id, 'APPROVED')} 
                      className="btn btn-sm me-2"
                      style={{ backgroundColor: '#ecfdf5', color: '#10b981', border: '1px solid #10b981', fontWeight: 'bold', borderRadius: '6px' }}
                    >
                      ✓ Approve
                    </button>
                    <button 
                      onClick={() => handleAction(req.id, 'REJECTED')} 
                      className="btn btn-sm"
                      style={{ backgroundColor: '#fef2f2', color: '#ef4444', border: '1px solid #ef4444', fontWeight: 'bold', borderRadius: '6px' }}
                    >
                      ✕ Reject
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}