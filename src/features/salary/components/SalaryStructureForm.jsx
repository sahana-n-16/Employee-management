
import { useCallback, useEffect, useRef, useState } from "react";

import {
  createSalaryStructure,
  getSalaryStructures,
  updateSalaryStructure,
} from "../salaryService";

const INITIAL_FORM = {
  annualCtc: "",
  effectiveFrom: "",
  effectiveTo: "",
  status: "ACTIVE",
};

function getTodayDate() {
  const today = new Date();

  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

function SalaryStructureForm({ employeeId = 1 }) {
  const [formData, setFormData] = useState({ ...INITIAL_FORM });
  const [structures, setStructures] = useState([]);
  const [editingId, setEditingId] = useState(null);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deactivatingId, setDeactivatingId] = useState(null);

  // Floating notification state
  const [notification, setNotification] = useState(null);
  const notificationTimer = useRef(null);

  // Clear notification timer
  const clearNotificationTimer = () => {
    if (notificationTimer.current) {
      clearTimeout(notificationTimer.current);
      notificationTimer.current = null;
    }
  };

  // Show floating notification
  const showNotification = (type, text) => {
    clearNotificationTimer();

    setNotification({
      type,
      text,
      id: Date.now(),
    });

    notificationTimer.current = setTimeout(() => {
      setNotification(null);
      notificationTimer.current = null;
    }, 3500);
  };

  // Close notification manually
  const closeNotification = () => {
    clearNotificationTimer();
    setNotification(null);
  };

  // Clean up timer
  useEffect(() => {
    return () => {
      if (notificationTimer.current) {
        clearTimeout(notificationTimer.current);
      }
    };
  }, []);

  // Load salary structures
  const loadStructures = useCallback(async () => {
    try {
      setLoading(true);

      const data = await getSalaryStructures(employeeId);

      setStructures(Array.isArray(data) ? data : []);
      return true;
    } catch (err) {
      showNotification(
        "error",
        err.message || "Unable to load salary structures."
      );
      return false;
    } finally {
      setLoading(false);
    }
  }, [employeeId]);

  useEffect(() => {
    loadStructures();
  }, [loadStructures]);

  // Handle input changes
  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // Reset form
  const resetForm = () => {
    setFormData({ ...INITIAL_FORM });
    setEditingId(null);
  };

  // Edit existing salary structure
  const handleEdit = (structure) => {
    setEditingId(structure.id);

    setFormData({
      annualCtc: structure.annualCtc ?? "",
      effectiveFrom: structure.effectiveFrom ?? "",
      effectiveTo: structure.effectiveTo ?? "",
      status: structure.status ?? "ACTIVE",
    });

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  // Validate salary structure
  const validateForm = () => {
    const ctcValue = String(formData.annualCtc).trim();

    const ctcPattern = /^\d{1,10}(\.\d{1,2})?$/;

    if (!ctcValue || !ctcPattern.test(ctcValue)) {
      showNotification(
        "error",
        "Enter a valid Annual CTC with up to 10 digits and 2 decimal places."
      );
      return false;
    }

    const ctc = Number(ctcValue);

    if (!Number.isFinite(ctc) || ctc <= 0) {
      showNotification(
        "error",
        "Annual CTC must be greater than zero."
      );
      return false;
    }

    if (!formData.effectiveFrom) {
      showNotification(
        "error",
        "Please select an effective from date."
      );
      return false;
    }

    if (
      formData.effectiveTo &&
      formData.effectiveTo < formData.effectiveFrom
    ) {
      showNotification(
        "error",
        "Effective to date cannot be before effective from date."
      );
      return false;
    }

    if (!["ACTIVE", "INACTIVE"].includes(formData.status)) {
      showNotification(
        "error",
        "Please select a valid salary status."
      );
      return false;
    }

    if (formData.status === "INACTIVE" && !formData.effectiveTo) {
      showNotification(
        "error",
        "Please select an Effective To date for inactive salary structures."
      );
      return false;
    }

    return true;
  };

  // Submit salary structure
  const handleSubmit = async (e) => {
    e.preventDefault();

    closeNotification();

    if (!validateForm()) {
      return;
    }

    const isEditing = editingId !== null;

    const payload = {
      annualCtc: Number(formData.annualCtc),
      effectiveFrom: formData.effectiveFrom,
      effectiveTo: formData.effectiveTo || null,
      status: formData.status,
    };

    try {
      setSaving(true);

      if (isEditing) {
        await updateSalaryStructure(editingId, payload);
      } else {
        await createSalaryStructure(employeeId, payload);
      }

      setFormData({ ...INITIAL_FORM });
      setEditingId(null);

      const refreshed = await loadStructures();

      if (refreshed) {
        showNotification(
          "success",
          isEditing
            ? "Salary structure updated successfully."
            : "Salary structure created successfully."
        );
      }
    } catch (err) {
      showNotification(
        "error",
        err.message || "Unable to save salary structure."
      );
    } finally {
      setSaving(false);
    }
  };

  // Deactivate salary structure
  const handleDeactivate = async (structure) => {
    const confirmed = window.confirm(
      `Are you sure you want to deactivate this salary structure?\n\n` +
      `Annual CTC: ₹${formatCurrency(structure.annualCtc)}\n\n` +
      `The current date will be saved as its Effective To date.`
    );

    if (!confirmed) {
      return;
    }

    const today = getTodayDate();

    if (structure.effectiveFrom > today) {
      showNotification(
        "error",
        "This salary structure cannot be deactivated before its effective start date."
      );
      return;
    }

    try {
      setDeactivatingId(structure.id);
      closeNotification();

      await updateSalaryStructure(structure.id, {
        annualCtc: Number(structure.annualCtc),
        effectiveFrom: structure.effectiveFrom,
        effectiveTo: today,
        status: "INACTIVE",
      });

      if (editingId === structure.id) {
        setFormData({ ...INITIAL_FORM });
        setEditingId(null);
      }

      const refreshed = await loadStructures();

      if (refreshed) {
        showNotification(
          "success",
          "Salary structure deactivated successfully."
        );
      }
    } catch (err) {
      showNotification(
        "error",
        err.message || "Unable to deactivate salary structure."
      );
    } finally {
      setDeactivatingId(null);
    }
  };

  // Format salary in Indian currency format
  function formatCurrency(amount) {
    return new Intl.NumberFormat("en-IN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(Number(amount));
  }

  return (
    <div className="salary-page">

      {/* Floating Toast Notification */}
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
            {notification.text}
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

        {/* Page Heading */}
        <div className="salary-page-heading">
          <div>
            <h1>Salary Management</h1>
            <p>
              Create and manage employee salary structures and annual CTC.
            </p>
          </div>
        </div>

        {/* Salary Structure Form */}
        <div className="salary-shell">

          <div className="salary-section-heading">
            <div className="salary-heading-icon">₹</div>

            <div>
              <h2>
                {editingId !== null
                  ? "Update Salary Structure"
                  : "Create Salary Structure"}
              </h2>

              <p>
                Enter the employee's compensation details below.
              </p>
            </div>
          </div>

          <form
            className="salary-form"
            onSubmit={handleSubmit}
            noValidate
          >
            <div className="salary-form-grid">

              {/* Annual CTC */}
              <div className="salary-field">
                <label htmlFor="annualCtc">
                  Annual CTC (₹)
                </label>

                <input
                  id="annualCtc"
                  type="number"
                  name="annualCtc"
                  className="salary-input"
                  placeholder="Enter annual CTC"
                  min="0.01"
                  max="9999999999.99"
                  step="0.01"
                  value={formData.annualCtc}
                  onChange={handleChange}
                  required
                  disabled={saving}
                />
              </div>

              {/* Salary Status */}
              <div className="salary-field">
                <label htmlFor="status">
                  Salary Status
                </label>

                <select
                  id="status"
                  name="status"
                  className="salary-input"
                  value={formData.status}
                  onChange={handleChange}
                  disabled={saving}
                >
                  <option value="ACTIVE">Active</option>
                  <option value="INACTIVE">Inactive</option>
                </select>
              </div>

              {/* Effective From */}
              <div className="salary-field">
                <label htmlFor="effectiveFrom">
                  Effective From
                </label>

                <input
                  id="effectiveFrom"
                  type="date"
                  name="effectiveFrom"
                  className="salary-input"
                  value={formData.effectiveFrom}
                  onChange={handleChange}
                  required
                  disabled={saving}
                />
              </div>

              {/* Effective To */}
              <div className="salary-field">
                <label htmlFor="effectiveTo">
                  Effective To <span>(Optional)</span>
                </label>

                <input
                  id="effectiveTo"
                  type="date"
                  name="effectiveTo"
                  className="salary-input"
                  value={formData.effectiveTo}
                  onChange={handleChange}
                  min={formData.effectiveFrom || undefined}
                  disabled={saving}
                />
              </div>

            </div>

            {/* Form Actions */}
            <div className="salary-form-actions">

              {editingId !== null && (
                <button
                  type="button"
                  className="salary-btn salary-btn-secondary"
                  onClick={resetForm}
                  disabled={saving}
                >
                  Cancel
                </button>
              )}

              <button
                type="submit"
                className="salary-btn salary-btn-primary"
                disabled={saving || loading}
              >
                {saving
                  ? "Saving..."
                  : editingId !== null
                    ? "Update Salary Structure"
                    : "Save Salary Structure"}
              </button>

            </div>
          </form>
        </div>

        {/* Salary Structure History */}
        <div className="salary-shell salary-list-shell">

          <div className="salary-list-heading">
            <div>
              <h2>Salary Structure History</h2>

              <p>
                Existing salary structures for Employee #{employeeId}
              </p>
            </div>

            <span className="salary-count">
              {structures.length} Records
            </span>
          </div>

          {loading ? (
            <p className="salary-empty">
              Loading salary structures...
            </p>
          ) : structures.length === 0 ? (
            <p className="salary-empty">
              No salary structures found for this employee.
            </p>
          ) : (
            <div className="salary-table-wrapper">
              <table className="salary-table">

                <thead>
                  <tr>
                    <th>Annual CTC</th>
                    <th>Effective From</th>
                    <th>Effective To</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>

                <tbody>
                  {structures.map((structure) => (
                    <tr key={structure.id}>

                      <td className="salary-amount">
                        ₹{formatCurrency(structure.annualCtc)}
                      </td>

                      <td>
                        {structure.effectiveFrom || "-"}
                      </td>

                      <td>
                        {structure.effectiveTo
                          ? structure.effectiveTo
                          : structure.status === "ACTIVE"
                            ? "Present"
                            : "Date not recorded"}
                      </td>

                      <td>
                        <span
                          className={`salary-status ${
                            structure.status === "ACTIVE"
                              ? "salary-status-active"
                              : "salary-status-inactive"
                          }`}
                        >
                          {structure.status}
                        </span>
                      </td>

                      <td>
                        <div className="salary-row-actions">

                          <button
                            type="button"
                            className="salary-edit-btn"
                            onClick={() => handleEdit(structure)}
                            disabled={
                              saving || deactivatingId !== null
                            }
                          >
                            Edit
                          </button>

                          {structure.status === "ACTIVE" && (
                            <button
                              type="button"
                              className="salary-deactivate-btn"
                              onClick={() => handleDeactivate(structure)}
                              disabled={
                                saving || deactivatingId !== null
                              }
                            >
                              {deactivatingId === structure.id
                                ? "Deactivating..."
                                : "Deactivate"}
                            </button>
                          )}

                        </div>
                      </td>

                    </tr>
                  ))}
                </tbody>

              </table>
            </div>
          )}

        </div>
      </div>
    </div>
  );
}

export default SalaryStructureForm;
