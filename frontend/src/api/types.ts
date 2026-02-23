/**
 * Shared API request/response types.
 * Feature-specific types go in src/features/<name>/types/
 */

export interface ApiResponse<T> {
	data: T;
	message?: string;
	status: number;
}

export interface ApiError {
	message: string;
	status: number;
	errors?: Record<string, string[]>;
}

export interface PaginatedResponse<T> {
	data: T[];
	page: number;
	pageSize: number;
	totalPages: number;
	totalItems: number;
}
