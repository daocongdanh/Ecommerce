import NavBrand from "@/components/global/navBrand";
import ProductList from "@/components/product/product.list";
import { get } from "@/services/axios.helper";
import { translateCategory } from "@/utils/translate";
export async function generateStaticParams() {
  const brands = await get<TBrand[]>(`/brands`);

  return brands.map((brand) => ({
    category: brand.category,
    brand: brand.brand,
  }));
}

export default async function Page({
  params,
}: {
  params: { category: string; brand: string };
}) {
  const products = await get<TProduct[]>(
    `/products?category=${params.category}&brand=${params.brand}`
  );
  return (
    <>
      {translateCategory(params.category) + " của chúng tôi"}
      <NavBrand params={params} category={params.category} />
      <h2 className="text-lg font-bold text-red-500">Filter???</h2>
      <ProductList products={products} />
    </>
  );
}
