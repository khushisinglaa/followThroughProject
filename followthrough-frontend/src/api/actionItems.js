import client from './client';

export const getAllActionItems = (page = 0) => client.get('/api/action-items', { params: { page } }).then((r) => r.data);

export const getOverdueActionItems = () => client.get('/api/action-items/overdue').then((r) => r.data);

export const updateStatus = (id, status) => client.patch(`/api/action-items/${id}/status`, { status }).then((r) => r.data);
