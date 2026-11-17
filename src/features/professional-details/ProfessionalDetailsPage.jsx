
import ProfessionalDetailsForm from "./components/ProfessionalDetailsForm";
import EmploymentHistory from "./components/EmploymentHistory";

function ProfessionalDetailsPage() {
  return (
    <main className="professional-page container-fluid">
      <div className="professional-page-content">
        <div className="professional-shell">

          {/* Page Heading */}
          <div className="professional-page-heading">
            <h1>Professional Details</h1>
            <p>
              Manage employee professional information and employment
              history.
            </p>
          </div>

          {/* Professional Details Form */}
          <ProfessionalDetailsForm />

          {/* Previous Employment */}
          <EmploymentHistory />

        </div>
      </div>
    </main>
  );
}

export default ProfessionalDetailsPage;
