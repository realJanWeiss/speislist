export type ShoppingListItem = {
    id: number;
    name: string;
    quantity: number;
    isCompleted: boolean;
};

export type UserDTO = {
    id: string;
    userName: string;
};

export type ShoppingList = {
    createdAt?: string;
    id: string;
    items?: ShoppingListItem[] | null;
    members?: UserDTO[] | null;
    name?: string;
};