import { get } from "../axios.helper";

export const getProduct = async (
  page: number = 1,
  limit: number = 10,
  category?: string,
  brand?: string,
  sort?: string,
  search?: string
) => {
  let query = `page=${page}&limit=${limit}`;
  if (category) query += `&category=${category}`;
  if (brand) query += `&brand=${brand}`;
  if (sort) query += `&sort=${sort}`;
  if (search) query += `&search=${search}`;
  try {
    const res = await get<Page<Product>>(`/products/search-product?${query}`);
    const products = res.data.result;
    const totalItem = res.data.totalItem;
    return { products, totalItem };
  } catch (error) {
    throw error;
  }
};

export const getAllProduct = async () => {
  try {
    const res = await get<ProductResponse[]>(`/products`);
    const products = res.data;
    return products;
  } catch {
    return [];
  }
};
// bug call ở client bị lỗi
export const searchProductByName = async (value: string) => {
  const query = `name:${value}`;
  const res = await getProduct(1, 5, undefined, undefined, undefined, query);
  return res.products;
};

export const getProductByCategorySortByViewCounter = async (
  category: string,
  limit: number
) => {
  const sort = `viewCount:desc`;
  const res = await getProduct(1, limit, category, undefined, sort);
  return res.products;
};

export const getProductById = async (id: number | string) => {
  try {
    const res = await get<Product>(`/products/${id}`);
    const data = res.data;
    return data;
  } catch (error) {
    throw error;
  }
};
export const getAttributesByCategory = async (
  category?: string,
  brand?: string
): Promise<AttibulteResponse[]> => {
  let query = "/attributes?";
  if (category) query += `category=${category}`;
  if (brand) query += `&brand=${brand}`;
  try {
    const res = await get<AttibulteResponse[]>(query);
    const attributes = res.data;
    return attributes;
  } catch {
    return [];
  }
};
