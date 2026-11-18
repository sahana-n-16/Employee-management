import { useState, useEffect } from 'react';
import { applyForLeave } from '../leaveService';

export default function LeaveForm({ balances, onSave, onCancel }) {
  const [formData, setFormData] = useState({
    type: 'Casual Leave',
    fromDate: '',
    toDate: '',
    reason: ''
  });
  const [error, setError] = useState('');
  const [totalDays, setTotalDays] = useState(0); 
  const [loading, setLoading] = useState(false);

  // Auto-calculate days when dates change
  useEffect(() => {
    if (formData.fromDate && formData.toDate) {
      const start = new Date(formData.fromDate);
      const end = new Date(formData.toDate);
      
      if (start <= end) {
        const timeDiff = end.getTime() - start.getTime();
        const daysDiff = Math.ceil(timeDiff / (1000 * 3600 * 24)) + 1;
        setTotalDays(daysDiff);
      } else {
        setTotalDays(0);
      }
    } else {
      setTotalDays(0);
    }
  }, [formData.fromDate, formData.toDate]);

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // 1. Check if dates are valid
    if (new Date(formData.fromDate) > new Date(formData.toDate)) {
      setError("From Date cannot be after To Date.");
      return;
    }

    // 2. Check if reason is not just numbers
    if (/^\d+$/.test(formData.reason.trim())) {
      setError("Reason cannot be just numbers. Please explain your leave.");
      return;
    }

    // 3. Check if user has enough leave balance (Using Deva's 'remaining' field)
    const selectedLeave = balances ? balances.find(b => b.leaveType === formData.type.split(' ')[0].toUpperCase()) : null;
    if (selectedLeave) {
      if (totalDays > selectedLeave.remaining) {
        setError(`Insufficient balance. You only have ${selectedLeave.remaining} days left for ${formData.type}.`);
        return;
      }
    }

    try {
      setLoading(true);
      await applyForLeave({ ...formData, totalDays });
      onSave();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to apply for leave.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal fade show d-block" style={{ backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 1050 }}>
      <div className="modal-dialog modal-lg modal-dialog-centered">
        <div className="modal-content border-0 shadow-lg" style={{ borderRadius: '12px' }}>
          
          <div className="modal-header bg-light border-bottom-0" style={{ borderTopLeftRadius: '12px', borderTopRightRadius: '12px' }}>
            <h5 className="modal-title fw-bold" style={{ color: '#172E50' }}>Apply for Leave</h5>
            <button type="button" className="btn-close" onClick={onCancel}></button>
          </div>
          
          <form onSubmit={handleSubmit}>
            <div className="modal-body p-4">
              {error && <div className="alert alert-danger py-2">{error}</div>}
              
              <div className="row g-3">
                <div className="col-12">
                  <label className="form-label fw-semibold small">Leave Type *</label>
                  <select required name="type" className="form-select" value={formData.type} onChange={handleChange}>
                    <option value="Casual Leave">Casual Leave</option>
                    <option value="Sick Leave">Sick Leave</option>
                    <option value="Earned Leave">Earned Leave</option>
                  </select>
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold small">From Date *</label>
                  <input required type="date" name="fromDate" className="form-control" value={formData.fromDate} onChange={handleChange} min={new Date().toISOString().split('T')[0]} />
                </div>
                <div className="col-md-6">
                  <label className="form-label fw-semibold small">To Date *</label>
                  <input required type="date" name="toDate" className="form-control" value={formData.toDate} onChange={handleChange} min={formData.fromDate || new Date().toISOString().split('T')[0]} />
                </div>

                {totalDays > 0 && (
                  <div className="col-12">
                    <div className="alert alert-info py-2 m-0">
                      Total Days Applied: <strong>{totalDays}</strong>
                    </div>
                  </div>
                )}

                <div className="col-12">
                  <label className="form-label fw-semibold small">Reason *</label>
                  <textarea required name="reason" className="form-control" rows="3" value={formData.reason} onChange={handleChange} placeholder="Why do you need this leave?"></textarea>
                </div>
              </div>
            </div>
            
            <div className="modal-footer border-top-0 pt-0 pb-4 px-4">
              <button type="button" className="btn-cancel" onClick={onCancel}>Cancel</button>
              <button type="submit" className="btn-save" disabled={loading}>
                {loading ? 'Submitting...' : 'Submit Application'}
              </button>
            </div>
          </form>
          
        </div>
      </div>
    </div>
  );
}