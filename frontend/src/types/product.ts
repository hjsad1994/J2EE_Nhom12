export interface Product {
  id: string;
  name: string;
  brand: string;
  categoryId?: string;
  categoryName?: string;
  price: number;
  originalPrice?: number;
  image: string;
  rating: number;
  badge?: string;
  specs?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateProductPayload {
  name: string;
  brand: string;
  categoryId?: string;
  price: number;
  originalPrice?: number;
  image: string;
  rating?: number;
  badge?: string;
  specs?: string;
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  icon?: string;
  createdAt?: string;
}

export interface CreateCategoryPayload {
  name: string;
  description?: string;
  icon?: string;
}
