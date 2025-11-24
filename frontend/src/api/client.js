import axios from "axios";

const client = axios.create({
  baseURL: "http://localhost:8080/api"
});

// attach API key to non-GET requests automatically
client.interceptors.request.use(config => {
  const key = import.meta.env.VITE_API_KEY;
  if (key && config && config.headers && config.method && config.method.toLowerCase() !== 'get') {
    config.headers['X-API-KEY'] = key;
  }
  return config;
});

export async function fetchAudits(entityType, entityId) {
  const res = await client.get(`/audits`, { params: { entityType, entityId } });
  return res.data;
}

export default client;
