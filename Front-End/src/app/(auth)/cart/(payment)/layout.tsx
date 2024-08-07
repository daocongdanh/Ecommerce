import Header2 from "@/components/header/header2";
import type { Metadata } from "next";
import NavPayment, {
  NavHeader,
  NavSubmit,
} from "@/app/(auth)/cart/(payment)/_components/nav";

export const metadata: Metadata = {
  title: "Thanh toán",
  description: "Thanh toán",
};
export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <>
      <Header2 />
      <div className="bg-[#f4f6f8] min-h-screen">
        <div className="max-w-[600px] mx-auto pb-[140px]">
          <NavHeader />
          <NavPayment />
          {children}
          <NavSubmit />
        </div>
      </div>
    </>
  );
}
