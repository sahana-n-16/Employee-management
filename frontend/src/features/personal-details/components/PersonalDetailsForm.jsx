import { useEffect, useState } from 'react'
import AddressForm from './AddressForm.jsx'

// Keep the temporary ID in one place until employee identity is provided by the parent or session.
const PERSONAL_DETAILS_DEMO_EMPLOYEE_ID = 100001

function getEmployeeId() {
  return PERSONAL_DETAILS_DEMO_EMPLOYEE_ID
}

const initialForm = {
  dateOfBirth: '',
  gender: '',
  mobileNumber: '',
  personalEmail: '',
  currentAddress: {
    addressLine1: '',
    addressLine2: '',
    city: '',
    pinCode: '',
  },
  permanentAddress: {
    addressLine1: '',
    addressLine2: '',
    city: '',
    pinCode: '',
  },
  emergencyContactName: '',
  emergencyContactMobile: '',
}

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

function calculateAge(dateOfBirth) {
  if (!dateOfBirth) return ''

  const [year, month, day] = dateOfBirth.split('-').map(Number)
  const today = new Date()
  let age = today.getFullYear() - year
  const birthdayHasPassed =
    today.getMonth() + 1 > month ||
    (today.getMonth() + 1 === month && today.getDate() >= day)

  if (!birthdayHasPassed) age -= 1
  return age
}

function validateForm(form, age, sameAsCurrent) {
  const errors = {}
  const required = (key, value, label) => {
    if (!value.trim()) errors[key] = `${label} is required.`
  }

  required('dateOfBirth', form.dateOfBirth, 'Date of birth')
  if (form.dateOfBirth && new Date(`${form.dateOfBirth}T00:00:00`) > new Date()) {
    errors.dateOfBirth = 'Date of birth cannot be in the future.'
  }

  required('gender', form.gender, 'Gender')
  required('mobileNumber', form.mobileNumber, 'Mobile number')
  if (form.mobileNumber && !/^\d{10}$/.test(form.mobileNumber)) {
    errors.mobileNumber = 'Mobile number must be exactly 10 digits.'
  }

  required('personalEmail', form.personalEmail, 'Personal email')
  if (form.personalEmail && !emailPattern.test(form.personalEmail)) {
    errors.personalEmail = 'Enter a valid email address.'
  }

  if (age === '' || age <= 0 || age > 999) {
    errors.age = 'Age must be a positive number with a maximum of 3 digits.'
  }

  Object.entries(form.currentAddress).forEach(([name, value]) => {
    if (name !== 'addressLine2' && !value.trim()) {
      errors[`currentAddress.${name}`] = `${addressLabel(name)} is required.`
    }
  })
  if (form.currentAddress.pinCode && !/^\d{6}$/.test(form.currentAddress.pinCode)) {
    errors['currentAddress.pinCode'] = 'PIN Code must be exactly 6 digits.'
  }

  if (!sameAsCurrent) {
    Object.entries(form.permanentAddress).forEach(([name, value]) => {
      if (name !== 'addressLine2' && !value.trim()) {
        errors[`permanentAddress.${name}`] = `${addressLabel(name)} is required.`
      }
    })
    if (form.permanentAddress.pinCode && !/^\d{6}$/.test(form.permanentAddress.pinCode)) {
      errors['permanentAddress.pinCode'] = 'PIN Code must be exactly 6 digits.'
    }
  }

  required('emergencyContactName', form.emergencyContactName, 'Emergency contact name')
  required('emergencyContactMobile', form.emergencyContactMobile, 'Emergency contact mobile number')
  if (form.emergencyContactMobile && !/^\d{10}$/.test(form.emergencyContactMobile)) {
    errors.emergencyContactMobile = 'Mobile number must be exactly 10 digits.'
  }

  return errors
}

function mapResponseToForm(details) {
  return {
    ...initialForm,
    dateOfBirth: details.dateOfBirth || '',
    gender: details.gender || '',
    mobileNumber: details.mobileNumber || '',
    personalEmail: details.personalEmail || '',
    emergencyContactName: details.emergencyContactName || '',
    emergencyContactMobile: details.emergencyContactMobile || '',
    currentAddress: {
      addressLine1: details.currentAddressLine1 || '',
      addressLine2: details.currentAddressLine2 || '',
      city: details.currentCity || '',
      pinCode: details.currentPinCode || '',
    },
    permanentAddress: {
      addressLine1: details.permanentAddressLine1 || '',
      addressLine2: details.permanentAddressLine2 || '',
      city: details.permanentCity || '',
      pinCode: details.permanentPinCode || '',
    },
  }
}

function mapFormToRequest(form, employeeId) {
  return {
    employeeId,
    dateOfBirth: form.dateOfBirth,
    gender: form.gender,
    mobileNumber: form.mobileNumber,
    personalEmail: form.personalEmail,
    emergencyContactName: form.emergencyContactName,
    emergencyContactMobile: form.emergencyContactMobile,
    currentAddressLine1: form.currentAddress.addressLine1,
    currentAddressLine2: form.currentAddress.addressLine2,
    currentCity: form.currentAddress.city,
    currentPinCode: form.currentAddress.pinCode,
    permanentAddressLine1: form.permanentAddress.addressLine1,
    permanentAddressLine2: form.permanentAddress.addressLine2,
    permanentCity: form.permanentAddress.city,
    permanentPinCode: form.permanentAddress.pinCode,
  }
}

async function getResponseError(response) {
  try {
    const body = await response.json()
    return body.message || body.detail || body.error
  } catch {
    return null
  }
}

function addressLabel(name) {
  return {
    addressLine1: 'Address Line 1',
    city: 'City',
    pinCode: 'PIN Code',
  }[name] || name
}

function FormField({
  id,
  name,
  label,
  value,
  error,
  onChange,
  type = 'text',
  placeholder,
  required = false,
  inputMode,
  maxLength,
  children,
  readOnly = false,
}) {
  return (
    <div className="field">
      <label htmlFor={id}>
        {label}
        {required && <span className="required-mark"> *</span>}
      </label>
      {children || (
        <input
          id={id}
          name={name}
          type={type}
          value={value}
          placeholder={placeholder}
          required={required}
          inputMode={inputMode}
          maxLength={maxLength}
          readOnly={readOnly}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${id}-error` : undefined}
          onChange={onChange}
        />
      )}
      {error && <span className="field-error" id={`${id}-error`}>{error}</span>}
    </div>
  )
}

function PersonalDetailsForm() {
  const [form, setForm] = useState(initialForm)
  const [savedForm, setSavedForm] = useState(initialForm)
  const [sameAsCurrent, setSameAsCurrent] = useState(false)
  const [savedSameAsCurrent, setSavedSameAsCurrent] = useState(false)
  const [errors, setErrors] = useState({})
  const [submitted, setSubmitted] = useState(false)
  const [hasExistingDetails, setHasExistingDetails] = useState(false)
  const [lastSubmissionWasUpdate, setLastSubmissionWasUpdate] = useState(false)
  const [requestError, setRequestError] = useState('')
  const [isSaving, setIsSaving] = useState(false)
  const age = calculateAge(form.dateOfBirth)

  useEffect(() => {
    const controller = new AbortController()

    async function loadPersonalDetails() {
      try {
        const response = await fetch(
          `/api/personal-details/${getEmployeeId()}`,
          { signal: controller.signal },
        )
        if (response.status === 404) return
        if (!response.ok) {
          const message = await getResponseError(response)
          throw new Error(message || `Request failed (${response.status}).`)
        }

        const details = await response.json()
        const loadedForm = mapResponseToForm(details)
        const addressesMatch = Object.keys(loadedForm.currentAddress).every(
          (field) => loadedForm.currentAddress[field] === loadedForm.permanentAddress[field],
        )
        setForm(loadedForm)
        setSavedForm(loadedForm)
        setSameAsCurrent(addressesMatch)
        setSavedSameAsCurrent(addressesMatch)
        setHasExistingDetails(true)
      } catch (error) {
        if (error.name !== 'AbortError') {
          setRequestError(`Could not load personal details. ${error.message}`)
        }
      }
    }

    loadPersonalDetails()
    return () => controller.abort()
  }, [])

  function updateField(name, value) {
    setRequestError('')
    setForm((current) => ({ ...current, [name]: value }))
    setErrors((current) => {
      if (!current[name] && !(name === 'dateOfBirth' && current.age)) return current
      const next = { ...current }
      delete next[name]
      if (name === 'dateOfBirth') delete next.age
      return next
    })
    setSubmitted(false)
  }

  function updateAddress(addressType, name, value) {
    setRequestError('')
    setForm((current) => {
      const updatedAddress = { ...current[addressType], [name]: value }
      return {
        ...current,
        [addressType]: updatedAddress,
        ...(addressType === 'currentAddress' && sameAsCurrent
          ? { permanentAddress: { ...updatedAddress } }
          : {}),
      }
    })
    const errorKey = `${addressType}.${name}`
    setErrors((current) => {
      const next = { ...current }
      delete next[errorKey]
      if (addressType === 'currentAddress' && sameAsCurrent) {
        delete next[`permanentAddress.${name}`]
      }
      return next
    })
    setSubmitted(false)
  }

  function handleSameAddressChange(checked) {
    setRequestError('')
    setSameAsCurrent(checked)
    if (checked) {
      setForm((current) => ({
        ...current,
        permanentAddress: { ...current.currentAddress },
      }))
      setErrors((current) =>
        Object.fromEntries(
          Object.entries(current).filter(([key]) => !key.startsWith('permanentAddress.')),
        ),
      )
    }
    setSubmitted(false)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const validationErrors = validateForm(form, age, sameAsCurrent)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) {
      setSubmitted(false)
      const firstInvalidId = Object.keys(validationErrors)[0].replace('.', '-')
      document.getElementById(firstInvalidId)?.focus()
      return
    }

    setIsSaving(true)
    setRequestError('')
    setSubmitted(false)
    const isUpdate = hasExistingDetails
    try {
      const response = await fetch('/api/personal-details', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(mapFormToRequest(form, getEmployeeId())),
      })
      if (!response.ok) {
        const message = await getResponseError(response)
        throw new Error(message || `Request failed (${response.status}).`)
      }
      setSavedForm(form)
      setSavedSameAsCurrent(sameAsCurrent)
      setHasExistingDetails(true)
      setLastSubmissionWasUpdate(isUpdate)
      setSubmitted(true)
    } catch (error) {
      setRequestError(`Could not save personal details. ${error.message}`)
    } finally {
      setIsSaving(false)
    }
  }

  function handleReset() {
    setForm(savedForm)
    setSameAsCurrent(savedSameAsCurrent)
    setErrors({})
    setSubmitted(false)
    setRequestError('')
  }

  return (
    <form className="personal-details-form" onSubmit={handleSubmit} noValidate>
      {submitted && (
        <div className="success-message" role="status">
          <span className="success-icon" aria-hidden="true">✓</span>
          <span>
            <strong>Details {lastSubmissionWasUpdate ? 'updated' : 'saved'} successfully.</strong>
            {' '}Your personal information has been {lastSubmissionWasUpdate ? 'updated' : 'saved'}.
          </span>
          <button type="button" aria-label="Dismiss success message" onClick={() => setSubmitted(false)}>×</button>
        </div>
      )}
      {requestError && (
        <div className="error-message" role="alert">
          {requestError}
        </div>
      )}

      <section className="form-card" aria-labelledby="personal-info-heading">
        <div className="section-heading">
          <div className="section-icon personal-icon" aria-hidden="true">♙</div>
          <div>
            <h2 id="personal-info-heading">Personal Information</h2>
            <p>Your basic identity and contact information</p>
          </div>
          <span className="required-note"><span className="required-mark">*</span> Required fields</span>
        </div>

        <div className="field-grid">
          <FormField
            id="employmentCode"
            label="Employment Code"
            value=""
            placeholder="Awaiting Employee Management integration"
            inputMode="numeric"
            maxLength={6}
            readOnly
          />
          <FormField
            id="fullName"
            label="Employee Full Name"
            value=""
            placeholder="Awaiting Employee Management integration"
            readOnly
          />
          <FormField
            id="dateOfBirth"
            name="dateOfBirth"
            label="Date of Birth"
            value={form.dateOfBirth}
            error={errors.dateOfBirth}
            type="date"
            required
            onChange={(event) => updateField('dateOfBirth', event.target.value)}
          />
          <FormField
            id="gender"
            name="gender"
            label="Gender"
            value={form.gender}
            error={errors.gender}
            required
          >
            <select
              id="gender"
              name="gender"
              value={form.gender}
              required
              aria-invalid={Boolean(errors.gender)}
              aria-describedby={errors.gender ? 'gender-error' : undefined}
              onChange={(event) => updateField('gender', event.target.value)}
            >
              <option value="">Select gender</option>
              <option value="female">Female</option>
              <option value="male">Male</option>
              <option value="non-binary">Non-binary</option>
              <option value="prefer-not-to-say">Prefer not to say</option>
            </select>
          </FormField>
          <FormField
            id="age"
            name="age"
            label="Age"
            value={age === '' ? '' : String(age)}
            error={errors.age}
            placeholder="Auto-calculated"
            inputMode="numeric"
            maxLength={3}
            readOnly
          />
          <FormField
            id="mobileNumber"
            name="mobileNumber"
            label="Mobile Number"
            value={form.mobileNumber}
            error={errors.mobileNumber}
            placeholder="10-digit mobile number"
            required
            inputMode="numeric"
            maxLength={10}
            onChange={(event) => updateField('mobileNumber', event.target.value)}
          />
          <FormField
            id="personalEmail"
            name="personalEmail"
            label="Personal Email"
            value={form.personalEmail}
            error={errors.personalEmail}
            placeholder="you@example.com"
            type="email"
            required
            onChange={(event) => updateField('personalEmail', event.target.value)}
          />
        </div>
      </section>

      <AddressForm
        title="Current Address"
        sectionId="currentAddress"
        value={form.currentAddress}
        errors={errors}
        onChange={(name, value) => updateAddress('currentAddress', name, value)}
      />

      <AddressForm
        title="Permanent Address"
        sectionId="permanentAddress"
        value={form.permanentAddress}
        errors={errors}
        onChange={(name, value) => updateAddress('permanentAddress', name, value)}
        isPermanent
        sameAsCurrent={sameAsCurrent}
        onSameAsCurrentChange={handleSameAddressChange}
      />

      <section className="form-card" aria-labelledby="emergency-heading">
        <div className="section-heading">
          <div className="section-icon emergency-icon" aria-hidden="true">♡</div>
          <div>
            <h2 id="emergency-heading">Emergency Contact</h2>
            <p>Someone we can reach in case of an emergency</p>
          </div>
        </div>
        <div className="field-grid emergency-grid">
          <FormField
            id="emergencyContactName"
            name="emergencyContactName"
            label="Contact Name"
            value={form.emergencyContactName}
            error={errors.emergencyContactName}
            placeholder="Enter contact's full name"
            required
            onChange={(event) => updateField('emergencyContactName', event.target.value)}
          />
          <FormField
            id="emergencyContactMobile"
            name="emergencyContactMobile"
            label="Contact Mobile Number"
            value={form.emergencyContactMobile}
            error={errors.emergencyContactMobile}
            placeholder="10-digit mobile number"
            required
            inputMode="numeric"
            maxLength={10}
            onChange={(event) => updateField('emergencyContactMobile', event.target.value)}
          />
        </div>
      </section>

      <div className="form-actions">
        <p><span className="required-mark">*</span> Please complete all required fields before saving.</p>
        <div className="action-buttons">
          <button type="button" className="button button-secondary" onClick={handleReset}>
            <span aria-hidden="true">↺</span> Reset
          </button>
          <button type="submit" className="button button-primary" disabled={isSaving}>
            {isSaving ? 'Saving…' : hasExistingDetails ? 'Update Details' : 'Save Details'}
            <span aria-hidden="true">→</span>
          </button>
        </div>
      </div>
    </form>
  )
}

export default PersonalDetailsForm
