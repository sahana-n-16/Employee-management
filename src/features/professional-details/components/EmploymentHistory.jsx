
import { useEffect, useState } from "react";
import {
  getEmploymentHistory,
  addEmploymentHistory,
  deleteEmploymentHistory,
} from "../professionalService";

function EmploymentHistory({ employeeId = 1 }) {
  const [records, setRecords] = useState([]);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("");

  // Automatically dismiss notifications after 4 seconds
  useEffect(() => {
    if (!message) return;

    const timer = setTimeout(() => {
      setMessage("");
      setMessageType("");
    }, 4000);

    return () => clearTimeout(timer);
  }, [message]);

  // Load existing employment history
  const loadEmploymentHistory = async (showLoader = true) => {
    try {
      if (showLoader) {
        setLoading(true);
      }

      const data = await getEmploymentHistory(employeeId);

      setRecords(
        data.map((record) => ({
          id: record.id,
          companyName: record.companyName || "",
          joiningDate: record.joiningDate || "",
          endDate: record.endDate || "",
        }))
      );
    } catch (error) {
      setMessage(error.message || "Unable to load employment history.");
      setMessageType("error");
    } finally {
      if (showLoader) {
        setLoading(false);
      }
    }
  };

  useEffect(() => {
    loadEmploymentHistory();
  }, [employeeId]);

  // Handle input changes
  const handleChange = (index, e) => {
    const { name, value } = e.target;

    setRecords((previous) =>
      previous.map((record, i) =>
        i === index ? { ...record, [name]: value } : record
      )
    );

    setErrors((previous) => ({
      ...previous,
      [`${index}-${name}`]: "",
    }));

    setMessage("");
  };

  // Add a new company
  const addRecord = () => {
    setRecords((previous) => [
      ...previous,
      {
        companyName: "",
        joiningDate: "",
        endDate: "",
      },
    ]);
  };

  // Remove an unsaved company from the form
  const removeRecord = (index) => {
    if (records[index]?.id) {
      return;
    }

    setRecords((previous) =>
      previous.filter((_, i) => i !== index)
    );

    setErrors({});
    setMessage("");
  };

  // Delete a saved company from the database
  const handleDelete = async (record) => {
    if (!record.id) return;

    const confirmed = window.confirm(
      `Are you sure you want to permanently delete ${record.companyName}?`
    );

    if (!confirmed) return;

    try {
      setDeletingId(record.id);
      setMessage("");

      await deleteEmploymentHistory(employeeId, record.id);

      // Remove the deleted record from the screen
      setRecords((previous) =>
        previous.filter((item) => item.id !== record.id)
      );

      setMessage("Employment record deleted successfully!");
      setMessageType("success");
    } catch (error) {
      setMessage(
        error.message || "Failed to delete employment record."
      );
      setMessageType("error");
    } finally {
      setDeletingId(null);
    }
  };

  // Validate new records
  const validateRecords = () => {
    const newErrors = {};

    records.forEach((record, index) => {
      // Existing database records are not resubmitted
      if (record.id) return;

      if (!record.companyName.trim()) {
        newErrors[`${index}-companyName`] =
          "Company name is required";
      }

      if (!record.joiningDate) {
        newErrors[`${index}-joiningDate`] =
          "Joining date is required";
      }

      if (!record.endDate) {
        newErrors[`${index}-endDate`] =
          "End date is required";
      } else if (
        record.joiningDate &&
        record.endDate < record.joiningDate
      ) {
        newErrors[`${index}-endDate`] =
          "End date cannot be before joining date";
      }

      // Previous employment dates cannot be in the future
      const today = new Date().toISOString().split("T")[0];

      if (record.joiningDate && record.joiningDate > today) {
        newErrors[`${index}-joiningDate`] =
          "Joining date cannot be in the future";
      }

      if (record.endDate && record.endDate > today) {
        newErrors[`${index}-endDate`] =
          "End date cannot be in the future";
      }
    });

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  // Save new employment records
  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");

    if (!validateRecords()) {
      return;
    }

    const newRecords = records.filter((record) => !record.id);

    if (newRecords.length === 0) {
      setMessage("There are no new employment records to save.");
      setMessageType("error");
      return;
    }

    try {
      setSaving(true);

      // Save records one by one
      for (const record of newRecords) {
        await addEmploymentHistory(employeeId, {
          companyName: record.companyName.trim(),
          joiningDate: record.joiningDate,
          endDate: record.endDate,
        });
      }

      await loadEmploymentHistory(false);

      setMessage("Employment history saved successfully!");
      setMessageType("success");
      setErrors({});
    } catch (error) {
      // Reload records in case some were saved before an error
      await loadEmploymentHistory(false);

      setMessage(
        error.message || "Failed to save employment history."
      );
      setMessageType("error");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <section className="card professional-card employment-section">
        <div className="card-body p-4">
          Loading employment history...
        </div>
      </section>
    );
  }

  return (
    <>
      {/* Notification */}
      {message && (
        <div
          className={`wf-toast ${
            messageType === "success"
              ? "wf-toast-success"
              : "wf-toast-error"
          }`}
          role="status"
          aria-live="polite"
        >
          <span className="wf-toast-icon">
            {messageType === "success" ? "✓" : "!"}
          </span>

          <span className="wf-toast-message">{message}</span>

          <button
            type="button"
            className="wf-toast-close"
            onClick={() => setMessage("")}
            aria-label="Dismiss notification"
          >
            ×
          </button>
        </div>
      )}

      <section className="card professional-card employment-section">
        <div className="card-body p-4">
          <div className="employment-heading">
            <div>
              <h2>Previous Employment</h2>
              <p>Add details of your previous work experience.</p>
            </div>

            <div className="professional-card-icon employment-icon">
              ◷
            </div>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="d-flex flex-column gap-3">
              {records.map((record, index) => (
                <div
                  className="employment-card"
                  key={record.id || `new-${index}`}
                >
                  <div className="d-flex justify-content-between align-items-center gap-3 mb-4">
                    <div>
                      <h3>
                        Company {String(index + 1).padStart(2, "0")}
                      </h3>

                      <p>
                        {record.id
                          ? "Saved employment record"
                          : "New employment details"}
                      </p>
                    </div>

                    {record.id ? (
                      <button
                        type="button"
                        className="btn btn-outline-danger btn-sm"
                        onClick={() => handleDelete(record)}
                        disabled={
                          saving || deletingId !== null
                        }
                      >
                        {deletingId === record.id
                          ? "Deleting..."
                          : "Remove"}
                      </button>
                    ) : (
                      <button
                        type="button"
                        className="btn btn-outline-danger btn-sm"
                        onClick={() => removeRecord(index)}
                        disabled={saving || deletingId !== null}
                      >
                        Remove
                      </button>
                    )}
                  </div>

                  <div className="row g-3">
                    <div className="col-12">
                      <label
                        htmlFor={`companyName-${index}`}
                        className="form-label professional-label"
                      >
                        Company Name
                      </label>

                      <input
                        id={`companyName-${index}`}
                        name="companyName"
                        type="text"
                        className={`form-control professional-input ${
                          errors[`${index}-companyName`]
                            ? "is-invalid"
                            : ""
                        }`}
                        placeholder="Enter company name"
                        value={record.companyName}
                        onChange={(e) => handleChange(index, e)}
                        readOnly={Boolean(record.id)}
                      />

                      {errors[`${index}-companyName`] && (
                        <div className="invalid-feedback">
                          {errors[`${index}-companyName`]}
                        </div>
                      )}
                    </div>

                    <div className="col-12 col-md-6">
                      <label
                        htmlFor={`joiningDate-${index}`}
                        className="form-label professional-label"
                      >
                        Joining Date
                      </label>

                      <input
                        id={`joiningDate-${index}`}
                        name="joiningDate"
                        type="date"
                        className={`form-control professional-input ${
                          errors[`${index}-joiningDate`]
                            ? "is-invalid"
                            : ""
                        }`}
                        value={record.joiningDate}
                        onChange={(e) => handleChange(index, e)}
                        readOnly={Boolean(record.id)}
                      />

                      {errors[`${index}-joiningDate`] && (
                        <div className="invalid-feedback">
                          {errors[`${index}-joiningDate`]}
                        </div>
                      )}
                    </div>

                    <div className="col-12 col-md-6">
                      <label
                        htmlFor={`endDate-${index}`}
                        className="form-label professional-label"
                      >
                        End Date
                      </label>

                      <input
                        id={`endDate-${index}`}
                        name="endDate"
                        type="date"
                        className={`form-control professional-input ${
                          errors[`${index}-endDate`]
                            ? "is-invalid"
                            : ""
                        }`}
                        value={record.endDate}
                        onChange={(e) => handleChange(index, e)}
                        readOnly={Boolean(record.id)}
                      />

                      {errors[`${index}-endDate`] && (
                        <div className="invalid-feedback">
                          {errors[`${index}-endDate`]}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}

              {records.length === 0 && (
                <p className="text-muted">
                  No previous employment records added yet.
                </p>
              )}
            </div>

            <div className="employment-actions mt-4">
              <button
                type="button"
                className="btn btn-outline-primary"
                onClick={addRecord}
                disabled={saving || deletingId !== null}
              >
                + Add Previous Company
              </button>

              <button
                type="submit"
                className="btn btn-primary px-4"
                disabled={saving || deletingId !== null}
              >
                {saving ? "Saving..." : "Save Employment History"}
              </button>
            </div>
          </form>
        </div>
      </section>
    </>
  );
}

export default EmploymentHistory;
