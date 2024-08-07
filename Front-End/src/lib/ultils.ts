import slugify from "slugify";
import { categories } from "@/hard-coding/data";
/*
 **
 */
export const splitFullName = (str: string) => str.split(" ")[0];

export const toSlugify = (slug: string) =>
  slugify(slug, {
    replacement: "-", // replace spaces with replacement character, defaults to `-`
    remove: undefined, // remove characters that match regex, defaults to `undefined`
    lower: true, // convert to lower case, defaults to `false`
    strict: false, // strip special characters except replacement, defaults to `false`
    locale: "vi", // language code of the locale to use
    trim: true, // trim leading and trailing replacement chars, defaults to `true`
  });

export const productToSlug = (name: string, id: number) =>
  `/product/${toSlugify(name)}-${id}.html`;

export const productSlugToId = (slug: string) => {
  const _slug = slug.replace(".html", "");
  const slugs = _slug.split("-");
  const id = +slugs[slugs.length - 1];
  return id || 1;
};

export const converPriceToVN = (price: number, unit?: string) =>
  !!price ? price.toLocaleString("vi-VN") + (unit ? unit : "") : "";

export const priceThrough = (price: number) => converPriceToVN(price, "đ");

export const priceShow = (price: number, discount: number) =>
  converPriceToVN((price * (100 - discount)) / 100, "đ");

export const discoutForMember = (discountForMember: number) =>
  converPriceToVN(discountForMember, "đ");

export const view = (viewCount: number) => converPriceToVN(viewCount);

export const DELAY = (ms: number) =>
  new Promise((resolve) => setTimeout(resolve, ms));

// hard coding
export const translateCategory = (en: string) => {
  const index = categories.findIndex(
    (item: { en: string }) => en.toUpperCase() === item.en.toUpperCase()
  );
  if (index !== -1) return categories[index].vi;
  return en;
};

// hard coding
export const limitProductByCategory = (id: number) => {
  const index = categories.findIndex((item) => id === item.id);
  if (index !== -1) return categories[index].limit;
  return 10;
};

export const formatDate = (isoString: string) => {
  const date = new Date(isoString);

  // Lấy các thành phần ngày, tháng, năm, giờ và phút
  const day = date.getDate();
  const month = date.getMonth() + 1; // Tháng trong JavaScript tính từ 0-11, nên cần +1
  const year = date.getFullYear();
  const hours = date.getHours();
  const minutes = date.getMinutes();

  // Định dạng các thành phần thành chuỗi có hai chữ số
  const formattedDay = day.toString().padStart(2, "0");
  const formattedMonth = month.toString().padStart(2, "0");
  const formattedYear = year.toString();
  const formattedHours = hours.toString().padStart(2, "0");
  const formattedMinutes = minutes.toString().padStart(2, "0");

  // Ghép các thành phần lại thành chuỗi định dạng mong muốn
  return `${formattedDay}/${formattedMonth}/${formattedYear} ${formattedHours}:${formattedMinutes}`;
};
