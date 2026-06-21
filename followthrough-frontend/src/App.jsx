import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Toast from './components/Toast';
import Landing from './pages/Landing';
import Upload from './pages/Upload';
import Decisions from './pages/Decisions';
import ActionItems from './pages/ActionItems';
import MeetingHistory from './pages/MeetingHistory';
import MeetingDetail from './pages/MeetingDetail';

export default function App() {
  return (
    <>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Landing />} />
          <Route path="/upload" element={<Upload />} />
          <Route path="/decisions" element={<Decisions />} />
          <Route path="/action-items" element={<ActionItems />} />
          <Route path="/history" element={<MeetingHistory />} />
          <Route path="/meetings/:id" element={<MeetingDetail />} />
        </Route>
      </Routes>
      <Toast />
    </>
  );
}
