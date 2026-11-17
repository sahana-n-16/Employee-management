
import { useState } from "react";

import ProfessionalDetailsForm from "./features/professional-details/components/ProfessionalDetailsForm";
import EmploymentHistory from "./features/professional-details/components/EmploymentHistory";
import FinanceDetailsForm from "./features/finance-details/components/FinanceDetailsForm";
import SalaryManagement from "./features/salary/components/SalaryManagement";

import "./App.css";

function App() {
  const employeeId = 1;

  const [activeTab, setActiveTab] = useState("professional");

  const tabs = [
    {
      id: "professional",
      label: "Professional Details",
      icon: "briefcase",
      description: "Employment information and history",
    },
    {
      id: "finance",
      label: "Finance Details",
      icon: "wallet",
      description: "Banking and financial information",
    },
    {
      id: "salary",
      label: "Salary Management",
      icon: "banknote",
      description: "Salary structures and payments",
    },
  ];

  const activeTabDetails = tabs.find(
    (tab) => tab.id === activeTab
  );

  return (
    <main className="professional-page container-fluid">
      <div className="professional-page-content">
        <div className="professional-shell">

          {/* Main Page Heading */}
          <header className="employee-page-heading">
            <h1>Employee Information</h1>
            <p>
              Manage professional information, financial details and salary.
            </p>
          </header>

          {/* Main Navigation */}
          <nav
            className="employee-tabs"
            aria-label="Employee information sections"
          >
            {tabs.map((tab) => (
              <button
                key={tab.id}
                type="button"
                className={`employee-tab ${
                  activeTab === tab.id ? "active" : ""
                }`}
                onClick={() => setActiveTab(tab.id)}
                aria-current={
                  activeTab === tab.id ? "page" : undefined
                }
              >
                <span className="employee-tab-icon" aria-hidden="true">
                  {tab.icon === "briefcase" && "▤"}
                  {tab.icon === "wallet" && "₹"}
                  {tab.icon === "banknote" && "▣"}
                </span>

                <span>{tab.label}</span>
              </button>
            ))}
          </nav>

          {/* Current Section Heading */}
          <section className="employee-tab-panel">

            <header className="employee-section-heading">
              <h2>{activeTabDetails.label}</h2>
              <p>{activeTabDetails.description}</p>
            </header>

            {/* Professional Details */}
            {activeTab === "professional" && (
              <>
                <ProfessionalDetailsForm employeeId={employeeId} />
                <EmploymentHistory employeeId={employeeId} />
              </>
            )}

            {/* Finance Details */}
            {activeTab === "finance" && (
              <FinanceDetailsForm employeeId={employeeId} />
            )}

            {/* Salary Management */}
            {activeTab === "salary" && (
              <SalaryManagement employeeId={employeeId} />
            )}

          </section>

        </div>
      </div>
    </main>
  );
}

export default App;
