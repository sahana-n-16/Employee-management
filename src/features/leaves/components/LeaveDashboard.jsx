import { useState, useEffect } from 'react';
import { getLeaveBalances, getLeaveHistory } from '../leaveService';
import LeaveForm from './LeaveForm';
import { toast } from 'react-hot-toast';

export default function LeaveDashboard() {
  const [balances, setBalances] = useState([]);
  const [history, setHistory] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [filterStatus, setFilterStatus] = useState('ALL');

  const fetchData = async () => {
    try {
      const balanceData = await getLeaveBalances(1);
      const historyData = await getLeaveHistory(1);
      setBalances(balanceData);
      setHistory(historyData);
    } catch (error) {
      toast.error("Failed to load leave data.");
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleApply = () => {
    setShowForm(false);
    toast.success("Leave application submitted!");
    fetchData(); // Refresh the table
  };

  const getStatusBadge = (status) => {
    if (status === 'APPROVED') return <span className="badge bg-success">Approved</span>;
    if (status === 'REJECTED') return <span className="badge bg-danger">Rejected</span>;
    return <span className="badge bg-warning text-dark">Pending</span>;
  };

  const filteredHistory = filterStatus === 'ALL' 
    ? history 
    : history.filter(leave => leave.status === filterStatus);

  return (
    <div className="container">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h3 className="fw-bold m-0" style={{ color: '#172E50' }}>Leave Management</h3>
          <p className="text-muted m-0">Manage your leave balances and history.</p>
        </div>
        <button onClick={() => setShowForm(true)} className="btn text-white px-4 py-2" style={{ backgroundColor: '#3478F6' }}>
          + Apply for Leave
        </button>
      </div>

      {/* Balance Cards */}
      <div className="row mb-4">
        {balances.map((b, index) => {
          const colors = ['#3478F6', '#10b981', '#f59e0b'];
          const cardColor = colors[index % colors.length];
          
          // Format "CASUAL" to "Casual Leave"
          const displayType = b.leaveType.charAt(0) + b.leaveType.slice(1).toLowerCase() + ' Leave';

          return (
            <div className="col-md-4" key={index}>
              <div className="card p-4 shadow-sm" style={{ backgroundColor: '#ffffff', borderTop: `4px solid ${cardColor}`, border: `1px solid ${cardColor}`, borderRadius: '12px' }}>
                <div className="d-flex justify-content-between align-items-center mb-3">
                  <h6 className="fw-bold m-0" style={{ color: cardColor }}>{displayType}</h6>
                  <span className="badge bg-light text-dark border">{b.totalAllocated} Total</span>
                </div>
                <div className="d-flex align-items-end gap-2">
                  <h2 className="fw-bold m-0" style={{ color: '#172E50' }}>{b.remaining}</h2>
                  <span className="text-muted mb-1">Days Available</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {showForm && (
        <LeaveForm 
          balances={balances} 
          onSave={handleApply} 
          onCancel={() => setShowForm(false)} 
        />
      )}

      {/* Leave History Table */}
      <div className="card p-4 shadow-sm" style={{ border: '1px solid #B0B7C3', borderLeft: '6px solid #3478F6', borderRadius: '12px' }}>
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h5 className="fw-bold m-0" style={{ color: '#172E50' }}>Leave Request History</h5>
          <select className="form-select w-auto border-secondary" value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
            <option value="ALL">All Requests</option>
            <option value="APPROVED">Approved</option>
            <option value="PENDING">Pending</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </div>

        <div className="table-responsive">
          <table className="table table-hover table-bordered align-middle">
            <thead className="table-light">
              <tr>
                <th>Leave Type</th>
                <th>From Date</th>
                <th>To Date</th>
                <th>Total Days</th>
                <th>Reason</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {filteredHistory.length === 0 ? (
                <tr><td colSpan="6" className="text-center py-4">No leave records found.</td></tr>
              ) : (
                filteredHistory.map((leave) => (
                  <tr key={leave.id}>
                    <td className="fw-bold">{leave.leaveType}</td>
                    <td>{leave.startDate}</td>
                    <td>{leave.endDate}</td>
                    <td>{leave.totalDays}</td>
                    <td>{leave.reason}</td>
                    <td>{getStatusBadge(leave.status)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}