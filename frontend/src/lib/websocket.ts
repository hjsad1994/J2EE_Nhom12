export function getBrokerUrl() {
  const backendUrl =
    import.meta.env.VITE_BACKEND_URL ??
    import.meta.env.VITE_API_BASE_URL?.replace(/\/api$/, '') ??
    'http://localhost:8080';

  return backendUrl.replace(/^http/, 'ws') + '/ws';
}
