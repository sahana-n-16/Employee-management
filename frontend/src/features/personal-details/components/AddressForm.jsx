const addressFields = [
  { name: 'addressLine1', label: 'Address Line 1', placeholder: 'House number and street name', required: true, wide: true },
  { name: 'addressLine2', label: 'Address Line 2', placeholder: 'Apartment, suite, unit (optional)', required: false, wide: true },
  { name: 'city', label: 'City', placeholder: 'Enter city', required: true },
  { name: 'pinCode', label: 'PIN Code', placeholder: '6-digit PIN code', required: true, inputMode: 'numeric', maxLength: 6 },
]

function AddressForm({
  title,
  sectionId,
  value,
  errors,
  onChange,
  isPermanent = false,
  sameAsCurrent = false,
  onSameAsCurrentChange,
}) {
  return (
    <section className="form-card" aria-labelledby={`${sectionId}-heading`}>
      <div className="section-heading">
        <div className="section-icon address-icon" aria-hidden="true">⌖</div>
        <div>
          <h2 id={`${sectionId}-heading`}>{title}</h2>
          <p>{isPermanent ? 'Your registered home address' : 'Where you currently live'}</p>
        </div>
        {isPermanent && (
          <span className="section-badge">OPTIONAL IF SAME</span>
        )}
      </div>

      {isPermanent && (
        <label className="same-address-option">
          <input
            type="checkbox"
            checked={sameAsCurrent}
            onChange={(event) => onSameAsCurrentChange(event.target.checked)}
          />
          <span className="custom-checkbox" aria-hidden="true"><span>✓</span></span>
          <span>Permanent address is same as current address</span>
        </label>
      )}

      <div className="field-grid address-grid">
        {addressFields.map((field) => {
          const fieldId = `${sectionId}-${field.name}`
          const error = errors[`${sectionId}.${field.name}`]

          return (
            <div className={`field ${field.wide ? 'field-wide' : ''}`} key={field.name}>
              <label htmlFor={fieldId}>
                {field.label}
                {field.required && <span className="required-mark"> *</span>}
              </label>
              <input
                id={fieldId}
                name={`${sectionId}.${field.name}`}
                type="text"
                value={value[field.name]}
                placeholder={field.placeholder}
                inputMode={field.inputMode}
                maxLength={field.maxLength}
                required={field.required && !(isPermanent && sameAsCurrent)}
                disabled={isPermanent && sameAsCurrent}
                aria-invalid={Boolean(error)}
                aria-describedby={error ? `${fieldId}-error` : undefined}
                onChange={(event) => onChange(field.name, event.target.value)}
              />
              {error && <span className="field-error" id={`${fieldId}-error`}>{error}</span>}
            </div>
          )
        })}
      </div>
    </section>
  )
}

export default AddressForm
