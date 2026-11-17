
import { useCallback, useEffect, useRef, useState } from "react";

import {
  getMonthlySalaries,
  createMonthlySalary,
  updateMonthlySalary,
  markMonthlySalaryAsPaid,
  deleteMonthlySalary,
} from "../salaryService";

const INITIAL_FORM = {
  month: String(new Date().getMonth() + 1),
  year: String(new Date().getFullYear()),
  basicSalary: "",
  allowances: "0",
  bonus: "0",
  deductions: "0",
};

const MONTHS = [
  "January", "February", "March", "April",
  "May", "June", "July", "August",
  "September", "October", "November", "December",
];

function MonthlySalaryForm({ employeeId = 1 }) {
  const [records, setRecords] = useState([]);
  const [formData, setFormData] = useState({ ...INITIAL_FORM });
  const [editingId, setEditingId] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [processingId, setProcessingId] = useState(null);

  const [notification, setNotification] = useState(null);
  const [filterYear, setFilterYear] = useState(
    String(new Date().getFullYear())
  );

  const [paymentDates, setPaymentDates] = useState({});
  const notificationTimer = useRef(null);

  // ========================================
  // NOTIFICATIONS
  // ========================================

  const clearNotificationTimer = () => {
    if (notificationTimer.current) {
      clearTimeout(notificationTimer.current);
      notificationTimer.current = null;
    }
  };

  const showNotification = (type, message) => {
    clearNotificationTimer();

    setNotification({ type, message, id: Date.now() });

    notificationTimer.current = setTimeout(() => {
      setNotification(null);
      notificationTimer.current = null;
    }, 3500);
  };

  const closeNotification = () => {
    clearNotificationTimer();
    setNotification(null);
  };

  useEffect(() => {
    return () => clearNotificationTimer();
  }, []);

  // ========================================
  // LOAD RECORDS
  // ========================================

  const loadRecords = useCallback(async () => {
    try {
      setLoading(true);

      const data = await getMonthlySalaries(employeeId);
      setRecords(Array.isArray(data) ? data : []);
    } catch (err) {
      showNotification(
        "error",
        err.message || "Unable to load monthly salary records."
      );
    } finally {
      setLoading(false);
    }
  }, [employeeId]);

  useEffect(() => {
    loadRecords();
  }, [loadRecords]);

  // ========================================
  // FORM AND MODAL
  // ========================================

  const openAddModal = () => {
    setEditingId(null);
    setFormData({ ...INITIAL_FORM });
    setModalOpen(true);
    closeNotification();
  };

  const closeModal = () => {
    if (saving) return;

    setModalOpen(false);
    setEditingId(null);
    setFormData({ ...INITIAL_FORM });
  };

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const handleEdit = (record) => {
    setEditingId(record.id);

    setFormData({
      month: String(record.month),
      year: String(record.year),
      basicSalary: String(record.basicSalary ?? ""),
      allowances: String(record.allowances ?? 0),
      bonus: String(record.bonus ?? 0),
      deductions: String(record.deductions ?? 0),
    });

    setModalOpen(true);
    closeNotification();
  };

  // ========================================
  // CALCULATIONS
  // ========================================

  const numberValue = (value) => Number(value || 0);

  const previewGross =
    numberValue(formData.basicSalary) +
    numberValue(formData.allowances) +
    numberValue(formData.bonus);

  const previewNet = previewGross - numberValue(formData.deductions);

  const formatCurrency = (amount) =>
    new Intl.NumberFormat("en-IN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(Number(amount || 0));

  // ========================================
  // VALIDATION
  // ========================================

  const validateForm = () => {
    const fields = [
      ["Basic Salary", formData.basicSalary],
      ["Allowances", formData.allowances],
      ["Bonus", formData.bonus],
      ["Deductions", formData.deductions],
    ];

    for (const [label, value] of fields) {
      if (String(value).trim() === "" || !Number.isFinite(Number(value))) {
        showNotification("error", `Enter a valid ${label.toLowerCase()}.`);
        return false;
      }

      if (Number(value) < 0) {
        showNotification("error", `${label} cannot be negative.`);
        return false;
      }

      if (!/^\d{1,10}(\.\d{1,2})?$/.test(String(value))) {
        showNotification(
          "error",
          `${label} must have up to 10 digits and 2 decimal places.`
        );
        return false;
      }
    }

    if (Number(formData.basicSalary) <= 0) {
      showNotification(
        "error",
        "Basic salary must be greater than zero."
      );
      return false;
    }

    if (previewNet < 0) {
      showNotification(
        "error",
        "Deductions cannot exceed gross salary."
      );
      return false;
    }

    const duplicate = records.some(
      (record) =>
        Number(record.month) === Number(formData.month) &&
        Number(record.year) === Number(formData.year) &&
        record.id !== editingId
    );

    if (duplicate) {
      showNotification(
        "error",
        "A salary record already exists for this month and year."
      );
      return false;
    }

    return true;
  };

  // ========================================
  // CREATE / UPDATE
  // ========================================

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) return;

    const isEditing = editingId !== null;

    const payload = {
      month: Number(formData.month),
      year: Number(formData.year),
      basicSalary: Number(formData.basicSalary),
      allowances: Number(formData.allowances),
      bonus: Number(formData.bonus),
      deductions: Number(formData.deductions),
    };

    try {
      setSaving(true);

      if (isEditing) {
        await updateMonthlySalary(employeeId, editingId, payload);
      } else {
        await createMonthlySalary(employeeId, {
          ...payload,
          paymentStatus: "PENDING",
        });
      }

      setModalOpen(false);
      setEditingId(null);
      setFormData({ ...INITIAL_FORM });

      await loadRecords();

      showNotification(
        "success",
        isEditing
          ? "Monthly salary updated successfully."
          : "Monthly salary record created successfully."
      );
    } catch (err) {
      showNotification(
        "error",
        err.message || "Unable to save monthly salary."
      );
    } finally {
      setSaving(false);
    }
  };

  // ========================================
  // MARK AS PAID
  // ========================================

  const handleMarkPaid = async (record) => {
    const paymentDate =
      paymentDates[record.id] || new Date().toLocaleDateString("en-CA");

    if (!paymentDate) {
      showNotification("error", "Please select a payment date.");
      return;
    }

    const confirmed = window.confirm(
      `Mark ${MONTHS[Number(record.month) - 1]} ${record.year} salary as paid?`
    );

    if (!confirmed) return;

    try {
      setProcessingId(record.id);
      closeNotification();

      await markMonthlySalaryAsPaid(
        employeeId,
        record.id,
        paymentDate
      );

      await loadRecords();

      showNotification(
        "success",
        "Monthly salary marked as paid successfully."
      );
    } catch (err) {
      showNotification(
        "error",
        err.message || "Unable to update payment status."
      );
    } finally {
      setProcessingId(null);
    }
  };

  // ========================================
  // DELETE PENDING RECORD
  // ========================================

  const handleDelete = async (record) => {
    if (record.paymentStatus === "PAID") {
      showNotification(
        "error",
        "Paid salary records cannot be deleted."
      );
      return;
    }

    const confirmed = window.confirm(
      `Delete the salary record for ${MONTHS[Number(record.month) - 1]} ${record.year}?`
    );

    if (!confirmed) return;

    try {
      setProcessingId(record.id);
      closeNotification();

      await deleteMonthlySalary(employeeId, record.id);
      await loadRecords();

      showNotification(
        "success",
        "Monthly salary record deleted successfully."
      );
    } catch (err) {
      showNotification(
        "error",
        err.message || "Unable to delete salary record."
      );
    } finally {
      setProcessingId(null);
    }
  };

  // ========================================
  // FILTER AND SUMMARY
  // ========================================

  const years = [
    ...new Set(records.map((record) => String(record.year))),
  ].sort((a, b) => Number(b) - Number(a));

  const filteredRecords = records
    .filter((record) => String(record.year) === filterYear)
    .sort(
      (a, b) =>
        Number(b.year) - Number(a.year) ||
        Number(b.month) - Number(a.month)
    );

  const paidCount = filteredRecords.filter(
    (record) => record.paymentStatus === "PAID"
  ).length;

  const pendingCount = filteredRecords.filter(
    (record) => record.paymentStatus === "PENDING"
  ).length;

  const totalNetPaid = filteredRecords
    .filter((record) => record.paymentStatus === "PAID")
    .reduce(
      (total, record) => total + Number(record.netSalary || 0),
      0
    );

  const totalPending = filteredRecords
    .filter((record) => record.paymentStatus === "PENDING")
    .reduce(
      (total, record) => total + Number(record.netSalary || 0),
      0
    );

  // ========================================
  // UI
  // ========================================

  return (
    <div className="salary-page monthly-salary-page">

      {/* FLOATING NOTIFICATION */}
      {notification && (
        <div
          key={notification.id}
          className={`salary-toast ${
            notification.type === "success"
              ? "salary-toast-success"
              : "salary-toast-error"
          }`}
          role={notification.type === "success" ? "status" : "alert"}
        >
          <div className="salary-toast-icon">
            {notification.type === "success" ? "✓" : "!"}
          </div>

          <div className="salary-toast-message">
            {notification.message}
          </div>

          <button
            type="button"
            className="salary-toast-close"
            onClick={closeNotification}
            aria-label="Close notification"
          >
            ×
          </button>
        </div>
      )}

      <div className="salary-page-content">

        {/* HEADING */}
        <div className="salary-page-heading">
          <h1>Monthly Salary</h1>
          <p>
            Generate, manage and track employee monthly salary payments.
          </p>
        </div>

        {/* YEAR FILTER */}
        <div className="salary-monthly-toolbar">
          <div className="salary-field">
            <label htmlFor="salaryYearFilter">Select Year</label>
            <select
              id="salaryYearFilter"
              className="salary-input"
              value={filterYear}
              onChange={(e) => setFilterYear(e.target.value)}
            >
              {[...new Set([
                String(new Date().getFullYear()),
                ...years,
              ])]
                .sort((a, b) => Number(b) - Number(a))
                .map((year) => (
                  <option key={year} value={year}>{year}</option>
                ))}
            </select>
          </div>

          <button
            type="button"
            className="salary-btn salary-btn-primary"
            onClick={openAddModal}
            disabled={loading || saving}
          >
            + Generate Monthly Salary
          </button>
        </div>

        {/* SUMMARY CARDS */}
        <div className="salary-monthly-summary">

          <div className="salary-summary-card">
            <span>Paid Records</span>
            <strong className="salary-total-earning">
              {paidCount}
            </strong>
            <small>₹{formatCurrency(totalNetPaid)} paid</small>
          </div>

          <div className="salary-summary-card">
            <span>Pending Records</span>
            <strong className="salary-total-deduction">
              {pendingCount}
            </strong>
            <small>₹{formatCurrency(totalPending)} pending</small>
          </div>

          <div className="salary-summary-card">
            <span>Total Records</span>
            <strong>{filteredRecords.length}</strong>
            <small>For {filterYear}</small>
          </div>

        </div>

        {/* MONTHLY SALARY TABLE */}
        <div className="salary-shell salary-list-shell">

          <div className="salary-list-heading">
            <div>
              <h2>Monthly Salary History</h2>
              <p>
                Salary records and payment status for {filterYear}.
              </p>
            </div>

            <span className="salary-count">
              {filteredRecords.length} Records
            </span>
          </div>

          {loading ? (
            <p className="salary-empty">Loading monthly salary records...</p>
          ) : filteredRecords.length === 0 ? (
            <div className="salary-component-empty">
              <div className="salary-component-empty-icon">₹</div>
              <h3>No monthly salary records</h3>
              <p>
                Generate a salary record to get started for {filterYear}.
              </p>
              <button
                type="button"
                className="salary-btn salary-btn-primary"
                onClick={openAddModal}
              >
                + Generate Salary
              </button>
            </div>
          ) : (
            <div className="salary-table-wrapper">
              <table className="salary-table salary-monthly-table">
                <thead>
                  <tr>
                    <th>Month</th>
                    <th>Basic Salary</th>
                    <th>Gross Salary</th>
                    <th>Deductions</th>
                    <th>Net Salary</th>
                    <th>Payment</th>
                    <th>Payment Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>

                <tbody>
                  {filteredRecords.map((record) => (
                    <tr key={record.id}>
                      <td>
                        <strong>
                          {MONTHS[Number(record.month) - 1]} {record.year}
                        </strong>
                      </td>

                      <td>₹{formatCurrency(record.basicSalary)}</td>

                      <td>₹{formatCurrency(record.grossSalary)}</td>

                      <td>₹{formatCurrency(record.deductions)}</td>

                      <td className="salary-amount">
                        ₹{formatCurrency(record.netSalary)}
                      </td>

                      <td>
                        <span
                          className={`salary-component-type ${
                            record.paymentStatus === "PAID"
                              ? "salary-component-earning"
                              : "salary-component-deduction"
                          }`}
                        >
                          {record.paymentStatus}
                        </span>
                      </td>

                      <td>
                        {record.paymentDate || "—"}
                      </td>

                      <td>
                        <div className="salary-row-actions">
                          {record.paymentStatus === "PENDING" && (
                            <>
                              <button
                                type="button"
                                className="salary-edit-btn"
                                onClick={() => handleEdit(record)}
                                disabled={processingId !== null}
                              >
                                Edit
                              </button>

                              <button
                                type="button"
                                className="salary-mark-paid-btn"
                                onClick={() => handleMarkPaid(record)}
                                disabled={processingId !== null}
                              >
                                {processingId === record.id
                                  ? "Processing..."
                                  : "Mark Paid"}
                              </button>

                              <button
                                type="button"
                                className="salary-deactivate-btn"
                                onClick={() => handleDelete(record)}
                                disabled={processingId !== null}
                              >
                                Delete
                              </button>
                            </>
                          )}

                          {record.paymentStatus === "PAID" && (
                            <span className="salary-paid-label">
                              Completed
                            </span>
                          )}
                        </div>

                        {record.paymentStatus === "PENDING" && (
                          <div className="salary-payment-date-control">
                            <label htmlFor={`payment-${record.id}`}>
                              Payment date
                            </label>
                            <input
                              id={`payment-${record.id}`}
                              type="date"
                              value={
                                paymentDates[record.id] ||
                                new Date().toLocaleDateString("en-CA")
                              }
                              onChange={(e) =>
                                setPaymentDates((previous) => ({
                                  ...previous,
                                  [record.id]: e.target.value,
                                }))
                              }
                              disabled={processingId !== null}
                            />
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* CREATE / EDIT MODAL */}
      {modalOpen && (
        <div
          className="salary-modal-overlay"
          onMouseDown={(e) => {
            if (e.target === e.currentTarget) closeModal();
          }}
        >
          <div
            className="salary-modal monthly-salary-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="monthlySalaryModalTitle"
          >
            <div className="salary-modal-header">
              <div>
                <h2 id="monthlySalaryModalTitle">
                  {editingId !== null
                    ? "Edit Monthly Salary"
                    : "Generate Monthly Salary"}
                </h2>
                <p>Enter the monthly salary breakdown.</p>
              </div>

              <button
                type="button"
                className="salary-modal-close"
                onClick={closeModal}
                disabled={saving}
                aria-label="Close modal"
              >
                ×
              </button>
            </div>

            <form onSubmit={handleSubmit} noValidate>
              <div className="salary-modal-body">

                <div className="salary-form-grid">
                  <div className="salary-field">
                    <label htmlFor="salaryMonth">Month</label>
                    <select
                      id="salaryMonth"
                      name="month"
                      className="salary-input"
                      value={formData.month}
                      onChange={handleChange}
                      disabled={saving || editingId !== null}
                    >
                      {MONTHS.map((month, index) => (
                        <option key={month} value={index + 1}>
                          {month}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="salary-field">
                    <label htmlFor="salaryYear">Year</label>
                    <input
                      id="salaryYear"
                      type="number"
                      name="year"
                      className="salary-input"
                      min="2000"
                      max="2100"
                      value={formData.year}
                      onChange={handleChange}
                      disabled={saving || editingId !== null}
                    />
                  </div>
                </div>

                <div className="salary-form-grid">
                  <div className="salary-field">
                    <label htmlFor="basicSalary">Basic Salary (₹)</label>
                    <input
                      id="basicSalary"
                      type="number"
                      name="basicSalary"
                      className="salary-input"
                      min="0.01"
                      step="0.01"
                      value={formData.basicSalary}
                      onChange={handleChange}
                      placeholder="Enter basic salary"
                      disabled={saving}
                    />
                  </div>

                  <div className="salary-field">
                    <label htmlFor="allowances">Allowances (₹)</label>
                    <input
                      id="allowances"
                      type="number"
                      name="allowances"
                      className="salary-input"
                      min="0"
                      step="0.01"
                      value={formData.allowances}
                      onChange={handleChange}
                      disabled={saving}
                    />
                  </div>

                  <div className="salary-field">
                    <label htmlFor="bonus">Bonus (₹)</label>
                    <input
                      id="bonus"
                      type="number"
                      name="bonus"
                      className="salary-input"
                      min="0"
                      step="0.01"
                      value={formData.bonus}
                      onChange={handleChange}
                      disabled={saving}
                    />
                  </div>

                  <div className="salary-field">
                    <label htmlFor="deductions">Deductions (₹)</label>
                    <input
                      id="deductions"
                      type="number"
                      name="deductions"
                      className="salary-input"
                      min="0"
                      step="0.01"
                      value={formData.deductions}
                      onChange={handleChange}
                      disabled={saving}
                    />
                  </div>
                </div>

                {/* CALCULATION PREVIEW */}
                <div className="monthly-calculation-preview">
                  <div>
                    <span>Gross Salary</span>
                    <strong>₹{formatCurrency(previewGross)}</strong>
                  </div>

                  <div>
                    <span>Net Salary</span>
                    <strong>₹{formatCurrency(previewNet)}</strong>
                  </div>
                </div>

                <p className="monthly-calculation-note">
                  Gross = Basic Salary + Allowances + Bonus.
                  Net = Gross Salary − Deductions.
                  The backend recalculates these values when saving.
                </p>
              </div>

              <div className="salary-modal-actions">
                <button
                  type="button"
                  className="salary-btn salary-btn-secondary"
                  onClick={closeModal}
                  disabled={saving}
                >
                  Cancel
                </button>

                <button
                  type="submit"
                  className="salary-btn salary-btn-primary"
                  disabled={saving}
                >
                  {saving
                    ? "Saving..."
                    : editingId !== null
                      ? "Update Salary"
                      : "Generate Salary"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default MonthlySalaryForm;
