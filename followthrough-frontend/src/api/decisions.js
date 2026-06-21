import client from './client';

export const getAllDecisions = (page = 0) => client.get('/api/decisions', { params: { page } }).then((r) => r.data);

export const searchDecisions = (query) => client.get('/api/decisions/search', { params: { q: query } }).then((r) => r.data);
