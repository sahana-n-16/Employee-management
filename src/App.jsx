import ProjectHistory from './features/projects/components/ProjectHistory';
import { Toaster } from 'react-hot-toast';

function App() {
  return (
    <div className="vh-100" style={{ backgroundColor: '#FAF8F3', overflowY: 'auto' }}>
      <Toaster position="top-right" />
      
      <main className="container pt-5 mt-5">
        <ProjectHistory />
      </main>
    </div>
  );
}

export default App;