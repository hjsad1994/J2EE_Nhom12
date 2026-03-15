import axios from 'axios';
import type { AbsaPredictResponse } from '@/types/absa';

const absaClient = axios.create({
  baseURL: import.meta.env.VITE_ABSA_API_BASE_URL ?? '/absa-api',
  headers: {
    'Content-Type': 'application/json',
  },
});

export async function predictCommentAspects(
  text: string,
  signal?: AbortSignal,
) {
  const response = await absaClient.post<AbsaPredictResponse>(
    '/predict',
    { text },
    { signal },
  );
  return response.data.results;
}

export default absaClient;
