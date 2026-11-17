
import { useEffect, useState } from "react";

import {
  getProfessionalDetails,
  updateProfessionalDetails,
} from "../professionalService";

const initialForm = {
  employmentCode: "",
  companyEmail: "",
  officePhone: "",
  dateOfJoining: "",
  designation: "",
  department: "",
  city: "",
  pinCode: "",
  officeAddress: "",
  reportingManager: "",
  hrName: "",
};

function ProfessionalDetailsForm({ employeeId = 1 }) {
  const [formData, setFormData] = useState(initialForm);

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

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

  // Load existing professional details
  useEffect(() => {
    const loadProfessionalDetails = async () => {
      try {
        setLoading(true);

        const data = await getProfessionalDetails(employeeId);

        setFormData({
          employmentCode: data.employmentCode || "",
          companyEmail: data.companyEmail || "",
          officePhone: data.officePhone || "",
          dateOfJoining: data.dateOfJoining || "",
          designation: data.designation || "",
          department: data.department || "",
          city: data.city || "",
          pinCode: data.pinCode || "",
          officeAddress: data.officeAddress || "",
          reportingManager: data.reportingManager || "",
          hrName: data.hrName || "",
        });
      } catch (error) {
        setMessage(
          error.message || "Unable to load professional details."
        );
        setMessageType("error");
      } finally {
        setLoading(false);
      }
    };

    loadProfessionalDetails();
  }, [employeeId]);

  // Handle input changes
  const handleChange = (e) => {
    const { name, value } = e.target;

    let formattedValue = value;

    // Phone number: digits only, maximum 12
    if (name === "officePhone") {
      formattedValue = value.replace(/\D/g, "").slice(0, 12);
    }

    // PIN code: digits only, maximum 6
    if (name === "pinCode") {
      formattedValue = value.replace(/\D/g, "").slice(0, 6);
    }

    setFormData((previous) => ({
      ...previous,
      [name]: formattedValue,
    }));

    setErrors((previous) => ({
      ...previous,
      [name]: "",
    }));

    setMessage("");
  };

  // Validate form
  const validateForm = () => {
    const newErrors = {};

    if (!formData.employmentCode.trim()) {
      newErrors.employmentCode = "Employment code is required.";
    }

    // Company email validation
    if (
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(
        formData.companyEmail.trim()
      )
    ) {
      newErrors.companyEmail = "Enter a valid company email.";
    }

    // Office phone validation
    if (!/^\d{8,12}$/.test(formData.officePhone)) {
      newErrors.officePhone =
        "Phone number must contain 8 to 12 digits.";
    }

    if (!formData.dateOfJoining) {
      newErrors.dateOfJoining = "Date of joining is required.";
    }

    if (!formData.designation.trim()) {
      newErrors.designation = "Designation is required.";
    }

    if (!formData.department.trim()) {
      newErrors.department = "Department is required.";
    }

    if (!formData.city.trim()) {
      newErrors.city = "City is required.";
    }

    if (!/^\d{6}$/.test(formData.pinCode)) {
      newErrors.pinCode =
        "PIN Code must contain exactly 6 digits.";
    }

    if (!formData.officeAddress.trim()) {
      newErrors.officeAddress = "Office address is required.";
    }

    if (!formData.reportingManager.trim()) {
      newErrors.reportingManager =
        "Reporting manager is required.";
    }

    if (!formData.hrName.trim()) {
      newErrors.hrName = "HR name is required.";
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  // Save updated details
  const handleSubmit = async (e) => {
    e.preventDefault();

    setMessage("");

    if (!validateForm()) {
      setMessage("Please correct the highlighted fields.");
      setMessageType("error");
      return;
    }

    try {
      setSaving(true);

      await updateProfessionalDetails(employeeId, formData);

      setMessage("Professional details updated successfully!");
      setMessageType("success");
    } catch (error) {
      setMessage(
        error.message || "Failed to update professional details."
      );
      setMessageType("error");
    } finally {
      setSaving(false);
    }
  };

  // Reusable form field
  const renderField = (
    name,
    label,
    type = "text",
    fullWidth = false,
    readOnly = false
  ) => (
    <div
      className={`col-12 ${fullWidth ? "" : "col-md-6"}`}
      key={name}
    >
      <label
        htmlFor={name}
        className="form-label professional-label"
      >
        {label}
      </label>

      <input
        id={name}
        name={name}
        type={type}
        className={`form-control professional-input ${
          errors[name] ? "is-invalid" : ""
        }`}
        placeholder={`Enter ${label.toLowerCase()}`}
        value={formData[name]}
        onChange={handleChange}
        readOnly={readOnly}
        inputMode={
          name === "pinCode" || name === "officePhone"
            ? "numeric"
            : undefined
        }
        maxLength={
          name === "pinCode"
            ? 6
            : name === "officePhone"
              ? 12
              : undefined
        }
        autoComplete="off"
        aria-invalid={Boolean(errors[name])}
        aria-describedby={
          errors[name] ? `${name}-error` : undefined
        }
      />

      {errors[name] && (
        <div
          id={`${name}-error`}
          className="invalid-feedback"
        >
          {errors[name]}
        </div>
      )}
    </div>
  );

  // Loading screen
  if (loading) {
    return (
      <div className="professional-card card">
        <div className="card-body p-4">
          Loading professional details...
        </div>
      </div>
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

          <span className="wf-toast-message">
            {message}
          </span>

          <button
            type="button"
            className="wf-toast-close"
            onClick={() => {
              setMessage("");
              setMessageType("");
            }}
            aria-label="Dismiss notification"
          >
            ×
          </button>
        </div>
      )}

      <form
        className="professional-form"
        onSubmit={handleSubmit}
        noValidate
      >
        <div className="row g-3">

          {/* Work Information */}
          <div className="col-12 col-lg-7">
            <section className="card professional-card h-100">
              <div className="card-body p-4">

                <div className="professional-card-heading">
                  <div className="professional-card-icon work-icon">
                    <span>▤</span>
                  </div>

                  <div>
                    <h3>Work Information</h3>
                    <p>Basic employment information</p>
                  </div>
                </div>

                <div className="row g-3">
                  {renderField(
                    "employmentCode",
                    "Employment Code",
                    "text",
                    false,
                    true
                  )}

                  {renderField(
                    "companyEmail",
                    "Company Email",
                    "email",
                    false,
                    true
                  )}

                  {renderField(
                    "officePhone",
                    "Office Phone",
                    "tel"
                  )}

                  {renderField(
                    "dateOfJoining",
                    "Date of Joining",
                    "date",
                    false,
                    true
                  )}

                  {renderField(
                    "designation",
                    "Designation"
                  )}

                  {renderField(
                    "department",
                    "Department"
                  )}
                </div>

              </div>
            </section>
          </div>

          {/* Reporting Structure */}
          <div className="col-12 col-lg-5">
            <section className="card professional-card h-100">
              <div className="card-body p-4">

                <div className="professional-card-heading">
                  <div className="professional-card-icon people-icon">
                    <span>♧</span>
                  </div>

                  <div>
                    <h3>Reporting Structure</h3>
                    <p>Manager and HR contact information</p>
                  </div>
                </div>

                <div className="row g-3">
                  {renderField(
                    "reportingManager",
                    "Reporting Manager (Code/Email)",
                    "text",
                    true
                  )}

                  {renderField(
                    "hrName",
                    "HR Name",
                    "text",
                    true
                  )}
                </div>

              </div>
            </section>
          </div>

          {/* Office Information */}
          <div className="col-12">
            <section className="card professional-card">
              <div className="card-body p-4">

                <div className="professional-card-heading">
                  <div className="professional-card-icon office-icon">
                    <span>⌖</span>
                  </div>

                  <div>
                    <h3>Office Information</h3>
                    <p>Workplace location and address</p>
                  </div>
                </div>

                <div className="row g-3">
                  {renderField("city", "City")}

                  {renderField("pinCode", "PIN Code")}

                  {renderField(
                    "officeAddress",
                    "Office Address",
                    "text",
                    true
                  )}
                </div>

                <div className="professional-form-actions">
                  <button
                    type="submit"
                    className="btn btn-primary px-4"
                    disabled={saving}
                  >
                    {saving ? "Saving..." : "Save Changes"}
                  </button>
                </div>

              </div>
            </section>
          </div>

        </div>
      </form>
    </>
  );
}

export default ProfessionalDetailsForm;
