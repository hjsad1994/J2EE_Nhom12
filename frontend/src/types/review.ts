export interface ReviewAspectAnalysis {
  aspect: string;
  sentiment: 'positive' | 'negative' | 'neutral';
  confidence?: number;
}

export interface Review {
  id: string;
  productId: string;
  userId: string;
  username: string;
  rating: number;
  comment: string;
  images?: string[];
  analysisResults?: ReviewAspectAnalysis[];
  createdAt: string;
}

export interface CreateReviewPayload {
  productId: string;
  rating: number;
  comment: string;
  images?: string[];
}
