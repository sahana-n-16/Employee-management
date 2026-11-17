
import { useEffect, useState } from "react";

import {
  getFinanceDetails,
  createFinanceDetails,
  updateFinanceDetails,
} from "../financeService";

const initialForm = {
  panNumber: "",
  aadhaarNumber: "",
  bankName: "",
  bankBranch: "",
  accountNumber: "",
  ifscCode: "",
};

function FinanceDetailsForm({ employeeId = 1 }) {
  const [formData, setFormData] = useState(initialForm);
  const [recordExists, setRecordExists] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [message, setMessage] = useState("");
  const [messageType, setMessageType] = useState("");

  const [errors, setErrors] = useState({});

  const [showAadhaar, setShowAadhaar] = useState(false);
  const [showAccount, setShowAccount] = useState(false);

  // Load existing finance details
  useEffect(() => {
    const loadDetails = async () => {
      try {
        setLoading(true);

        const data = await getFinanceDetails(employeeId);

        if (data) {
          setFormData({
            panNumber: data.panNumber || "",
            aadhaarNumber: data.aadhaarNumber || "",
            bankName: data.bankName || "",
            bankBranch: data.bankBranch || "",
            accountNumber: data.accountNumber || "",
            ifscCode: data.ifscCode || "",
          });

          setRecordExists(true);
        }
      } catch (error) {
        setMessage(
          error.message || "Unable to load finance details."
        );
        setMessageType("error");
      } finally {
        setLoading(false);
      }
    };

    loadDetails();
  }, [employeeId]);

  // Handle input changes and sanitize values
  const handleChange = (event) => {
    const { name, value } = event.target;

    let formattedValue = value;

    switch (name) {
      case "aadhaarNumber":
        formattedValue = value.replace(/\D/g, "").slice(0, 12);
        break;

      case "accountNumber":
        formattedValue = value.replace(/\D/g, "").slice(0, 18);
        break;

      case "panNumber":
        formattedValue = value
          .replace(/[^a-zA-Z0-9]/g, "")
          .toUpperCase()
          .slice(0, 10);
        break;

      case "ifscCode":
        formattedValue = value
          .replace(/[^a-zA-Z0-9]/g, "")
          .toUpperCase()
          .slice(0, 11);
        break;

      default:
        formattedValue = value;
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

  // Validate all fields before saving
  const validateForm = () => {
    const newErrors = {};

    if (!/^[A-Z]{5}[0-9]{4}[A-Z]$/.test(formData.panNumber)) {
      newErrors.panNumber =
        "Enter a valid PAN number (e.g. ABCDE1234F).";
    }

    if (!/^[0-9]{12}$/.test(formData.aadhaarNumber)) {
      newErrors.aadhaarNumber =
        "Aadhaar number must contain exactly 12 digits.";
    }

    if (!formData.bankName.trim()) {
      newErrors.bankName = "Bank name is required.";
    }

    if (!formData.bankBranch.trim()) {
      newErrors.bankBranch = "Bank branch is required.";
    }

    if (!/^[0-9]{9,18}$/.test(formData.accountNumber)) {
      newErrors.accountNumber =
        "Account number must contain 9 to 18 digits.";
    }

    if (!/^[A-Z]{4}0[A-Z0-9]{6}$/.test(formData.ifscCode)) {
      newErrors.ifscCode =
        "Enter a valid IFSC code (e.g. SBIN0001234).";
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  // Save or update finance details
  const handleSubmit = async (event) => {
    event.preventDefault();

    setMessage("");

    if (!validateForm()) {
      setMessage("Please correct the highlighted fields.");
      setMessageType("error");
      return;
    }

    try {
      setSaving(true);

      if (recordExists) {
        await updateFinanceDetails(employeeId, formData);

        setMessage("Finance details updated successfully.");
      } else {
        await createFinanceDetails(employeeId, formData);

        setRecordExists(true);

        setMessage("Finance details saved successfully.");
      }

      setMessageType("success");
    } catch (error) {
      setMessage(
        error.message || "Unable to save finance details."
      );

      setMessageType("error");
    } finally {
      setSaving(false);
    }
  };

  // Reusable form field
  const renderField = (
    label,
    name,
    placeholder,
    type = "text",
    options = {}
  ) => {
    const isSensitive = options.sensitive;

    const isVisible =
      name === "aadhaarNumber"
        ? showAadhaar
        : showAccount;

    const isNumeric =
      name === "aadhaarNumber" ||
      name === "accountNumber";

    const maxLength = {
      panNumber: 10,
      aadhaarNumber: 12,
      accountNumber: 18,
      ifscCode: 11,
    }[name];

    return (
      <div className="col-12 col-md-6" key={name}>
        <label htmlFor={name} className="finance-label">
          {label}
        </label>

        <div className="finance-input-wrapper">
          <input
            id={name}
            name={name}
            type={
              isSensitive && !isVisible
                ? "password"
                : type
            }
            value={formData[name]}
            onChange={handleChange}
            placeholder={placeholder}
            className={`finance-input ${
              errors[name] ? "finance-input-error" : ""
            }`}
            autoComplete="off"
            maxLength={maxLength}
            inputMode={isNumeric ? "numeric" : "text"}
            aria-invalid={Boolean(errors[name])}
            aria-describedby={
              errors[name] ? `${name}-error` : undefined
            }
            spellCheck="false"
            autoCapitalize={
              name === "panNumber" || name === "ifscCode"
                ? "characters"
                : "off"
            }
          />

          {isSensitive && (
            <button
              type="button"
              className="finance-visibility-btn"
              onClick={() => {
                if (name === "aadhaarNumber") {
                  setShowAadhaar((previous) => !previous);
                } else {
                  setShowAccount((previous) => !previous);
                }
              }}
              aria-label={
                isVisible ? "Hide number" : "Show number"
              }
            >
              {isVisible ? "Hide" : "Show"}
            </button>
          )}
        </div>

        {errors[name] && (
          <div
            id={`${name}-error`}
            className="finance-error-message"
          >
            {errors[name]}
          </div>
        )}
      </div>
    );
  };

  // Loading screen
  if (loading) {
    return (
      <div className="finance-loading">
        <span className="finance-spinner" />
        Loading finance details...
      </div>
    );
  }

  return (
    <section className="finance-details-section">

      {/* Floating success/error notification */}
      {message && (
        <div
          className={`finance-toast ${
            messageType === "success"
              ? "finance-toast-success"
              : "finance-toast-error"
          }`}
          role="status"
          aria-live="polite"
        >
          <span className="finance-toast-icon">
            {messageType === "success" ? "✓" : "!"}
          </span>

          <span>{message}</span>

          <button
            type="button"
            className="finance-toast-close"
            onClick={() => setMessage("")}
            aria-label="Close notification"
          >
            ×
          </button>
        </div>
      )}

      

      <form onSubmit={handleSubmit} noValidate>

        {/* Identity and Tax Information */}
        <section className="finance-card">

          <div className="finance-card-heading">
            <div className="finance-card-icon finance-tax-icon">
              ID
            </div>

            <div>
              <h3>Identity & Tax Information</h3>
              <p>Employee identification and tax details</p>
            </div>
          </div>

          <div className="row g-4">
            {renderField(
              "PAN Number",
              "panNumber",
              "ABCDE1234F"
            )}

            {renderField(
              "Aadhaar Number",
              "aadhaarNumber",
              "12-digit Aadhaar number",
              "text",
              { sensitive: true }
            )}
          </div>

        </section>

        {/* Bank Account Information */}
        <section className="finance-card">

          <div className="finance-card-heading">
            <div className="finance-card-icon finance-bank-icon">
              ₹
            </div>

            <div>
              <h3>Bank Account Information</h3>
              <p>Employee salary account details</p>
            </div>
          </div>

          <div className="row g-4">
            {renderField(
              "Bank Name",
              "bankName",
              "Enter bank name"
            )}

            {renderField(
              "Bank Branch",
              "bankBranch",
              "Enter branch name"
            )}

            {renderField(
              "Account Number",
              "accountNumber",
              "Enter account number",
              "text",
              { sensitive: true }
            )}

            {renderField(
              "IFSC Code",
              "ifscCode",
              "SBIN0001234"
            )}
          </div>

        </section>

        {/* Save button */}
        <div className="finance-form-actions">
          <button
            type="submit"
            className="finance-save-btn"
            disabled={saving}
          >
            {saving
              ? "Saving..."
              : recordExists
                ? "Update Finance Details"
                : "Save Finance Details"}
          </button>
        </div>

      </form>
    </section>
  );
}

export default FinanceDetailsForm;
