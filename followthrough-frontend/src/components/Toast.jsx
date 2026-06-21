import { useState, useEffect } from 'react';

let showToastFn = () => {};

export function toast(message, type = 'error') {
  showToastFn(message, type);
}

export default function Toast() {
  const [visible, setVisible] = useState(false);
  const [msg, setMsg] = useState('');
  const [type, setType] = useState('error');

  useEffect(() => {
    showToastFn = (message, t) => {
      setMsg(message);
      setType(t);
      setVisible(true);
      setTimeout(() => setVisible(false), 3000);
    };
  }, []);

  if (!visible) return null;

  const colors = type === 'error' ? 'bg-red-600' : 'bg-green-600';

  return (
    <div className={`fixed bottom-4 right-4 px-4 py-2 rounded-lg text-white text-sm shadow-lg ${colors}`}>
      {msg}
    </div>
  );
}
