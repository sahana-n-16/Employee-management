
import { useState } from "react";
import "./EmployeeDashboard.css";

const employee = {
  name: "Sahana N",
  id: "EMP001",
  role: "Software Developer",
  department: "Information Technology",
  email: "employee@company.com",
  manager: "HR Manager",
  joiningDate: "01 September 2026",
  project: "Employee Management System",
};

const sections = [
  { id: "overview", icon: "⌂", label: "Overview" },
  { id: "personal", icon: "♙", label: "Personal Details" },
  { id: "professional", icon: "▤", label: "Professional Details" },
  { id: "projects", icon: "▣", label: "Project Details" },
  { id: "finance", icon: "₹", label: "Finance Details" },
  { id: "payslips", icon: "▧", label: "Payslip Management" },
];

function Info({ label, value }) {
  return (
    <div className="info-item">
      <span>{label}</span>
      <strong>{value || "Not available"}</strong>
    </div>
  );
}

export default function EmployeeDashboard() {
  const [active, setActive] = useState("overview");

  const heading = sections.find(s => s.id === active)?.label;

  const renderOverview = () => (
    <>
      <div className="emp-welcome">
        <div>
          <span className="emp-eyebrow">EMPLOYEE SELF SERVICE</span>
          <h1>Welcome back, {employee.name}!</h1>
          <p>Here's your employee workspace at a glance.</p>
          <button onClick={() => setActive("personal")}>
            View My Profile →
          </button>
        </div>
        <div className="welcome-avatar">E</div>
      </div>

      <div className="emp-stats">
        <div className="emp-stat">
          <span>EMPLOYEE ID</span>
          <h2>{employee.id}</h2>
          <small>Your identification number</small>
        </div>
        <div className="emp-stat">
          <span>DESIGNATION</span>
          <h2>{employee.role}</h2>
          <small>Your current position</small>
        </div>
        <div className="emp-stat">
          <span>DEPARTMENT</span>
          <h2>{employee.department}</h2>
          <small>Your working department</small>
        </div>
        <div className="emp-stat">
          <span>JOINING DATE</span>
          <h2>{employee.joiningDate}</h2>
          <small>Employment information</small>
        </div>
      </div>

      <div className="emp-section-title">
        <div>
          <h2>Your Information</h2>
          <p>Access your employee records</p>
        </div>
      </div>

      <div className="emp-module-grid">
        {sections.slice(1).map(section => (
          <button
            className="emp-module"
            key={section.id}
            onClick={() => setActive(section.id)}
          >
            <span className={`module-icon ${section.id}`}>
              {section.icon}
            </span>
            <span className="module-text">
              <strong>{section.label}</strong>
              <small>View your {section.label.toLowerCase()}</small>
            </span>
            <span className="module-arrow">→</span>
          </button>
        ))}
      </div>

      <div className="emp-bottom-grid">
        <div className="emp-panel">
          <h3>Employee Overview</h3>
          <Info label="Employee Name" value={employee.name} />
          <Info label="Company Email" value={employee.email} />
          <Info label="Reporting Manager" value={employee.manager} />
          <Info label="Current Project" value={employee.project} />
        </div>

        <div className="emp-panel emp-notice">
          <span className="notice-icon">✦</span>
          <h3>Your Employee Portal</h3>
          <p>
            All your personal, professional, project and finance
            information is available in one place.
          </p>
          <p className="notice-small">
            For changes to your records, please contact your HR team.
          </p>
        </div>
      </div>
    </>
  );

  const renderSection = () => {
    switch (active) {
      case "personal":
        return (
          <div className="emp-panel">
            <h2>Personal Information</h2>
            <p className="section-subtitle">Your personal employee records</p>
            <div className="emp-info-grid">
              <Info label="Full Name" value={employee.name} />
              <Info label="Employee Code" value={employee.id} />
              <Info label="Date of Birth" />
              <Info label="Gender" />
              <Info label="Age" />
              <Info label="Mobile Number" />
              <Info label="Personal Email" />
              <Info label="Emergency Contact" />
              <Info label="Current Address" />
              <Info label="Permanent Address" />
            </div>
          </div>
        );

      case "professional":
        return (
          <div className="emp-panel">
            <h2>Professional Information</h2>
            <p className="section-subtitle">Your employment details</p>
            <div className="emp-info-grid">
              <Info label="Employee Code" value={employee.id} />
              <Info label="Designation" value={employee.role} />
              <Info label="Department" value={employee.department} />
              <Info label="Company Email" value={employee.email} />
              <Info label="Office Phone" />
              <Info label="Office Address" />
              <Info label="Reporting Manager" value={employee.manager} />
              <Info label="Date of Joining" value={employee.joiningDate} />
              <Info label="HR Name" />
              <Info label="Employment History" />
            </div>
          </div>
        );

      case "projects":
        return (
          <div className="emp-panel">
            <h2>Project Details</h2>
            <p className="section-subtitle">Your current and previous projects</p>
            <div className="project-card">
              <span className="project-tag">CURRENT PROJECT</span>
              <h3>{employee.project}</h3>
              <p>Project information and assignment details</p>
              <div className="emp-info-grid">
                <Info label="Project Code" />
                <Info label="Client Name" />
                <Info label="Start Date" />
                <Info label="End Date" />
                <Info label="Reporting Manager" value={employee.manager} />
              </div>
            </div>
          </div>
        );

      case "finance":
        return (
          <div className="emp-panel">
            <h2>Finance Information</h2>
            <p className="section-subtitle">Your confidential finance records</p>
            <div className="emp-info-grid">
              <Info label="PAN Number" />
              <Info label="Aadhaar Number" />
              <Info label="Bank Name" />
              <Info label="Branch" />
              <Info label="Account Number" />
              <Info label="IFSC Code" />
              <Info label="Annual CTC" />
              <Info label="CTC Breakup" />
            </div>
            <p className="privacy-note">
              🔒 Your financial information is available only to authorized users.
            </p>
          </div>
        );

      case "payslips":
        return (
          <div className="emp-panel">
            <h2>My Payslips</h2>
            <p className="section-subtitle">
              Access your latest six months of salary statements.
            </p>
            <div className="payslip-list">
              {Array.from({ length: 6 }, (_, i) => {
                const date = new Date();
                date.setDate(1);
                date.setMonth(date.getMonth() - i);
                const month = date.toLocaleString("en-IN", {
                  month: "long",
                  year: "numeric",
                });
                return (
                  <div className="payslip-row" key={month}>
                    <div className="pdf-icon">PDF</div>
                    <div className="payslip-info">
                      <strong>{month}</strong>
                      <small>Salary statement</small>
                    </div>
                    <span className="payslip-status">Available via API</span>
                    <button
                      className="download-btn"
                      onClick={() => alert("Payslip download API is not connected yet.")}
                    >
                      ↓ Download
                    </button>
                  </div>
                );
              })}
            </div>
          </div>
        );

      default:
        return renderOverview();
    }
  };

  return (
    <div className="emp-app">
      <aside className="emp-sidebar">
        <div className="emp-brand">
          <div className="emp-brand-logo">E</div>
          <div>
            <h2>EMPLOYEE<span>360</span></h2>
            <small>WORKSPACE PORTAL</small>
          </div>
        </div>

        <div className="emp-menu-label">WORKSPACE</div>
        <nav>
          {sections.map(section => (
            <button
              key={section.id}
              className={active === section.id ? "emp-nav selected" : "emp-nav"}
              onClick={() => setActive(section.id)}
            >
              <span>{section.icon}</span>
              {section.label}
              {active === section.id && <i />}
            </button>
          ))}
        </nav>

        <div className="emp-sidebar-bottom">
          <div className="help-icon">?</div>
          <div>
            <strong>Need assistance?</strong>
            <small>Contact your HR department</small>
          </div>
        </div>
      </aside>

      <main className="emp-main">
        <header className="emp-header">
          <div>
            <small>Employee Portal / {heading}</small>
            <h2>{heading}</h2>
          </div>
          <div className="emp-header-user">
            <div className="emp-small-avatar">E</div>
            <div>
              <strong>{employee.name}</strong>
              <small>{employee.id}</small>
            </div>
          </div>
        </header>

        <div className="emp-content">
          {renderSection()}
          <footer>© 2026 Employee360 · Employee Self Service Portal</footer>
        </div>
      </main>
    </div>
  );
}
git remote -v
git branch