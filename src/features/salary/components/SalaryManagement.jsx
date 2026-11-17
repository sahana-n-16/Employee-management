
import { useState } from "react";

import SalaryStructureForm from "./SalaryStructureForm";
import SalaryComponentForm from "./SalaryComponentForm";
import MonthlySalaryForm from "./MonthlySalaryForm";

function SalaryManagement({ employeeId = 1 }) {
  const [activeSection, setActiveSection] = useState("structure");

  const tabs = [
    { id: "structure", title: "Salary Structure" },
    { id: "components", title: "Salary Components" },
    { id: "monthly", title: "Monthly Salary" },
  ];

  return (
    <div className="salary-workspace">

      {/* Minimal section switcher */}
      <nav className="salary-subnav" aria-label="Salary sections">
        {tabs.map((tab, index) => (
          <span className="salary-subnav-item" key={tab.id}>
            <button
              type="button"
              className={`salary-subnav-link ${
                activeSection === tab.id ? "active" : ""
              }`}
              onClick={() => setActiveSection(tab.id)}
              aria-current={
                activeSection === tab.id ? "page" : undefined
              }
            >
              {tab.title}
            </button>

            {index !== tabs.length - 1 && (
              <span className="salary-subnav-separator">/</span>
            )}
          </span>
        ))}
      </nav>

      {activeSection === "structure" && (
        <section className="salary-workspace-panel">
          <SalaryStructureForm employeeId={employeeId} />
        </section>
      )}

      {activeSection === "components" && (
        <section className="salary-workspace-panel">
          <SalaryComponentForm employeeId={employeeId} />
        </section>
      )}

      {activeSection === "monthly" && (
        <section className="salary-workspace-panel">
          <MonthlySalaryForm employeeId={employeeId} />
        </section>
      )}

    </div>
  );
}

export default SalaryManagement;
