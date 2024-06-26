import Header from "@/components/header/header";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Smart shop | Uy tín-Chất lượng-Giá rẻ",
  description:
    "Shop bán đồ công nghệ  giá rẻ uy tín, chất lượng nhất nhất Việt Nam",
};
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div className="max-w-[1280px] mx-auto relative">
      <Header />
      <main className="text-[#444444] max-w-[1200px] mx-auto w-full px-2 mt-[132px] pt-5">
        {children}
      </main>
    </div>
  );
}
