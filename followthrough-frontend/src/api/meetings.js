import client from './client';

export const createMeeting = (data) => client.post('/api/meetings', data).then((r) => r.data);

export const extractMeeting = (id) => client.post(`/api/meetings/${id}/extract`).then((r) => r.data);

export const getAllMeetings = (page = 0) => client.get('/api/meetings', { params: { page } }).then((r) => r.data);
