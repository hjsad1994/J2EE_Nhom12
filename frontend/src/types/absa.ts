export interface AbsaSentimentScores {
  positive: number;
  negative: number;
  neutral: number;
}

export interface AbsaResult {
  aspect: string;
  sentiment: 'positive' | 'negative' | 'neutral';
  confidence: number;
  scores: AbsaSentimentScores;
}

export interface AbsaPredictResponse {
  results: AbsaResult[];
}
