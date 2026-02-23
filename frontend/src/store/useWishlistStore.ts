import { create } from "zustand";
import { persist } from "zustand/middleware";

import type { Product } from "@/types/product";

interface WishlistState {
	items: Product[];
	toggle: (product: Product) => void;
	has: (id: string) => boolean;
	clear: () => void;
}

export const useWishlistStore = create<WishlistState>()(
	persist(
		(set, get) => ({
			items: [],

			toggle: (product) => {
				const exists = get().items.some((p) => p.id === product.id);
				set({
					items: exists
						? get().items.filter((p) => p.id !== product.id)
						: [...get().items, product],
				});
			},

			has: (id) => get().items.some((p) => p.id === id),

			clear: () => set({ items: [] }),
		}),
		{ name: "nebula-wishlist" },
	),
);
