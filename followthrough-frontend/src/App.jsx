import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Upload from './pages/Upload';

function Decisions() {
  return <h1 className="text-2xl font-bold">Decisions</h1>;
}
function ActionItems() {
  return <h1 className="text-2xl font-bold">Action Items</h1>;
}
function History() {
  return <h1 className="text-2xl font-bold">Meeting History</h1>;
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Upload />} />
        <Route path="/decisions" element={<Decisions />} />
        <Route path="/action-items" element={<ActionItems />} />
        <Route path="/history" element={<History />} />
      </Route>
    </Routes>
  );
}
