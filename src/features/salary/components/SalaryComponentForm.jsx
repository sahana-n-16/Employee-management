
import { useCallback, useEffect, useRef, useState } from "react";

import {
  getSalaryStructures,
  getSalaryComponents,
  createSalaryComponent,
  updateSalaryComponent,
  deleteSalaryComponent,
} from "../salaryService";

const INITIAL_FORM = {
  componentName: "",
  componentType: "EARNING",
  annualAmount: "",
};

function SalaryComponentForm({ employeeId = 1 }) {
  const [structures, setStructures] = useState([]);
  const [selectedStructureId, setSelectedStructureId] = useState("");
  const [components, setComponents] = useState([]);

  const [formData, setFormData] = useState({ ...INITIAL_FORM });
  const [editingId, setEditingId] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);

  const [loading, setLoading] = useState(true);
  const [componentsLoading, setComponentsLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);

  const [notification, setNotification] = useState(null);
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

    setNotification({
      type,
      message,
      id: Date.now(),
    });

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
  // LOAD SALARY STRUCTURES
  // ========================================

  const loadStructures = useCallback(async () => {
    try {
      setLoading(true);

      const data = await getSalaryStructures(employeeId);
      const list = Array.isArray(data) ? data : [];

      setStructures(list);

      setSelectedStructureId((currentId) => {
        if (
          currentId &&
          list.some(
            (item) => String(item.id) === String(currentId)
          )
        ) {
          return currentId;
        }

        const activeStructure = list.find(
          (item) => item.status === "ACTIVE"
        );

        return String(activeStructure?.id ?? list[0]?.id ?? "");
      });
    } catch (err) {
      showNotification(
        "error",
        err.message || "Unable to load salary structures."
      );
    } finally {
      setLoading(false);
    }
  }, [employeeId]);

  useEffect(() => {
    loadStructures();
  }, [loadStructures]);

  // ========================================
  // LOAD SALARY COMPONENTS
  // ========================================

  const loadComponents = useCallback(async () => {
    if (!selectedStructureId) {
      setComponents([]);
      return;
    }

    try {
      setComponentsLoading(true);

      const data = await getSalaryComponents(selectedStructureId);

      setComponents(Array.isArray(data) ? data : []);
    } catch (err) {
      setComponents([]);

      showNotification(
        "error",
        err.message || "Unable to load salary components."
      );
    } finally {
      setComponentsLoading(false);
    }
  }, [selectedStructureId]);

  useEffect(() => {
    loadComponents();
  }, [loadComponents]);

  // ========================================
  // FORMAT CURRENCY
  // ========================================

  const formatCurrency = (amount) =>
    new Intl.NumberFormat("en-IN", {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(Number(amount || 0));

  // ========================================
  // CALCULATE TOTALS
  // ========================================

  const totalEarnings = components
    .filter((item) => item.componentType === "EARNING")
    .reduce(
      (total, item) => total + Number(item.annualAmount || 0),
      0
    );

  const totalDeductions = components
    .filter((item) => item.componentType === "DEDUCTION")
    .reduce(
      (total, item) => total + Number(item.annualAmount || 0),
      0
    );

  const netAmount = totalEarnings - totalDeductions;

  const selectedStructure = structures.find(
    (item) => String(item.id) === String(selectedStructureId)
  );

  // ========================================
  // MODAL CONTROLS
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

  const handleStructureChange = (e) => {
    setSelectedStructureId(e.target.value);
    setModalOpen(false);
    setEditingId(null);
    setFormData({ ...INITIAL_FORM });
  };

  // ========================================
  // VALIDATION
  // ========================================

  const validateForm = () => {
    const name = formData.componentName.trim();
    const amount = String(formData.annualAmount).trim();

    if (!name) {
      showNotification("error", "Please enter a component name.");
      return false;
    }

    if (name.length > 100) {
      showNotification(
        "error",
        "Component name cannot exceed 100 characters."
      );
      return false;
    }

    const duplicate = components.some(
      (component) =>
        component.componentName?.trim().toLowerCase() ===
          name.toLowerCase() &&
        component.id !== editingId
    );

    if (duplicate) {
      showNotification(
        "error",
        "This component already exists in this salary structure."
      );
      return false;
    }

    if (!["EARNING", "DEDUCTION"].includes(formData.componentType)) {
      showNotification("error", "Please select a valid component type.");
      return false;
    }

    const amountPattern = /^\d{1,10}(\.\d{1,2})?$/;

    if (!amount || !amountPattern.test(amount)) {
      showNotification(
        "error",
        "Enter a valid amount with up to 2 decimal places."
      );
      return false;
    }

    if (!Number.isFinite(Number(amount)) || Number(amount) <= 0) {
      showNotification(
        "error",
        "Annual amount must be greater than zero."
      );
      return false;
    }

    return true;
  };

  // ========================================
  // ADD / UPDATE COMPONENT
  // ========================================

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!selectedStructureId) {
      showNotification("error", "Please select a salary structure.");
      return;
    }

    if (!validateForm()) return;

    const isEditing = editingId !== null;

    const payload = {
      componentName: formData.componentName.trim(),
      componentType: formData.componentType,
      annualAmount: Number(formData.annualAmount),
    };

    try {
      setSaving(true);

      if (isEditing) {
        await updateSalaryComponent(
          selectedStructureId,
          editingId,
          payload
        );
      } else {
        await createSalaryComponent(
          selectedStructureId,
          payload
        );
      }

      setModalOpen(false);
      setEditingId(null);
      setFormData({ ...INITIAL_FORM });

      await loadComponents();

      showNotification(
        "success",
        isEditing
          ? "Salary component updated successfully."
          : "Salary component added successfully."
      );
    } catch (err) {
      showNotification(
        "error",
        err.message || "Unable to save salary component."
      );
    } finally {
      setSaving(false);
    }
  };

  // ========================================
  // EDIT COMPONENT
  // ========================================

  const handleEdit = (component) => {
    setEditingId(component.id);

    setFormData({
      componentName: component.componentName ?? "",
      componentType: component.componentType ?? "EARNING",
      annualAmount: String(component.annualAmount ?? ""),
    });

    setModalOpen(true);
    closeNotification();
  };

  // ========================================
  // DELETE COMPONENT
  // ========================================

  const handleDelete = async (component) => {
    const confirmed = window.confirm(
      `Are you sure you want to delete "${component.componentName}"?`
    );

    if (!confirmed) return;

    try {
      setDeletingId(component.id);
      closeNotification();

      await deleteSalaryComponent(
        selectedStructureId,
        component.id
      );

      if (editingId === component.id) {
        setEditingId(null);
        setModalOpen(false);
        setFormData({ ...INITIAL_FORM });
      }

      await loadComponents();

      showNotification(
        "success",
        "Salary component deleted successfully."
      );
    } catch (err) {
      showNotification(
        "error",
        err.message || "Unable to delete salary component."
      );
    } finally {
      setDeletingId(null);
    }
  };

  // ========================================
  // UI
  // ========================================

  return (
    <div className="salary-page salary-components-page">

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

        {/* PAGE HEADING */}

        <div className="salary-page-heading">
          <h1>Salary Components</h1>
          <p>
            Manage employee earnings and deductions for each salary structure.
          </p>
        </div>

        {loading ? (
          <div className="salary-shell">
            <p className="salary-empty">
              Loading salary structures...
            </p>
          </div>
        ) : structures.length === 0 ? (
          <div className="salary-shell">
            <p className="salary-empty">
              No salary structures found. Please create a salary structure first.
            </p>
          </div>
        ) : (
          <>
            {/* =================================
                SUMMARY CARDS - FIRST
            ================================= */}

            <div className="salary-component-summary">

              <div className="salary-summary-card">
                <span>Total Earnings</span>
                <strong className="salary-total-earning">
                  ₹{formatCurrency(totalEarnings)}
                </strong>
              </div>

              <div className="salary-summary-card">
                <span>Total Deductions</span>
                <strong className="salary-total-deduction">
                  ₹{formatCurrency(totalDeductions)}
                </strong>
              </div>

              <div className="salary-summary-card">
                <span>Net Component Amount</span>
                <strong>
                  ₹{formatCurrency(netAmount)}
                </strong>
              </div>

            </div>

            {/* =================================
                SELECTED SALARY STRUCTURE
            ================================= */}

            <div className="salary-shell salary-compact-structure">

              <div className="salary-compact-top">
                <div>
                  <h2>Selected Salary Structure</h2>
                  <p>
                    Choose which salary structure you want to manage.
                  </p>
                </div>

                {selectedStructure && (
                  <span
                    className={`salary-structure-status ${
                      selectedStructure.status === "ACTIVE"
                        ? "salary-status-active"
                        : "salary-status-inactive"
                    }`}
                  >
                    {selectedStructure.status}
                  </span>
                )}
              </div>

              <div className="salary-field">
                <label htmlFor="componentStructure">
                  Salary Structure
                </label>

                <select
                  id="componentStructure"
                  className="salary-input"
                  value={selectedStructureId}
                  onChange={handleStructureChange}
                  disabled={saving || deletingId !== null}
                >
                  {structures.map((structure) => (
                    <option key={structure.id} value={structure.id}>
                      ₹{formatCurrency(structure.annualCtc)}
                      {" | "}
                      {structure.effectiveFrom}
                      {" | "}
                      {structure.status}
                    </option>
                  ))}
                </select>
              </div>

              {selectedStructure && (
                <div className="salary-structure-meta">

                  <div>
                    <span>Annual CTC</span>
                    <strong>
                      ₹{formatCurrency(selectedStructure.annualCtc)}
                    </strong>
                  </div>

                  <div>
                    <span>Effective From</span>
                    <strong>{selectedStructure.effectiveFrom}</strong>
                  </div>

                  <div>
                    <span>Effective To</span>
                    <strong>
                      {selectedStructure.effectiveTo ||
                        (selectedStructure.status === "ACTIVE"
                          ? "Present"
                          : "Date not recorded")}
                    </strong>
                  </div>

                </div>
              )}
            </div>

            {/* =================================
                SALARY COMPONENT TABLE
            ================================= */}

            <div className="salary-shell salary-list-shell">

              <div className="salary-list-heading">
                <div>
                  <h2>Salary Components</h2>
                  <p>
                    Earnings and deductions assigned to this salary structure.
                  </p>
                </div>

                <button
                  type="button"
                  className="salary-btn salary-btn-primary salary-add-component-btn"
                  onClick={openAddModal}
                  disabled={
                    saving ||
                    deletingId !== null ||
                    componentsLoading
                  }
                >
                  <span>+</span> Add Component
                </button>
              </div>

              <div className="salary-component-count">
                {components.length}{" "}
                {components.length === 1 ? "Component" : "Components"}
              </div>

              {componentsLoading ? (
                <p className="salary-empty">
                  Loading salary components...
                </p>
              ) : components.length === 0 ? (
                <div className="salary-component-empty">
                  <div className="salary-component-empty-icon">
                    ▤
                  </div>

                  <h3>No components added yet</h3>

                  <p>
                    Add earnings and deductions to build this salary structure.
                  </p>

                  <button
                    type="button"
                    className="salary-btn salary-btn-primary"
                    onClick={openAddModal}
                  >
                    + Add First Component
                  </button>
                </div>
              ) : (
                <div className="salary-table-wrapper">

                  <table className="salary-table salary-component-table">

                    <thead>
                      <tr>
                        <th>Component Name</th>
                        <th>Type</th>
                        <th>Annual Amount</th>
                        <th>Action</th>
                      </tr>
                    </thead>

                    <tbody>
                      {components.map((component) => (
                        <tr key={component.id}>

                          <td>
                            <strong>{component.componentName}</strong>
                          </td>

                          <td>
                            <span
                              className={`salary-component-type ${
                                component.componentType === "EARNING"
                                  ? "salary-component-earning"
                                  : "salary-component-deduction"
                              }`}
                            >
                              {component.componentType}
                            </span>
                          </td>

                          <td className="salary-amount">
                            ₹{formatCurrency(component.annualAmount)}
                          </td>

                          <td>
                            <div className="salary-row-actions">

                              <button
                                type="button"
                                className="salary-edit-btn"
                                onClick={() => handleEdit(component)}
                                disabled={
                                  saving || deletingId !== null
                                }
                              >
                                Edit
                              </button>

                              <button
                                type="button"
                                className="salary-deactivate-btn"
                                onClick={() => handleDelete(component)}
                                disabled={
                                  saving || deletingId !== null
                                }
                              >
                                {deletingId === component.id
                                  ? "Deleting..."
                                  : "Delete"}
                              </button>

                            </div>
                          </td>

                        </tr>
                      ))}
                    </tbody>

                  </table>

                </div>
              )}

            </div>
          </>
        )}
      </div>

      {/* =====================================
          ADD / EDIT MODAL
      ===================================== */}

      {modalOpen && (
        <div
          className="salary-modal-overlay"
          onMouseDown={(e) => {
            if (e.target === e.currentTarget) {
              closeModal();
            }
          }}
        >
          <div
            className="salary-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="salaryModalTitle"
          >

            <div className="salary-modal-header">

              <div>
                <h2 id="salaryModalTitle">
                  {editingId !== null
                    ? "Edit Salary Component"
                    : "Add Salary Component"}
                </h2>

                <p>
                  Enter the component details below.
                </p>
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

                <div className="salary-field">
                  <label htmlFor="componentName">
                    Component Name
                  </label>

                  <input
                    id="componentName"
                    type="text"
                    name="componentName"
                    className="salary-input"
                    placeholder="e.g. Basic Salary, HRA, EPF"
                    value={formData.componentName}
                    onChange={handleChange}
                    maxLength={100}
                    autoFocus
                    disabled={saving}
                  />
                </div>

                <div className="salary-field">
                  <label htmlFor="componentType">
                    Component Type
                  </label>

                  <select
                    id="componentType"
                    name="componentType"
                    className="salary-input"
                    value={formData.componentType}
                    onChange={handleChange}
                    disabled={saving}
                  >
                    <option value="EARNING">Earning</option>
                    <option value="DEDUCTION">Deduction</option>
                  </select>
                </div>

                <div className="salary-field">
                  <label htmlFor="annualAmount">
                    Annual Amount (₹)
                  </label>

                  <input
                    id="annualAmount"
                    type="number"
                    name="annualAmount"
                    className="salary-input"
                    placeholder="Enter annual amount"
                    min="0.01"
                    step="0.01"
                    value={formData.annualAmount}
                    onChange={handleChange}
                    disabled={saving}
                  />
                </div>

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
                      ? "Update Component"
                      : "Add Component"}
                </button>

              </div>

            </form>
          </div>
        </div>
      )}

    </div>
  );
}

export default SalaryComponentForm;
