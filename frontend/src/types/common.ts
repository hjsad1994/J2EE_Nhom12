/**
 * Common shared types used across features.
 */

/** Standard pagination params for API requests */
export interface PaginationParams {
  page: number
  pageSize: number
}

/** Generic ID type — adjust to match your backend (number | string) */
export type EntityId = number

/** Base entity with common fields */
export interface BaseEntity {
  id: EntityId
  createdAt: string
  updatedAt: string
}
